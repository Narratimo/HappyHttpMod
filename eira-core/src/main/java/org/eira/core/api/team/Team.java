package org.eira.core.api.team;

import java.util.Set;
import java.util.UUID;

/**
 * Immutable representation of a team for API consumers.
 */
public record Team(
    UUID id,
    String name,
    UUID leaderId,
    Set<UUID> memberIds,
    int maxSize,
    String color,
    long createdAt
) {
    /**
     * Check if the team is at maximum capacity.
     */
    public boolean isFull() {
        return memberIds.size() >= maxSize;
    }

    /**
     * Check if a player is a member of this team.
     */
    public boolean hasMember(UUID playerId) {
        return memberIds.contains(playerId);
    }

    /**
     * Check if a player is the leader of this team.
     */
    public boolean isLeader(UUID playerId) {
        return leaderId.equals(playerId);
    }

    /**
     * Get the current member count.
     */
    public int memberCount() {
        return memberIds.size();
    }
}
