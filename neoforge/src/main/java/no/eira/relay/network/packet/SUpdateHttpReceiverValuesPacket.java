package no.eira.relay.network.packet;

import no.eira.relay.Constants;
import no.eira.relay.blockentity.HttpReceiverBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SUpdateHttpReceiverValuesPacket(BlockPos entityPos, HttpReceiverBlockEntity.Values values) implements CustomPacketPayload {

    public static final Type<SUpdateHttpReceiverValuesPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "update_http_receiver"));

    public static final StreamCodec<FriendlyByteBuf, SUpdateHttpReceiverValuesPacket> STREAM_CODEC = StreamCodec.of(
            SUpdateHttpReceiverValuesPacket::encode,
            SUpdateHttpReceiverValuesPacket::decode
    );

    private static SUpdateHttpReceiverValuesPacket decode(FriendlyByteBuf buf) {
        return new SUpdateHttpReceiverValuesPacket(buf.readBlockPos(), HttpReceiverBlockEntity.Values.readBuffer(buf));
    }

    private static void encode(FriendlyByteBuf buf, SUpdateHttpReceiverValuesPacket packet) {
        buf.writeBlockPos(packet.entityPos);
        packet.values.writeValues(buf);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SUpdateHttpReceiverValuesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ServerLevel level = player.serverLevel();
                BlockEntity entity = level.getBlockEntity(packet.entityPos);
                if (entity instanceof HttpReceiverBlockEntity receiver) {
                    receiver.updateValues(packet.values);
                }
            }
        });
    }
}
