package no.eira.relay.platform.registry;

import no.eira.relay.Constants;
import no.eira.relay.blockentity.BlockEntityFactory;
import no.eira.relay.platform.DeferredObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class BlockEntityRegistry implements IBlockEntityRegistry {

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Constants.MOD_ID);

    @Override
    public <T extends BlockEntity> DeferredObject<BlockEntityType<T>> registerBlockEntity(ResourceLocation identifier, BlockEntityFactory<T> factory, Supplier<Block> block) {
        RegistryObject<BlockEntityType<T>> registryObject = BLOCK_ENTITY_TYPES.register(identifier.getPath(), () -> {
            return BlockEntityType.Builder.of(factory::create, block.get()).build(null);
        });
        return new DeferredObject<>(registryObject);
    }

    public void finishRegistry(){
        BLOCK_ENTITY_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
