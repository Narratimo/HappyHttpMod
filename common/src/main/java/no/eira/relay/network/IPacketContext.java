package no.eira.relay.network;

import net.minecraft.server.level.ServerPlayer;

public interface IPacketContext {

    ServerPlayer getSender();
    boolean isServerSide();
    boolean isClientSide();

}
