package no.eira.relay.registry;

import no.eira.relay.Constants;
import no.eira.relay.block.HttpReceiverBlock;
import no.eira.relay.block.HttpSenderBlock;
import no.eira.relay.platform.DeferredObject;
import no.eira.relay.platform.Services;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Supplier;

public class ModBlocks {

    public static Block httpReceiverBlock;
    public static Block httpSenderBlock;

    public static void registerBlocks(){
        Services.BLOCK_REGISTRY.registerBlock(id("receiver"), () -> httpReceiverBlock = new HttpReceiverBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.FIRE)
                .requiresCorrectToolForDrops()
                .strength(5.0F, 6.0F)
                .sound(SoundType.METAL)
        ));
        Services.BLOCK_REGISTRY.registerBlock(id("sender"), () -> httpSenderBlock = new HttpSenderBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.EMERALD)
                .requiresCorrectToolForDrops()
                .strength(5.0F, 6.0F)
                .sound(SoundType.METAL)
        ));
        Services.BLOCK_REGISTRY.finishRegistry();
    }

    private static ResourceLocation id(String name) {
        return new ResourceLocation(Constants.MOD_ID, name);
    }

}
