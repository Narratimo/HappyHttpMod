package no.eira.relay.platform.registry;

import no.eira.relay.Constants;
import no.eira.relay.blockentity.BlockEntityFactory;
import no.eira.relay.platform.DeferredObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BlockEntityRegistry implements IBlockEntityRegistry {

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);

    @Override
    public <T extends BlockEntity> DeferredObject<BlockEntityType<T>> registerBlockEntity(
            ResourceLocation identifier, 
            BlockEntityFactory<T> factory, 
            Supplier<Block> block) {
        
        var registryObject = BLOCK_ENTITIES.register(identifier.getPath(), () -> 
            BlockEntityType.Builder.of(factory::create, block.get()).build(null)
        );
        
        return new DeferredObject<>(() -> (BlockEntityType<T>) registryObject.get());
    }

    @Override
    public void finishRegistry() {
        // Registration happens via event bus, nothing to do here
    }
    
    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}

