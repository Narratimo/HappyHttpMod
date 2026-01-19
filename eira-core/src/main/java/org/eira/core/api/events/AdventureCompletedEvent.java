package org.eira.core.api.events;

import java.util.UUID;

/**
 * Event published when an adventure is completed successfully.
 */
public record AdventureCompletedEvent(
    UUID instanceId,
    String adventureId,
    UUID teamId,
    int score,
    long durationSeconds
) implements EiraEvent {
    public static final String TYPE = "ADVENTURE_COMPLETED";
}
