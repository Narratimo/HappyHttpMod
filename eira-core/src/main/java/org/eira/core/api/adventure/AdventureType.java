package org.eira.core.api.adventure;

/**
 * Types of adventures in the Eira ecosystem.
 */
public enum AdventureType {
    /** Sequential checkpoints that must be completed in order */
    LINEAR,
    /** Checkpoints can be completed in any order */
    OPEN,
    /** Adventure has a time limit */
    TIMED,
    /** Teams compete against each other */
    COMPETITIVE,
    /** Teams work together */
    COOPERATIVE
}
