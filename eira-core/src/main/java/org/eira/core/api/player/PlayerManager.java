package org.eira.core.api.player;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface for managing player data in the Eira ecosystem.
 */
public interface PlayerManager {

    /**
     * Get player data by Minecraft UUID.
     * Creates a new entry if player doesn't exist.
     *
     * @param minecraftUuid Player's Minecraft UUID
     * @return Player data
     */
    EiraPlayer get(UUID minecraftUuid);

    /**
     * Get player data by name (case-insensitive).
     *
     * @param name Player name
     * @return Player data if found
     */
    Optional<EiraPlayer> getByName(String name);

    /**
     * Get player data by Eira player ID.
     *
     * @param eiraPlayerId External Eira player ID
     * @return Player data if found
     */
    Optional<EiraPlayer> getByEiraId(String eiraPlayerId);

    /**
     * Link a Minecraft player to an external Eira account.
     *
     * @param minecraftUuid Player's Minecraft UUID
     * @param eiraPlayerId External Eira player ID
     */
    void linkEiraId(UUID minecraftUuid, String eiraPlayerId);

    /**
     * Unlink a Minecraft player from their Eira account.
     *
     * @param minecraftUuid Player's Minecraft UUID
     */
    void unlinkEiraId(UUID minecraftUuid);

    /**
     * Get all known players.
     *
     * @return Collection of all players
     */
    Collection<EiraPlayer> getAllPlayers();

    /**
     * Get all currently online players.
     *
     * @return Collection of online players
     */
    Collection<EiraPlayer> getOnlinePlayers();

    /**
     * Check if a player is currently online.
     *
     * @param minecraftUuid Player's Minecraft UUID
     * @return true if online
     */
    boolean isOnline(UUID minecraftUuid);

    /**
     * Mark a player as online.
     *
     * @param minecraftUuid Player's Minecraft UUID
     * @param name Player's current name
     */
    void setOnline(UUID minecraftUuid, String name);

    /**
     * Mark a player as offline.
     *
     * @param minecraftUuid Player's Minecraft UUID
     */
    void setOffline(UUID minecraftUuid);

    /**
     * Get player progress tracker.
     *
     * @param minecraftUuid Player's Minecraft UUID
     * @return Progress tracker
     */
    PlayerProgress getProgress(UUID minecraftUuid);
}
