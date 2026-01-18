package no.eira.relay;

import no.eira.relay.blockentity.HttpReceiverBlockEntity;
import no.eira.relay.platform.config.HttpServerConfig;
import no.eira.relay.platform.network.PacketHandler;
import no.eira.relay.registry.ModBlocks;
import net.minecraft.client.telemetry.events.WorldLoadEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Set;

@Mod(Constants.MOD_ID)
public class EiraRelay {

    public EiraRelay() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, HttpServerConfig.COMMON_SPEC);
        CommonClass.init();
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarted);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStopping);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onFMLCommonSetup);
        // Register creative tab listener
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::addCreative);
    }

    private void onServerStarting(ServerStartingEvent e){
        CommonClass.onServerStarting();
    }

    private void onServerStarted(ServerStartedEvent e){
        CommonClass.onServerStarted();
    }

    private void onServerStopping(ServerStoppingEvent e){
        CommonClass.onServerStopping();
    }

    private void onFMLCommonSetup(FMLCommonSetupEvent e){
        e.enqueueWork(CommonClass::registerPackets);
    }

    // Add items to Creative Tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Add to Redstone tab (most appropriate for this mod)
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            if (ModBlocks.httpReceiverBlock != null) {
                event.accept(new ItemStack(ModBlocks.httpReceiverBlock));
            }
        }
    }

}
