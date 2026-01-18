package no.eira.relay.registry;

import no.eira.relay.Constants;
import no.eira.relay.blockentity.HttpReceiverBlockEntity;
import no.eira.relay.platform.DeferredObject;
import no.eira.relay.platform.Services;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {

    //public static BlockEntityType<HttpReceiverBlockEntity> httpReceiverBlockEntity;
    public static DeferredObject<BlockEntityType<HttpReceiverBlockEntity>> httpReceiverBlockEntity;


    public static void registerBlockEntities(){
        httpReceiverBlockEntity = Services.BLOCK_ENTITIES_REGISTRY.registerBlockEntity(id("receiver"), HttpReceiverBlockEntity::new, () -> ModBlocks.httpReceiverBlock);
        Services.BLOCK_ENTITIES_REGISTRY.finishRegistry();
    }

    private static ResourceLocation id(String name) {
        return new ResourceLocation(Constants.MOD_ID, name);
    }

}
