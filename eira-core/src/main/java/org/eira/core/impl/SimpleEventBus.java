package org.eira.core.impl;

import org.eira.core.EiraCore;
import org.eira.core.api.events.EiraEvent;
import org.eira.core.api.events.EiraEventBus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Thread-safe implementation of the Eira event bus.
 *
 * Uses ConcurrentHashMap and CopyOnWriteArrayList to ensure thread safety
 * when publishing and subscribing to events from different threads.
 */
public class SimpleEventBus implements EiraEventBus {

    private final Map<Class<?>, List<Consumer<?>>> subscribers = new ConcurrentHashMap<>();

    @Override
    public <T extends EiraEvent> void subscribe(Class<T> eventType, Consumer<T> handler) {
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
            .add(handler);

        EiraCore.LOG.debug("Subscribed to event: {}", eventType.getSimpleName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends EiraEvent> void unsubscribe(Class<T> eventType, Consumer<T> handler) {
        List<Consumer<?>> handlers = subscribers.get(eventType);
        if (handlers != null) {
            handlers.remove(handler);
            if (handlers.isEmpty()) {
                subscribers.remove(eventType);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void publish(EiraEvent event) {
        List<Consumer<?>> handlers = subscribers.get(event.getClass());
        if (handlers == null || handlers.isEmpty()) {
            EiraCore.LOG.debug("No subscribers for event: {}", event.getClass().getSimpleName());
            return;
        }

        EiraCore.LOG.debug("Publishing event {} to {} subscriber(s)",
            event.getClass().getSimpleName(), handlers.size());

        for (Consumer<?> handler : handlers) {
            try {
                ((Consumer<EiraEvent>) handler).accept(event);
            } catch (Exception e) {
                // Log error but don't propagate - other handlers should still run
                EiraCore.LOG.error("Error in event handler for {}: {}",
                    event.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean hasSubscribers(Class<? extends EiraEvent> eventType) {
        List<Consumer<?>> handlers = subscribers.get(eventType);
        return handlers != null && !handlers.isEmpty();
    }
}
