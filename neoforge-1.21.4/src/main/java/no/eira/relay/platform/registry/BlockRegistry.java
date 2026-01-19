package no.eira.relay.platform.registry;

import no.eira.relay.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BlockRegistry implements IBlockRegistry {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, Constants.MOD_ID);

    @Override
    public void registerBlock(ResourceLocation identifier, Supplier<Block> block) {
        BLOCKS.register(identifier.getPath(), block);
    }

    @Override
    public void finishRegistry() {
        // Registration happens via event bus, nothing to do here
    }
    
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}

