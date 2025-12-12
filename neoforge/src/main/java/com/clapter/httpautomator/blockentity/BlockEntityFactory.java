package com.clapter.httpautomator.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockEntityFactory<T extends BlockEntity> {

    T create(BlockPos pos, BlockState state);

}
