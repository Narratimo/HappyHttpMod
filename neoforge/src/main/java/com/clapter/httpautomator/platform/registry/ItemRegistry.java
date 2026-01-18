package com.clapter.httpautomator.platform.registry;

import com.clapter.httpautomator.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ItemRegistry implements IItemRegistry {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Constants.MOD_ID);

    @Override
    public void registerItem(ResourceLocation identifier, Supplier<Item> item) {
        ITEMS.register(identifier.getPath(), item);
    }

    @Override
    public void finishRegistry() {
        // Registration happens via event bus, nothing to do here
    }
    
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

