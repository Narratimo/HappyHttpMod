package no.eira.relay.platform.network;

import no.eira.relay.network.IPacketContext;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketContext implements IPacketContext {

    private final IPayloadContext context;

    public PacketContext(IPayloadContext context) {
        this.context = context;
    }

    @Override
    public ServerPlayer getSender() {
        if (context.player() instanceof ServerPlayer serverPlayer) {
            return serverPlayer;
        }
        return null;
    }

    @Override
    public boolean isServerSide() {
        return context.player() instanceof ServerPlayer;
    }

    @Override
    public boolean isClientSide() {
        return !isServerSide();
    }
}

