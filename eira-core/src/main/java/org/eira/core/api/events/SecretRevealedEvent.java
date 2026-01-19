package org.eira.core.api.events;

import java.util.UUID;

/**
 * Event published when a secret is revealed.
 */
public record SecretRevealedEvent(
    UUID playerId,
    String storyId,
    String secretId,
    int level,
    int maxLevel
) implements EiraEvent {
    public static final String TYPE = "SECRET_REVEALED";
}
