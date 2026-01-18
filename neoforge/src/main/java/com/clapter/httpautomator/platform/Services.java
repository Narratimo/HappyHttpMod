package com.clapter.httpautomator.platform;

import com.clapter.httpautomator.platform.config.IHttpServerConfig;
import com.clapter.httpautomator.platform.network.IPacketHandler;
import com.clapter.httpautomator.platform.registry.IBlockEntityRegistry;
import com.clapter.httpautomator.platform.registry.IBlockRegistry;
import com.clapter.httpautomator.platform.registry.IItemRegistry;
import com.clapter.httpautomator.utils.ImplLoader;

public class Services {

    //public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final IBlockRegistry BLOCK_REGISTRY = ImplLoader.loadSingle(IBlockRegistry.class);
    public static final IItemRegistry ITEM_REGISTRY = ImplLoader.loadSingle(IItemRegistry.class);
    public static final IBlockEntityRegistry BLOCK_ENTITIES_REGISTRY = ImplLoader.loadSingle(IBlockEntityRegistry.class);
    public static final IPacketHandler PACKET_HANDLER = ImplLoader.loadSingle(IPacketHandler.class);
    public static final IHttpServerConfig HTTP_CONFIG = ImplLoader.loadSingle(IHttpServerConfig.class);

}