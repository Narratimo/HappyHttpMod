package no.eira.relay.registry;

import no.eira.relay.Constants;
import no.eira.relay.block.HttpReceiverBlock;
import no.eira.relay.block.HttpSenderBlock;
import no.eira.relay.block.SceneSequencerBlock;
import no.eira.relay.platform.Services;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class ModBlocks {

    public static Block httpReceiverBlock;
    public static Block httpSenderBlock;
    public static Block sceneSequencerBlock;

    public static void registerBlocks() {
        Services.BLOCK_REGISTRY.registerBlock(id("receiver"), () -> httpReceiverBlock = new HttpReceiverBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.FIRE)
                .requiresCorrectToolForDrops()
                .strength(5.0F, 6.0F)
                .sound(SoundType.METAL)
        ));
        Services.BLOCK_REGISTRY.registerBlock(id("sender"), () -> httpSenderBlock = new HttpSenderBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.FIRE)
                .requiresCorrectToolForDrops()
                .strength(5.0F, 6.0F)
                .sound(SoundType.METAL)
        ));
        Services.BLOCK_REGISTRY.registerBlock(id("sequencer"), () -> sceneSequencerBlock = new SceneSequencerBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .requiresCorrectToolForDrops()
                .strength(5.0F, 6.0F)
                .sound(SoundType.METAL)
        ));
        Services.BLOCK_REGISTRY.finishRegistry();
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name);
    }
}
