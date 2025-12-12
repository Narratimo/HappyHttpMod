package com.clapter.httpautomator.network;

import net.minecraft.server.level.ServerPlayer;

public interface IPacketContext {

    ServerPlayer getSender();
    boolean isServerSide();
    boolean isClientSide();

}
