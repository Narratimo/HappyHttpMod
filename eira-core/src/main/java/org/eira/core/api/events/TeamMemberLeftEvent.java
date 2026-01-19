package org.eira.core.api.events;

import org.eira.core.api.team.LeaveReason;
import java.util.UUID;

/**
 * Event published when a player leaves a team.
 */
public record TeamMemberLeftEvent(
    UUID teamId,
    UUID playerId,
    LeaveReason reason
) implements EiraEvent {
    public static final String TYPE = "TEAM_MEMBER_LEFT";
}
