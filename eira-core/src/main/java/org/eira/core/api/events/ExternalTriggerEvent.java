package org.eira.core.api.events;

import java.util.Map;

/**
 * Event published when an external trigger is received (e.g., QR code scan, sensor activation).
 */
public record ExternalTriggerEvent(
    String source,
    String triggerId,
    Map<String, Object> data
) implements EiraEvent {}
