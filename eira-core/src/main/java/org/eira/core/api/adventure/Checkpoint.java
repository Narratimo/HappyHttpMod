package org.eira.core.api.adventure;

import java.util.List;
import java.util.Map;

/**
 * Definition of a checkpoint in an adventure.
 */
public record Checkpoint(
    String id,
    String name,
    String description,
    String triggerType,
    Map<String, Object> triggerData,
    String hintNpcId,
    List<String> prerequisites
) {
    /**
     * Check if this checkpoint has prerequisites.
     */
    public boolean hasPrerequisites() {
        return prerequisites != null && !prerequisites.isEmpty();
    }

    /**
     * Check if a specific checkpoint is a prerequisite.
     */
    public boolean requiresCheckpoint(String checkpointId) {
        return prerequisites != null && prerequisites.contains(checkpointId);
    }

    /**
     * Get trigger data value.
     */
    @SuppressWarnings("unchecked")
    public <T> T getTriggerData(String key, T defaultValue) {
        if (triggerData == null) return defaultValue;
        Object value = triggerData.get(key);
        return value != null ? (T) value : defaultValue;
    }
}
