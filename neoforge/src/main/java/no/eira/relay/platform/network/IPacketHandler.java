package no.eira.relay.platform.network;

import no.eira.relay.network.PacketDirection;
import no.eira.relay.network.packet.BasePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface IPacketHandler {

    <T extends BasePacket> void registerPacket(Class<T> packetClass, BiConsumer<T, FriendlyByteBuf> encode,
                                               Function<FriendlyByteBuf, T> decode, PacketDirection direction);
    void sendPacketToPlayer(Object packet, ServerPlayer player);
    void sendPacketToServer(Object packet);
}
