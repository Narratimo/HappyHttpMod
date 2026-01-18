package no.eira.relay.network.packet;

import no.eira.relay.Constants;
import no.eira.relay.blockentity.HttpSenderBlockEntity;
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

public record SUpdateHttpSenderValuesPacket(BlockPos entityPos, HttpSenderBlockEntity.Values values) implements CustomPacketPayload {

    public static final Type<SUpdateHttpSenderValuesPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "update_http_sender"));

    public static final StreamCodec<FriendlyByteBuf, SUpdateHttpSenderValuesPacket> STREAM_CODEC = StreamCodec.of(
            SUpdateHttpSenderValuesPacket::encode,
            SUpdateHttpSenderValuesPacket::decode
    );

    private static SUpdateHttpSenderValuesPacket decode(FriendlyByteBuf buf) {
        return new SUpdateHttpSenderValuesPacket(buf.readBlockPos(), HttpSenderBlockEntity.Values.readBuffer(buf));
    }

    private static void encode(FriendlyByteBuf buf, SUpdateHttpSenderValuesPacket packet) {
        buf.writeBlockPos(packet.entityPos);
        packet.values.writeValues(buf);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SUpdateHttpSenderValuesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ServerLevel level = player.serverLevel();
                BlockEntity entity = level.getBlockEntity(packet.entityPos);
                if (entity instanceof HttpSenderBlockEntity sender) {
                    sender.updateValues(packet.values);
                }
            }
        });
    }
}
