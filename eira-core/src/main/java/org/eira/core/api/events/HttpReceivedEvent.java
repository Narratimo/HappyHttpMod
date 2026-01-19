package org.eira.core.api.events;

import java.util.Map;

/**
 * Event published when an HTTP request is received by Eira Relay.
 */
public record HttpReceivedEvent(
    String endpoint,
    String method,
    Map<String, Object> params
) implements EiraEvent {
    /** Event type identifier for cross-mod compatibility */
    public static final String TYPE = "HTTP_RECEIVED";
}
