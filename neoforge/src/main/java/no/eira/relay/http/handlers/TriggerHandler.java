package no.eira.relay.http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import no.eira.relay.block.HttpReceiverBlock;
import no.eira.relay.blockentity.HttpReceiverBlockEntity;
import no.eira.relay.http.api.IHttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handler for POST /trigger/{triggerId}
 * Named trigger endpoint for QR codes, sensors, and external systems.
 */
public class TriggerHandler implements IHttpHandler {

    private static final String ALLOWED_METHOD = "POST";
    private static final Gson GSON = new Gson();

    // Global registry of trigger IDs to block positions
    private static final Map<String, Set<TriggerTarget>> triggerRegistry = new ConcurrentHashMap<>();

    // Track server level reference
    private static ServerLevel serverLevel;

    /**
     * Register a block to receive triggers for a specific trigger ID
     */
    public static void registerTrigger(String triggerId, BlockPos pos, ServerLevel level) {
        if (triggerId == null || triggerId.isEmpty()) return;

        triggerRegistry.computeIfAbsent(triggerId, k -> ConcurrentHashMap.newKeySet())
            .add(new TriggerTarget(pos, level));
        serverLevel = level;

        System.out.println("[EiraRelay] Registered trigger '" + triggerId + "' for block at " + pos);
    }

    /**
     * Unregister a block from receiving triggers
     */
    public static void unregisterTrigger(String triggerId, BlockPos pos) {
        if (triggerId == null || triggerId.isEmpty()) return;

        Set<TriggerTarget> targets = triggerRegistry.get(triggerId);
        if (targets != null) {
            targets.removeIf(t -> t.pos().equals(pos));
            if (targets.isEmpty()) {
                triggerRegistry.remove(triggerId);
            }
        }
        System.out.println("[EiraRelay] Unregistered trigger '" + triggerId + "' for block at " + pos);
    }

    /**
     * Get all registered trigger IDs
     */
    public static Set<String> getRegisteredTriggers() {
        return Collections.unmodifiableSet(triggerRegistry.keySet());
    }

    @Override
    public String getUrl() {
        return "/trigger";
    }

    @Override
    public List<String> httpMethods() {
        return List.of(ALLOWED_METHOD);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        System.out.println("[EiraRelay] Received " + exchange.getRequestMethod() + " request to " + path);

        // Extract trigger ID from path: /trigger/{triggerId}
        String triggerId = extractTriggerId(path);
        if (triggerId == null || triggerId.isEmpty()) {
            sendJsonResponse(exchange, 400, Map.of(
                "success", false,
                "error", "Missing trigger ID. Use /trigger/{triggerId}"
            ));
            return;
        }

        // Validate API key if required
        if (!validateApiKey(exchange)) {
            return; // Response already sent
        }

        // Parse request body
        TriggerRequest request = parseRequestBody(exchange);

        // Find blocks registered for this trigger
        Set<TriggerTarget> targets = triggerRegistry.get(triggerId);
        int blocksTriggered = 0;

        if (targets != null && !targets.isEmpty()) {
            for (TriggerTarget target : targets) {
                if (triggerBlock(target)) {
                    blocksTriggered++;
                }
            }
        }

        // Send success response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("triggerId", triggerId);
        response.put("blocksTriggered", blocksTriggered);
        response.put("eventPublished", true);

        if (request != null) {
            if (request.teamId != null) response.put("teamId", request.teamId);
            if (request.playerId != null) response.put("playerId", request.playerId);
        }

        sendJsonResponse(exchange, 200, response);
        System.out.println("[EiraRelay] Trigger '" + triggerId + "' activated " + blocksTriggered + " block(s)");
    }

    private String extractTriggerId(String path) {
        // Path format: /trigger/{triggerId}
        if (path.startsWith("/trigger/")) {
            String id = path.substring("/trigger/".length());
            // Remove trailing slash if present
            if (id.endsWith("/")) {
                id = id.substring(0, id.length() - 1);
            }
            return id.isEmpty() ? null : id;
        }
        return null;
    }

    private boolean validateApiKey(HttpExchange exchange) throws IOException {
        // TODO: Phase 2 - Add global API key validation from HttpServerConfig
        // For now, allow all requests (per-block token validation handled separately)
        return true;
    }

    private TriggerRequest parseRequestBody(HttpExchange exchange) {
        try {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder buf = new StringBuilder();
            int b;
            while ((b = br.read()) != -1) {
                buf.append((char) b);
            }
            br.close();

            String body = buf.toString();
            if (body.isEmpty()) return null;

            return GSON.fromJson(body, TriggerRequest.class);
        } catch (Exception e) {
            System.err.println("[EiraRelay] Failed to parse trigger request body: " + e.getMessage());
            return null;
        }
    }

    private boolean triggerBlock(TriggerTarget target) {
        if (target.level() == null || target.level().getServer() == null) {
            return false;
        }

        MinecraftServer server = target.level().getServer();

        server.execute(() -> {
            try {
                BlockEntity blockEntity = target.level().getBlockEntity(target.pos());
                if (blockEntity instanceof HttpReceiverBlockEntity) {
                    BlockState state = target.level().getBlockState(target.pos());
                    if (state.getBlock() instanceof HttpReceiverBlock block) {
                        block.onSignal(state, target.level(), target.pos());
                        System.out.println("[EiraRelay] Triggered block at " + target.pos());
                    }
                }
            } catch (Exception e) {
                System.err.println("[EiraRelay] Error triggering block at " + target.pos() + ": " + e.getMessage());
            }
        });

        return true;
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

    // Request body structure
    private static class TriggerRequest {
        String teamId;
        String playerId;
        Map<String, Object> data;
    }

    // Target for trigger activation
    private record TriggerTarget(BlockPos pos, ServerLevel level) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TriggerTarget that = (TriggerTarget) o;
            return Objects.equals(pos, that.pos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos);
        }
    }
}
