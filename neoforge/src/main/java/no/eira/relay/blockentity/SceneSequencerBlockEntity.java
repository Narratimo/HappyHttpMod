package no.eira.relay.blockentity;

import no.eira.relay.CommonClass;
import no.eira.relay.Constants;
import no.eira.relay.block.SceneSequencerBlock;
import no.eira.relay.enums.EnumAuthType;
import no.eira.relay.enums.EnumHttpMethod;
import no.eira.relay.enums.EnumStepCondition;
import no.eira.relay.enums.EnumStopBehavior;
import no.eira.relay.registry.ModBlockEntities;
import no.eira.relay.utils.JsonUtils;
import no.eira.relay.utils.NBTConverter;
import no.eira.relay.utils.QueryBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SceneSequencerBlockEntity extends BlockEntity {

    private final Values values;

    // Execution state
    private boolean isRunning;
    private int currentStepIndex;
    private int ticksUntilNextStep;
    private int lastStepStatusCode;
    private boolean lastStepSuccess;

    // Active state for visual feedback
    private boolean isActive;
    private long activeStartTick;
    private static final int ACTIVE_DURATION_TICKS = 10;

    public SceneSequencerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.sceneSequencerBlockEntity.get().get(), pos, state);
        this.values = new Values();
        this.isRunning = false;
        this.currentStepIndex = 0;
        this.ticksUntilNextStep = 0;
        this.lastStepStatusCode = 0;
        this.lastStepSuccess = true;
    }

    public void startSequence() {
        if (values.steps.isEmpty()) return;

        this.isRunning = true;
        this.currentStepIndex = 0;
        this.lastStepSuccess = true;

        // Get delay for first step
        SequenceStep firstStep = values.steps.get(0);
        this.ticksUntilNextStep = firstStep.delayTicks;

        updateBlockRunningState(true);
        Constants.LOG.debug("Scene Sequencer started at {}", getBlockPos());
    }

    public void stopSequence() {
        this.isRunning = false;
        updateBlockRunningState(false);
        Constants.LOG.debug("Scene Sequencer stopped at {}", getBlockPos());
    }

    public void resetSequence() {
        stopSequence();
        this.currentStepIndex = 0;
        this.ticksUntilNextStep = 0;
    }

    public void onRedstoneOff() {
        switch (values.stopBehavior) {
            case STOP -> stopSequence();
            case RESET -> resetSequence();
            case IGNORE -> {} // Do nothing
        }
    }

    public void tick() {
        if (this.level == null || this.level.isClientSide) return;

        // Handle active state timeout
        if (isActive && this.level.getGameTime() - activeStartTick > ACTIVE_DURATION_TICKS) {
            setActiveState(false);
        }

        if (!isRunning) return;
        if (values.steps.isEmpty()) {
            stopSequence();
            return;
        }

        // Countdown to next step
        if (ticksUntilNextStep > 0) {
            ticksUntilNextStep--;
            return;
        }

        // Execute current step
        executeCurrentStep();
    }

    private void executeCurrentStep() {
        if (currentStepIndex >= values.steps.size()) {
            // Sequence complete
            if (values.looping) {
                currentStepIndex = 0;
                if (!values.steps.isEmpty()) {
                    ticksUntilNextStep = values.steps.get(0).delayTicks;
                }
            } else {
                stopSequence();
            }
            return;
        }

        SequenceStep step = values.steps.get(currentStepIndex);

        // Check condition
        boolean shouldExecute = switch (step.condition) {
            case ALWAYS -> true;
            case ON_SUCCESS -> lastStepSuccess;
            case ON_FAILURE -> !lastStepSuccess;
        };

        if (shouldExecute && !step.url.isEmpty()) {
            setActiveState(true);
            sendStepRequest(step);
        } else if (!shouldExecute) {
            // Skip this step due to condition
            Constants.LOG.debug("Scene Sequencer skipping step {} due to condition", currentStepIndex);
            advanceToNextStep();
        } else {
            // Empty URL, just advance
            advanceToNextStep();
        }
    }

    private void sendStepRequest(SequenceStep step) {
        Map<String, String> headers = buildAuthHeaders(step);

        CompletableFuture<String> future;
        if (step.httpMethod == EnumHttpMethod.GET) {
            String params = QueryBuilder.paramsToQueryString(step.parameters);
            future = CommonClass.HTTP_CLIENT.sendGetWithRetry(step.url, params, headers);
        } else {
            String params = JsonUtils.parametersFromMapToString(step.parameters);
            future = CommonClass.HTTP_CLIENT.sendPostWithRetry(step.url, params, headers);
        }

        final int stepIndex = currentStepIndex;
        future.thenAccept(response -> {
            // This runs on a different thread, so we need to be careful
            // For now, assume success if we got any response
            lastStepSuccess = response != null && !response.isEmpty();
            lastStepStatusCode = lastStepSuccess ? 200 : 0;
            Constants.LOG.debug("Scene Sequencer step {} completed: success={}", stepIndex, lastStepSuccess);

            // Schedule advancement on next tick
            if (level != null && !level.isClientSide) {
                advanceToNextStep();
            }
        }).exceptionally(ex -> {
            lastStepSuccess = false;
            lastStepStatusCode = 0;
            Constants.LOG.warn("Scene Sequencer step {} failed: {}", stepIndex, ex.getMessage());
            if (level != null && !level.isClientSide) {
                advanceToNextStep();
            }
            return null;
        });
    }

    private void advanceToNextStep() {
        currentStepIndex++;

        if (currentStepIndex >= values.steps.size()) {
            if (values.looping) {
                currentStepIndex = 0;
                if (!values.steps.isEmpty()) {
                    ticksUntilNextStep = values.steps.get(0).delayTicks;
                }
            } else {
                stopSequence();
            }
        } else {
            ticksUntilNextStep = values.steps.get(currentStepIndex).delayTicks;
        }
    }

    private Map<String, String> buildAuthHeaders(SequenceStep step) {
        Map<String, String> headers = new HashMap<>();
        switch (step.authType) {
            case BEARER -> {
                if (!step.authValue.isEmpty()) {
                    headers.put("Authorization", "Bearer " + step.authValue);
                }
            }
            case BASIC -> {
                if (!step.authValue.isEmpty()) {
                    String encoded = Base64.getEncoder().encodeToString(
                        step.authValue.getBytes(StandardCharsets.UTF_8));
                    headers.put("Authorization", "Basic " + encoded);
                }
            }
            case CUSTOM_HEADER -> {
                if (!step.customHeaderName.isEmpty()) {
                    headers.put(step.customHeaderName, step.customHeaderValue);
                }
            }
        }
        return headers;
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
            if (state.getBlock() instanceof SceneSequencerBlock sequencer) {
                sequencer.setActive(this.level, this.getBlockPos(), active);
            }
        }
    }

    private void updateBlockRunningState(boolean running) {
        if (this.level != null && !this.level.isClientSide) {
            BlockState state = this.level.getBlockState(this.getBlockPos());
            if (state.getBlock() instanceof SceneSequencerBlock sequencer) {
                sequencer.setRunning(this.level, this.getBlockPos(), running);
            }
        }
    }

    public void updateValues(Values values) {
        this.values.updateValues(values);
        setChanged();
    }

    public Values getValues() {
        return this.values;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getCurrentStepIndex() {
        return currentStepIndex;
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        CompoundTag compound = nbt.getCompound(Constants.MOD_ID);

        // Load steps
        ListTag stepsTag = compound.getList("steps", Tag.TAG_COMPOUND);
        this.values.steps.clear();
        for (int i = 0; i < stepsTag.size(); i++) {
            CompoundTag stepTag = stepsTag.getCompound(i);
            SequenceStep step = new SequenceStep();
            step.url = stepTag.getString("url");
            step.httpMethod = EnumHttpMethod.getByName(stepTag.getString("httpMethod"));
            step.parameters = NBTConverter.convertNBTToMap(
                stepTag.getCompound("parameters"),
                String::valueOf,
                String::valueOf
            );
            step.delayTicks = stepTag.getInt("delayTicks");
            step.condition = EnumStepCondition.getById(stepTag.getInt("condition"));
            step.authType = EnumAuthType.getById(stepTag.getInt("authType"));
            step.authValue = stepTag.getString("authValue");
            step.customHeaderName = stepTag.getString("customHeaderName");
            step.customHeaderValue = stepTag.getString("customHeaderValue");
            this.values.steps.add(step);
        }

        this.values.stopBehavior = EnumStopBehavior.getById(compound.getInt("stopBehavior"));
        this.values.looping = compound.getBoolean("looping");
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        CompoundTag compound = new CompoundTag();

        // Save steps
        ListTag stepsTag = new ListTag();
        for (SequenceStep step : this.values.steps) {
            CompoundTag stepTag = new CompoundTag();
            stepTag.putString("url", step.url);
            stepTag.putString("httpMethod", step.httpMethod.toString());
            stepTag.put("parameters", NBTConverter.convertMapToNBT(
                step.parameters,
                String::valueOf,
                String::valueOf
            ));
            stepTag.putInt("delayTicks", step.delayTicks);
            stepTag.putInt("condition", step.condition.getId());
            stepTag.putInt("authType", step.authType.getId());
            stepTag.putString("authValue", step.authValue);
            stepTag.putString("customHeaderName", step.customHeaderName);
            stepTag.putString("customHeaderValue", step.customHeaderValue);
            stepsTag.add(stepTag);
        }
        compound.put("steps", stepsTag);

        compound.putInt("stopBehavior", this.values.stopBehavior.getId());
        compound.putBoolean("looping", this.values.looping);
        nbt.put(Constants.MOD_ID, compound);
    }

    /**
     * Represents a single step in the sequence.
     */
    public static class SequenceStep {
        public String url = "";
        public EnumHttpMethod httpMethod = EnumHttpMethod.POST;
        public Map<String, String> parameters = new HashMap<>();
        public int delayTicks = 20; // 1 second default delay before this step
        public EnumStepCondition condition = EnumStepCondition.ALWAYS;
        public EnumAuthType authType = EnumAuthType.NONE;
        public String authValue = "";
        public String customHeaderName = "";
        public String customHeaderValue = "";

        public void writeToBuffer(FriendlyByteBuf buf) {
            buf.writeUtf(this.url);
            buf.writeEnum(this.httpMethod);
            buf.writeUtf(JsonUtils.parametersFromMapToString(this.parameters));
            buf.writeInt(this.delayTicks);
            buf.writeEnum(this.condition);
            buf.writeEnum(this.authType);
            buf.writeUtf(this.authValue);
            buf.writeUtf(this.customHeaderName);
            buf.writeUtf(this.customHeaderValue);
        }

        public static SequenceStep readFromBuffer(FriendlyByteBuf buf) {
            SequenceStep step = new SequenceStep();
            step.url = buf.readUtf();
            step.httpMethod = buf.readEnum(EnumHttpMethod.class);
            String paramsJson = buf.readUtf();
            if (!paramsJson.isEmpty() && !paramsJson.equals("{}")) {
                try {
                    step.parameters = JsonUtils.getParametersAsMap(paramsJson);
                } catch (Exception e) {
                    step.parameters = new HashMap<>();
                }
            }
            step.delayTicks = buf.readInt();
            step.condition = buf.readEnum(EnumStepCondition.class);
            step.authType = buf.readEnum(EnumAuthType.class);
            step.authValue = buf.readUtf();
            step.customHeaderName = buf.readUtf();
            step.customHeaderValue = buf.readUtf();
            return step;
        }

        public SequenceStep copy() {
            SequenceStep copy = new SequenceStep();
            copy.url = this.url;
            copy.httpMethod = this.httpMethod;
            copy.parameters = new HashMap<>(this.parameters);
            copy.delayTicks = this.delayTicks;
            copy.condition = this.condition;
            copy.authType = this.authType;
            copy.authValue = this.authValue;
            copy.customHeaderName = this.customHeaderName;
            copy.customHeaderValue = this.customHeaderValue;
            return copy;
        }
    }

    /**
     * Configuration values for the sequencer.
     */
    public static class Values {
        public List<SequenceStep> steps = new ArrayList<>();
        public EnumStopBehavior stopBehavior = EnumStopBehavior.IGNORE;
        public boolean looping = false;

        public void writeValues(FriendlyByteBuf buf) {
            buf.writeInt(this.steps.size());
            for (SequenceStep step : this.steps) {
                step.writeToBuffer(buf);
            }
            buf.writeEnum(this.stopBehavior);
            buf.writeBoolean(this.looping);
        }

        public static Values readBuffer(FriendlyByteBuf buf) {
            Values values = new Values();
            int stepCount = buf.readInt();
            for (int i = 0; i < stepCount; i++) {
                values.steps.add(SequenceStep.readFromBuffer(buf));
            }
            values.stopBehavior = buf.readEnum(EnumStopBehavior.class);
            values.looping = buf.readBoolean();
            return values;
        }

        private void updateValues(Values values) {
            this.steps.clear();
            for (SequenceStep step : values.steps) {
                this.steps.add(step.copy());
            }
            this.stopBehavior = values.stopBehavior;
            this.looping = values.looping;
        }
    }
}
