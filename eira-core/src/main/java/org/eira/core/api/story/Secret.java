package org.eira.core.api.story;

import java.util.List;

/**
 * Definition of a secret that can be revealed during a story.
 */
public record Secret(
    String id,
    String name,
    String revealChapter,
    List<String> hintChapters,
    int maxLevel
) {
    /**
     * Check if hints are available in a given chapter.
     */
    public boolean hasHintInChapter(String chapterId) {
        return hintChapters != null && hintChapters.contains(chapterId);
    }

    /**
     * Check if this secret can be fully revealed in a given chapter.
     */
    public boolean canRevealInChapter(String chapterId) {
        return revealChapter != null && revealChapter.equals(chapterId);
    }
}
