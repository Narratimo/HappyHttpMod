package no.eira.relay.registry;

import no.eira.relay.Constants;
import no.eira.relay.block.HttpReceiverBlock;
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

    //public static DeferredObject<Block> testBlock;
    //public static DeferredObject<Block> httpReceiverBlock;
    public static Block httpReceiverBlock;

    public static void registerBlocks(){
        //Services.BLOCK_REGISTRY.registerBlock(id("test"), () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
        Services.BLOCK_REGISTRY.registerBlock(id("receiver"), () -> httpReceiverBlock = new HttpReceiverBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.FIRE)
                .requiresCorrectToolForDrops()
                .strength(5.0F, 6.0F)
                .sound(SoundType.METAL)
        ));
        Services.BLOCK_REGISTRY.finishRegistry();
        //System.out.println("BLOCK: "+httpReceiverBlock.get());
        //Registering items for the coresponding blocks. If more blocks are planned,
        //then an iteration algorithm could be implemented, to avoid manual adding of items.
        //Services.ITEM_REGISTRY.registerItem(id("test"), () -> new BlockItem(testBlock, new Item.Properties()));

    }

    private static ResourceLocation id(String name) {
        return new ResourceLocation(Constants.MOD_ID, name);
    }

}
