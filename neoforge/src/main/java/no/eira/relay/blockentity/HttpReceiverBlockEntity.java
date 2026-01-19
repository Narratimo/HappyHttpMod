package no.eira.relay.blockentity;

import no.eira.relay.Constants;
import no.eira.relay.block.HttpReceiverBlock;
import no.eira.relay.enums.EnumPoweredType;
import no.eira.relay.enums.EnumTimerUnit;
import no.eira.relay.http.handlers.HttpReceiverBlockHandler;
import no.eira.relay.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class HttpReceiverBlockEntity extends BlockEntity {

    private long lastPoweredMilli;
    private long lastPoweredTick;
    private final Values values;
    private boolean isPowerOn;

    // Active state tracking for visual feedback
    private boolean isActive;
    private long activeStartTick;
    private static final int ACTIVE_DURATION_TICKS = 10; // Half second visual feedback

    public HttpReceiverBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.httpReceiverBlockEntity.get().get(), pos, state);
        this.values = new Values();
    }

    public void onSignal() {
        switch (values.poweredType) {
            case SWITCH -> this.switchPowered();
            case TIMER -> this.startTimer();
        }
    }

    private void startTimer() {
        lastPoweredMilli = System.currentTimeMillis();
        if (this.level != null) {
            lastPoweredTick = this.level.getGameTime();
        }
    }

    private void switchPowered() {
        BlockState state = this.level.getBlockState(this.getBlockPos());
        Block block = state.getBlock();
        if (block instanceof HttpReceiverBlock receiver) {
            receiver.switchSignal(state, this.level, this.getBlockPos());
            isPowerOn = state.getValue(HttpReceiverBlock.POWERED);
        }
    }

    private void setBlockPowered(boolean powered) {
        BlockState state = this.level.getBlockState(this.getBlockPos());
        Block block = state.getBlock();
        if (block instanceof HttpReceiverBlock receiver) {
            receiver.setPowered(state, this.level, this.getBlockPos(), powered);
            isPowerOn = powered;
        }
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
            if (state.getBlock() instanceof HttpReceiverBlock receiver) {
                receiver.setActive(state, this.level, this.getBlockPos(), active);
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

        switch (this.values.timerUnit) {
            case SECONDS -> {
                if (isTimeUnder(System.currentTimeMillis(), this.lastPoweredMilli, (long)(this.values.timer * 1000))) {
                    if (!isPowerOn) setBlockPowered(true);
                } else {
                    if (isPowerOn) setBlockPowered(false);
                }
            }
            case TICKS -> {
                if (isTimeUnder(this.level.getGameTime(), this.lastPoweredTick, (long)this.values.timer)) {
                    if (!isPowerOn) setBlockPowered(true);
                } else {
                    if (isPowerOn) setBlockPowered(false);
                }
            }
        }
    }

    private boolean isTimeUnder(long current, long last, long value) {
        return current - last <= value;
    }

    public void updateValues(Values values) {
        this.values.updateValues(values);
        // Turn block off when power set to timer
        if (values.poweredType.equals(EnumPoweredType.TIMER)) {
            this.setBlockPowered(false);
        }
        setChanged();
        if (!this.getLevel().isClientSide) {
            HttpReceiverBlockHandler.create(this, this.values.url, this.values.secretToken);
        }
    }

    private void postLoad() {
        HttpReceiverBlockHandler.create(this, this.values.url, this.values.secretToken);
    }

    public Values getValues() {
        return this.values;
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        CompoundTag compound = nbt.getCompound(Constants.MOD_ID);
        this.values.url = compound.getString("url");
        this.values.poweredType = EnumPoweredType.getById(compound.getInt("poweredType"));
        this.values.timerUnit = EnumTimerUnit.getById(compound.getInt("timerUnit"));
        this.values.timer = compound.getFloat("timer");
        this.values.secretToken = compound.getString("secretToken");
        this.postLoad();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        CompoundTag compound = new CompoundTag();
        compound.putString("url", this.values.url);
        compound.putInt("poweredType", this.values.poweredType.getId());
        compound.putInt("timerUnit", this.values.timerUnit.getId());
        compound.putFloat("timer", this.values.timer);
        compound.putString("secretToken", this.values.secretToken);
        nbt.put(Constants.MOD_ID, compound);
    }

    public static class Values {

        public String url = "";
        public EnumPoweredType poweredType = EnumPoweredType.SWITCH;
        public float timer = 20; // Default 20 ticks = 1 second
        public EnumTimerUnit timerUnit = EnumTimerUnit.TICKS;
        public String secretToken = "";

        public void writeValues(FriendlyByteBuf buf) {
            buf.writeUtf(this.url);
            buf.writeEnum(this.poweredType);
            buf.writeFloat(this.timer);
            buf.writeEnum(this.timerUnit);
            buf.writeUtf(this.secretToken);
        }

        public static Values readBuffer(FriendlyByteBuf buf) {
            Values values = new Values();
            values.url = buf.readUtf();
            values.poweredType = buf.readEnum(EnumPoweredType.class);
            values.timer = buf.readFloat();
            values.timerUnit = buf.readEnum(EnumTimerUnit.class);
            values.secretToken = buf.readUtf();
            return values;
        }

        private void updateValues(Values values) {
            this.url = values.url;
            this.poweredType = values.poweredType;
            this.timer = values.timer;
            this.timerUnit = values.timerUnit;
            this.secretToken = values.secretToken;
        }
    }
}
