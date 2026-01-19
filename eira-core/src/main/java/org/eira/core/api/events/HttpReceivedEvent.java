package org.eira.core.api.events;

import java.util.Map;

/**
 * Event published when an HTTP request is received by Eira Relay.
 */
public record HttpReceivedEvent(
    String endpoint,
    String method,
    Map<String, Object> params
) implements EiraEvent {}
