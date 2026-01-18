package no.eira.relay.platform.registry;

import no.eira.relay.blockentity.BlockEntityFactory;
import no.eira.relay.platform.DeferredObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public interface IBlockEntityRegistry {

    <T extends BlockEntity> DeferredObject<BlockEntityType<T>> registerBlockEntity(ResourceLocation identifier, BlockEntityFactory<T> factory, Supplier<Block> block);
    void finishRegistry();
}
