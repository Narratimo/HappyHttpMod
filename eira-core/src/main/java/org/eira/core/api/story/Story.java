package org.eira.core.api.story;

import java.util.List;
import java.util.Map;

/**
 * Definition of a story in the Eira ecosystem.
 */
public record Story(
    String id,
    String name,
    String description,
    List<Chapter> chapters,
    List<Secret> secrets,
    Map<String, Object> metadata
) {
    /**
     * Get the first chapter of the story.
     */
    public Chapter getFirstChapter() {
        return chapters.isEmpty() ? null : chapters.get(0);
    }

    /**
     * Get a chapter by ID.
     */
    public Chapter getChapter(String chapterId) {
        return chapters.stream()
            .filter(c -> c.id().equals(chapterId))
            .findFirst()
            .orElse(null);
    }

    /**
     * Get a secret by ID.
     */
    public Secret getSecret(String secretId) {
        return secrets.stream()
            .filter(s -> s.id().equals(secretId))
            .findFirst()
            .orElse(null);
    }
}
