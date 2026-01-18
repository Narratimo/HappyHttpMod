package no.eira.relay.blockentity;

import no.eira.relay.CommonClass;
import no.eira.relay.Constants;
import no.eira.relay.enums.EnumHttpMethod;
import no.eira.relay.enums.EnumPoweredType;
import no.eira.relay.enums.EnumTimerUnit;
import no.eira.relay.registry.ModBlockEntities;
import no.eira.relay.utils.JsonUtils;
import no.eira.relay.utils.NBTConverter;
import no.eira.relay.utils.QueryBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class HttpSenderBlockEntity extends BlockEntity {

    private long lastSentMilli;
    private long lastSentTick;
    private boolean inCooldown;
    private final Values values;

    public HttpSenderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.httpSenderBlockEntity.get().get(), pos, state);
        this.values = new Values();
    }

    public void onPowered() {
        switch (values.poweredType) {
            case SWITCH -> sendHttpRequest();
            case TIMER -> {
                if (!inCooldown) {
                    sendHttpRequest();
                    startCooldown();
                }
            }
        }
    }

    private void sendHttpRequest() {
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

    private void startCooldown() {
        lastSentMilli = System.currentTimeMillis();
        if (this.level != null) {
            lastSentTick = this.level.getGameTime();
        }
        inCooldown = true;
    }

    public void tick() {
        if (this.level == null || this.level.isClientSide) return;
        if (!this.values.poweredType.equals(EnumPoweredType.TIMER)) return;
        if (!inCooldown) return;

        switch (this.values.timerUnit) {
            case SECONDS -> {
                if (!isTimeUnder(System.currentTimeMillis(), this.lastSentMilli, (long)(this.values.timer * 1000))) {
                    inCooldown = false;
                }
            }
            case TICKS -> {
                if (!isTimeUnder(this.level.getGameTime(), this.lastSentTick, (long)this.values.timer)) {
                    inCooldown = false;
                }
            }
        }
    }

    private boolean isTimeUnder(long current, long last, long value) {
        return current - last <= value;
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
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        CompoundTag compound = nbt.getCompound(Constants.MOD_ID);
        this.values.url = compound.getString("url");
        this.values.parameterMap = NBTConverter.convertNBTToMap(
            compound.getCompound("parameters"),
            String::valueOf,
            String::valueOf
        );
        this.values.httpMethod = EnumHttpMethod.getByName(compound.getString("httpMethod"));
        this.values.poweredType = EnumPoweredType.getById(compound.getInt("poweredType"));
        this.values.timerUnit = EnumTimerUnit.getById(compound.getInt("timerUnit"));
        this.values.timer = compound.getFloat("timer");
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        CompoundTag compound = new CompoundTag();
        compound.putString("url", this.values.url);
        CompoundTag paramsTag = NBTConverter.convertMapToNBT(
            this.values.parameterMap,
            String::valueOf,
            String::valueOf
        );
        compound.put("parameters", paramsTag);
        compound.putString("httpMethod", this.values.httpMethod.toString());
        compound.putInt("poweredType", this.values.poweredType.getId());
        compound.putInt("timerUnit", this.values.timerUnit.getId());
        compound.putFloat("timer", this.values.timer);
        nbt.put(Constants.MOD_ID, compound);
    }

    public static class Values {

        public String url = "";
        public Map<String, String> parameterMap = new HashMap<>();
        public EnumHttpMethod httpMethod;
        public EnumPoweredType poweredType = EnumPoweredType.SWITCH;
        public float timer = 20; // Default 20 ticks = 1 second
        public EnumTimerUnit timerUnit = EnumTimerUnit.TICKS;

        public Values() {
            this.httpMethod = EnumHttpMethod.GET;
        }

        public void writeValues(FriendlyByteBuf buf) {
            buf.writeUtf(this.url);
            // Serialize map as JSON string for compatibility
            buf.writeUtf(JsonUtils.parametersFromMapToString(this.parameterMap));
            buf.writeEnum(this.httpMethod);
            buf.writeEnum(this.poweredType);
            buf.writeFloat(this.timer);
            buf.writeEnum(this.timerUnit);
        }

        public static Values readBuffer(FriendlyByteBuf buf) {
            Values values = new Values();
            values.url = buf.readUtf();
            // Deserialize map from JSON string
            String paramsJson = buf.readUtf();
            if (!paramsJson.isEmpty() && !paramsJson.equals("{}")) {
                try {
                    values.parameterMap = JsonUtils.getParametersAsMap(paramsJson);
                } catch (Exception e) {
                    values.parameterMap = new HashMap<>();
                }
            }
            values.httpMethod = buf.readEnum(EnumHttpMethod.class);
            values.poweredType = buf.readEnum(EnumPoweredType.class);
            values.timer = buf.readFloat();
            values.timerUnit = buf.readEnum(EnumTimerUnit.class);
            return values;
        }

        private void updateValues(Values values) {
            this.url = values.url;
            this.parameterMap = values.parameterMap;
            this.httpMethod = values.httpMethod;
            this.poweredType = values.poweredType;
            this.timer = values.timer;
            this.timerUnit = values.timerUnit;
        }
    }
}
