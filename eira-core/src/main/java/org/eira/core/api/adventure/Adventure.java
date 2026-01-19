package org.eira.core.api.adventure;

import java.util.List;
import java.util.Map;

/**
 * Definition of an adventure in the Eira ecosystem.
 */
public record Adventure(
    String id,
    String name,
    String description,
    AdventureType type,
    int timeLimitSeconds,
    int minTeams,
    int maxTeams,
    int minTeamSize,
    int maxTeamSize,
    List<Checkpoint> checkpoints,
    Map<String, Object> rewards,
    Map<String, Object> metadata
) {
    /**
     * Check if this adventure has a time limit.
     */
    public boolean isTimed() {
        return timeLimitSeconds > 0;
    }

    /**
     * Get a checkpoint by ID.
     */
    public Checkpoint getCheckpoint(String checkpointId) {
        return checkpoints.stream()
            .filter(c -> c.id().equals(checkpointId))
            .findFirst()
            .orElse(null);
    }

    /**
     * Get the total number of checkpoints.
     */
    public int checkpointCount() {
        return checkpoints != null ? checkpoints.size() : 0;
    }

    /**
     * Get metadata value.
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, T defaultValue) {
        if (metadata == null) return defaultValue;
        Object value = metadata.get(key);
        return value != null ? (T) value : defaultValue;
    }
}
