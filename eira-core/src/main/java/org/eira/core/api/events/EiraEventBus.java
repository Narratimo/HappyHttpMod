package org.eira.core.api.events;

import java.util.function.Consumer;

/**
 * Event bus for cross-mod communication without direct dependencies.
 *
 * Any mod can publish events, and any mod can subscribe to events.
 * This allows loose coupling between Eira ecosystem mods.
 *
 * Usage:
 * <pre>
 * // Subscribe to events
 * EiraAPI.get().events().subscribe(MyEvent.class, event -> {
 *     // Handle event
 * });
 *
 * // Publish events
 * EiraAPI.get().events().publish(new MyEvent(data));
 * </pre>
 */
public interface EiraEventBus {

    /**
     * Subscribe to events of a specific type.
     *
     * @param eventType The class of events to subscribe to
     * @param handler   The handler to call when events are published
     * @param <T>       The event type
     */
    <T extends EiraEvent> void subscribe(Class<T> eventType, Consumer<T> handler);

    /**
     * Unsubscribe a handler from events.
     *
     * @param eventType The class of events
     * @param handler   The handler to remove
     * @param <T>       The event type
     */
    <T extends EiraEvent> void unsubscribe(Class<T> eventType, Consumer<T> handler);

    /**
     * Publish an event to all subscribers.
     *
     * @param event The event to publish
     */
    void publish(EiraEvent event);

    /**
     * Check if there are any subscribers for an event type.
     *
     * @param eventType The event type to check
     * @return true if there are subscribers
     */
    boolean hasSubscribers(Class<? extends EiraEvent> eventType);
}
