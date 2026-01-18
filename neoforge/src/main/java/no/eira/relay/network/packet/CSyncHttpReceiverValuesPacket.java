package no.eira.relay.network.packet;

import no.eira.relay.blockentity.HttpReceiverBlockEntity;
import no.eira.relay.client.gui.HttpReceiverSettingsScreen;
import no.eira.relay.network.IPacketContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CSyncHttpReceiverValuesPacket extends BasePacket{

    private final BlockPos entityPos;
    private final HttpReceiverBlockEntity.Values values;

    public CSyncHttpReceiverValuesPacket(BlockPos pos, HttpReceiverBlockEntity.Values endpoint){
        this.entityPos = pos;
        this.values = endpoint;
    }

    public CSyncHttpReceiverValuesPacket(FriendlyByteBuf buf){
        this(buf.readBlockPos(), HttpReceiverBlockEntity.Values.readBuffer(buf));
    }

    public void encode(FriendlyByteBuf buf){
        buf.writeBlockPos(this.entityPos);
        this.values.writeValues(buf);
    }


    @Override
    public void handle(IPacketContext context) {
        if(context.isClientSide()){
            ClientLevel level = Minecraft.getInstance().level;
            BlockEntity entity = level.getBlockEntity(this.entityPos);
            if(entity instanceof HttpReceiverBlockEntity receiver){
                receiver.updateValues(this.values);
                //TODO: Separate screen opening from this packet
                Minecraft.getInstance().setScreen(new HttpReceiverSettingsScreen(receiver));
            }
        }
    }
}
