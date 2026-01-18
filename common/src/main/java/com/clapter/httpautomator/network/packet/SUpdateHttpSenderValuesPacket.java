package com.clapter.httpautomator.network.packet;

import com.clapter.httpautomator.blockentity.HttpSenderBlockEntity;
import com.clapter.httpautomator.network.IPacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SUpdateHttpSenderValuesPacket extends BasePacket {

    private final BlockPos entityPos;
    private final HttpSenderBlockEntity.Values values;

    public SUpdateHttpSenderValuesPacket(BlockPos pos, HttpSenderBlockEntity.Values endpoint){
        this.entityPos = pos;
        this.values = endpoint;
    }

    public SUpdateHttpSenderValuesPacket(FriendlyByteBuf buf){
        this(buf.readBlockPos(), HttpSenderBlockEntity.Values.readBuffer(buf));
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
            if(entity instanceof HttpSenderBlockEntity sender){
                sender.updateValues(this.values);
            }
        }
    }



}
