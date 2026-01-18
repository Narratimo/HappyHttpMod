package com.clapter.httpautomator.blockentity;

import com.clapter.httpautomator.CommonClass;
import com.clapter.httpautomator.Constants;
import com.clapter.httpautomator.enums.EnumHttpMethod;
import com.clapter.httpautomator.enums.EnumTimerUnit;
import com.clapter.httpautomator.registry.ModBlockEntities;
import com.clapter.httpautomator.utils.JsonUtils;
import com.clapter.httpautomator.utils.NBTConverter;
import com.clapter.httpautomator.utils.QueryBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class HttpSenderBlockEntity extends BlockEntity {


    private final HttpSenderBlockEntity.Values values;

    public HttpSenderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.httpSenderBlockEntity.get().get(), pos, state);
        this.values = new HttpSenderBlockEntity.Values();
    }

    public void onPowered(){
        if(!this.values.url.isEmpty()){
            if(this.values.httpMethod.equals(EnumHttpMethod.GET)){
                String params = QueryBuilder.paramsToQueryString(this.values.parameterMap);
                CommonClass.HTTP_CLIENT.sendGet(this.values.url, params);
            }
            if(this.values.httpMethod.equals(EnumHttpMethod.POST)) {
                String params = JsonUtils.parametersFromMapToString(this.values.parameterMap);
                CommonClass.HTTP_CLIENT.sendPost(this.values.url, params);
            }
        }
    }

    public void onUnpowered(){

    }

    public void updateValues(HttpSenderBlockEntity.Values values){
        this.values.updateValues(values);
        setChanged();
    }

    public HttpSenderBlockEntity.Values getValues(){
        return this.values;
    }


    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        CompoundTag compound = nbt.getCompound(Constants.MOD_ID);
        this.values.url = compound.getString("url");
        this.values.parameterMap = NBTConverter.convertNBTToMap(compound.getCompound("parameters"),
                String::valueOf, String::valueOf);
        this.values.httpMethod = EnumHttpMethod.getByName(compound.getString("httpMethod"));
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        CompoundTag compound = new CompoundTag();
        compound.putString("url", this.values.url);
        CompoundTag compoundTag = NBTConverter.convertMapToNBT(this.values.parameterMap, String::valueOf, String::valueOf);
        compound.put("parameters", compoundTag);
        compound.putString("httpMethod", this.values.httpMethod.toString());
        nbt.put(Constants.MOD_ID, compound);
    }

    public static class Values {

        public String url = "";
        public Map<String, String> parameterMap = new HashMap<String, String>();
        public EnumHttpMethod httpMethod;

        public Values(){
            this.httpMethod = EnumHttpMethod.GET;
        }

        public void writeValues(FriendlyByteBuf buf){
            buf.writeUtf(this.url);
            buf.writeMap(this.parameterMap, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeUtf);
            buf.writeEnum(this.httpMethod);
        }

        public static HttpSenderBlockEntity.Values readBuffer(FriendlyByteBuf buf){
            HttpSenderBlockEntity.Values values = new HttpSenderBlockEntity.Values();
            values.url = buf.readUtf();
            values.parameterMap = buf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readUtf);
            values.httpMethod = buf.readEnum(EnumHttpMethod.class);
            return values;
        }

        private void updateValues(HttpSenderBlockEntity.Values values){
            this.url = values.url;
            this.parameterMap = values.parameterMap;
            this.httpMethod = values.httpMethod;
        }

    }

}