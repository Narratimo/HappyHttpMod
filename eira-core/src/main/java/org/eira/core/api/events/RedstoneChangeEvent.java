package org.eira.core.api.events;

import net.minecraft.core.BlockPos;

/**
 * Event published when a redstone detector block sees a change.
 */
public record RedstoneChangeEvent(
    BlockPos pos,
    int oldStrength,
    int newStrength
) implements EiraEvent {}
