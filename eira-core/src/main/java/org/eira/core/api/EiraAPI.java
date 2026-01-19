package org.eira.core.api;

import org.eira.core.EiraCore;
import org.eira.core.api.adventure.AdventureManager;
import org.eira.core.api.events.EiraEventBus;
import org.eira.core.api.player.PlayerManager;
import org.eira.core.api.story.StoryManager;
import org.eira.core.api.team.TeamManager;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Main entry point for the Eira Core API.
 *
 * Other mods can use this to:
 * - Publish and subscribe to events
 * - Manage teams and players
 * - Track story and adventure progress
 * - Communicate with Eira Server
 *
 * Usage:
 * <pre>
 * EiraAPI eira = EiraAPI.get();
 * if (eira != null) {
 *     eira.events().publish(new MyEvent(data));
 *     eira.teams().create("My Team", playerId);
 * }
 * </pre>
 */
public interface EiraAPI {

    /**
     * Get the Eira API instance.
     *
     * @return The API instance, or null if Eira Core is not loaded
     */
    @Nullable
    static EiraAPI get() {
        return EiraCore.getAPI();
    }

    /**
     * Safely execute code if Eira Core is available.
     *
     * @param consumer Code to execute with the API
     */
    static void ifPresent(Consumer<EiraAPI> consumer) {
        EiraAPI api = get();
        if (api != null) {
            consumer.accept(api);
        }
    }

    /**
     * Get the event bus for cross-mod communication.
     *
     * @return The event bus
     */
    EiraEventBus events();

    /**
     * Get the team manager for team operations.
     *
     * @return The team manager
     */
    TeamManager teams();

    /**
     * Get the player manager for player data operations.
     *
     * @return The player manager
     */
    PlayerManager players();

    /**
     * Get the story manager for story state tracking.
     *
     * @return The story manager
     */
    StoryManager stories();

    /**
     * Get the adventure manager for adventure/objective tracking.
     *
     * @return The adventure manager
     */
    AdventureManager adventures();
}
