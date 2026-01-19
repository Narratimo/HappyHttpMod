package no.eira.relay;

import no.eira.relay.network.packet.CHttpSenderOpenGuiPacket;
import no.eira.relay.network.packet.CSceneSequencerOpenGuiPacket;
import no.eira.relay.network.packet.CSyncHttpReceiverValuesPacket;
import no.eira.relay.network.packet.SUpdateHttpReceiverValuesPacket;
import no.eira.relay.network.packet.SUpdateHttpSenderValuesPacket;
import no.eira.relay.network.packet.SUpdateSceneSequencerValuesPacket;
import no.eira.relay.platform.config.HttpServerConfig;
import no.eira.relay.platform.registry.BlockEntityRegistry;
import no.eira.relay.platform.registry.BlockRegistry;
import no.eira.relay.platform.registry.ItemRegistry;
import no.eira.relay.registry.ModBlocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

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
        modEventBus.addListener(this::registerPayloads);

        // Register game event listeners
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);

        Constants.LOG.info("Eira Relay initialized for NeoForge 1.21.1!");
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        // No longer needed - packets registered via RegisterPayloadHandlersEvent
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Constants.MOD_ID);

        // Client-bound packets (server -> client)
        registrar.playToClient(
                CSyncHttpReceiverValuesPacket.TYPE,
                CSyncHttpReceiverValuesPacket.STREAM_CODEC,
                CSyncHttpReceiverValuesPacket::handle
        );
        registrar.playToClient(
                CHttpSenderOpenGuiPacket.TYPE,
                CHttpSenderOpenGuiPacket.STREAM_CODEC,
                CHttpSenderOpenGuiPacket::handle
        );
        registrar.playToClient(
                CSceneSequencerOpenGuiPacket.TYPE,
                CSceneSequencerOpenGuiPacket.STREAM_CODEC,
                CSceneSequencerOpenGuiPacket::handle
        );

        // Server-bound packets (client -> server)
        registrar.playToServer(
                SUpdateHttpReceiverValuesPacket.TYPE,
                SUpdateHttpReceiverValuesPacket.STREAM_CODEC,
                SUpdateHttpReceiverValuesPacket::handle
        );
        registrar.playToServer(
                SUpdateHttpSenderValuesPacket.TYPE,
                SUpdateHttpSenderValuesPacket.STREAM_CODEC,
                SUpdateHttpSenderValuesPacket::handle
        );
        registrar.playToServer(
                SUpdateSceneSequencerValuesPacket.TYPE,
                SUpdateSceneSequencerValuesPacket.STREAM_CODEC,
                SUpdateSceneSequencerValuesPacket::handle
        );

        Constants.LOG.info("Registered Eira Relay network packets");
    }

    private void onServerStarting(ServerStartingEvent event) {
        CommonClass.onServerStarting();
    }

    private void onServerStarted(ServerStartedEvent event) {
        CommonClass.onServerStarted();
        // Initialize server level for HTTP handlers
        ServerLevel overworld = event.getServer().getLevel(Level.OVERWORLD);
        if (overworld != null) {
            CommonClass.initServerLevel(overworld);
        }
    }

    private void onServerTick(ServerTickEvent.Post event) {
        // Process redstone emissions
        CommonClass.onServerTick();
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
            if (ModBlocks.sceneSequencerBlock != null) {
                event.accept(new ItemStack(ModBlocks.sceneSequencerBlock));
            }
        }
    }
}

