package no.eira.relay.platform.registry;

import no.eira.relay.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ItemRegistry implements IItemRegistry {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);

    @Override
    public void registerItem(ResourceLocation identifier, Supplier<Item> item) {
        ITEMS.register(identifier.getPath(), item);
    }

    public void finishRegistry(){
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
