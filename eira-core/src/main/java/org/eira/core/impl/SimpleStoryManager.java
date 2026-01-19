package org.eira.core.impl;

import org.eira.core.api.events.*;
import org.eira.core.api.story.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory implementation of StoryManager.
 */
public class SimpleStoryManager implements StoryManager {

    private final EiraEventBus eventBus;
    private final Map<String, Story> stories = new ConcurrentHashMap<>();
    private final Map<String, Map<UUID, SimpleStoryState>> playerStates = new ConcurrentHashMap<>();
    private final Map<String, Map<UUID, SimpleStoryState>> teamStates = new ConcurrentHashMap<>();

    public SimpleStoryManager(EiraEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void registerStory(Story story) {
        stories.put(story.id(), story);
        playerStates.putIfAbsent(story.id(), new ConcurrentHashMap<>());
        teamStates.putIfAbsent(story.id(), new ConcurrentHashMap<>());
    }

    @Override
    public void unregisterStory(String storyId) {
        stories.remove(storyId);
        playerStates.remove(storyId);
        teamStates.remove(storyId);
    }

    @Override
    public Optional<Story> getStory(String storyId) {
        return Optional.ofNullable(stories.get(storyId));
    }

    @Override
    public Collection<Story> getAllStories() {
        return List.copyOf(stories.values());
    }

    @Override
    public StoryState getState(UUID playerId, String storyId) {
        Map<UUID, SimpleStoryState> states = playerStates.get(storyId);
        if (states == null) {
            states = new ConcurrentHashMap<>();
            playerStates.put(storyId, states);
        }

        return states.computeIfAbsent(playerId, id ->
            new SimpleStoryState(storyId, id, eventBus));
    }

    @Override
    public boolean hasStarted(UUID playerId, String storyId) {
        Map<UUID, SimpleStoryState> states = playerStates.get(storyId);
        return states != null && states.containsKey(playerId);
    }

    @Override
    public StoryState startStory(UUID playerId, String storyId) {
        Story story = stories.get(storyId);
        if (story == null) {
            throw new IllegalArgumentException("Story not found: " + storyId);
        }

        SimpleStoryState state = new SimpleStoryState(storyId, playerId, eventBus);

        // Unlock first chapter
        Chapter firstChapter = story.getFirstChapter();
        if (firstChapter != null) {
            state.unlockChapter(firstChapter.id());
            state.setCurrentChapter(firstChapter.id());
        }

        playerStates.computeIfAbsent(storyId, k -> new ConcurrentHashMap<>())
            .put(playerId, state);

        return state;
    }

    @Override
    public void resetStory(UUID playerId, String storyId) {
        Map<UUID, SimpleStoryState> states = playerStates.get(storyId);
        if (states != null) {
            SimpleStoryState state = states.get(playerId);
            if (state != null) {
                state.reset();
            }
        }
    }

    @Override
    public StoryState getTeamState(UUID teamId, String storyId) {
        Map<UUID, SimpleStoryState> states = teamStates.get(storyId);
        if (states == null) {
            states = new ConcurrentHashMap<>();
            teamStates.put(storyId, states);
        }

        return states.computeIfAbsent(teamId, id ->
            new SimpleStoryState(storyId, id, eventBus));
    }

    @Override
    public StoryState startTeamStory(UUID teamId, String storyId) {
        Story story = stories.get(storyId);
        if (story == null) {
            throw new IllegalArgumentException("Story not found: " + storyId);
        }

        SimpleStoryState state = new SimpleStoryState(storyId, teamId, eventBus);

        // Unlock first chapter
        Chapter firstChapter = story.getFirstChapter();
        if (firstChapter != null) {
            state.unlockChapter(firstChapter.id());
            state.setCurrentChapter(firstChapter.id());
        }

        teamStates.computeIfAbsent(storyId, k -> new ConcurrentHashMap<>())
            .put(teamId, state);

        return state;
    }

    /**
     * Simple implementation of StoryState.
     */
    private static class SimpleStoryState implements StoryState {
        private final String storyId;
        private final UUID playerId;
        private final EiraEventBus eventBus;
        private String currentChapter;
        private final Set<String> unlockedChapters = ConcurrentHashMap.newKeySet();
        private final Set<String> completedRequirements = ConcurrentHashMap.newKeySet();
        private final Map<String, Integer> secretLevels = new ConcurrentHashMap<>();
        private final Map<String, List<String>> conversationHistory = new ConcurrentHashMap<>();

        SimpleStoryState(String storyId, UUID playerId, EiraEventBus eventBus) {
            this.storyId = storyId;
            this.playerId = playerId;
            this.eventBus = eventBus;
        }

        @Override
        public String getStoryId() {
            return storyId;
        }

        @Override
        public UUID getPlayerId() {
            return playerId;
        }

        @Override
        public String getCurrentChapter() {
            return currentChapter;
        }

        @Override
        public void setCurrentChapter(String chapterId) {
            this.currentChapter = chapterId;
        }

        @Override
        public boolean isChapterUnlocked(String chapterId) {
            return unlockedChapters.contains(chapterId);
        }

        @Override
        public Set<String> getUnlockedChapters() {
            return Set.copyOf(unlockedChapters);
        }

        @Override
        public void unlockChapter(String chapterId) {
            if (unlockedChapters.add(chapterId)) {
                eventBus.publish(new ChapterUnlockedEvent(playerId, storyId, chapterId));
            }
        }

        @Override
        public boolean hasRequirement(String requirement) {
            return completedRequirements.contains(requirement);
        }

        @Override
        public void markRequirement(String requirement) {
            completedRequirements.add(requirement);
        }

        @Override
        public Set<String> getCompletedRequirements() {
            return Set.copyOf(completedRequirements);
        }

        @Override
        public int getSecretLevel(String secretId) {
            return secretLevels.getOrDefault(secretId, 0);
        }

        @Override
        public void revealSecret(String secretId, int level) {
            int currentLevel = secretLevels.getOrDefault(secretId, 0);
            if (level > currentLevel) {
                secretLevels.put(secretId, level);
                // maxLevel would come from the Story definition, but we don't have access here
                eventBus.publish(new SecretRevealedEvent(playerId, storyId, secretId, level, level));
            }
        }

        @Override
        public void recordConversation(String npcId, String topic) {
            conversationHistory.computeIfAbsent(npcId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(topic);
        }

        @Override
        public List<String> getConversationHistory(String npcId) {
            List<String> history = conversationHistory.get(npcId);
            return history != null ? List.copyOf(history) : List.of();
        }

        @Override
        public boolean hasDiscussed(String npcId, String topic) {
            List<String> history = conversationHistory.get(npcId);
            return history != null && history.contains(topic);
        }

        @Override
        public void reset() {
            currentChapter = null;
            unlockedChapters.clear();
            completedRequirements.clear();
            secretLevels.clear();
            conversationHistory.clear();
        }
    }
}
