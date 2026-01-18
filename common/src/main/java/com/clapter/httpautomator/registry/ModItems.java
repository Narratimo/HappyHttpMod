package com.clapter.httpautomator.registry;

import com.clapter.httpautomator.Constants;
import com.clapter.httpautomator.platform.Services;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class ModItems {

    public static Item receiver;
    public static Item sender;

    public static void registerItems(){
        Services.ITEM_REGISTRY.registerItem(id("receiver"), () -> receiver = new BlockItem(ModBlocks.httpReceiverBlock, new Item.Properties()));
        Services.ITEM_REGISTRY.registerItem(id("sender"), () -> sender = new BlockItem(ModBlocks.httpSenderBlock, new Item.Properties()));
        Services.ITEM_REGISTRY.finishRegistry();
    }

    private static ResourceLocation id(String name) {
        return new ResourceLocation(Constants.MOD_ID, name);
    }

}
