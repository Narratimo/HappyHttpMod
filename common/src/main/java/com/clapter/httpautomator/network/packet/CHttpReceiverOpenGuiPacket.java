package com.clapter.httpautomator.network.packet;

import com.clapter.httpautomator.CommonClass;
import com.clapter.httpautomator.blockentity.HttpReceiverBlockEntity;
import com.clapter.httpautomator.client.gui.HttpReceiverSettingsScreen;
import com.clapter.httpautomator.network.IPacketContext;
import com.clapter.httpautomator.utils.GuiOpener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CHttpReceiverOpenGuiPacket extends BasePacket{

    private final BlockPos entityPos;
    private final HttpReceiverBlockEntity.Values values;

    public CHttpReceiverOpenGuiPacket(BlockPos pos, HttpReceiverBlockEntity.Values endpoint){
        this.entityPos = pos;
        this.values = endpoint;
    }

    public CHttpReceiverOpenGuiPacket(FriendlyByteBuf buf){
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
                receiver.updateValuesClient(this.values);
                GuiOpener.openGui("receiver", receiver);
            }
        }
    }
}
