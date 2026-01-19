package org.eira.core.api.events;

import java.util.UUID;

/**
 * Event published when an adventure fails.
 */
public record AdventureFailedEvent(
    UUID instanceId,
    String adventureId,
    UUID teamId,
    String reason
) implements EiraEvent {
    public static final String TYPE = "ADVENTURE_FAILED";
}
