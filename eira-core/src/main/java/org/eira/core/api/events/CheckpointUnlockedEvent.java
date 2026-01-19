package org.eira.core.api.events;

import java.util.UUID;

/**
 * Event published when a checkpoint becomes available in an adventure.
 */
public record CheckpointUnlockedEvent(
    UUID instanceId,
    String adventureId,
    String checkpointId
) implements EiraEvent {
    public static final String TYPE = "CHECKPOINT_UNLOCKED";
}
