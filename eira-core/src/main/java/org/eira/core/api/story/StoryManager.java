package org.eira.core.api.story;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface for managing stories in the Eira ecosystem.
 */
public interface StoryManager {

    /**
     * Register a story definition.
     */
    void registerStory(Story story);

    /**
     * Unregister a story definition.
     */
    void unregisterStory(String storyId);

    /**
     * Get a story by ID.
     */
    Optional<Story> getStory(String storyId);

    /**
     * Get all registered stories.
     */
    Collection<Story> getAllStories();

    /**
     * Get a player's state for a story.
     * Creates a new state if the player hasn't started the story.
     */
    StoryState getState(UUID playerId, String storyId);

    /**
     * Check if a player has started a story.
     */
    boolean hasStarted(UUID playerId, String storyId);

    /**
     * Start a player on a story.
     * Unlocks the first chapter.
     */
    StoryState startStory(UUID playerId, String storyId);

    /**
     * Reset a player's progress on a story.
     */
    void resetStory(UUID playerId, String storyId);

    /**
     * Get a team's shared state for a story.
     * Team state is separate from individual player state.
     */
    StoryState getTeamState(UUID teamId, String storyId);

    /**
     * Start a team on a story with shared progress.
     */
    StoryState startTeamStory(UUID teamId, String storyId);
}
