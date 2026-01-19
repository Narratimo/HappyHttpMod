package no.eira.relay.http.handlers;

import no.eira.relay.CommonClass;
import no.eira.relay.block.HttpReceiverBlock;
import no.eira.relay.blockentity.HttpReceiverBlockEntity;
import no.eira.relay.http.api.IHttpHandler;
import no.eira.relay.platform.Services;
import no.eira.relay.platform.config.GlobalParam;
import no.eira.relay.utils.ParameterReader;
import no.eira.relay.utils.PlayerDetector;
import org.eira.core.api.EiraAPI;
import org.eira.core.api.events.HttpReceivedEvent;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpReceiverBlockHandler implements IHttpHandler {

    private static final Gson GSON = new GsonBuilder().create();

    // Store block positions with their player detection settings
    private Map<BlockPos, BlockSettings> blockSettingsMap;
    private ServerLevel serverLevel;
    private String url;
    private String secretToken;
    private static final String ALLOWED_METHOD = "POST";

    private record BlockSettings(boolean playerDetection, double playerDetectionRadius) {}

    public HttpReceiverBlockHandler(HttpReceiverBlockEntity entity, String url, String secretToken){
        this.blockSettingsMap = new HashMap<>();
        HttpReceiverBlockEntity.Values values = entity.getValues();
        this.blockSettingsMap.put(entity.getBlockPos(),
            new BlockSettings(values.playerDetection, values.playerDetectionRadius));
        this.serverLevel = (ServerLevel) entity.getLevel();
        this.url = url;
        this.secretToken = secretToken;
    }

    public static void create(HttpReceiverBlockEntity entity, String url, String secretToken){
        // Ensure URL starts with /
        String normalizedUrl = url.startsWith("/") ? url : "/" + url;

        IHttpHandler handler = CommonClass.HTTP_SERVER.getHandlerByUrl(normalizedUrl);
        if(handler != null) {
            if (handler instanceof HttpReceiverBlockHandler receiverHandler) {
                // Add to existing handler (uses token from first block)
                receiverHandler.addBlockPosition(entity);
                System.out.println("[HttpAutomator] Added block at " + entity.getBlockPos() + " to existing handler for: " + normalizedUrl);
                return;
            }
            // Error because URL already exists with different handler type
            System.out.println("[HttpAutomator] ERROR: URL " + normalizedUrl + " already registered with different handler type");
            return;
        }
        HttpReceiverBlockHandler newHandler = new HttpReceiverBlockHandler(entity, normalizedUrl, secretToken);
        CommonClass.HTTP_SERVER.registerHandler(newHandler);
        System.out.println("[HttpAutomator] Registered NEW handler for endpoint: " + normalizedUrl + " at block " + entity.getBlockPos());
    }

    private void addBlockPosition(HttpReceiverBlockEntity entity){
        BlockPos pos = entity.getBlockPos();
        if (!this.blockSettingsMap.containsKey(pos)) {
            HttpReceiverBlockEntity.Values values = entity.getValues();
            this.blockSettingsMap.put(pos,
                new BlockSettings(values.playerDetection, values.playerDetectionRadius));
        }
        // Update level reference if needed
        if (this.serverLevel == null) {
            this.serverLevel = (ServerLevel) entity.getLevel();
        }
    }

    @Override
    public String getUrl() {
        return this.url;
    }

    @Override
    public List<String> httpMethods() {
        return List.of(ALLOWED_METHOD);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("[EiraRelay] Received " + exchange.getRequestMethod() + " request to " + exchange.getRequestURI());

        try {
            // Check secret token first
            if (!validateToken(exchange)) {
                return; // Response already sent
            }

            // Check global parameters
            List<GlobalParam> globalParams = Services.HTTP_CONFIG.getGlobalParams();
            if (!globalParams.isEmpty()) {
                if (!checkGlobalParams(exchange, globalParams)) {
                    return; // Response already sent by checkGlobalParams
                }
            }

            int signalsSent = 0;
            List<Map<String, Object>> triggeredBlocks = new ArrayList<>();

            if (serverLevel != null && serverLevel.getServer() != null) {
                MinecraftServer server = serverLevel.getServer();

                // Copy the map to avoid concurrent modification
                Map<BlockPos, BlockSettings> settingsCopy = new HashMap<>(blockSettingsMap);

                for (Map.Entry<BlockPos, BlockSettings> entry : settingsCopy.entrySet()) {
                    BlockPos pos = entry.getKey();
                    BlockSettings settings = entry.getValue();

                    // Detect player if enabled
                    PlayerDetector.PlayerInfo detectedPlayer = null;
                    if (settings.playerDetection) {
                        Optional<PlayerDetector.PlayerInfo> playerInfo =
                            PlayerDetector.getNearestPlayerInfo(serverLevel, pos, settings.playerDetectionRadius);
                        if (playerInfo.isPresent()) {
                            detectedPlayer = playerInfo.get();
                        }
                    }

                    // Build block trigger info
                    Map<String, Object> blockInfo = new LinkedHashMap<>();
                    blockInfo.put("x", pos.getX());
                    blockInfo.put("y", pos.getY());
                    blockInfo.put("z", pos.getZ());

                    if (detectedPlayer != null) {
                        Map<String, Object> playerData = new LinkedHashMap<>();
                        playerData.put("uuid", detectedPlayer.uuid().toString());
                        playerData.put("name", detectedPlayer.name());
                        playerData.put("distance", Math.round(detectedPlayer.distance() * 100.0) / 100.0);
                        blockInfo.put("player", playerData);
                    }

                    triggeredBlocks.add(blockInfo);

                    // Capture player for event
                    final PlayerDetector.PlayerInfo finalPlayer = detectedPlayer;

                    // Schedule on main server thread
                    server.execute(() -> {
                        try {
                            // Look up the block entity fresh each time
                            BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
                            if (blockEntity instanceof HttpReceiverBlockEntity receiver) {
                                // Get current block state
                                BlockState state = serverLevel.getBlockState(pos);
                                if (state.getBlock() instanceof HttpReceiverBlock block) {
                                    // Directly call onSignal on the block
                                    block.onSignal(state, serverLevel, pos);
                                    System.out.println("[EiraRelay] Triggered signal at block position: " + pos +
                                        (finalPlayer != null ? " (player: " + finalPlayer.name() + ")" : ""));
                                }
                            } else {
                                System.out.println("[EiraRelay] Block entity at " + pos + " is not HttpReceiverBlockEntity or is null");
                            }
                        } catch (Exception e) {
                            System.err.println("[EiraRelay] Error triggering block at " + pos + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                    signalsSent++;
                }
            } else {
                System.out.println("[EiraRelay] Server level is null, cannot process request");
            }

            // Publish event to Eira Core if available
            final String endpoint = getUrl();
            final String method = exchange.getRequestMethod();
            try {
                Map<String, String> allParams = ParameterReader.getAllParameters(exchange);
                Map<String, Object> eventParams = new HashMap<>(allParams);
                // Add triggered blocks info to event
                eventParams.put("triggeredBlocks", triggeredBlocks);
                EiraAPI.ifPresent(api -> {
                    api.events().publish(new HttpReceivedEvent(endpoint, method, eventParams));
                });
            } catch (Exception e) {
                // Ignore event publishing errors
            }

            // Build JSON response
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "ok");
            response.put("message", "Signal sent to " + signalsSent + " block(s)");
            response.put("blocks", triggeredBlocks);

            String jsonResponse = GSON.toJson(response);
            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }

            System.out.println("[EiraRelay] Response sent: " + jsonResponse);

        } catch (Exception e) {
            // Send error response
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            String jsonError = GSON.toJson(errorResponse);
            byte[] errorBytes = jsonError.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(500, errorBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(errorBytes);
            }
            System.err.println("[EiraRelay] Error handling request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean checkGlobalParams(HttpExchange exchange, List<GlobalParam> globalParams) throws IOException {
        Map<String, String> params = ParameterReader.getAllParameters(exchange);
        String redirect = Services.HTTP_CONFIG.getGlobalRedirect();

        // If no params in request, redirect to global redirect URL if configured
        if (params.isEmpty()) {
            if (redirect != null && !redirect.isEmpty()) {
                exchange.getResponseHeaders().add("Location", redirect);
                exchange.sendResponseHeaders(308, 0);
                exchange.close();
            }
            return false;
        }

        // Check each global param
        for (GlobalParam param : globalParams) {
            if (param.name == null) continue;

            String requestParam = params.get(param.name);
            if (requestParam == null) {
                // Param not in the request
                if (param.redirectWrong != null && !param.redirectWrong.isEmpty()) {
                    exchange.getResponseHeaders().add("Location", param.redirectWrong);
                    exchange.sendResponseHeaders(308, 0);
                    exchange.close();
                }
                return false;
            }
            if (param.value != null && !param.value.equals(requestParam)) {
                // Param value doesn't match
                if (param.redirectWrong != null && !param.redirectWrong.isEmpty()) {
                    exchange.getResponseHeaders().add("Location", param.redirectWrong);
                    exchange.sendResponseHeaders(308, 0);
                    exchange.close();
                }
                return false;
            }
        }
        return true;
    }

    private boolean validateToken(HttpExchange exchange) throws IOException {
        // No token required if not configured
        if (secretToken == null || secretToken.isEmpty()) {
            return true;
        }

        // Check Authorization header first (Bearer token)
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.equals("Bearer " + secretToken)) {
            return true;
        }

        // Check query parameter as fallback
        Map<String, String> params = ParameterReader.getAllParameters(exchange);
        String paramToken = params.get("token");
        if (paramToken != null && paramToken.equals(secretToken)) {
            return true;
        }

        // Unauthorized - send 401 response
        String error = "{\"error\": \"Unauthorized\"}";
        byte[] errorBytes = error.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(401, errorBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(errorBytes);
        }
        System.out.println("[EiraRelay] Unauthorized request - invalid or missing token");
        return false;
    }
}
