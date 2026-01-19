package no.eira.relay.block;

import no.eira.relay.blockentity.SceneSequencerBlockEntity;
import no.eira.relay.network.packet.CSceneSequencerOpenGuiPacket;
import no.eira.relay.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

public class SceneSequencerBlock extends Block implements EntityBlock {

    public static final MapCodec<SceneSequencerBlock> CODEC = simpleCodec(SceneSequencerBlock::new);
    public static final BooleanProperty RUNNING = BooleanProperty.create("running");
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public SceneSequencerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(RUNNING, Boolean.FALSE)
                .setValue(ACTIVE, Boolean.FALSE));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof SceneSequencerBlockEntity entity) {
                if (!player.isCreative()) return InteractionResult.FAIL;
                PacketDistributor.sendToPlayer(
                    (ServerPlayer) player,
                    new CSceneSequencerOpenGuiPacket(pos, entity.getValues())
                );
            }
        }
        return InteractionResult.SUCCESS;
    }

    private void onUnpowered(BlockState state, BlockPos pos, Level level) {
        if (level.getBlockEntity(pos) instanceof SceneSequencerBlockEntity entity) {
            entity.onRedstoneOff();
        }
    }

    private void onPowered(Level level, BlockPos pos) {
        // Spawn particles for visual feedback
        spawnSequencerParticles(level, pos);

        if (level.getBlockEntity(pos) instanceof SceneSequencerBlockEntity entity) {
            if (!entity.isRunning()) {
                entity.startSequence();
            }
        }
    }

    public void setRunning(Level level, BlockPos pos, boolean running) {
        BlockState state = level.getBlockState(pos);
        if (state.getValue(RUNNING) != running) {
            level.setBlock(pos, state.setValue(RUNNING, running), 3);
        }
    }

    public void setActive(Level level, BlockPos pos, boolean active) {
        BlockState state = level.getBlockState(pos);
        if (state.getValue(ACTIVE) != active) {
            level.setBlock(pos, state.setValue(ACTIVE, active), 3);
        }
    }

    public void spawnSequencerParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.5;

            // Purple witch particles for sequence execution
            serverLevel.sendParticles(
                    ParticleTypes.WITCH,
                    x, y, z,
                    15,           // count
                    0.3, 0.3, 0.3, // spread
                    0.02          // speed
            );
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean powered = level.hasNeighborSignal(pos);
            boolean wasRunning = state.getValue(RUNNING);

            if (powered && !wasRunning) {
                // Rising edge - start sequence
                this.onPowered(level, pos);
            } else if (!powered && wasRunning) {
                // Falling edge - handle stop behavior
                this.onUnpowered(state, pos, level);
            }
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Not used - ticking handled by block entity
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RUNNING, ACTIVE);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof SceneSequencerBlockEntity sequencer) {
                sequencer.tick();
            }
        };
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlockEntities.sceneSequencerBlockEntity.get().get().create(pos, state);
    }
}
