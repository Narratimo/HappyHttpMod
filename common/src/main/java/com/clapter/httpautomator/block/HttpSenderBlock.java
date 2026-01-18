package com.clapter.httpautomator.block;

import com.clapter.httpautomator.blockentity.HttpSenderBlockEntity;
import com.clapter.httpautomator.network.packet.CHttpSenderOpenGuiPacket;
import com.clapter.httpautomator.platform.Services;
import com.clapter.httpautomator.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

public class HttpSenderBlock extends Block implements EntityBlock {

    public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

    public HttpSenderBlock(Properties $$0) {
        super($$0);
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, Boolean.valueOf(false)));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (!level.isClientSide) {

            if(level.getBlockEntity(pos) instanceof HttpSenderBlockEntity entity) {
                if(!player.isCreative())return InteractionResult.FAIL;
                Services.PACKET_HANDLER.sendPacketToPlayer(new CHttpSenderOpenGuiPacket(pos, entity.getValues()), (ServerPlayer)player);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private void onUnpowered(BlockState state, BlockPos pos, Level level){
        if(level.getBlockEntity(pos) instanceof HttpSenderBlockEntity entity) {
            entity.onUnpowered();
        }
        level.setBlock(pos, state.cycle(LIT), 2);
    }

    private void onPowered(Level level, BlockPos pos){
        if(level.getBlockEntity(pos) instanceof HttpSenderBlockEntity entity) {
            entity.onPowered();
        }
        level.scheduleTick(pos, this, 4);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext $$0) {
        return this.defaultBlockState().setValue(LIT, Boolean.valueOf($$0.getLevel().hasNeighborSignal($$0.getClickedPos())));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block $$3, BlockPos $$4, boolean $$5) {
        if (!level.isClientSide) {
            boolean $$6 = state.getValue(LIT);
            if ($$6 != level.hasNeighborSignal(pos)) {
                if ($$6) {
                    this.onUnpowered(state, pos, level);
                } else {

                    this.onPowered(level, pos);
                }
            }
        }
    }

    @Override
    public void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
        if ($$0.getValue(LIT) && !$$1.hasNeighborSignal($$2)) {
            $$1.setBlock($$2, $$0.cycle(LIT), 2);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
        $$0.add(LIT);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos var1, BlockState var2) {
        return ModBlockEntities.httpSenderBlockEntity.get().get().create(var1, var2);
    }

}
