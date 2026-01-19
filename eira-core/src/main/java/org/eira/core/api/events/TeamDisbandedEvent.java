package org.eira.core.api.events;

import java.util.UUID;

/**
 * Event published when a team is disbanded.
 */
public record TeamDisbandedEvent(
    UUID teamId,
    String teamName
) implements EiraEvent {
    public static final String TYPE = "TEAM_DISBANDED";
}
