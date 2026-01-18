package no.eira.relay.block;

import no.eira.relay.blockentity.HttpSenderBlockEntity;
import no.eira.relay.network.packet.CHttpSenderOpenGuiPacket;
import no.eira.relay.platform.Services;
import no.eira.relay.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

public class HttpSenderBlock extends Block implements EntityBlock {

    public static final MapCodec<HttpSenderBlock> CODEC = simpleCodec(HttpSenderBlock::new);
    public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

    public HttpSenderBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, Boolean.FALSE));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof HttpSenderBlockEntity entity) {
                if (!player.isCreative()) return InteractionResult.FAIL;
                Services.PACKET_HANDLER.sendPacketToPlayer(
                    new CHttpSenderOpenGuiPacket(pos, entity.getValues()),
                    (ServerPlayer) player
                );
            }
        }
        return InteractionResult.SUCCESS;
    }

    private void onUnpowered(BlockState state, BlockPos pos, Level level) {
        if (level.getBlockEntity(pos) instanceof HttpSenderBlockEntity entity) {
            entity.onUnpowered();
        }
        level.setBlock(pos, state.cycle(LIT), 2);
    }

    private void onPowered(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof HttpSenderBlockEntity entity) {
            entity.onPowered();
        }
        level.scheduleTick(pos, this, 4);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(LIT, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean lit = state.getValue(LIT);
            if (lit != level.hasNeighborSignal(pos)) {
                if (lit) {
                    this.onUnpowered(state, pos, level);
                } else {
                    this.onPowered(level, pos);
                }
            }
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT) && !level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.cycle(LIT), 2);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof HttpSenderBlockEntity sender) {
                sender.tick();
            }
        };
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlockEntities.httpSenderBlockEntity.get().get().create(pos, state);
    }
}
