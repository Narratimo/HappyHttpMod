package org.eira.core.api;

import org.eira.core.EiraCore;
import org.eira.core.api.events.EiraEventBus;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Main entry point for the Eira Core API.
 *
 * Other mods can use this to:
 * - Publish and subscribe to events
 * - Access team and player APIs (future)
 * - Communicate with Eira Server (future)
 *
 * Usage:
 * <pre>
 * EiraAPI eira = EiraAPI.get();
 * if (eira != null) {
 *     eira.events().publish(new MyEvent(data));
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
}
