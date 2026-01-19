package org.eira.core.impl;

import org.eira.core.api.player.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory implementation of PlayerManager.
 */
public class SimplePlayerManager implements PlayerManager {

    private final Map<UUID, PlayerImpl> players = new ConcurrentHashMap<>();
    private final Map<String, UUID> nameToUuid = new ConcurrentHashMap<>();
    private final Map<String, UUID> eiraIdToUuid = new ConcurrentHashMap<>();
    private final Set<UUID> onlinePlayers = ConcurrentHashMap.newKeySet();

    @Override
    public EiraPlayer get(UUID minecraftUuid) {
        PlayerImpl player = players.computeIfAbsent(minecraftUuid, id -> {
            long now = System.currentTimeMillis();
            return new PlayerImpl(id, "Unknown", null, null, now, now);
        });
        return player.toImmutable();
    }

    @Override
    public Optional<EiraPlayer> getByName(String name) {
        UUID uuid = nameToUuid.get(name.toLowerCase());
        if (uuid == null) return Optional.empty();
        PlayerImpl player = players.get(uuid);
        return player != null ? Optional.of(player.toImmutable()) : Optional.empty();
    }

    @Override
    public Optional<EiraPlayer> getByEiraId(String eiraPlayerId) {
        UUID uuid = eiraIdToUuid.get(eiraPlayerId);
        if (uuid == null) return Optional.empty();
        PlayerImpl player = players.get(uuid);
        return player != null ? Optional.of(player.toImmutable()) : Optional.empty();
    }

    @Override
    public void linkEiraId(UUID minecraftUuid, String eiraPlayerId) {
        PlayerImpl player = players.get(minecraftUuid);
        if (player == null) return;

        // Remove old mapping if exists
        if (player.eiraPlayerId != null) {
            eiraIdToUuid.remove(player.eiraPlayerId);
        }

        player.eiraPlayerId = eiraPlayerId;
        eiraIdToUuid.put(eiraPlayerId, minecraftUuid);
    }

    @Override
    public void unlinkEiraId(UUID minecraftUuid) {
        PlayerImpl player = players.get(minecraftUuid);
        if (player == null || player.eiraPlayerId == null) return;

        eiraIdToUuid.remove(player.eiraPlayerId);
        player.eiraPlayerId = null;
    }

    @Override
    public Collection<EiraPlayer> getAllPlayers() {
        return players.values().stream()
            .map(PlayerImpl::toImmutable)
            .toList();
    }

    @Override
    public Collection<EiraPlayer> getOnlinePlayers() {
        return onlinePlayers.stream()
            .map(players::get)
            .filter(Objects::nonNull)
            .map(PlayerImpl::toImmutable)
            .toList();
    }

    @Override
    public boolean isOnline(UUID minecraftUuid) {
        return onlinePlayers.contains(minecraftUuid);
    }

    @Override
    public void setOnline(UUID minecraftUuid, String name) {
        PlayerImpl player = players.computeIfAbsent(minecraftUuid, id -> {
            long now = System.currentTimeMillis();
            return new PlayerImpl(id, name, null, null, now, now);
        });

        // Update name mapping
        if (player.name != null) {
            nameToUuid.remove(player.name.toLowerCase());
        }
        player.name = name;
        player.lastSeen = System.currentTimeMillis();
        nameToUuid.put(name.toLowerCase(), minecraftUuid);

        onlinePlayers.add(minecraftUuid);
    }

    @Override
    public void setOffline(UUID minecraftUuid) {
        PlayerImpl player = players.get(minecraftUuid);
        if (player != null) {
            player.lastSeen = System.currentTimeMillis();
        }
        onlinePlayers.remove(minecraftUuid);
    }

    @Override
    public PlayerProgress getProgress(UUID minecraftUuid) {
        PlayerImpl player = players.computeIfAbsent(minecraftUuid, id -> {
            long now = System.currentTimeMillis();
            return new PlayerImpl(id, "Unknown", null, null, now, now);
        });
        return player.progress;
    }

    /**
     * Update team assignment for a player.
     */
    public void setTeam(UUID minecraftUuid, UUID teamId) {
        PlayerImpl player = players.get(minecraftUuid);
        if (player != null) {
            player.teamId = teamId;
        }
    }

    /**
     * Mutable internal player implementation.
     */
    private static class PlayerImpl {
        final UUID minecraftUuid;
        String name;
        String eiraPlayerId;
        UUID teamId;
        final long firstSeen;
        long lastSeen;
        final SimplePlayerProgress progress = new SimplePlayerProgress();

        PlayerImpl(UUID minecraftUuid, String name, String eiraPlayerId,
                   UUID teamId, long firstSeen, long lastSeen) {
            this.minecraftUuid = minecraftUuid;
            this.name = name;
            this.eiraPlayerId = eiraPlayerId;
            this.teamId = teamId;
            this.firstSeen = firstSeen;
            this.lastSeen = lastSeen;
        }

        EiraPlayer toImmutable() {
            return new EiraPlayer(
                minecraftUuid,
                name,
                Optional.ofNullable(eiraPlayerId),
                Optional.ofNullable(teamId),
                firstSeen,
                lastSeen
            );
        }
    }

    /**
     * Simple implementation of PlayerProgress.
     */
    private static class SimplePlayerProgress implements PlayerProgress {
        private int score = 0;
        private final Set<String> flags = ConcurrentHashMap.newKeySet();
        private final Map<String, Integer> counters = new ConcurrentHashMap<>();

        @Override
        public int getScore() {
            return score;
        }

        @Override
        public synchronized void addScore(int points) {
            score += points;
        }

        @Override
        public boolean hasFlag(String flag) {
            return flags.contains(flag);
        }

        @Override
        public void setFlag(String flag, boolean value) {
            if (value) {
                flags.add(flag);
            } else {
                flags.remove(flag);
            }
        }

        @Override
        public Set<String> getFlags() {
            return Set.copyOf(flags);
        }

        @Override
        public int getCounter(String key) {
            return counters.getOrDefault(key, 0);
        }

        @Override
        public void setCounter(String key, int value) {
            counters.put(key, value);
        }

        @Override
        public synchronized int incrementCounter(String key, int amount) {
            int newValue = counters.getOrDefault(key, 0) + amount;
            counters.put(key, newValue);
            return newValue;
        }

        @Override
        public Map<String, Integer> getCounters() {
            return Map.copyOf(counters);
        }

        @Override
        public void reset() {
            score = 0;
            flags.clear();
            counters.clear();
        }
    }
}
