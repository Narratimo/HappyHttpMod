package org.eira.core.api.adventure;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface for managing adventures in the Eira ecosystem.
 */
public interface AdventureManager {

    /**
     * Register an adventure definition.
     */
    void registerAdventure(Adventure adventure);

    /**
     * Unregister an adventure definition.
     */
    void unregisterAdventure(String adventureId);

    /**
     * Get an adventure by ID.
     */
    Optional<Adventure> getAdventure(String adventureId);

    /**
     * Get all registered adventures.
     */
    Collection<Adventure> getAllAdventures();

    /**
     * Start an adventure for a team.
     */
    AdventureInstance start(String adventureId, UUID teamId);

    /**
     * Get an adventure instance by ID.
     */
    Optional<AdventureInstance> getInstance(UUID instanceId);

    /**
     * Get the active adventure instance for a team.
     */
    Optional<AdventureInstance> getActiveInstance(UUID teamId);

    /**
     * Get all active adventure instances.
     */
    Collection<AdventureInstance> getActiveInstances();

    /**
     * Get all active instances for a specific adventure.
     */
    Collection<AdventureInstance> getActiveInstances(String adventureId);

    /**
     * Get completed instances for an adventure (for leaderboard).
     */
    Collection<AdventureInstance> getCompletedInstances(String adventureId);

    /**
     * Get leaderboard for an adventure.
     *
     * @param adventureId Adventure ID
     * @param limit Maximum entries to return
     * @return Sorted list of completed instances (best first)
     */
    List<AdventureInstance> getLeaderboard(String adventureId, int limit);

    /**
     * Check if a team has an active adventure.
     */
    boolean hasActiveAdventure(UUID teamId);

    /**
     * Cancel a team's active adventure.
     */
    void cancelAdventure(UUID teamId, String reason);
}
