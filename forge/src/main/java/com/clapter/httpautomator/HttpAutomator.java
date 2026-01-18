package com.clapter.httpautomator;

import com.clapter.httpautomator.platform.config.HttpServerConfig;
import com.clapter.httpautomator.registry.ModBlocks;
import com.clapter.httpautomator.registry.ModItems;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID)
public class HttpAutomator {

    public HttpAutomator() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, HttpServerConfig.COMMON_CONFIG);
        CommonClass.init();
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarted);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStopping);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCreativeTabsBuildContent);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onFMLCommonSetup);
        HttpServerConfig.loadConfig();
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

    private void onCreativeTabsBuildContent(BuildCreativeModeTabContentsEvent e){
        if(e.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS){
            e.accept(ModBlocks.httpReceiverBlock);
            e.accept(ModBlocks.httpSenderBlock);
        }
    }


}