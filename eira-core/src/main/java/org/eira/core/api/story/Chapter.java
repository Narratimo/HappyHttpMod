package org.eira.core.api.story;

import java.util.List;

/**
 * Definition of a story chapter.
 */
public record Chapter(
    String id,
    String name,
    String description,
    List<String> npcIds,
    String unlocksAfter,
    List<String> requirements
) {
    /**
     * Check if this is the first chapter (no prerequisites).
     */
    public boolean isFirstChapter() {
        return unlocksAfter == null || unlocksAfter.isEmpty();
    }

    /**
     * Check if an NPC is part of this chapter.
     */
    public boolean hasNpc(String npcId) {
        return npcIds != null && npcIds.contains(npcId);
    }
}
