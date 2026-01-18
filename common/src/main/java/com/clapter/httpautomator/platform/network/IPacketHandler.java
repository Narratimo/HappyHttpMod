package com.clapter.httpautomator.platform.network;

import com.clapter.httpautomator.network.PacketDirection;
import com.clapter.httpautomator.network.packet.BasePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface IPacketHandler {

    <T extends BasePacket> void registerPacket(Class<T> packetClass, BiConsumer<T, FriendlyByteBuf> encode,
                                               Function<FriendlyByteBuf, T> decode, PacketDirection direction);
    void sendPacketToPlayer(Object packet, ServerPlayer player);
    void sendPacketToServer(Object packet);
}
