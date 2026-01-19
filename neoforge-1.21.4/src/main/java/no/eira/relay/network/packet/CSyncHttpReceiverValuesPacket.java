package no.eira.relay.network.packet;

import no.eira.relay.Constants;
import no.eira.relay.blockentity.HttpReceiverBlockEntity;
import no.eira.relay.client.gui.HttpReceiverSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record CSyncHttpReceiverValuesPacket(BlockPos entityPos, HttpReceiverBlockEntity.Values values) implements CustomPacketPayload {

    public static final Type<CSyncHttpReceiverValuesPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "sync_http_receiver"));

    public static final StreamCodec<FriendlyByteBuf, CSyncHttpReceiverValuesPacket> STREAM_CODEC = StreamCodec.of(
            CSyncHttpReceiverValuesPacket::encode,
            CSyncHttpReceiverValuesPacket::decode
    );

    private static CSyncHttpReceiverValuesPacket decode(FriendlyByteBuf buf) {
        return new CSyncHttpReceiverValuesPacket(buf.readBlockPos(), HttpReceiverBlockEntity.Values.readBuffer(buf));
    }

    private static void encode(FriendlyByteBuf buf, CSyncHttpReceiverValuesPacket packet) {
        buf.writeBlockPos(packet.entityPos);
        packet.values.writeValues(buf);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CSyncHttpReceiverValuesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                BlockEntity entity = level.getBlockEntity(packet.entityPos);
                if (entity instanceof HttpReceiverBlockEntity receiver) {
                    receiver.updateValues(packet.values);
                    Minecraft.getInstance().setScreen(new HttpReceiverSettingsScreen(receiver));
                }
            }
        });
    }
}
