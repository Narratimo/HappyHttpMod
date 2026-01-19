package no.eira.relay.http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import no.eira.relay.Constants;
import no.eira.relay.http.api.IHttpHandler;
import no.eira.relay.platform.Services;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Handler for GET /status
 * Health check endpoint with uptime and trigger information.
 */
public class StatusHandler implements IHttpHandler {

    private static final String ALLOWED_METHOD = "GET";
    private static final Gson GSON = new Gson();
    private static final String MOD_VERSION = "1.1.0";

    // Track server start time
    private static long serverStartTime = 0;

    /**
     * Record the server start time
     */
    public static void recordServerStart() {
        serverStartTime = System.currentTimeMillis();
    }

    @Override
    public String getUrl() {
        return "/status";
    }

    @Override
    public List<String> httpMethods() {
        return List.of(ALLOWED_METHOD);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("[EiraRelay] Received " + exchange.getRequestMethod() + " request to /status");

        // Build status response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "online");
        response.put("modId", Constants.MOD_ID);
        response.put("modName", Constants.MOD_NAME);
        response.put("version", MOD_VERSION);

        // Calculate uptime
        if (serverStartTime > 0) {
            long uptimeMs = System.currentTimeMillis() - serverStartTime;
            response.put("uptimeSeconds", uptimeMs / 1000);
            response.put("uptimeFormatted", formatUptime(uptimeMs));
        } else {
            response.put("uptimeSeconds", 0);
            response.put("uptimeFormatted", "0s");
        }

        // Server configuration
        Map<String, Object> serverConfig = new LinkedHashMap<>();
        serverConfig.put("port", Services.HTTP_CONFIG.getPort());
        response.put("server", serverConfig);

        // Registered triggers
        Set<String> triggers = TriggerHandler.getRegisteredTriggers();
        response.put("registeredTriggers", triggers);
        response.put("triggerCount", triggers.size());

        // Send response
        sendJsonResponse(exchange, 200, response);
    }

    private String formatUptime(long uptimeMs) {
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%dd %dh %dm %ds", days, hours % 24, minutes % 60, seconds % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, Map<String, Object> data) throws IOException {
        String json = GSON.toJson(data);
        byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
