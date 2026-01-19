package no.eira.relay.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Utility for detecting players near blocks.
 */
public class PlayerDetector {

    public static final double DEFAULT_RADIUS = 16.0;

    /**
     * Find the nearest player to a block position.
     *
     * @param level The server level
     * @param pos The block position
     * @param radius Detection radius
     * @return Optional containing the nearest player, or empty if none found
     */
    public static Optional<ServerPlayer> findNearestPlayer(ServerLevel level, BlockPos pos, double radius) {
        if (level == null) {
            return Optional.empty();
        }

        AABB searchArea = new AABB(pos).inflate(radius);
        List<ServerPlayer> players = level.getPlayers(player ->
            searchArea.contains(player.getX(), player.getY(), player.getZ())
        );

        if (players.isEmpty()) {
            return Optional.empty();
        }

        // Find nearest by distance
        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 0.5;
        double centerZ = pos.getZ() + 0.5;

        return players.stream()
            .min(Comparator.comparingDouble(p ->
                p.distanceToSqr(centerX, centerY, centerZ)
            ));
    }

    /**
     * Find the nearest player using default radius.
     */
    public static Optional<ServerPlayer> findNearestPlayer(ServerLevel level, BlockPos pos) {
        return findNearestPlayer(level, pos, DEFAULT_RADIUS);
    }

    /**
     * Get all players within radius of a block.
     *
     * @param level The server level
     * @param pos The block position
     * @param radius Detection radius
     * @return List of players within range
     */
    public static List<ServerPlayer> findPlayersInRange(ServerLevel level, BlockPos pos, double radius) {
        if (level == null) {
            return List.of();
        }

        AABB searchArea = new AABB(pos).inflate(radius);
        return level.getPlayers(player ->
            searchArea.contains(player.getX(), player.getY(), player.getZ())
        );
    }

    /**
     * Information about a detected player.
     */
    public record PlayerInfo(UUID uuid, String name, double distance) {

        /**
         * Create PlayerInfo from a player entity and block position.
         */
        public static PlayerInfo from(ServerPlayer player, BlockPos pos) {
            double distance = Math.sqrt(player.distanceToSqr(
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5
            ));
            return new PlayerInfo(
                player.getUUID(),
                player.getName().getString(),
                distance
            );
        }
    }

    /**
     * Get info about the nearest player.
     */
    public static Optional<PlayerInfo> getNearestPlayerInfo(ServerLevel level, BlockPos pos, double radius) {
        return findNearestPlayer(level, pos, radius)
            .map(player -> PlayerInfo.from(player, pos));
    }
}
