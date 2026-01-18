package com.clapter.httpautomator.network.packet;

import com.clapter.httpautomator.blockentity.HttpReceiverBlockEntity;
import com.clapter.httpautomator.network.IPacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SUpdateHttpReceiverValuesPacket extends BasePacket {

    private final BlockPos entityPos;
    private final HttpReceiverBlockEntity.Values values;

    public SUpdateHttpReceiverValuesPacket(BlockPos pos, HttpReceiverBlockEntity.Values endpoint){
        this.entityPos = pos;
        this.values = endpoint;
    }

    public SUpdateHttpReceiverValuesPacket(FriendlyByteBuf buf){
        this(buf.readBlockPos(), HttpReceiverBlockEntity.Values.readBuffer(buf));
    }

    public void encode(FriendlyByteBuf buf){
        buf.writeBlockPos(this.entityPos);
        this.values.writeValues(buf);
    }

    @Override
    public void handle(IPacketContext context){
        if(context.isServerSide()){
            if(context.getSender() == null)return;
            ServerPlayer player = context.getSender();
            ServerLevel level = player.serverLevel();
            BlockEntity entity = level.getBlockEntity(this.entityPos);
            if(entity instanceof HttpReceiverBlockEntity receiver){
                receiver.updateValues(this.values);
            }
        }
    }



}
