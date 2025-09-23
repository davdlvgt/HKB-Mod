package de.davidvogt.hkbmod.item.custom;

import de.davidvogt.hkbmod.util.KeySequenceManager;
import de.davidvogt.hkbmod.util.ServerGrowthEffectManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

public class GrowthAcceleratorWandItem extends Item {

    private static final int DURABILITY_COST = 1;
    private static final int GROWTH_STAGES = 3; // Number of growth stages to advance

    public GrowthAcceleratorWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();
        BlockState blockState = level.getBlockState(pos);
        Block block = blockState.getBlock();

        // Check if target is growable
        boolean isValidTarget = false;
        BlockPos targetPos = pos;

        // Handle different types of growable blocks
        if (isGrowableBlock(block)) {
            isValidTarget = true;
        }
        // Handle saplings
        else if (block instanceof SaplingBlock) {
            isValidTarget = true;
        }
        // Handle farmland with seeds on top
        else if (block instanceof FarmBlock) {
            BlockPos abovePos = pos.above();
            BlockState aboveState = level.getBlockState(abovePos);
            Block aboveBlock = aboveState.getBlock();

            if (isGrowableBlock(aboveBlock)) {
                isValidTarget = true;
                targetPos = abovePos; // Target the plant above farmland
            }
        }

        if (!isValidTarget) {
            if (player != null) {
                player.displayClientMessage(Component.literal("§cTarget a growable plant!"), true);
            }
            return InteractionResult.FAIL;
        }

        // Simplified approach: check for a special NBT tag on the wand
        if (!level.isClientSide) {
            // Server side - check if the wand has a completed sequence marker
            if (player != null) {
                player.displayClientMessage(Component.literal("§7[DEBUG] Server side - checking for completion marker"), false);
            }

            if (itemStack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) {
                var customData = itemStack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
                var tag = customData.copyTag();

                if (player != null) {
                    player.displayClientMessage(Component.literal("§7[DEBUG] Has custom data"), false);
                }

                if (tag.contains("SequenceCompleted")) {
                    String completedPosStr = String.valueOf(tag.getString("SequenceCompleted"));
                    String targetPosStr = targetPos.getX() + "," + targetPos.getY() + "," + targetPos.getZ();

                    if (player != null) {
                        player.displayClientMessage(Component.literal("§7[DEBUG] Found completion marker: " + completedPosStr), false);
                        player.displayClientMessage(Component.literal("§7[DEBUG] Target position: " + targetPosStr), false);
                    }

                    if (completedPosStr.equals(targetPosStr)) {
                        if (player != null) {
                            player.displayClientMessage(Component.literal("§a[DEBUG] Positions match! Triggering growth"), false);
                        }

                        // Trigger the actual growth effect
                        ServerGrowthEffectManager.getInstance().triggerGrowthEffect((ServerLevel) level, targetPos, player);

                        // Remove the completion marker
                        tag.remove("SequenceCompleted");
                        itemStack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                            net.minecraft.world.item.component.CustomData.of(tag));

                        // Damage the wand
                        if (player != null) {
                            itemStack.hurtAndBreak(DURABILITY_COST, player, player.getUsedItemHand());
                        }

                        return InteractionResult.SUCCESS;
                    } else {
                        if (player != null) {
                            player.displayClientMessage(Component.literal("§c[DEBUG] Positions don't match!"), false);
                        }
                    }
                } else {
                    if (player != null) {
                        player.displayClientMessage(Component.literal("§c[DEBUG] No completion marker found"), false);
                    }
                }
            } else {
                if (player != null) {
                    player.displayClientMessage(Component.literal("§c[DEBUG] No custom data on wand"), false);
                }
            }

            // No completed sequence
            return InteractionResult.PASS;
        } else {
            // Client side - start sequence challenge
            KeySequenceManager manager = KeySequenceManager.getInstance();

            // Check if a sequence is already active
            if (manager.isSequenceActive()) {
                player.displayClientMessage(Component.literal("§cSequence already in progress!"), true);
                return InteractionResult.FAIL;
            }

            // Start the key sequence challenge
            manager.startSequence(targetPos);

            return InteractionResult.SUCCESS;
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
}