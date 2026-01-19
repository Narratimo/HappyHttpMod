package org.eira.core.impl;

import org.eira.core.api.events.*;
import org.eira.core.api.team.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory implementation of TeamManager.
 */
public class SimpleTeamManager implements TeamManager {

    private final EiraEventBus eventBus;
    private final Map<UUID, TeamImpl> teams = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> playerToTeam = new ConcurrentHashMap<>();

    public SimpleTeamManager(EiraEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public Team create(String name, UUID leaderId) {
        return create(name, leaderId, 8, "#FFFFFF");
    }

    @Override
    public Team create(String name, UUID leaderId, int maxSize, String color) {
        // Check if leader is already in a team
        if (playerToTeam.containsKey(leaderId)) {
            throw new IllegalStateException("Leader is already in a team");
        }

        UUID teamId = UUID.randomUUID();
        Set<UUID> members = ConcurrentHashMap.newKeySet();
        members.add(leaderId);

        TeamImpl team = new TeamImpl(teamId, name, leaderId, members, maxSize, color, System.currentTimeMillis());
        teams.put(teamId, team);
        playerToTeam.put(leaderId, teamId);

        Team immutableTeam = team.toImmutable();
        eventBus.publish(new TeamCreatedEvent(immutableTeam, leaderId));

        return immutableTeam;
    }

    @Override
    public Optional<Team> getById(UUID teamId) {
        TeamImpl team = teams.get(teamId);
        return team != null ? Optional.of(team.toImmutable()) : Optional.empty();
    }

    @Override
    public Optional<Team> getTeamOf(UUID playerId) {
        UUID teamId = playerToTeam.get(playerId);
        if (teamId == null) return Optional.empty();
        return getById(teamId);
    }

    @Override
    public Collection<Team> getAllTeams() {
        return teams.values().stream()
            .map(TeamImpl::toImmutable)
            .toList();
    }

    @Override
    public boolean addMember(UUID teamId, UUID playerId) {
        // Check if player is already in a team
        if (playerToTeam.containsKey(playerId)) {
            return false;
        }

        TeamImpl team = teams.get(teamId);
        if (team == null || team.isFull()) {
            return false;
        }

        synchronized (team) {
            if (team.isFull()) return false;
            team.memberIds.add(playerId);
            playerToTeam.put(playerId, teamId);
        }

        eventBus.publish(new TeamMemberJoinedEvent(teamId, playerId));
        return true;
    }

    @Override
    public boolean removeMember(UUID teamId, UUID playerId, LeaveReason reason) {
        TeamImpl team = teams.get(teamId);
        if (team == null) return false;

        synchronized (team) {
            if (!team.memberIds.contains(playerId)) return false;

            // Don't remove the leader - must transfer or disband
            if (team.leaderId.equals(playerId)) {
                return false;
            }

            team.memberIds.remove(playerId);
            playerToTeam.remove(playerId);
        }

        eventBus.publish(new TeamMemberLeftEvent(teamId, playerId, reason));
        return true;
    }

    @Override
    public boolean setLeader(UUID teamId, UUID newLeaderId) {
        TeamImpl team = teams.get(teamId);
        if (team == null) return false;

        synchronized (team) {
            if (!team.memberIds.contains(newLeaderId)) return false;
            team.leaderId = newLeaderId;
        }

        return true;
    }

    @Override
    public boolean disband(UUID teamId) {
        TeamImpl team = teams.remove(teamId);
        if (team == null) return false;

        String teamName = team.name;

        // Remove all members
        for (UUID memberId : team.memberIds) {
            playerToTeam.remove(memberId);
            if (!memberId.equals(team.leaderId)) {
                eventBus.publish(new TeamMemberLeftEvent(teamId, memberId, LeaveReason.DISBANDED));
            }
        }

        eventBus.publish(new TeamDisbandedEvent(teamId, teamName));
        return true;
    }

    @Override
    public Optional<Object> getData(UUID teamId, String key) {
        TeamImpl team = teams.get(teamId);
        if (team == null) return Optional.empty();
        return Optional.ofNullable(team.data.get(key));
    }

    @Override
    public void setData(UUID teamId, String key, Object value) {
        TeamImpl team = teams.get(teamId);
        if (team != null) {
            team.data.put(key, value);
        }
    }

    @Override
    public int incrementData(UUID teamId, String key, int amount) {
        TeamImpl team = teams.get(teamId);
        if (team == null) return 0;

        synchronized (team) {
            Object current = team.data.get(key);
            int value = (current instanceof Number) ? ((Number) current).intValue() : 0;
            int newValue = value + amount;
            team.data.put(key, newValue);
            return newValue;
        }
    }

    /**
     * Mutable internal team implementation.
     */
    private static class TeamImpl {
        final UUID id;
        final String name;
        UUID leaderId;
        final Set<UUID> memberIds;
        final int maxSize;
        final String color;
        final long createdAt;
        final Map<String, Object> data = new ConcurrentHashMap<>();

        TeamImpl(UUID id, String name, UUID leaderId, Set<UUID> memberIds,
                 int maxSize, String color, long createdAt) {
            this.id = id;
            this.name = name;
            this.leaderId = leaderId;
            this.memberIds = memberIds;
            this.maxSize = maxSize;
            this.color = color;
            this.createdAt = createdAt;
        }

        boolean isFull() {
            return memberIds.size() >= maxSize;
        }

        Team toImmutable() {
            return new Team(id, name, leaderId, Set.copyOf(memberIds),
                maxSize, color, createdAt);
        }
    }
}
