package org.eira.core.api.events;

import java.util.Map;

/**
 * Event published when a command is received from the Eira Server.
 * Eira Relay listens to this to execute redstone/webhook commands.
 */
public record ServerCommandEvent(
    String command,
    Map<String, Object> params
) implements EiraEvent {}
