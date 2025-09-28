package de.davidvogt.hkbmod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

public class ServerGrowthEffectManager {
    private static ServerGrowthEffectManager instance;

    public static ServerGrowthEffectManager getInstance() {
        if (instance == null) {
            instance = new ServerGrowthEffectManager();
        }
        return instance;
    }

    public void triggerGrowthEffect(ServerLevel level, BlockPos pos, Player player) {
        BlockState blockState = level.getBlockState(pos);
        Block block = blockState.getBlock();

        boolean didGrow = false;

        // Handle different types of growable blocks - make them FULLY grown
        if (isGrowableBlock(block)) {
            didGrow = fullyGrowPlant(level, pos, blockState, block);
        }
        // Handle saplings
        else if (block instanceof SaplingBlock saplingBlock) {
            didGrow = growSapling(level, pos, saplingBlock, blockState);
        }
        // Handle farmland with seeds on top
        else if (block instanceof FarmBlock) {
            BlockPos abovePos = pos.above();
            BlockState aboveState = level.getBlockState(abovePos);
            Block aboveBlock = aboveState.getBlock();

            if (isGrowableBlock(aboveBlock)) {
                didGrow = fullyGrowPlant(level, abovePos, aboveState, aboveBlock);
                pos = abovePos; // Update position for particles
            }
        }

        if (didGrow) {
            // Play success sound and show particles
            level.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            showGrowthParticles(level, pos);
        }
    }

    private boolean isGrowableBlock(Block block) {
        return block instanceof CropBlock ||
               block instanceof StemBlock ||
               block instanceof BambooStalkBlock ||
               block instanceof SugarCaneBlock ||
               block instanceof CactusBlock ||
               block instanceof ChorusPlantBlock ||
               block instanceof NetherWartBlock ||
               block instanceof CocoaBlock ||
               block instanceof SweetBerryBushBlock;
    }

    private boolean fullyGrowPlant(Level level, BlockPos pos, BlockState blockState, Block block) {
        if (block instanceof CropBlock cropBlock) {
            if (!cropBlock.isMaxAge(blockState)) {
                BlockState maxAgeState = cropBlock.getStateForAge(cropBlock.getMaxAge());
                level.setBlock(pos, maxAgeState, 2);
                return true;
            }
        }
        else if (block instanceof StemBlock stemBlock) {
            int currentAge = blockState.getValue(StemBlock.AGE);
            int maxAge = 7;

            if (currentAge < maxAge) {
                BlockState maxAgeState = blockState.setValue(StemBlock.AGE, maxAge);
                level.setBlock(pos, maxAgeState, 2);
                return true;
            }
        }
        else if (block instanceof NetherWartBlock netherWartBlock) {
            int currentAge = blockState.getValue(NetherWartBlock.AGE);
            if (currentAge < 3) {
                BlockState maxAgeState = blockState.setValue(NetherWartBlock.AGE, 3);
                level.setBlock(pos, maxAgeState, 2);
                return true;
            }
        }
        else if (block instanceof CocoaBlock cocoaBlock) {
            int currentAge = blockState.getValue(CocoaBlock.AGE);
            if (currentAge < 2) {
                BlockState maxAgeState = blockState.setValue(CocoaBlock.AGE, 2);
                level.setBlock(pos, maxAgeState, 2);
                return true;
            }
        }
        else if (block instanceof SweetBerryBushBlock sweetBerryBush) {
            int currentAge = blockState.getValue(SweetBerryBushBlock.AGE);
            if (currentAge < 3) {
                BlockState maxAgeState = blockState.setValue(SweetBerryBushBlock.AGE, 3);
                level.setBlock(pos, maxAgeState, 2);
                return true;
            }
        }
        else if (block instanceof BambooStalkBlock ||
                 block instanceof SugarCaneBlock ||
                 block instanceof CactusBlock) {
            return growTallPlantToMax(level, pos, block);
        }
        else {
            // Generic growth attempt using bonemeal behavior - repeated until max
            if (block instanceof BonemealableBlock bonemealable) {
                boolean grew = false;
                for (int i = 0; i < 10; i++) { // Try up to 10 times to fully grow
                    if (bonemealable.isValidBonemealTarget(level, pos, blockState) &&
                        bonemealable.isBonemealSuccess(level, level.random, pos, blockState)) {
                        bonemealable.performBonemeal((ServerLevel) level, level.random, pos, blockState);
                        blockState = level.getBlockState(pos); // Update state for next iteration
                        grew = true;
                    } else {
                        break; // Can't grow further
                    }
                }
                return grew;
            }
        }

        return false;
    }

    private boolean growTallPlantToMax(Level level, BlockPos pos, Block block) {
        // Grow bamboo, sugar cane, or cactus to maximum height
        int maxHeight = (block instanceof BambooStalkBlock) ? 12 : 3;
        int currentHeight = 1;

        // Count current height
        BlockPos checkPos = pos.below();
        while (level.getBlockState(checkPos).getBlock() == block && currentHeight < 16) {
            currentHeight++;
            checkPos = checkPos.below();
        }

        // Grow upward to max height
        boolean grew = false;
        BlockPos growPos = pos.above();
        while (currentHeight < maxHeight && level.isEmptyBlock(growPos)) {
            level.setBlock(growPos, block.defaultBlockState(), 2);
            growPos = growPos.above();
            currentHeight++;
            grew = true;
        }

        return grew;
    }

    private boolean growSapling(Level level, BlockPos pos, SaplingBlock saplingBlock, BlockState blockState) {
        // Force sapling to grow into a tree
        if (saplingBlock.isValidBonemealTarget(level, pos, blockState)) {
            // Try multiple times to ensure tree growth
            for (int i = 0; i < 5; i++) {
                if (saplingBlock.isBonemealSuccess(level, level.random, pos, blockState)) {
                    saplingBlock.performBonemeal((ServerLevel) level, level.random, pos, blockState);
                    return true;
                }
            }
        }
        return false;
    }

    private void showGrowthParticles(ServerLevel level, BlockPos pos) {
        // Create enhanced particle effects
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        // Green sparkle particles
        for (int i = 0; i < 30; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 2.0;
            double offsetY = level.random.nextDouble() * 1.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 2.0;

            level.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                x + offsetX, y + offsetY, z + offsetZ,
                1, 0.0, 0.1, 0.0, 0.0
            );
        }

        // Additional green particles
        for (int i = 0; i < 20; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.5;
            double offsetY = level.random.nextDouble();
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.5;

            level.sendParticles(
                ParticleTypes.COMPOSTER,
                x + offsetX, y + offsetY, z + offsetZ,
                1, 0.0, 0.05, 0.0, 0.0
            );
        }
    }
}