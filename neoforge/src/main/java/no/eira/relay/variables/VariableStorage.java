package no.eira.relay.variables;

import no.eira.relay.Constants;
import net.minecraft.core.BlockPos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Storage system for HTTP response variables.
 * Supports global, block-scoped, and player-scoped variables.
 */
public class VariableStorage {

    private static final VariableStorage INSTANCE = new VariableStorage();

    // Global variables accessible from any block
    private final Map<String, String> globalVariables = new ConcurrentHashMap<>();

    // Block-scoped variables: BlockPos -> VariableName -> Value
    private final Map<BlockPos, Map<String, String>> blockVariables = new ConcurrentHashMap<>();

    // Player-scoped variables: PlayerUUID -> VariableName -> Value
    private final Map<UUID, Map<String, String>> playerVariables = new ConcurrentHashMap<>();

    // Last response storage per block (for debugging/display)
    private final Map<BlockPos, ResponseInfo> lastResponses = new ConcurrentHashMap<>();

    private VariableStorage() {}

    public static VariableStorage getInstance() {
        return INSTANCE;
    }

    // ===== Global Variables =====

    public void setGlobal(String name, String value) {
        globalVariables.put(name, value);
        Constants.LOG.debug("Set global variable '{}' = '{}'", name, truncate(value));
    }

    public String getGlobal(String name) {
        return globalVariables.getOrDefault(name, "");
    }

    public String getGlobal(String name, String defaultValue) {
        return globalVariables.getOrDefault(name, defaultValue);
    }

    public boolean hasGlobal(String name) {
        return globalVariables.containsKey(name);
    }

    public void removeGlobal(String name) {
        globalVariables.remove(name);
    }

    public void clearGlobalVariables() {
        globalVariables.clear();
    }

    public Map<String, String> getAllGlobalVariables() {
        return new ConcurrentHashMap<>(globalVariables);
    }

    // ===== Block-Scoped Variables =====

    public void setBlock(BlockPos pos, String name, String value) {
        blockVariables.computeIfAbsent(pos, k -> new ConcurrentHashMap<>()).put(name, value);
        Constants.LOG.debug("Set block variable at {} '{}' = '{}'", pos, name, truncate(value));
    }

    public String getBlock(BlockPos pos, String name) {
        Map<String, String> vars = blockVariables.get(pos);
        return vars != null ? vars.getOrDefault(name, "") : "";
    }

    public String getBlock(BlockPos pos, String name, String defaultValue) {
        Map<String, String> vars = blockVariables.get(pos);
        return vars != null ? vars.getOrDefault(name, defaultValue) : defaultValue;
    }

    public boolean hasBlock(BlockPos pos, String name) {
        Map<String, String> vars = blockVariables.get(pos);
        return vars != null && vars.containsKey(name);
    }

    public void clearBlockVariables(BlockPos pos) {
        blockVariables.remove(pos);
        lastResponses.remove(pos);
    }

    public Map<String, String> getAllBlockVariables(BlockPos pos) {
        Map<String, String> vars = blockVariables.get(pos);
        return vars != null ? new ConcurrentHashMap<>(vars) : new ConcurrentHashMap<>();
    }

    // ===== Player-Scoped Variables =====

    public void setPlayer(UUID playerId, String name, String value) {
        playerVariables.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>()).put(name, value);
        Constants.LOG.debug("Set player variable for {} '{}' = '{}'", playerId, name, truncate(value));
    }

    public String getPlayer(UUID playerId, String name) {
        Map<String, String> vars = playerVariables.get(playerId);
        return vars != null ? vars.getOrDefault(name, "") : "";
    }

    public String getPlayer(UUID playerId, String name, String defaultValue) {
        Map<String, String> vars = playerVariables.get(playerId);
        return vars != null ? vars.getOrDefault(name, defaultValue) : defaultValue;
    }

    public void clearPlayerVariables(UUID playerId) {
        playerVariables.remove(playerId);
    }

    // ===== Last Response Tracking =====

    public void setLastResponse(BlockPos pos, int statusCode, String body, long responseTimeMs) {
        lastResponses.put(pos, new ResponseInfo(statusCode, body, responseTimeMs, System.currentTimeMillis()));
    }

    public ResponseInfo getLastResponse(BlockPos pos) {
        return lastResponses.get(pos);
    }

    // ===== Variable Resolution =====

    /**
     * Resolve a variable by name, checking block scope first, then global.
     */
    public String resolve(BlockPos blockPos, String name) {
        // Check block scope first
        if (blockPos != null && hasBlock(blockPos, name)) {
            return getBlock(blockPos, name);
        }
        // Fall back to global
        return getGlobal(name);
    }

    /**
     * Resolve a variable with player context.
     */
    public String resolve(BlockPos blockPos, UUID playerId, String name) {
        // Check player scope first
        if (playerId != null) {
            Map<String, String> vars = playerVariables.get(playerId);
            if (vars != null && vars.containsKey(name)) {
                return vars.get(name);
            }
        }
        // Fall back to block/global resolution
        return resolve(blockPos, name);
    }

    // ===== Cleanup =====

    /**
     * Clear all variables (call on server stop).
     */
    public void clearAll() {
        globalVariables.clear();
        blockVariables.clear();
        playerVariables.clear();
        lastResponses.clear();
        Constants.LOG.debug("Cleared all response variables");
    }

    private String truncate(String value) {
        if (value == null) return "null";
        return value.length() > 50 ? value.substring(0, 50) + "..." : value;
    }

    /**
     * Information about the last HTTP response for a block.
     */
    public record ResponseInfo(int statusCode, String body, long responseTimeMs, long timestamp) {
        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }
    }
}
