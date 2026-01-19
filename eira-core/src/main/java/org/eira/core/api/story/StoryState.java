package org.eira.core.api.story;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Interface for tracking a player's progress through a story.
 */
public interface StoryState {

    /**
     * Get the story ID this state is for.
     */
    String getStoryId();

    /**
     * Get the player UUID this state belongs to.
     */
    UUID getPlayerId();

    /**
     * Get the current chapter ID.
     */
    String getCurrentChapter();

    /**
     * Set the current chapter.
     */
    void setCurrentChapter(String chapterId);

    /**
     * Check if a chapter is unlocked.
     */
    boolean isChapterUnlocked(String chapterId);

    /**
     * Get all unlocked chapters.
     */
    Set<String> getUnlockedChapters();

    /**
     * Unlock a chapter.
     */
    void unlockChapter(String chapterId);

    /**
     * Check if a requirement has been completed.
     */
    boolean hasRequirement(String requirement);

    /**
     * Mark a requirement as completed.
     */
    void markRequirement(String requirement);

    /**
     * Get all completed requirements.
     */
    Set<String> getCompletedRequirements();

    /**
     * Get the revealed level of a secret (0 = not revealed).
     */
    int getSecretLevel(String secretId);

    /**
     * Reveal a secret at a certain level.
     */
    void revealSecret(String secretId, int level);

    /**
     * Record a conversation with an NPC.
     */
    void recordConversation(String npcId, String topic);

    /**
     * Get conversation history with an NPC.
     */
    List<String> getConversationHistory(String npcId);

    /**
     * Check if a conversation topic has been discussed.
     */
    boolean hasDiscussed(String npcId, String topic);

    /**
     * Reset all story progress.
     */
    void reset();
}
