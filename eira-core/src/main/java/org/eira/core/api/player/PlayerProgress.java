package org.eira.core.api.player;

import java.util.Map;
import java.util.Set;

/**
 * Interface for tracking player progress.
 */
public interface PlayerProgress {

    /**
     * Get the player's total score.
     */
    int getScore();

    /**
     * Add to the player's score.
     *
     * @param points Points to add (can be negative)
     */
    void addScore(int points);

    /**
     * Check if a flag is set.
     *
     * @param flag Flag name
     * @return true if flag is set
     */
    boolean hasFlag(String flag);

    /**
     * Set or clear a flag.
     *
     * @param flag Flag name
     * @param value true to set, false to clear
     */
    void setFlag(String flag, boolean value);

    /**
     * Get all set flags.
     *
     * @return Set of flag names
     */
    Set<String> getFlags();

    /**
     * Get a counter value.
     *
     * @param key Counter key
     * @return Counter value (0 if not set)
     */
    int getCounter(String key);

    /**
     * Set a counter value.
     *
     * @param key Counter key
     * @param value Counter value
     */
    void setCounter(String key, int value);

    /**
     * Increment a counter.
     *
     * @param key Counter key
     * @param amount Amount to add (can be negative)
     * @return New counter value
     */
    int incrementCounter(String key, int amount);

    /**
     * Get all counters.
     *
     * @return Map of counter keys to values
     */
    Map<String, Integer> getCounters();

    /**
     * Reset all progress.
     */
    void reset();
}
