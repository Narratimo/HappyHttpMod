package no.eira.relay.blockentity;

import no.eira.relay.CommonClass;
import no.eira.relay.Constants;
import no.eira.relay.enums.EnumAuthType;
import no.eira.relay.enums.EnumHttpMethod;
import no.eira.relay.enums.EnumPoweredType;
import no.eira.relay.enums.EnumTimerUnit;
import no.eira.relay.registry.ModBlockEntities;
import no.eira.relay.utils.JsonUtils;
import no.eira.relay.utils.NBTConverter;
import no.eira.relay.utils.QueryBuilder;
import no.eira.relay.variables.JsonPathExtractor;
import no.eira.relay.variables.ResponseCapture;
import no.eira.relay.variables.VariableStorage;
import no.eira.relay.variables.VariableSubstitutor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import no.eira.relay.block.HttpSenderBlock;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpSenderBlockEntity extends BlockEntity {

    private long lastSentMilli;
    private long lastSentTick;
    private boolean inCooldown;
    private final Values values;

    // Active state tracking for visual feedback
    private boolean isActive;
    private long activeStartTick;
    private static final int ACTIVE_DURATION_TICKS = 20; // 1 second for network request visualization

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
            // Set active state when starting request
            setActiveState(true);

            // Get server level for variable substitution
            ServerLevel serverLevel = (this.level instanceof ServerLevel) ? (ServerLevel) this.level : null;

            // Apply variable substitution to URL
            String resolvedUrl = VariableSubstitutor.substitute(this.values.url, this.getBlockPos(), serverLevel);

            // Apply variable substitution to parameters
            Map<String, String> resolvedParams = VariableSubstitutor.substituteMap(
                this.values.parameterMap, this.getBlockPos(), serverLevel);

            Map<String, String> headers = buildAuthHeaders();
            long startTime = System.currentTimeMillis();

            if (this.values.httpMethod.equals(EnumHttpMethod.GET)) {
                String params = QueryBuilder.paramsToQueryString(resolvedParams);
                // Use async retry method for reliability
                CommonClass.HTTP_CLIENT.sendGetWithRetry(resolvedUrl, params, headers)
                    .thenAccept(response -> handleResponse(response, startTime));
            }
            if (this.values.httpMethod.equals(EnumHttpMethod.POST)) {
                String params = JsonUtils.parametersFromMapToString(resolvedParams);
                // Use async retry method for reliability
                CommonClass.HTTP_CLIENT.sendPostWithRetry(resolvedUrl, params, headers)
                    .thenAccept(response -> handleResponse(response, startTime));
            }
        }
    }

    private void handleResponse(String response, long startTime) {
        long responseTime = System.currentTimeMillis() - startTime;
        int statusCode = (response != null && !response.isEmpty()) ? 200 : 0;

        // Store last response info
        VariableStorage.getInstance().setLastResponse(
            this.getBlockPos(), statusCode, response, responseTime);

        if (response == null || response.isEmpty()) {
            Constants.LOG.debug("HTTP Sender to {} completed with empty response", this.values.url);
            return;
        }

        Constants.LOG.debug("HTTP Sender to {} completed in {}ms", this.values.url, responseTime);

        // Process response captures if enabled
        if (this.values.captureResponse && !this.values.responseCaptures.isEmpty()) {
            processResponseCaptures(response);
        }
    }

    private void processResponseCaptures(String response) {
        VariableStorage storage = VariableStorage.getInstance();

        for (ResponseCapture capture : this.values.responseCaptures) {
            if (!capture.isValid()) continue;

            String value = JsonPathExtractor.extract(response, capture.getJsonPath(), capture.getDefaultValue());

            switch (capture.getScope()) {
                case GLOBAL -> storage.setGlobal(capture.getVariableName(), value);
                case BLOCK -> storage.setBlock(this.getBlockPos(), capture.getVariableName(), value);
                case PLAYER -> {
                    // Player scope requires player context - for now, store as block scope
                    // Full player support will come with Player-Specific Triggers feature
                    storage.setBlock(this.getBlockPos(), capture.getVariableName(), value);
                }
            }

            Constants.LOG.debug("Captured variable '{}' = '{}' from path '{}'",
                capture.getVariableName(),
                value.length() > 50 ? value.substring(0, 50) + "..." : value,
                capture.getJsonPath());
        }
    }

    private Map<String, String> buildAuthHeaders() {
        Map<String, String> headers = new HashMap<>();
        switch (this.values.authType) {
            case BEARER -> {
                if (!this.values.authValue.isEmpty()) {
                    headers.put("Authorization", "Bearer " + this.values.authValue);
                }
            }
            case BASIC -> {
                if (!this.values.authValue.isEmpty()) {
                    String encoded = Base64.getEncoder().encodeToString(
                        this.values.authValue.getBytes(StandardCharsets.UTF_8));
                    headers.put("Authorization", "Basic " + encoded);
                }
            }
            case CUSTOM_HEADER -> {
                if (!this.values.customHeaderName.isEmpty()) {
                    headers.put(this.values.customHeaderName, this.values.customHeaderValue);
                }
            }
        }
        return headers;
    }

    private void startCooldown() {
        lastSentMilli = System.currentTimeMillis();
        if (this.level != null) {
            lastSentTick = this.level.getGameTime();
        }
        inCooldown = true;
    }

    public void setActiveState(boolean active) {
        this.isActive = active;
        if (active && this.level != null) {
            this.activeStartTick = this.level.getGameTime();
        }
        updateBlockActiveState(active);
    }

    private void updateBlockActiveState(boolean active) {
        if (this.level != null && !this.level.isClientSide) {
            BlockState state = this.level.getBlockState(this.getBlockPos());
            if (state.getBlock() instanceof HttpSenderBlock sender) {
                sender.setActive(this.level, this.getBlockPos(), active);
            }
        }
    }

    public void tick() {
        if (this.level == null || this.level.isClientSide) return;

        // Handle active state timeout
        if (isActive && this.level.getGameTime() - activeStartTick > ACTIVE_DURATION_TICKS) {
            setActiveState(false);
        }

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
        this.values.authType = EnumAuthType.getById(compound.getInt("authType"));
        this.values.authValue = compound.getString("authValue");
        this.values.customHeaderName = compound.getString("customHeaderName");
        this.values.customHeaderValue = compound.getString("customHeaderValue");
        // Response capture settings
        this.values.captureResponse = compound.getBoolean("captureResponse");
        this.values.responseCaptures = new ArrayList<>();
        if (compound.contains("responseCaptures", Tag.TAG_LIST)) {
            ListTag capturesList = compound.getList("responseCaptures", Tag.TAG_COMPOUND);
            for (int i = 0; i < capturesList.size(); i++) {
                this.values.responseCaptures.add(ResponseCapture.fromNBT(capturesList.getCompound(i)));
            }
        }
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
        compound.putInt("authType", this.values.authType.getId());
        compound.putString("authValue", this.values.authValue);
        compound.putString("customHeaderName", this.values.customHeaderName);
        compound.putString("customHeaderValue", this.values.customHeaderValue);
        // Response capture settings
        compound.putBoolean("captureResponse", this.values.captureResponse);
        ListTag capturesList = new ListTag();
        for (ResponseCapture capture : this.values.responseCaptures) {
            capturesList.add(capture.toNBT());
        }
        compound.put("responseCaptures", capturesList);
        nbt.put(Constants.MOD_ID, compound);
    }

    public static class Values {

        public String url = "";
        public Map<String, String> parameterMap = new HashMap<>();
        public EnumHttpMethod httpMethod;
        public EnumPoweredType poweredType = EnumPoweredType.SWITCH;
        public float timer = 20; // Default 20 ticks = 1 second
        public EnumTimerUnit timerUnit = EnumTimerUnit.TICKS;
        public EnumAuthType authType = EnumAuthType.NONE;
        public String authValue = "";
        public String customHeaderName = "";
        public String customHeaderValue = "";

        // Response capture settings
        public boolean captureResponse = false;
        public List<ResponseCapture> responseCaptures = new ArrayList<>();

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
            buf.writeEnum(this.authType);
            buf.writeUtf(this.authValue);
            buf.writeUtf(this.customHeaderName);
            buf.writeUtf(this.customHeaderValue);
            // Response capture settings
            buf.writeBoolean(this.captureResponse);
            buf.writeInt(this.responseCaptures.size());
            for (ResponseCapture capture : this.responseCaptures) {
                capture.writeToBuffer(buf);
            }
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
            values.authType = buf.readEnum(EnumAuthType.class);
            values.authValue = buf.readUtf();
            values.customHeaderName = buf.readUtf();
            values.customHeaderValue = buf.readUtf();
            // Response capture settings
            values.captureResponse = buf.readBoolean();
            int captureCount = buf.readInt();
            values.responseCaptures = new ArrayList<>();
            for (int i = 0; i < captureCount; i++) {
                values.responseCaptures.add(ResponseCapture.readFromBuffer(buf));
            }
            return values;
        }

        private void updateValues(Values values) {
            this.url = values.url;
            this.parameterMap = values.parameterMap;
            this.httpMethod = values.httpMethod;
            this.poweredType = values.poweredType;
            this.timer = values.timer;
            this.timerUnit = values.timerUnit;
            this.authType = values.authType;
            this.authValue = values.authValue;
            this.customHeaderName = values.customHeaderName;
            this.customHeaderValue = values.customHeaderValue;
            this.captureResponse = values.captureResponse;
            this.responseCaptures = new ArrayList<>();
            for (ResponseCapture capture : values.responseCaptures) {
                this.responseCaptures.add(capture.copy());
            }
        }
    }
}
