package org.eira.core.api.events;

import java.util.UUID;

/**
 * Event published when a checkpoint is completed (received from Eira Server).
 */
public record CheckpointCompletedEvent(
    String gameId,
    String checkpointId,
    UUID playerId,
    UUID teamId
) implements EiraEvent {}
