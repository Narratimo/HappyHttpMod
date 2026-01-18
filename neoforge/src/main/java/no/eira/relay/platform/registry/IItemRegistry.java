package no.eira.relay.platform.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public interface IItemRegistry {

    void registerItem(ResourceLocation identifier, Supplier<Item> item);
    void finishRegistry();
}
