package org.eira.core.api.events;

import org.eira.core.api.team.Team;
import java.util.UUID;

/**
 * Event published when a new team is created.
 */
public record TeamCreatedEvent(
    Team team,
    UUID creatorId
) implements EiraEvent {
    public static final String TYPE = "TEAM_CREATED";
}
