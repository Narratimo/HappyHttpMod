package no.eira.relay.blockentity;

import no.eira.relay.CommonClass;
import no.eira.relay.Constants;
import no.eira.relay.enums.EnumHttpMethod;
import no.eira.relay.registry.ModBlockEntities;
import no.eira.relay.utils.JsonUtils;
import no.eira.relay.utils.NBTConverter;
import no.eira.relay.utils.QueryBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class HttpSenderBlockEntity extends BlockEntity {

    private final Values values;

    public HttpSenderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.httpSenderBlockEntity.get().get(), pos, state);
        this.values = new Values();
    }

    public void onPowered() {
        if (!this.values.url.isEmpty()) {
            if (this.values.httpMethod.equals(EnumHttpMethod.GET)) {
                String params = QueryBuilder.paramsToQueryString(this.values.parameterMap);
                CommonClass.HTTP_CLIENT.sendGet(this.values.url, params);
            }
            if (this.values.httpMethod.equals(EnumHttpMethod.POST)) {
                String params = JsonUtils.parametersFromMapToString(this.values.parameterMap);
                CommonClass.HTTP_CLIENT.sendPost(this.values.url, params);
            }
        }
    }

    public void onUnpowered() {
        // No action on unpower for now
    }

    public void updateValues(Values values) {
        this.values.updateValues(values);
        setChanged();
    }

    public Values getValues() {
        return this.values;
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        CompoundTag compound = nbt.getCompound(Constants.MOD_ID);
        this.values.url = compound.getString("url");
        this.values.parameterMap = NBTConverter.convertNBTToMap(
            compound.getCompound("parameters"),
            String::valueOf,
            String::valueOf
        );
        this.values.httpMethod = EnumHttpMethod.getByName(compound.getString("httpMethod"));
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        CompoundTag compound = new CompoundTag();
        compound.putString("url", this.values.url);
        CompoundTag paramsTag = NBTConverter.convertMapToNBT(
            this.values.parameterMap,
            String::valueOf,
            String::valueOf
        );
        compound.put("parameters", paramsTag);
        compound.putString("httpMethod", this.values.httpMethod.toString());
        nbt.put(Constants.MOD_ID, compound);
    }

    public static class Values {

        public String url = "";
        public Map<String, String> parameterMap = new HashMap<>();
        public EnumHttpMethod httpMethod;

        public Values() {
            this.httpMethod = EnumHttpMethod.GET;
        }

        public void writeValues(FriendlyByteBuf buf) {
            buf.writeUtf(this.url);
            buf.writeUtf(JsonUtils.parametersFromMapToString(this.parameterMap));
            buf.writeEnum(this.httpMethod);
        }

        public static Values readBuffer(FriendlyByteBuf buf) {
            Values values = new Values();
            values.url = buf.readUtf();
            String paramsJson = buf.readUtf();
            if (!paramsJson.isEmpty() && !paramsJson.equals("{}")) {
                try {
                    values.parameterMap = JsonUtils.getParametersAsMap(paramsJson);
                } catch (Exception e) {
                    values.parameterMap = new HashMap<>();
                }
            }
            values.httpMethod = buf.readEnum(EnumHttpMethod.class);
            return values;
        }

        private void updateValues(Values values) {
            this.url = values.url;
            this.parameterMap = values.parameterMap;
            this.httpMethod = values.httpMethod;
        }
    }
}
