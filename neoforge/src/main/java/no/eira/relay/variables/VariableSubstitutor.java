package no.eira.relay.variables;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Substitutes {{variableName}} placeholders in strings with actual values.
 */
public class VariableSubstitutor {

    // Pattern for {{variableName}} placeholders
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([a-zA-Z0-9_]+)}}");

    /**
     * Substitute all {{variable}} placeholders in a string.
     *
     * @param input The string containing placeholders
     * @param blockPos The block position for block-scoped variables (can be null)
     * @param level The server level for built-in variables (can be null)
     * @return The string with placeholders replaced
     */
    public static String substitute(String input, BlockPos blockPos, ServerLevel level) {
        return substitute(input, blockPos, null, level);
    }

    /**
     * Substitute all {{variable}} placeholders in a string with player context.
     *
     * @param input The string containing placeholders
     * @param blockPos The block position for block-scoped variables (can be null)
     * @param playerId The player UUID for player-scoped variables (can be null)
     * @param level The server level for built-in variables (can be null)
     * @return The string with placeholders replaced
     */
    public static String substitute(String input, BlockPos blockPos, UUID playerId, ServerLevel level) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        VariableStorage storage = VariableStorage.getInstance();
        StringBuffer result = new StringBuffer();
        Matcher matcher = VARIABLE_PATTERN.matcher(input);

        while (matcher.find()) {
            String varName = matcher.group(1);
            String value = resolveVariable(varName, blockPos, playerId, level, storage);
            // Escape special regex replacement characters
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Substitute variables in a map of parameters.
     */
    public static Map<String, String> substituteMap(Map<String, String> params, BlockPos blockPos, ServerLevel level) {
        return substituteMap(params, blockPos, null, level);
    }

    /**
     * Substitute variables in a map of parameters with player context.
     */
    public static Map<String, String> substituteMap(Map<String, String> params, BlockPos blockPos, UUID playerId, ServerLevel level) {
        if (params == null || params.isEmpty()) {
            return params;
        }

        java.util.Map<String, String> result = new java.util.HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = substitute(entry.getKey(), blockPos, playerId, level);
            String value = substitute(entry.getValue(), blockPos, playerId, level);
            result.put(key, value);
        }
        return result;
    }

    /**
     * Resolve a single variable name to its value.
     */
    private static String resolveVariable(String varName, BlockPos blockPos, UUID playerId,
                                          ServerLevel level, VariableStorage storage) {
        // Check for built-in variables first
        String builtIn = resolveBuiltInVariable(varName, blockPos, playerId, level);
        if (builtIn != null) {
            return builtIn;
        }

        // Then check storage (player -> block -> global)
        return storage.resolve(blockPos, playerId, varName);
    }

    /**
     * Resolve built-in variables.
     */
    private static String resolveBuiltInVariable(String varName, BlockPos blockPos,
                                                  UUID playerId, ServerLevel level) {
        switch (varName.toLowerCase()) {
            case "blockx":
                return blockPos != null ? String.valueOf(blockPos.getX()) : "";
            case "blocky":
                return blockPos != null ? String.valueOf(blockPos.getY()) : "";
            case "blockz":
                return blockPos != null ? String.valueOf(blockPos.getZ()) : "";
            case "blockpos":
                return blockPos != null ? String.format("%d,%d,%d", blockPos.getX(), blockPos.getY(), blockPos.getZ()) : "";

            case "worldtime":
            case "gametime":
                return level != null ? String.valueOf(level.getGameTime()) : "";
            case "daytime":
                return level != null ? String.valueOf(level.getDayTime()) : "";

            case "playeruuid":
                return playerId != null ? playerId.toString() : "";

            case "playername":
                if (playerId != null && level != null) {
                    Player player = level.getPlayerByUUID(playerId);
                    return player != null ? player.getName().getString() : "";
                }
                // Try to find nearest player if no specific player
                if (blockPos != null && level != null) {
                    Player nearest = findNearestPlayer(level, blockPos, 16);
                    return nearest != null ? nearest.getName().getString() : "";
                }
                return "";

            case "timestamp":
                return String.valueOf(System.currentTimeMillis());
            case "timestampsec":
                return String.valueOf(System.currentTimeMillis() / 1000);

            default:
                return null; // Not a built-in variable
        }
    }

    /**
     * Find the nearest player to a position.
     */
    private static Player findNearestPlayer(ServerLevel level, BlockPos pos, double radius) {
        AABB area = new AABB(pos).inflate(radius);
        List<Player> players = level.getEntitiesOfClass(Player.class, area);
        if (players.isEmpty()) {
            return null;
        }

        Player nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (Player player : players) {
            double dist = player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = player;
            }
        }
        return nearest;
    }

    /**
     * Check if a string contains any variable placeholders.
     */
    public static boolean containsVariables(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return VARIABLE_PATTERN.matcher(input).find();
    }

    /**
     * Extract all variable names from a string.
     */
    public static java.util.Set<String> extractVariableNames(String input) {
        java.util.Set<String> names = new java.util.HashSet<>();
        if (input == null || input.isEmpty()) {
            return names;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(input);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }
}
