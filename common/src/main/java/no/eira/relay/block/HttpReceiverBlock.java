package no.eira.relay.block;

import no.eira.relay.blockentity.HttpReceiverBlockEntity;
import no.eira.relay.client.gui.HttpReceiverSettingsScreen;
import no.eira.relay.network.packet.CSyncHttpReceiverValuesPacket;
import no.eira.relay.platform.Services;
import no.eira.relay.registry.ModBlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.telemetry.events.WorldLoadEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

//MOST OVERRIDEN METHODS ARE DEPRECATED.
//PEOPLE AT FORGE FORUM SAY IT IS NOT A PROBLEM AND THESE METHODS CAN STILL BE USED
//IN OTHER INSTANCES OF BLOCKS SAFELY
public class HttpReceiverBlock extends PoweredBlock implements EntityBlock {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public HttpReceiverBlock(Properties $$0) {
        super($$0);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(POWERED, Boolean.valueOf(false))
        );
    }

    @Override
    public void onPlace(BlockState $$0, Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
        super.onPlace($$0, $$1, $$2, $$3, $$4);
    }

    public void onSignal(BlockState state, Level level, BlockPos pos){
        BlockState newState = this.switchPowered(state, level, pos);
        float pitch = newState.getValue(POWERED) ? 0.6F : 0.5F;
        level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, pitch);
        level.gameEvent(null, newState.getValue(POWERED) ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (level.isClientSide) {
           // if(level.getBlockEntity(pos) instanceof HttpReceiverBlockEntity entity){
                //if(!player.isCreative())return InteractionResult.FAIL;
                //Minecraft.getInstance().setScreen(new HttpReceiverSettingsScreen(entity));
           // }
        }else{
            if(level.getBlockEntity(pos) instanceof HttpReceiverBlockEntity entity) {
                if(!player.isCreative())return InteractionResult.FAIL;
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

    @Override
    public void onRemove(BlockState $$0, Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
        if (!$$4 && !$$0.is($$3.getBlock())) {
            if ($$0.getValue(POWERED)) {
                this.updateNeighbours($$0, $$1, $$2);
            }

            super.onRemove($$0, $$1, $$2, $$3, $$4);
        }
    }

    private void updateNeighbours(BlockState state, Level level, BlockPos pos) {
        // Notify all neighbors that this block's state changed
        level.updateNeighborsAt(pos, this);
        // Also update neighbors in all directions for redstone
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(pos.relative(direction), this);
        }
    }

    @Override
    public int getSignal(BlockState $$0, BlockGetter $$1, BlockPos $$2, Direction $$3) {
        return $$0.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
        $$0.add(POWERED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos var1, BlockState var2) {
        return ModBlockEntities.httpReceiverBlockEntity.get().get().create(var1, var2);
    }
}
