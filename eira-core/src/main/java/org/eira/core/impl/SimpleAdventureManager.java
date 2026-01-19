package org.eira.core.impl;

import org.eira.core.api.adventure.*;
import org.eira.core.api.events.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Simple in-memory implementation of AdventureManager.
 */
public class SimpleAdventureManager implements AdventureManager {

    private final EiraEventBus eventBus;
    private final Map<String, Adventure> adventures = new ConcurrentHashMap<>();
    private final Map<UUID, SimpleAdventureInstance> instances = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> teamToInstance = new ConcurrentHashMap<>();
    private final Map<String, List<SimpleAdventureInstance>> completedInstances = new ConcurrentHashMap<>();

    public SimpleAdventureManager(EiraEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void registerAdventure(Adventure adventure) {
        adventures.put(adventure.id(), adventure);
        completedInstances.putIfAbsent(adventure.id(), Collections.synchronizedList(new ArrayList<>()));
    }

    @Override
    public void unregisterAdventure(String adventureId) {
        adventures.remove(adventureId);
    }

    @Override
    public Optional<Adventure> getAdventure(String adventureId) {
        return Optional.ofNullable(adventures.get(adventureId));
    }

    @Override
    public Collection<Adventure> getAllAdventures() {
        return List.copyOf(adventures.values());
    }

    @Override
    public AdventureInstance start(String adventureId, UUID teamId) {
        Adventure adventure = adventures.get(adventureId);
        if (adventure == null) {
            throw new IllegalArgumentException("Adventure not found: " + adventureId);
        }

        // Check if team already has active adventure
        if (teamToInstance.containsKey(teamId)) {
            throw new IllegalStateException("Team already has an active adventure");
        }

        UUID instanceId = UUID.randomUUID();
        SimpleAdventureInstance instance = new SimpleAdventureInstance(
            instanceId, adventureId, teamId, adventure, eventBus
        );

        instances.put(instanceId, instance);
        teamToInstance.put(teamId, instanceId);

        // Unlock initial checkpoints (those without prerequisites)
        for (Checkpoint checkpoint : adventure.checkpoints()) {
            if (!checkpoint.hasPrerequisites()) {
                instance.unlockCheckpoint(checkpoint.id());
            }
        }

        eventBus.publish(new AdventureStartedEvent(instanceId, adventureId, teamId));

        return instance;
    }

    @Override
    public Optional<AdventureInstance> getInstance(UUID instanceId) {
        return Optional.ofNullable(instances.get(instanceId));
    }

    @Override
    public Optional<AdventureInstance> getActiveInstance(UUID teamId) {
        UUID instanceId = teamToInstance.get(teamId);
        if (instanceId == null) return Optional.empty();
        SimpleAdventureInstance instance = instances.get(instanceId);
        return instance != null && instance.isActive() ? Optional.of(instance) : Optional.empty();
    }

    @Override
    public Collection<AdventureInstance> getActiveInstances() {
        return instances.values().stream()
            .filter(AdventureInstance::isActive)
            .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Collection<AdventureInstance> getActiveInstances(String adventureId) {
        return instances.values().stream()
            .filter(i -> i.getAdventureId().equals(adventureId) && i.isActive())
            .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Collection<AdventureInstance> getCompletedInstances(String adventureId) {
        List<SimpleAdventureInstance> completed = completedInstances.get(adventureId);
        return completed != null ? List.copyOf(completed) : List.of();
    }

    @Override
    public List<AdventureInstance> getLeaderboard(String adventureId, int limit) {
        List<SimpleAdventureInstance> completed = completedInstances.get(adventureId);
        if (completed == null) return List.of();

        return completed.stream()
            .sorted((a, b) -> {
                // Sort by score descending, then by time ascending
                int scoreCompare = Integer.compare(b.getScore(), a.getScore());
                if (scoreCompare != 0) return scoreCompare;
                return Long.compare(a.getElapsedTime().toMillis(), b.getElapsedTime().toMillis());
            })
            .limit(limit)
            .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public boolean hasActiveAdventure(UUID teamId) {
        UUID instanceId = teamToInstance.get(teamId);
        if (instanceId == null) return false;
        SimpleAdventureInstance instance = instances.get(instanceId);
        return instance != null && instance.isActive();
    }

    @Override
    public void cancelAdventure(UUID teamId, String reason) {
        UUID instanceId = teamToInstance.get(teamId);
        if (instanceId == null) return;

        SimpleAdventureInstance instance = instances.get(instanceId);
        if (instance != null && instance.isActive()) {
            instance.fail(reason);
            teamToInstance.remove(teamId);
        }
    }

    /**
     * Called when an instance is completed or failed.
     */
    void onInstanceEnded(SimpleAdventureInstance instance) {
        teamToInstance.remove(instance.getTeamId());
        if (instance.isCompleted()) {
            completedInstances.computeIfAbsent(instance.getAdventureId(),
                    k -> Collections.synchronizedList(new ArrayList<>()))
                .add(instance);
        }
    }

    /**
     * Simple implementation of AdventureInstance.
     */
    private class SimpleAdventureInstance implements AdventureInstance {
        private final UUID instanceId;
        private final String adventureId;
        private final UUID teamId;
        private final Adventure adventure;
        private final EiraEventBus eventBus;
        private final long startTime;
        private final List<String> completedCheckpoints = Collections.synchronizedList(new ArrayList<>());
        private final Set<String> unlockedCheckpoints = ConcurrentHashMap.newKeySet();
        private String currentCheckpoint;
        private int score = 0;
        private boolean active = true;
        private boolean completed = false;
        private boolean failed = false;
        private String failureReason;
        private final Map<String, Object> results = new ConcurrentHashMap<>();

        SimpleAdventureInstance(UUID instanceId, String adventureId, UUID teamId,
                                Adventure adventure, EiraEventBus eventBus) {
            this.instanceId = instanceId;
            this.adventureId = adventureId;
            this.teamId = teamId;
            this.adventure = adventure;
            this.eventBus = eventBus;
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public String getAdventureId() {
            return adventureId;
        }

        @Override
        public UUID getInstanceId() {
            return instanceId;
        }

        @Override
        public UUID getTeamId() {
            return teamId;
        }

        @Override
        public float getProgress() {
            int total = adventure.checkpointCount();
            if (total == 0) return 1.0f;
            return (float) completedCheckpoints.size() / total;
        }

        @Override
        public List<String> getCompletedCheckpointIds() {
            return List.copyOf(completedCheckpoints);
        }

        @Override
        public Set<String> getUnlockedCheckpointIds() {
            return Set.copyOf(unlockedCheckpoints);
        }

        @Override
        public Optional<String> getCurrentCheckpointId() {
            return Optional.ofNullable(currentCheckpoint);
        }

        @Override
        public boolean isCheckpointCompleted(String checkpointId) {
            return completedCheckpoints.contains(checkpointId);
        }

        @Override
        public boolean isCheckpointUnlocked(String checkpointId) {
            return unlockedCheckpoints.contains(checkpointId);
        }

        @Override
        public void completeCheckpoint(String checkpointId) {
            if (!active || completedCheckpoints.contains(checkpointId)) return;

            completedCheckpoints.add(checkpointId);
            eventBus.publish(new CheckpointCompletedEvent(
                adventureId, checkpointId, null, teamId));

            // Unlock dependent checkpoints
            for (Checkpoint checkpoint : adventure.checkpoints()) {
                if (checkpoint.requiresCheckpoint(checkpointId) &&
                    !unlockedCheckpoints.contains(checkpoint.id())) {
                    // Check if all prerequisites are met
                    boolean allMet = checkpoint.prerequisites().stream()
                        .allMatch(completedCheckpoints::contains);
                    if (allMet) {
                        unlockCheckpoint(checkpoint.id());
                    }
                }
            }

            // Check if adventure is complete
            if (completedCheckpoints.size() >= adventure.checkpointCount()) {
                complete();
            }
        }

        @Override
        public void unlockCheckpoint(String checkpointId) {
            if (unlockedCheckpoints.add(checkpointId)) {
                if (currentCheckpoint == null) {
                    currentCheckpoint = checkpointId;
                }
                eventBus.publish(new CheckpointUnlockedEvent(instanceId, adventureId, checkpointId));
            }
        }

        @Override
        public long getStartTime() {
            return startTime;
        }

        @Override
        public Duration getElapsedTime() {
            return Duration.ofMillis(System.currentTimeMillis() - startTime);
        }

        @Override
        public Optional<Duration> getRemainingTime() {
            if (!adventure.isTimed()) return Optional.empty();
            long remaining = (adventure.timeLimitSeconds() * 1000L) -
                (System.currentTimeMillis() - startTime);
            return Optional.of(Duration.ofMillis(Math.max(0, remaining)));
        }

        @Override
        public boolean isTimedOut() {
            if (!adventure.isTimed()) return false;
            return getElapsedTime().toSeconds() >= adventure.timeLimitSeconds();
        }

        @Override
        public boolean isActive() {
            return active;
        }

        @Override
        public boolean isCompleted() {
            return completed;
        }

        @Override
        public boolean isFailed() {
            return failed;
        }

        @Override
        public void complete() {
            if (!active) return;
            active = false;
            completed = true;

            long durationSeconds = getElapsedTime().toSeconds();
            results.put("score", score);
            results.put("durationSeconds", durationSeconds);
            results.put("checkpointsCompleted", completedCheckpoints.size());

            eventBus.publish(new AdventureCompletedEvent(
                instanceId, adventureId, teamId, score, durationSeconds));

            onInstanceEnded(this);
        }

        @Override
        public void fail(String reason) {
            if (!active) return;
            active = false;
            failed = true;
            failureReason = reason;

            eventBus.publish(new AdventureFailedEvent(instanceId, adventureId, teamId, reason));

            onInstanceEnded(this);
        }

        @Override
        public void abandon() {
            fail("Abandoned");
        }

        @Override
        public int getScore() {
            return score;
        }

        @Override
        public void addScore(int points) {
            score += points;
        }

        @Override
        public Map<String, Object> getResults() {
            return Map.copyOf(results);
        }

        @Override
        public Optional<String> getFailureReason() {
            return Optional.ofNullable(failureReason);
        }
    }
}
