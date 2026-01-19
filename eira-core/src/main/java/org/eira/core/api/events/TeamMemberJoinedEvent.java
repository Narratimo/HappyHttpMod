package org.eira.core.api.events;

import java.util.UUID;

/**
 * Event published when a player joins a team.
 */
public record TeamMemberJoinedEvent(
    UUID teamId,
    UUID playerId
) implements EiraEvent {
    public static final String TYPE = "TEAM_MEMBER_JOINED";
}
