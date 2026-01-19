package org.eira.core.api.events;

import java.util.UUID;

/**
 * Event published when a story chapter is unlocked.
 */
public record ChapterUnlockedEvent(
    UUID playerId,
    String storyId,
    String chapterId
) implements EiraEvent {
    public static final String TYPE = "CHAPTER_UNLOCKED";
}
