package no.eira.relay.platform.network;

import no.eira.relay.Constants;
import no.eira.relay.network.PacketDirection;
import no.eira.relay.network.packet.BasePacket;
import no.eira.relay.network.packet.CSyncHttpReceiverValuesPacket;
import no.eira.relay.network.packet.SUpdateHttpReceiverValuesPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class PacketHandler implements IPacketHandler {

    private static final SimpleChannel INSTANCE = ChannelBuilder.named(
            new ResourceLocation(Constants.MOD_ID, "packet_handler"))
            .serverAcceptedVersions((status, version) -> true)
            .clientAcceptedVersions((status, version) -> true)
            .networkProtocolVersion(1)
            .simpleChannel();

    //THE HANDLER METHOD FOR HANDLING PACKETS AFTER THEIR DECODING PROCESS
    //HAS TO START HERE, SINCE THE CONTEXT OBJECT IS A FORGE CLASS
    private static void handle(BasePacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> packet.handle(new PacketContext(context)));
        context.setPacketHandled(true);
    }

    @Override
    public <T extends BasePacket> void registerPacket(Class<T> packetClass, BiConsumer<T, FriendlyByteBuf> encode,
                                                      Function<FriendlyByteBuf, T> decode, PacketDirection direction) {
        NetworkDirection dir = this.getNetworkDirectionFromPacketDirection(direction);
        INSTANCE.messageBuilder(packetClass, dir)
                .encoder(encode)
                .decoder(decode)
                .consumerMainThread(PacketHandler::handle)
                .add();
    }

    @Override
    public void sendPacketToPlayer(Object packet, ServerPlayer player) {
        INSTANCE.send(packet, PacketDistributor.PLAYER.with(player));
    }

    @Override
    public void sendPacketToServer(Object packet) {
        INSTANCE.send(packet, PacketDistributor.SERVER.noArg());
    }

    private NetworkDirection getNetworkDirectionFromPacketDirection(PacketDirection direction){
        return switch (direction) {
            case PLAY_TO_CLIENT -> NetworkDirection.PLAY_TO_CLIENT;
            case PLAY_TO_SERVER -> NetworkDirection.PLAY_TO_SERVER;
        };
    }

}
