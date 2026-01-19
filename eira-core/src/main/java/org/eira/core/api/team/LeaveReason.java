package org.eira.core.api.team;

/**
 * Reasons why a player might leave a team.
 */
public enum LeaveReason {
    /** Player chose to leave voluntarily */
    LEFT,
    /** Player was removed by the team leader */
    KICKED,
    /** Team was disbanded */
    DISBANDED,
    /** Player disconnected from the server */
    DISCONNECTED
}
