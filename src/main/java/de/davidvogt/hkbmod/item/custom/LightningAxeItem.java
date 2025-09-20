package de.davidvogt.hkbmod.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class LightningAxeItem extends Item {

    private static final String COOLDOWN_KEY = "LightningCooldown";
    private static final int COOLDOWN_TICKS = 100; // 5 seconds (20 ticks = 1 second)
    private static final int LIGHTNING_RADIUS = 8;
    private static final int LIGHTNING_STRIKES = 20; // Number of lightning strikes

    public LightningAxeItem(Properties properties) {
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
                Component.literal("Lightning Axe cooldown: " + remainingSeconds + " seconds remaining"),
                true
            );
            return InteractionResult.FAIL;
        }

        // Perform lightning strike
        performLightningStrike(serverLevel, player);

        // Set cooldown
        setCooldown(itemStack, currentTime + COOLDOWN_TICKS);

        // Damage the item
        itemStack.hurtAndBreak(2, player, player.getUsedItemHand());

        // Play sound
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0F, 1.0F);

        return InteractionResult.SUCCESS;
    }

    private void performLightningStrike(ServerLevel serverLevel, Player player) {
        Vec3 playerPos = player.position();
        RandomSource random = serverLevel.getRandom();

        // Create multiple lightning strikes in a radius around the player
        for (int i = 0; i < LIGHTNING_STRIKES; i++) {
            BlockPos targetPos;
            int attempts = 0;

            do {
                // Generate random position within radius, but not too close to player
                double angle = random.nextDouble() * 2 * Math.PI;
                double distance = 2.0 + random.nextDouble() * (LIGHTNING_RADIUS - 2.0); // Minimum 2 blocks away

                double x = playerPos.x + Math.cos(angle) * distance;
                double z = playerPos.z + Math.sin(angle) * distance;

                // Find the highest block at this position
                targetPos = BlockPos.containing(x, playerPos.y, z);
                targetPos = serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, targetPos);

                attempts++;
            } while (targetPos.distSqr(player.blockPosition()) < 4 && attempts < 10); // At least 2 blocks away

            // Create lightning bolt
            LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, serverLevel);
            lightningBolt.setPos(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
            lightningBolt.setVisualOnly(false); // Allow damage
            serverLevel.addFreshEntity(lightningBolt);
        }

        player.displayClientMessage(
            Component.literal("Lightning strikes summoned!"),
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
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        // Make it work like an axe for wood-related blocks
        if (state.is(BlockTags.MINEABLE_WITH_AXE)) {
            return 8.0F; // Diamond axe speed
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        // Make it work like an axe
        return state.is(BlockTags.MINEABLE_WITH_AXE);
    }

    public int getEnchantmentValue() {
        return 10; // Same as diamond tools
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

        // If on cooldown, show blue/cyan color for cooldown
        if (isOnCooldown(stack, currentTime)) {
            return 0x00FFFF; // Cyan color for cooldown
        }

        // Otherwise show normal damage color (red to green)
        float damage = (float) getDamageValue(stack) / (float) getMaxDamage(stack);
        return net.minecraft.util.Mth.hsvToRgb(Math.max(0.0F, (1.0F - damage) / 3.0F), 1.0F, 1.0F);
    }

    private long getCurrentGameTime(ItemStack stack) {
        // This is a workaround since we don't have direct access to Level here
        // The bar will update when the item is used or when inventory is opened
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
}