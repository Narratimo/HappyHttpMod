package com.clapter.httpautomator.network.packet;

import com.clapter.httpautomator.blockentity.HttpSenderBlockEntity;
import com.clapter.httpautomator.client.gui.HttpSenderSettingsScreen;
import com.clapter.httpautomator.network.IPacketContext;
import com.clapter.httpautomator.utils.GuiOpener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CHttpSenderOpenGuiPacket extends BasePacket{

    private final BlockPos entityPos;
    private final HttpSenderBlockEntity.Values values;

    public CHttpSenderOpenGuiPacket(BlockPos pos, HttpSenderBlockEntity.Values endpoint){
        this.entityPos = pos;
        this.values = endpoint;
    }

    public CHttpSenderOpenGuiPacket(FriendlyByteBuf buf){
        this(buf.readBlockPos(), HttpSenderBlockEntity.Values.readBuffer(buf));
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
            if(entity instanceof HttpSenderBlockEntity sender){
                sender.updateValues(this.values);
                GuiOpener.openGui("sender", sender);
            }
        }
    }
}
