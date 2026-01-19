package org.eira.core.api.team;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface for managing teams in the Eira ecosystem.
 */
public interface TeamManager {

    /**
     * Create a new team.
     *
     * @param name Team name
     * @param leaderId UUID of the team leader
     * @return The created team
     */
    Team create(String name, UUID leaderId);

    /**
     * Create a new team with custom settings.
     *
     * @param name Team name
     * @param leaderId UUID of the team leader
     * @param maxSize Maximum team size
     * @param color Team color (hex or name)
     * @return The created team
     */
    Team create(String name, UUID leaderId, int maxSize, String color);

    /**
     * Get a team by its ID.
     *
     * @param teamId Team UUID
     * @return The team if found
     */
    Optional<Team> getById(UUID teamId);

    /**
     * Get the team a player belongs to.
     *
     * @param playerId Player UUID
     * @return The player's team if they are in one
     */
    Optional<Team> getTeamOf(UUID playerId);

    /**
     * Get all teams.
     *
     * @return Collection of all teams
     */
    Collection<Team> getAllTeams();

    /**
     * Add a player to a team.
     *
     * @param teamId Team UUID
     * @param playerId Player UUID
     * @return true if successful, false if team is full or player already in a team
     */
    boolean addMember(UUID teamId, UUID playerId);

    /**
     * Remove a player from a team.
     *
     * @param teamId Team UUID
     * @param playerId Player UUID
     * @param reason Reason for leaving
     * @return true if successful
     */
    boolean removeMember(UUID teamId, UUID playerId, LeaveReason reason);

    /**
     * Transfer team leadership to another member.
     *
     * @param teamId Team UUID
     * @param newLeaderId New leader's UUID
     * @return true if successful
     */
    boolean setLeader(UUID teamId, UUID newLeaderId);

    /**
     * Disband a team, removing all members.
     *
     * @param teamId Team UUID
     * @return true if successful
     */
    boolean disband(UUID teamId);

    /**
     * Get custom data for a team.
     *
     * @param teamId Team UUID
     * @param key Data key
     * @return The value if present
     */
    Optional<Object> getData(UUID teamId, String key);

    /**
     * Set custom data for a team.
     *
     * @param teamId Team UUID
     * @param key Data key
     * @param value Data value
     */
    void setData(UUID teamId, String key, Object value);

    /**
     * Increment a numeric counter for a team.
     *
     * @param teamId Team UUID
     * @param key Counter key
     * @param amount Amount to add (can be negative)
     * @return The new value
     */
    int incrementData(UUID teamId, String key, int amount);
}
