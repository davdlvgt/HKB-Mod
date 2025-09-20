package de.davidvogt.hkbmod.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LightningWandItem extends Item {

    private static final String COOLDOWN_KEY = "LightningWandCooldown";
    private static final int COOLDOWN_TICKS = 40; // 2 seconds (20 ticks = 1 second)
    private static final double MAX_RANGE = 100.0; // Maximum range for lightning strike

    public LightningWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // Only work on server side
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        long currentTime = level.getGameTime();

        // Check cooldown
        if (isOnCooldown(itemStack, currentTime)) {
            long remainingCooldown = getRemainingCooldown(itemStack, currentTime);
            long remainingSeconds = (remainingCooldown + 19) / 20; // Round up to nearest second
            player.displayClientMessage(
                Component.literal("Lightning Wand cooldown: " + remainingSeconds + " seconds remaining"),
                true
            );
            return InteractionResult.FAIL;
        }

        // Perform lightning strike at target location
        BlockPos targetPos = getTargetPosition(serverLevel, player);
        if (targetPos != null) {
            strikeLightning(serverLevel, targetPos, player);

            // Set cooldown
            setCooldown(itemStack, currentTime + COOLDOWN_TICKS);

            // Damage the item
            itemStack.hurtAndBreak(1, player, player.getUsedItemHand());

            // Play sound
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.5F, 1.2F);

            return InteractionResult.SUCCESS;
        } else {
            player.displayClientMessage(
                Component.literal("No valid target found!"),
                true
            );
            return InteractionResult.FAIL;
        }
    }

    private BlockPos getTargetPosition(ServerLevel serverLevel, Player player) {
        // Raycast to find where the player is looking
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.scale(MAX_RANGE));

        BlockHitResult hitResult = serverLevel.clip(new ClipContext(
            eyePos,
            endPos,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            player
        ));

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = hitResult.getBlockPos();
            // Get the surface position for lightning strike
            return serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, hitPos);
        }

        return null;
    }

    private void strikeLightning(ServerLevel serverLevel, BlockPos targetPos, Player player) {
        // Create lightning bolt at target position
        LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, serverLevel);
        lightningBolt.setPos(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
        lightningBolt.setVisualOnly(false); // Allow damage
        serverLevel.addFreshEntity(lightningBolt);

        // Play additional effects
        serverLevel.playSound(null, targetPos.getX(), targetPos.getY(), targetPos.getZ(),
                SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.0F, 1.0F);

        player.displayClientMessage(
            Component.literal("Lightning strike summoned at target!"),
            true
        );
    }

    private boolean isOnCooldown(ItemStack itemStack, long currentTime) {
        if (!itemStack.has(DataComponents.CUSTOM_DATA)) {
            return false;
        }
        CompoundTag tag = itemStack.get(DataComponents.CUSTOM_DATA).copyTag();
        if (!tag.contains(COOLDOWN_KEY)) {
            return false;
        }

        long cooldownEnd = tag.getLong(COOLDOWN_KEY).orElse(0L);
        return currentTime < cooldownEnd;
    }

    private long getRemainingCooldown(ItemStack itemStack, long currentTime) {
        if (!itemStack.has(DataComponents.CUSTOM_DATA)) {
            return 0;
        }
        CompoundTag tag = itemStack.get(DataComponents.CUSTOM_DATA).copyTag();
        if (!tag.contains(COOLDOWN_KEY)) {
            return 0;
        }

        long cooldownEnd = tag.getLong(COOLDOWN_KEY).orElse(0L);
        return Math.max(0, cooldownEnd - currentTime);
    }

    private void setCooldown(ItemStack itemStack, long cooldownEnd) {
        CompoundTag tag;
        if (itemStack.has(DataComponents.CUSTOM_DATA)) {
            tag = itemStack.get(DataComponents.CUSTOM_DATA).copyTag();
        } else {
            tag = new CompoundTag();
        }
        tag.putLong(COOLDOWN_KEY, cooldownEnd);
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        // Show bar if on cooldown or if item is damaged
        return isDamaged(stack) || isOnCooldown(stack, getCurrentGameTime(stack));
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        long currentTime = getCurrentGameTime(stack);

        // If on cooldown, show cooldown progress
        if (isOnCooldown(stack, currentTime)) {
            long remainingCooldown = getRemainingCooldown(stack, currentTime);
            double progress = 1.0 - ((double) remainingCooldown / COOLDOWN_TICKS);
            return Math.round(13.0F * (float) progress);
        }

        // Otherwise show damage bar
        return Math.round(13.0F * (1.0F - (float) getDamageValue(stack) / (float) getMaxDamage(stack)));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        long currentTime = getCurrentGameTime(stack);

        // If on cooldown, show purple color for magic
        if (isOnCooldown(stack, currentTime)) {
            return 0x9932CC; // Purple color for magic cooldown
        }

        // Otherwise show normal damage color (red to green)
        float damage = (float) getDamageValue(stack) / (float) getMaxDamage(stack);
        return net.minecraft.util.Mth.hsvToRgb(Math.max(0.0F, (1.0F - damage) / 3.0F), 1.0F, 1.0F);
    }

    private long getCurrentGameTime(ItemStack stack) {
        return System.currentTimeMillis() / 50; // Approximate game time
    }

    private int getDamageValue(ItemStack stack) {
        return stack.getDamageValue();
    }

    private int getMaxDamage(ItemStack stack) {
        return stack.getMaxDamage();
    }

    private boolean isDamaged(ItemStack stack) {
        return stack.isDamaged();
    }

    public int getEnchantmentValue() {
        return 15; // Higher than normal tools for magical item
    }
}