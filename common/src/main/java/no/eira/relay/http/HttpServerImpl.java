package no.eira.relay.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import no.eira.relay.http.api.IHttpHandler;
import no.eira.relay.http.api.IHttpServer;
import no.eira.relay.http.handlers.BroadcastHandler;
import no.eira.relay.http.handlers.RedstoneHandler;
import no.eira.relay.http.handlers.StatusHandler;
import no.eira.relay.http.handlers.TriggerHandler;
import no.eira.relay.platform.Services;
import no.eira.relay.utils.ImplLoader;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class HttpServerImpl implements IHttpServer {

    private HttpServer server;
    private RateLimiter rateLimiter;

    //private Map<Integer, IHttpHandler> handlerMap;
    private Map<String, IHttpHandler> handlerMap;
    //USING MAP INSTEAD OF QUEUE FOR FAST RETRIEVAL OF A HANDLER FROM THE QUEUE BY ITS KEY
    private Map<String, IHttpHandler> handlerToRegisterQueue;


    public HttpServerImpl(){
        handlerMap = new HashMap<String, IHttpHandler>();
        //urlToHandlerMap = new HashMap<String, IHttpHandler>();
        handlerToRegisterQueue = new HashMap<String, IHttpHandler>();
    }

    // Default to localhost for security - only accessible from this machine
    private static final String DEFAULT_BIND_ADDRESS = "127.0.0.1";

    /**
     * Initialize rate limiter based on config
     */
    private void initRateLimiter() {
        if (Services.HTTP_CONFIG.isRateLimitEnabled()) {
            int requestsPerMinute = Services.HTTP_CONFIG.getRateLimitPerMinute();
            rateLimiter = new RateLimiter(requestsPerMinute, 60000); // 1 minute window
            System.out.println("[EiraRelay] Rate limiting enabled: " + requestsPerMinute + " requests/minute");
        } else {
            rateLimiter = null;
        }
    }

    public boolean startServer() throws IOException {
        int port = Services.HTTP_CONFIG.getPort();
        // Initialize rate limiter
        initRateLimiter();
        // Bind to localhost by default for security
        InetSocketAddress address = new InetSocketAddress(DEFAULT_BIND_ADDRESS, port);
        server = HttpServer.create(address, 0);
        server.setExecutor(null); // creates a default executor
        server.start();
        this.initBuiltInHandlers();
        this.handleHandlersInQueue();
        StatusHandler.recordServerStart();
        System.out.println("HTTP Server started on " + DEFAULT_BIND_ADDRESS + ":" + port);
        return true;
    }

    /**
     * Register built-in handlers for the new API endpoints
     */
    private void initBuiltInHandlers() {
        // Register /status endpoint
        registerHandler(new StatusHandler());

        // Register /trigger endpoint (handles /trigger/{triggerId})
        registerHandler(new TriggerHandler());

        // Register /redstone endpoint
        registerHandler(new RedstoneHandler());

        // Register /broadcast endpoint
        registerHandler(new BroadcastHandler());

        System.out.println("[EiraRelay] Built-in handlers registered: /status, /trigger, /redstone, /broadcast");
    }

    private void handleHandlersInQueue() {
        handlerToRegisterQueue.values().forEach(this::registerHandler);
    }

    @Override
    public void initHandlers(){
        List<IHttpHandler> handlerList = ImplLoader.loadAll(IHttpHandler.class);
        handlerList.forEach(this::registerHandler);
    }

    @Override
    public void registerHandler(IHttpHandler handler) {
        if(server == null){
            handlerToRegisterQueue.put(handler.getUrl(), handler);
        }else{
            this.handleRegisteringHandlers(handler);
        }
    }

    @Override
    public IHttpHandler getHandlerByUrl(String url) {
        if(handlerMap.get(url) != null)return handlerMap.get(url);
        if(handlerToRegisterQueue.get(url) != null)return handlerToRegisterQueue.get(url);
        return null;
    }

    @Override
    public void unregisterHandler(String url) {
        if (url == null || url.isEmpty()) return;

        // Remove from queue if not yet registered
        handlerToRegisterQueue.remove(url);

        // Remove from active handlers
        if (handlerMap.containsKey(url)) {
            handlerMap.remove(url);
            if (server != null) {
                try {
                    server.removeContext(url);
                } catch (IllegalArgumentException e) {
                    // Context doesn't exist, ignore
                }
            }
        }
    }

    private void handleRegisteringHandlers(IHttpHandler handler) {
        if(!handlerMap.containsKey(handler.getUrl())) {
            registerAndPutInMap(handler);
        }else{
            //ALREADY CONTAINING A HANDLER FOR THAT ID (FOR THAT BLOCK IN OUR TEST)
            //FOR NOW JUST OVERRIDE WITH NEW ONE
            server.removeContext(handler.getUrl());
            registerAndPutInMap(handler);

        }
    }

    private void registerAndPutInMap(IHttpHandler handler) {
        // Wrap handler with middleware for rate limiting and CORS
        HttpHandler wrappedHandler = exchange -> {
            // Add CORS headers if enabled
            if (Services.HTTP_CONFIG.isCorsEnabled()) {
                addCorsHeaders(exchange);
            }

            // Handle CORS preflight
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // Check rate limit
            if (rateLimiter != null) {
                String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
                if (!rateLimiter.isAllowed(clientIp)) {
                    long retryAfter = rateLimiter.getRetryAfterMs(clientIp);
                    sendRateLimitResponse(exchange, retryAfter);
                    return;
                }
            }

            // Delegate to actual handler
            handler.handle(exchange);
        };

        server.createContext(handler.getUrl(), wrappedHandler);
        handlerMap.put(handler.getUrl(), handler);
    }

    /**
     * Add CORS headers to response
     */
    private void addCorsHeaders(HttpExchange exchange) {
        List<String> origins = Services.HTTP_CONFIG.getCorsOrigins();
        String allowOrigin = origins.isEmpty() ? "*" : String.join(", ", origins);

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", allowOrigin);
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Api-Key");
        exchange.getResponseHeaders().add("Access-Control-Max-Age", "86400"); // 24 hours
    }

    /**
     * Send 429 Too Many Requests response
     */
    private void sendRateLimitResponse(HttpExchange exchange, long retryAfterMs) throws IOException {
        String json = "{\"error\": \"Rate limit exceeded\", \"retryAfterMs\": " + retryAfterMs + "}";
        byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Retry-After", String.valueOf((retryAfterMs + 999) / 1000)); // Seconds
        exchange.sendResponseHeaders(429, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }

        System.out.println("[EiraRelay] Rate limited request from " +
            exchange.getRemoteAddress().getAddress().getHostAddress());
    }

    @Override
    public void stopServer() {
        if(server != null){
            server.stop(1);
            server = null;
            //System.gc();
        }
    }
}
