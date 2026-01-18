package com.clapter.httpautomator.http;

import com.clapter.httpautomator.http.api.IHttpHandler;
import com.clapter.httpautomator.http.api.IHttpServer;
import com.clapter.httpautomator.platform.Services;
import com.clapter.httpautomator.utils.ImplLoader;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.HttpMessage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.Provider;
import java.util.*;

public class HttpServerImpl implements IHttpServer {

    private HttpServer server;

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

    public boolean startServer() throws IOException {
        int port = Services.HTTP_CONFIG.getPort();
        // Bind to localhost by default for security
        InetSocketAddress address = new InetSocketAddress(DEFAULT_BIND_ADDRESS, port);
        server = HttpServer.create(address, 0);
        server.setExecutor(null); // creates a default executor
        server.start();
        this.handleHandlersInQueue();
        System.out.println("HTTP Server started on " + DEFAULT_BIND_ADDRESS + ":" + port);
        return true;
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
        server.createContext(handler.getUrl(), handler);
        handlerMap.put(handler.getUrl(), handler);
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
