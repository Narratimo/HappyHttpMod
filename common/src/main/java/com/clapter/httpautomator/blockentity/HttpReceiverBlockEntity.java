package com.clapter.httpautomator.blockentity;

import com.clapter.httpautomator.CommonClass;
import com.clapter.httpautomator.Constants;
import com.clapter.httpautomator.block.HttpReceiverBlock;
import com.clapter.httpautomator.enums.EnumPoweredType;
import com.clapter.httpautomator.enums.EnumTimerUnit;
import com.clapter.httpautomator.http.handlers.HttpReceiverBlockHandler;
import com.clapter.httpautomator.registry.ModBlockEntities;
import com.clapter.httpautomator.utils.NBTConverter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class HttpReceiverBlockEntity extends BlockEntity {

    private long lastPoweredMilli;
    private long lastPoweredTick;
    private final Values values;
    private boolean isPowerOn;

    private HttpReceiverBlockHandler currHandler;

    public HttpReceiverBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.httpReceiverBlockEntity.get().get(), pos, state);
        this.values = new Values();
    }

    public void onSignal(){
        switch (values.poweredType){
            case SWITCH -> this.switchPowered();
            case TIMER -> this.startTimer();
        }
    }

    private void startTimer(){
        lastPoweredMilli = System.currentTimeMillis();
        lastPoweredTick = this.level.getGameTime();
    }

    private void switchPowered(){
        BlockState state = this.level.getBlockState(this.getBlockPos());
        Block block = state.getBlock();
        if(block instanceof HttpReceiverBlock receiver) {
            receiver.switchSignal(state, this.level, this.getBlockPos());
            isPowerOn = state.getValue(HttpReceiverBlock.POWERED);
        }
    }

    private void setBlockPowered(boolean powered){
        BlockState state = this.level.getBlockState(this.getBlockPos());
        Block block = state.getBlock();
        if(block instanceof HttpReceiverBlock receiver) {
            receiver.setPowered(state, this.level, this.getBlockPos(), powered);
            isPowerOn = state.getValue(HttpReceiverBlock.POWERED);
        }
    }

    public void tick(){
        if(!this.values.poweredType.equals(EnumPoweredType.TIMER))return;
        switch (this.values.timerUnit){
            case SECONDS -> {
                if(isTimeUnder(System.currentTimeMillis(), this.lastPoweredMilli, this.values.timer*1000)){
                    if(!isPowerOn)setBlockPowered(true);
                }else{
                    if(isPowerOn)setBlockPowered(false);
                }
            }
            case TICKS -> {
                if(isTimeUnder(this.level.getGameTime(), this.lastPoweredTick, this.values.timer)){
                    if(!isPowerOn)setBlockPowered(true);
                }else{
                    if(isPowerOn)setBlockPowered(false);
                }
            }
        }
    }

    private boolean isTimeUnder(long current, long last, float value){
        return current - last <= value;
    }

    public void updateValues(Values values){
        this.values.updateValues(values);
        //TURN BLOCK OFF, WHEN POWER SET TO TIMER
        if(values.poweredType.equals(EnumPoweredType.TIMER)){
            this.setBlockPowered(false);
        }
        setChanged();
        this.registerHandler();
    }

    public void updateValuesClient(Values values){
        this.values.updateValues(values);
    }

    private void registerHandler() {
        if (currHandler != null) {
            currHandler.removeBlockFromHandler(this);
        }
        currHandler = HttpReceiverBlockHandler.create(this, this.values.url);

    }

    private void postLoad(){
        this.registerHandler();
    }

    public Values getValues(){
        return this.values;
    }


    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        CompoundTag compound = nbt.getCompound(Constants.MOD_ID);
        this.values.url = compound.getString("url");
        this.values.poweredType = EnumPoweredType.getById(compound.getInt("poweredType"));
        this.values.timerUnit = EnumTimerUnit.getById(compound.getInt("timerUnit"));
        this.values.timer = compound.getFloat("timer");
        this.values.redirectClientUrl = compound.getString("redirectClientUrl");
        this.values.parameterMap = NBTConverter.convertNBTToMap(compound.getCompound("parameters"),
                String::valueOf, String::valueOf);
        this.postLoad();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        CompoundTag compound = new CompoundTag();
        compound.putString("url", this.values.url);
        compound.putInt("poweredType", this.values.poweredType.ordinal());
        compound.putInt("timerUnit", this.values.timerUnit.ordinal());
        compound.putFloat("timer", this.values.timer);
        compound.putString("redirectClientUrl", this.values.redirectClientUrl);
        CompoundTag compoundTag = NBTConverter.convertMapToNBT(this.values.parameterMap, String::valueOf, String::valueOf);
        compound.put("parameters", compoundTag);
        nbt.put(Constants.MOD_ID, compound);
    }

    public static class Values {

        public String url = "";
        public EnumPoweredType poweredType = EnumPoweredType.SWITCH;
        public float timer;
        public EnumTimerUnit timerUnit = EnumTimerUnit.TICKS;
        public String redirectClientUrl = "";
        public Map<String, String> parameterMap = new HashMap<String, String>();
        public String privateAdress;
        public String publicAdress;

        public void writeValues(FriendlyByteBuf buf){
            buf.writeUtf(this.url);
            buf.writeEnum(this.poweredType);
            buf.writeFloat(this.timer);
            buf.writeEnum(this.timerUnit);
            buf.writeUtf(this.redirectClientUrl);
            buf.writeMap(this.parameterMap, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeUtf);
            buf.writeUtf(CommonClass.HTTP_SERVER.getServerAddress());
            buf.writeUtf(CommonClass.HTTP_SERVER.getServerPublicAdress());
        }

        public static Values readBuffer(FriendlyByteBuf buf){
            Values values = new Values();
            values.url = buf.readUtf();
            values.poweredType = buf.readEnum(EnumPoweredType.class);
            values.timer = buf.readFloat();
            values.timerUnit = buf.readEnum(EnumTimerUnit.class);
            values.redirectClientUrl = buf.readUtf();
            values.parameterMap = buf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readUtf);
            values.privateAdress = buf.readUtf();
            values.publicAdress = buf.readUtf();

            return values;
        }

        private void updateValues(Values values){
            this.url = values.url;
            this.redirectClientUrl = values.redirectClientUrl;
            this.timer = values.timer;
            this.timerUnit = values.timerUnit;
            this.poweredType = values.poweredType;
            this.parameterMap = values.parameterMap;
            this.privateAdress = values.privateAdress;
            this.publicAdress = values.publicAdress;
        }

    }

}
