package org.eira.core.api.player;

import java.util.Optional;
import java.util.UUID;

/**
 * Extended player data for the Eira ecosystem.
 */
public record EiraPlayer(
    UUID minecraftUuid,
    String name,
    Optional<String> eiraPlayerId,
    Optional<UUID> teamId,
    long firstSeen,
    long lastSeen
) {
    /**
     * Check if this player is linked to an external Eira account.
     */
    public boolean isLinked() {
        return eiraPlayerId.isPresent();
    }

    /**
     * Check if this player is in a team.
     */
    public boolean hasTeam() {
        return teamId.isPresent();
    }
}
