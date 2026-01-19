package no.eira.relay.platform;

import no.eira.relay.platform.config.IHttpServerConfig;
import no.eira.relay.platform.network.IPacketHandler;
import no.eira.relay.platform.registry.IBlockEntityRegistry;
import no.eira.relay.platform.registry.IBlockRegistry;
import no.eira.relay.platform.registry.IItemRegistry;
import no.eira.relay.utils.ImplLoader;

public class Services {

    //public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final IBlockRegistry BLOCK_REGISTRY = ImplLoader.loadSingle(IBlockRegistry.class);
    public static final IItemRegistry ITEM_REGISTRY = ImplLoader.loadSingle(IItemRegistry.class);
    public static final IBlockEntityRegistry BLOCK_ENTITIES_REGISTRY = ImplLoader.loadSingle(IBlockEntityRegistry.class);
    public static final IPacketHandler PACKET_HANDLER = ImplLoader.loadSingle(IPacketHandler.class);
    public static final IHttpServerConfig HTTP_CONFIG = ImplLoader.loadSingle(IHttpServerConfig.class);

}