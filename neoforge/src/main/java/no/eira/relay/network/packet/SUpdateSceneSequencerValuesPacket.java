package no.eira.relay.network.packet;

import no.eira.relay.Constants;
import no.eira.relay.blockentity.SceneSequencerBlockEntity;
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

public record SUpdateSceneSequencerValuesPacket(BlockPos entityPos, SceneSequencerBlockEntity.Values values) implements CustomPacketPayload {

    public static final Type<SUpdateSceneSequencerValuesPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "update_scene_sequencer"));

    public static final StreamCodec<FriendlyByteBuf, SUpdateSceneSequencerValuesPacket> STREAM_CODEC = StreamCodec.of(
            SUpdateSceneSequencerValuesPacket::encode,
            SUpdateSceneSequencerValuesPacket::decode
    );

    private static SUpdateSceneSequencerValuesPacket decode(FriendlyByteBuf buf) {
        return new SUpdateSceneSequencerValuesPacket(buf.readBlockPos(), SceneSequencerBlockEntity.Values.readBuffer(buf));
    }

    private static void encode(FriendlyByteBuf buf, SUpdateSceneSequencerValuesPacket packet) {
        buf.writeBlockPos(packet.entityPos);
        packet.values.writeValues(buf);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SUpdateSceneSequencerValuesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ServerLevel level = player.serverLevel();
                BlockEntity entity = level.getBlockEntity(packet.entityPos);
                if (entity instanceof SceneSequencerBlockEntity sequencer) {
                    sequencer.updateValues(packet.values);
                }
            }
        });
    }
}
