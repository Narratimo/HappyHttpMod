package no.eira.relay.http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import no.eira.relay.http.api.IHttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handler for POST /redstone
 * Direct redstone emission at specified coordinates.
 */
public class RedstoneHandler implements IHttpHandler {

    private static final String ALLOWED_METHOD = "POST";
    private static final Gson GSON = new Gson();

    // Active redstone emitters (position -> expiry time in ticks)
    private static final Map<BlockPos, RedstoneEmission> activeEmissions = new ConcurrentHashMap<>();

    // Server level reference
    private static ServerLevel serverLevel;

    /**
     * Set the server level for redstone operations
     */
    public static void setServerLevel(ServerLevel level) {
        serverLevel = level;
    }

    /**
     * Get active emissions for tick processing
     */
    public static Map<BlockPos, RedstoneEmission> getActiveEmissions() {
        return activeEmissions;
    }

    /**
     * Process tick for active emissions (called from server tick)
     */
    public static void tick() {
        if (activeEmissions.isEmpty()) return;

        Iterator<Map.Entry<BlockPos, RedstoneEmission>> it = activeEmissions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, RedstoneEmission> entry = it.next();
            RedstoneEmission emission = entry.getValue();
            emission.ticksRemaining--;

            if (emission.ticksRemaining <= 0) {
                it.remove();
                // Notify neighbors that redstone changed
                if (serverLevel != null) {
                    BlockPos pos = entry.getKey();
                    serverLevel.updateNeighborsAt(pos, Blocks.AIR);
                    System.out.println("[EiraRelay] Redstone emission ended at " + pos);
                }
            }
        }
    }

    /**
     * Get redstone signal strength at position (for neighbor queries)
     */
    public static int getSignalAt(BlockPos pos) {
        RedstoneEmission emission = activeEmissions.get(pos);
        return emission != null ? emission.strength : 0;
    }

    @Override
    public String getUrl() {
        return "/redstone";
    }

    @Override
    public List<String> httpMethods() {
        return List.of(ALLOWED_METHOD);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("[EiraRelay] Received " + exchange.getRequestMethod() + " request to /redstone");

        // Parse request body
        RedstoneRequest request = parseRequestBody(exchange);
        if (request == null) {
            sendJsonResponse(exchange, 400, Map.of(
                "success", false,
                "error", "Invalid request body. Expected: {\"x\": int, \"y\": int, \"z\": int, \"strength\": int, \"duration\": int}"
            ));
            return;
        }

        // Validate coordinates
        if (request.x == null || request.y == null || request.z == null) {
            sendJsonResponse(exchange, 400, Map.of(
                "success", false,
                "error", "Missing coordinates (x, y, z required)"
            ));
            return;
        }

        // Default values
        int strength = request.strength != null ? Math.min(15, Math.max(0, request.strength)) : 15;
        int duration = request.duration != null ? Math.max(1, request.duration) : 20; // Default 1 second (20 ticks)

        BlockPos pos = new BlockPos(request.x, request.y, request.z);

        // Create emission
        if (serverLevel != null && serverLevel.getServer() != null) {
            MinecraftServer server = serverLevel.getServer();

            final int finalStrength = strength;
            server.execute(() -> {
                activeEmissions.put(pos, new RedstoneEmission(finalStrength, duration));
                // Notify neighbors
                serverLevel.updateNeighborsAt(pos, Blocks.REDSTONE_BLOCK);
                System.out.println("[EiraRelay] Redstone emission started at " + pos +
                    " (strength=" + finalStrength + ", duration=" + duration + " ticks)");
            });

            sendJsonResponse(exchange, 200, Map.of(
                "success", true,
                "position", Map.of("x", request.x, "y", request.y, "z", request.z),
                "strength", strength,
                "durationTicks", duration
            ));
        } else {
            sendJsonResponse(exchange, 503, Map.of(
                "success", false,
                "error", "Server not available"
            ));
        }
    }

    private RedstoneRequest parseRequestBody(HttpExchange exchange) {
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

            return GSON.fromJson(body, RedstoneRequest.class);
        } catch (Exception e) {
            System.err.println("[EiraRelay] Failed to parse redstone request: " + e.getMessage());
            return null;
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

    // Request body structure
    private static class RedstoneRequest {
        Integer x;
        Integer y;
        Integer z;
        Integer strength;
        Integer duration; // in ticks
    }

    // Active emission tracking
    public static class RedstoneEmission {
        public int strength;
        public int ticksRemaining;

        public RedstoneEmission(int strength, int ticksRemaining) {
            this.strength = strength;
            this.ticksRemaining = ticksRemaining;
        }
    }
}
