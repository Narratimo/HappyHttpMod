package no.eira.relay.http.handlers;

import no.eira.relay.CommonClass;
import no.eira.relay.block.HttpReceiverBlock;
import no.eira.relay.blockentity.HttpReceiverBlockEntity;
import no.eira.relay.http.api.IHttpHandler;
import com.sun.net.httpserver.HttpExchange;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpReceiverBlockHandler implements IHttpHandler {

    // Store block positions and their level instead of direct entity references
    private List<BlockPos> blockPositions;
    private ServerLevel serverLevel;
    private String url;
    private static final String ALLOWED_METHOD = "POST";

    public HttpReceiverBlockHandler(HttpReceiverBlockEntity entity, String url){
        this.blockPositions = new ArrayList<>();
        this.blockPositions.add(entity.getBlockPos());
        this.serverLevel = (ServerLevel) entity.getLevel();
        this.url = url;
    }

    public static void create(HttpReceiverBlockEntity entity, String url){
        // Ensure URL starts with /
        String normalizedUrl = url.startsWith("/") ? url : "/" + url;
        
        IHttpHandler handler = CommonClass.HTTP_SERVER.getHandlerByUrl(normalizedUrl);
        if(handler != null) {
            if (handler instanceof HttpReceiverBlockHandler receiverHandler) {
                // Add to existing handler
                receiverHandler.addBlockPosition(entity.getBlockPos(), (ServerLevel) entity.getLevel());
                System.out.println("[HttpAutomator] Added block at " + entity.getBlockPos() + " to existing handler for: " + normalizedUrl);
                return;
            }
            // Error because URL already exists with different handler type
            System.out.println("[HttpAutomator] ERROR: URL " + normalizedUrl + " already registered with different handler type");
            return;
        }
        HttpReceiverBlockHandler newHandler = new HttpReceiverBlockHandler(entity, normalizedUrl);
        CommonClass.HTTP_SERVER.registerHandler(newHandler);
        System.out.println("[HttpAutomator] Registered NEW handler for endpoint: " + normalizedUrl + " at block " + entity.getBlockPos());
    }

    private void addBlockPosition(BlockPos pos, ServerLevel level){
        if (!this.blockPositions.contains(pos)) {
            this.blockPositions.add(pos);
        }
        // Update level reference if needed
        if (this.serverLevel == null) {
            this.serverLevel = level;
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
        System.out.println("[HttpAutomator] Received " + exchange.getRequestMethod() + " request to " + exchange.getRequestURI());
        
        try {
            int signalsSent = 0;
            
            if (serverLevel != null && serverLevel.getServer() != null) {
                MinecraftServer server = serverLevel.getServer();
                
                // Copy the list to avoid concurrent modification
                List<BlockPos> positionsCopy = new ArrayList<>(blockPositions);
                
                for (BlockPos pos : positionsCopy) {
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
                                    System.out.println("[HttpAutomator] Triggered signal at block position: " + pos);
                                }
                            } else {
                                System.out.println("[HttpAutomator] Block entity at " + pos + " is not HttpReceiverBlockEntity or is null");
                            }
                        } catch (Exception e) {
                            System.err.println("[HttpAutomator] Error triggering block at " + pos + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                    signalsSent++;
                }
            } else {
                System.out.println("[HttpAutomator] Server level is null, cannot process request");
            }
            
            // Send success response
            String response = "OK - Signal sent to " + signalsSent + " block(s)";
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
            
            System.out.println("[HttpAutomator] Response sent: " + response);
            
        } catch (Exception e) {
            // Send error response
            String errorResponse = "Error: " + e.getMessage();
            byte[] errorBytes = errorResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, errorBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(errorBytes);
            }
            System.err.println("[HttpAutomator] Error handling request: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
