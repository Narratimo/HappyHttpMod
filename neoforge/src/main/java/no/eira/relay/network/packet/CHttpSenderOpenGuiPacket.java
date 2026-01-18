package no.eira.relay.network.packet;

import no.eira.relay.Constants;
import no.eira.relay.blockentity.HttpSenderBlockEntity;
import no.eira.relay.client.gui.HttpSenderSettingsScreen;
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

public record CHttpSenderOpenGuiPacket(BlockPos entityPos, HttpSenderBlockEntity.Values values) implements CustomPacketPayload {

    public static final Type<CHttpSenderOpenGuiPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "open_http_sender_gui"));

    public static final StreamCodec<FriendlyByteBuf, CHttpSenderOpenGuiPacket> STREAM_CODEC = StreamCodec.of(
            CHttpSenderOpenGuiPacket::encode,
            CHttpSenderOpenGuiPacket::decode
    );

    private static CHttpSenderOpenGuiPacket decode(FriendlyByteBuf buf) {
        return new CHttpSenderOpenGuiPacket(buf.readBlockPos(), HttpSenderBlockEntity.Values.readBuffer(buf));
    }

    private static void encode(FriendlyByteBuf buf, CHttpSenderOpenGuiPacket packet) {
        buf.writeBlockPos(packet.entityPos);
        packet.values.writeValues(buf);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CHttpSenderOpenGuiPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                BlockEntity entity = level.getBlockEntity(packet.entityPos);
                if (entity instanceof HttpSenderBlockEntity sender) {
                    sender.updateValues(packet.values);
                    Minecraft.getInstance().setScreen(new HttpSenderSettingsScreen(sender));
                }
            }
        });
    }
}
