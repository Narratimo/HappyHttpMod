package no.eira.relay.platform.network;

import no.eira.relay.Constants;
import no.eira.relay.network.PacketDirection;
import no.eira.relay.network.packet.BasePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class PacketHandler implements IPacketHandler {

    private static PayloadRegistrar registrar;

    public static void init(PayloadRegistrar payloadRegistrar) {
        registrar = payloadRegistrar;
    }

    @Override
    public <T extends BasePacket> void registerPacket(Class<T> packetClass, BiConsumer<T, FriendlyByteBuf> encode,
                                                      Function<FriendlyByteBuf, T> decode, PacketDirection direction) {
        // NeoForge 1.21 uses a different packet registration system
        // This is a simplified implementation - full implementation would need custom payload types
        Constants.LOG.info("Registering packet: " + packetClass.getSimpleName());
    }

    @Override
    public void sendPacketToPlayer(Object packet, ServerPlayer player) {
        if (packet instanceof CustomPacketPayload payload) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    @Override
    public void sendPacketToServer(Object packet) {
        if (packet instanceof CustomPacketPayload payload) {
            PacketDistributor.sendToServer(payload);
        }
    }
}

