package com.clapter.httpautomator.blockentity;

import com.clapter.httpautomator.Constants;
import com.clapter.httpautomator.block.HttpReceiverBlock;
import com.clapter.httpautomator.http.handlers.HttpReceiverBlockHandler;
import com.clapter.httpautomator.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class HttpReceiverBlockEntity extends BlockEntity {

    private final Values values;

    public HttpReceiverBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.httpReceiverBlockEntity.get().get(), pos, state);
        this.values = new Values();
    }

    public void onSignal(){
        BlockState state = this.level.getBlockState(this.getBlockPos());
        Block block = state.getBlock();
        if(block instanceof HttpReceiverBlock receiver){
            receiver.onSignal(state, this.level, this.getBlockPos());
        }
    }

    public void updateValues(Values values){
        this.values.updateValues(values);
        setChanged();
        if(!this.getLevel().isClientSide) {
            HttpReceiverBlockHandler.create(this, this.values.url);
        }
    }

    private void postLoad(){
        HttpReceiverBlockHandler.create(this, this.values.url);
    }

    public Values getValues(){
        return this.values;
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        CompoundTag compound = nbt.getCompound(Constants.MOD_ID);
        this.values.url = compound.getString("url");
        this.postLoad();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        CompoundTag compound = new CompoundTag();
        compound.putString("url", this.values.url);
        nbt.put(Constants.MOD_ID, compound);
    }

    public static class Values {

        public String url = "";

        public void writeValues(FriendlyByteBuf buf){
            buf.writeUtf(this.url);
        }

        public static Values readBuffer(FriendlyByteBuf buf){
            Values values = new Values();
            values.url = buf.readUtf();
            return values;
        }

        private void updateValues(Values values){
            this.url = values.url;
        }
    }
}
