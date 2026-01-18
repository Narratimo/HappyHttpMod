package no.eira.relay.block;

import no.eira.relay.CommonClass;
import no.eira.relay.blockentity.HttpReceiverBlockEntity;
import no.eira.relay.network.packet.CSyncHttpReceiverValuesPacket;
import no.eira.relay.platform.Services;
import no.eira.relay.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class HttpReceiverBlock extends Block implements EntityBlock {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public HttpReceiverBlock(Properties props) {
        super(props);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(POWERED, Boolean.valueOf(false))
        );
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    public void onSignal(BlockState state, Level level, BlockPos pos){
        BlockState newState = this.switchPowered(state, level, pos);
        float pitch = newState.getValue(POWERED) ? 0.6F : 0.5F;
        level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, pitch);
        level.gameEvent(null, newState.getValue(POWERED) ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            if(level.getBlockEntity(pos) instanceof HttpReceiverBlockEntity entity) {
                if(!player.isCreative()) return InteractionResult.FAIL;
                Services.PACKET_HANDLER.sendPacketToPlayer(new CSyncHttpReceiverValuesPacket(pos, entity.getValues()), (ServerPlayer)player);
            }
        }
        return InteractionResult.SUCCESS;
    }

    public BlockState switchPowered(BlockState state, Level level, BlockPos pos) {
        state = state.cycle(POWERED);
        level.setBlock(pos, state, 3);
        this.updateNeighbours(state, level, pos);
        return state;
    }

    public void switchSignal(BlockState state, Level level, BlockPos pos) {
        BlockState newState = this.switchPowered(state, level, pos);
        float pitch = newState.getValue(POWERED) ? 0.6F : 0.5F;
        level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, pitch);
    }

    public void setPowered(BlockState state, Level level, BlockPos pos, boolean powered) {
        state = state.setValue(POWERED, powered);
        level.setBlock(pos, state, 3);
        this.updateNeighbours(state, level, pos);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof HttpReceiverBlockEntity receiver) {
                receiver.tick();
            }
        };
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!movedByPiston && !state.is(newState.getBlock())) {
            // Unregister HTTP handler when block is removed
            if (!level.isClientSide) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof HttpReceiverBlockEntity receiver) {
                    String url = receiver.getValues().url;
                    if (url != null && !url.isEmpty()) {
                        CommonClass.HTTP_SERVER.unregisterHandler(url);
                    }
                }
            }

            if (state.getValue(POWERED)) {
                this.updateNeighbours(state, level, pos);
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    private void updateNeighbours(BlockState state, Level level, BlockPos pos) {
        level.updateNeighborsAt(pos, this);
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(pos.relative(direction), this);
        }
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlockEntities.httpReceiverBlockEntity.get().get().create(pos, state);
    }
}
