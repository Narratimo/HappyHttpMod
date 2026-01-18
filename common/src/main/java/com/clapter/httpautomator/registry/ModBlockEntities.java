package com.clapter.httpautomator.registry;

import com.clapter.httpautomator.Constants;
import com.clapter.httpautomator.blockentity.HttpReceiverBlockEntity;
import com.clapter.httpautomator.blockentity.HttpSenderBlockEntity;
import com.clapter.httpautomator.platform.DeferredObject;
import com.clapter.httpautomator.platform.Services;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {

    //public static BlockEntityType<HttpReceiverBlockEntity> httpReceiverBlockEntity;
    public static DeferredObject<BlockEntityType<HttpReceiverBlockEntity>> httpReceiverBlockEntity;
    public static DeferredObject<BlockEntityType<HttpSenderBlockEntity>> httpSenderBlockEntity;

    public static void registerBlockEntities(){
        httpReceiverBlockEntity = Services.BLOCK_ENTITIES_REGISTRY.registerBlockEntity(id("receiver"), HttpReceiverBlockEntity::new, () -> ModBlocks.httpReceiverBlock);
        httpSenderBlockEntity = Services.BLOCK_ENTITIES_REGISTRY.registerBlockEntity(id("sender"), HttpSenderBlockEntity::new, () -> ModBlocks.httpSenderBlock);

        Services.BLOCK_ENTITIES_REGISTRY.finishRegistry();
    }

    private static ResourceLocation id(String name) {
        return new ResourceLocation(Constants.MOD_ID, name);
    }

}
