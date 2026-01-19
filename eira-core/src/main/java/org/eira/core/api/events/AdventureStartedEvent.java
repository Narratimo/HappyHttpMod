package org.eira.core.api.events;

import java.util.UUID;

/**
 * Event published when an adventure is started.
 */
public record AdventureStartedEvent(
    UUID instanceId,
    String adventureId,
    UUID teamId
) implements EiraEvent {
    public static final String TYPE = "ADVENTURE_STARTED";
}
