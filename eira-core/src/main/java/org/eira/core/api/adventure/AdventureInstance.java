package org.eira.core.api.adventure;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Interface for tracking an active adventure instance.
 */
public interface AdventureInstance {

    /**
     * Get the adventure definition ID.
     */
    String getAdventureId();

    /**
     * Get the unique instance ID.
     */
    UUID getInstanceId();

    /**
     * Get the team participating in this adventure.
     */
    UUID getTeamId();

    /**
     * Get progress as a fraction (0.0 to 1.0).
     */
    float getProgress();

    /**
     * Get IDs of completed checkpoints.
     */
    List<String> getCompletedCheckpointIds();

    /**
     * Get IDs of currently available checkpoints.
     */
    Set<String> getUnlockedCheckpointIds();

    /**
     * Get the current active checkpoint ID.
     */
    Optional<String> getCurrentCheckpointId();

    /**
     * Check if a checkpoint is completed.
     */
    boolean isCheckpointCompleted(String checkpointId);

    /**
     * Check if a checkpoint is unlocked.
     */
    boolean isCheckpointUnlocked(String checkpointId);

    /**
     * Mark a checkpoint as completed.
     */
    void completeCheckpoint(String checkpointId);

    /**
     * Unlock a checkpoint.
     */
    void unlockCheckpoint(String checkpointId);

    /**
     * Get the start time in milliseconds.
     */
    long getStartTime();

    /**
     * Get elapsed time since start.
     */
    Duration getElapsedTime();

    /**
     * Get remaining time (for timed adventures).
     */
    Optional<Duration> getRemainingTime();

    /**
     * Check if the adventure has timed out.
     */
    boolean isTimedOut();

    /**
     * Check if the adventure is still active.
     */
    boolean isActive();

    /**
     * Check if the adventure was completed successfully.
     */
    boolean isCompleted();

    /**
     * Check if the adventure failed.
     */
    boolean isFailed();

    /**
     * Mark the adventure as completed.
     */
    void complete();

    /**
     * Mark the adventure as failed.
     */
    void fail(String reason);

    /**
     * Abandon the adventure.
     */
    void abandon();

    /**
     * Get the current score.
     */
    int getScore();

    /**
     * Add to the score.
     */
    void addScore(int points);

    /**
     * Get the results map (for completed adventures).
     */
    Map<String, Object> getResults();

    /**
     * Get the failure reason (if failed).
     */
    Optional<String> getFailureReason();
}
