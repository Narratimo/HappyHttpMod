package com.clapter.httpautomator.platform.registry;

import com.clapter.httpautomator.blockentity.BlockEntityFactory;
import com.clapter.httpautomator.platform.DeferredObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public interface IBlockEntityRegistry {

    <T extends BlockEntity> DeferredObject<BlockEntityType<T>> registerBlockEntity(ResourceLocation identifier, BlockEntityFactory<T> factory, Supplier<Block> block);
    void finishRegistry();
}
