package de.davidvogt.hkbmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class JumpBlock extends Block {
    public JumpBlock(Block.Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos,
                                               Player pPlayer, BlockHitResult pHitResult) {

        pLevel.playSound(pPlayer, pPos, SoundEvents.SLIME_JUMP, SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.PASS;
    }

    @Override
    public void stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity) {
        if (pEntity.onGround() && pEntity instanceof net.minecraft.world.entity.LivingEntity living) {
            knockbackEntity(living, pPos);
            playJumpSound(pLevel, pPos, living);
        }
        super.stepOn(pLevel, pPos, pState, pEntity);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, net.minecraft.world.entity.InsideBlockEffectApplier effectApplier) {
        if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
            knockbackEntity(living, pos);
            playJumpSound(level, pos, living);
        }
        super.entityInside(state, level, pos, entity, effectApplier);
    }

    private void knockbackEntity(net.minecraft.world.entity.LivingEntity entity, BlockPos pos) {
        double dx = entity.getX() - (pos.getX() + 0.5);
        double dz = entity.getZ() - (pos.getZ() + 0.5);
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length == 0) length = 1;

        double strength = 1.5;
        entity.setDeltaMovement((dx / length) * strength, 1.0, (dz / length) * strength);
        entity.hurtMarked = true;
    }

    private void playJumpSound(Level level, BlockPos pos, Entity entity) {
        level.playSound(entity instanceof Player player ? player : null, pos,
                SoundEvents.SLIME_JUMP, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Block.box(0.1, 0.1, 0.1, 15.9, 15.9, 15.9); // leicht kleiner als ein voller Block
    }
}