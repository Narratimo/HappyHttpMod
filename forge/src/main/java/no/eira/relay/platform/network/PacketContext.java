package no.eira.relay.platform.network;

import no.eira.relay.network.IPacketContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import org.jetbrains.annotations.Nullable;


public class PacketContext implements IPacketContext {

    private final ServerPlayer sender;
    private final boolean isServerSide;
    private final boolean isClientSide;

    public PacketContext(CustomPayloadEvent.Context context){
        this.sender = context.getSender();
        this.isServerSide = context.isServerSide();
        this.isClientSide = context.isClientSide();
    }

    @Nullable
    @Override
    public ServerPlayer getSender() {
        return this.sender;
    }

    @Override
    public boolean isServerSide() {
        return this.isServerSide;
    }

    @Override
    public boolean isClientSide() {
        return this.isClientSide;
    }
}
