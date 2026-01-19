package no.eira.relay.http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import no.eira.relay.http.api.IHttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Handler for POST /broadcast
 * Send messages to players in-game.
 */
public class BroadcastHandler implements IHttpHandler {

    private static final String ALLOWED_METHOD = "POST";
    private static final Gson GSON = new Gson();

    // Server level reference
    private static ServerLevel serverLevel;

    /**
     * Set the server level for broadcast operations
     */
    public static void setServerLevel(ServerLevel level) {
        serverLevel = level;
    }

    @Override
    public String getUrl() {
        return "/broadcast";
    }

    @Override
    public List<String> httpMethods() {
        return List.of(ALLOWED_METHOD);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("[EiraRelay] Received " + exchange.getRequestMethod() + " request to /broadcast");

        // Parse request body
        BroadcastRequest request = parseRequestBody(exchange);
        if (request == null || request.message == null || request.message.isEmpty()) {
            sendJsonResponse(exchange, 400, Map.of(
                "success", false,
                "error", "Invalid request body. Expected: {\"message\": \"string\", \"type\": \"chat|title|actionbar\", \"radius\": int, \"position\": [x,y,z]}"
            ));
            return;
        }

        // Default message type
        String type = request.type != null ? request.type.toLowerCase() : "chat";
        if (!type.equals("chat") && !type.equals("title") && !type.equals("actionbar")) {
            type = "chat";
        }

        if (serverLevel == null || serverLevel.getServer() == null) {
            sendJsonResponse(exchange, 503, Map.of(
                "success", false,
                "error", "Server not available"
            ));
            return;
        }

        MinecraftServer server = serverLevel.getServer();
        final String messageType = type;
        final int[] playersReached = {0};

        server.execute(() -> {
            List<ServerPlayer> players = getTargetPlayers(request);
            Component message = Component.literal(request.message);

            for (ServerPlayer player : players) {
                try {
                    switch (messageType) {
                        case "chat":
                            player.sendSystemMessage(message);
                            break;
                        case "title":
                            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(message));
                            break;
                        case "actionbar":
                            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket(message));
                            break;
                    }
                    playersReached[0]++;
                } catch (Exception e) {
                    System.err.println("[EiraRelay] Failed to send message to player: " + e.getMessage());
                }
            }

            System.out.println("[EiraRelay] Broadcast sent to " + playersReached[0] + " player(s): " + request.message);
        });

        // Send response (note: player count may not be accurate due to async execution)
        sendJsonResponse(exchange, 200, Map.of(
            "success", true,
            "message", request.message,
            "type", messageType,
            "broadcast", true
        ));
    }

    private List<ServerPlayer> getTargetPlayers(BroadcastRequest request) {
        if (serverLevel == null) return Collections.emptyList();

        List<ServerPlayer> allPlayers = serverLevel.players();

        // If no position or radius specified, send to all players
        if (request.position == null || request.position.length < 3 || request.radius == null || request.radius <= 0) {
            return new ArrayList<>(allPlayers);
        }

        // Filter by radius from position
        double x = request.position[0];
        double y = request.position[1];
        double z = request.position[2];
        double radiusSq = request.radius * request.radius;

        List<ServerPlayer> nearbyPlayers = new ArrayList<>();
        for (ServerPlayer player : allPlayers) {
            double dx = player.getX() - x;
            double dy = player.getY() - y;
            double dz = player.getZ() - z;
            double distSq = dx * dx + dy * dy + dz * dz;

            if (distSq <= radiusSq) {
                nearbyPlayers.add(player);
            }
        }

        return nearbyPlayers;
    }

    private BroadcastRequest parseRequestBody(HttpExchange exchange) {
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

            return GSON.fromJson(body, BroadcastRequest.class);
        } catch (Exception e) {
            System.err.println("[EiraRelay] Failed to parse broadcast request: " + e.getMessage());
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
    private static class BroadcastRequest {
        String message;
        String type; // "chat", "title", "actionbar"
        Integer radius; // blocks from position
        double[] position; // [x, y, z]
    }
}
