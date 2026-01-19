package no.eira.relay.network.packet;

import no.eira.relay.Constants;
import no.eira.relay.blockentity.SceneSequencerBlockEntity;
import no.eira.relay.client.gui.SceneSequencerSettingsScreen;
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

public record CSceneSequencerOpenGuiPacket(BlockPos entityPos, SceneSequencerBlockEntity.Values values) implements CustomPacketPayload {

    public static final Type<CSceneSequencerOpenGuiPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "open_scene_sequencer_gui"));

    public static final StreamCodec<FriendlyByteBuf, CSceneSequencerOpenGuiPacket> STREAM_CODEC = StreamCodec.of(
            CSceneSequencerOpenGuiPacket::encode,
            CSceneSequencerOpenGuiPacket::decode
    );

    private static CSceneSequencerOpenGuiPacket decode(FriendlyByteBuf buf) {
        return new CSceneSequencerOpenGuiPacket(buf.readBlockPos(), SceneSequencerBlockEntity.Values.readBuffer(buf));
    }

    private static void encode(FriendlyByteBuf buf, CSceneSequencerOpenGuiPacket packet) {
        buf.writeBlockPos(packet.entityPos);
        packet.values.writeValues(buf);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CSceneSequencerOpenGuiPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                BlockEntity entity = level.getBlockEntity(packet.entityPos);
                if (entity instanceof SceneSequencerBlockEntity sequencer) {
                    sequencer.updateValues(packet.values);
                    Minecraft.getInstance().setScreen(new SceneSequencerSettingsScreen(sequencer));
                }
            }
        });
    }
}
