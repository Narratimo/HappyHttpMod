package no.eira.relay;

import no.eira.relay.platform.config.HttpServerConfig;
import no.eira.relay.platform.registry.BlockEntityRegistry;
import no.eira.relay.platform.registry.BlockRegistry;
import no.eira.relay.platform.registry.ItemRegistry;
import no.eira.relay.registry.ModBlocks;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@Mod(Constants.MOD_ID)
public class EiraRelay {

    public EiraRelay(IEventBus modEventBus, ModContainer modContainer) {
        // Register config
        modContainer.registerConfig(ModConfig.Type.COMMON, HttpServerConfig.COMMON_SPEC);

        // Register DeferredRegisters to the mod event bus FIRST
        BlockRegistry.register(modEventBus);
        ItemRegistry.register(modEventBus);
        BlockEntityRegistry.register(modEventBus);

        // Initialize common code (registers blocks, items, etc.)
        CommonClass.init();

        // Register mod event listeners
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::addCreative);

        // Register game event listeners
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);

        Constants.LOG.info("Eira Relay initialized for NeoForge 1.21.1!");
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(CommonClass::registerPackets);
    }

    private void onServerStarting(ServerStartingEvent event) {
        CommonClass.onServerStarting();
    }

    private void onServerStarted(ServerStartedEvent event) {
        CommonClass.onServerStarted();
    }

    private void onServerStopping(ServerStoppingEvent event) {
        CommonClass.onServerStopping();
    }

    // Add items to Creative Tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            if (ModBlocks.httpReceiverBlock != null) {
                event.accept(new ItemStack(ModBlocks.httpReceiverBlock));
            }
            if (ModBlocks.httpSenderBlock != null) {
                event.accept(new ItemStack(ModBlocks.httpSenderBlock));
            }
        }
    }
}

