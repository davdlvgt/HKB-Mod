package de.davidvogt.hkbmod.item.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class MagneticBarItem extends Item {

    private static final double MAGNETIC_RANGE = 8.0; // Blocks radius
    private static final double PULL_STRENGTH = 5; // How fast items are pulled
    // private static final int COOLDOWN_TICKS = 10; // Cooldown between uses - removed due to API compatibility
    private static final int DURABILITY_COST = 1; // Durability cost per use

    public MagneticBarItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);

        if (!level.isClientSide) {
            // Server side - perform the magnetic pull
            pullNearbyItems(level, player, itemStack);

            // Cooldown removed due to API compatibility issues
            // The durability cost provides natural usage limiting

            // Damage the item
            itemStack.hurtAndBreak(DURABILITY_COST, player, usedHand);
        } else {
            // Client side - show feedback
            player.displayClientMessage(Component.literal("§bMagnetic pull activated!"), true);
        }

        return InteractionResult.SUCCESS;
    }

    private void pullNearbyItems(Level level, Player player, ItemStack magneticBar) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 playerPos = player.position();

        // Create bounding box for search area
        AABB searchArea = new AABB(
            playerPos.x - MAGNETIC_RANGE, playerPos.y - MAGNETIC_RANGE, playerPos.z - MAGNETIC_RANGE,
            playerPos.x + MAGNETIC_RANGE, playerPos.y + MAGNETIC_RANGE, playerPos.z + MAGNETIC_RANGE
        );

        // Find all item entities in range
        List<ItemEntity> nearbyItems = level.getEntitiesOfClass(ItemEntity.class, searchArea);

        int itemsPulled = 0;

        for (ItemEntity itemEntity : nearbyItems) {
            // Skip if item is already being picked up or is too young
            if (itemEntity.hasPickUpDelay() || itemEntity.tickCount < 10) {
                continue;
            }

            // Calculate direction from item to player
            Vec3 itemPos = itemEntity.position();
            Vec3 direction = playerPos.subtract(itemPos).normalize();

            // Calculate distance-based pull strength (closer = stronger pull)
            double distance = itemPos.distanceTo(playerPos);
            double adjustedPullStrength = PULL_STRENGTH * (MAGNETIC_RANGE - distance) / MAGNETIC_RANGE;

            // Apply velocity towards player
            Vec3 pullVelocity = direction.scale(adjustedPullStrength);
            itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().add(pullVelocity));

            // Add particle effects around the item
            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                itemPos.x, itemPos.y + 0.5, itemPos.z,
                3, 0.2, 0.2, 0.2, 0.1);

            itemsPulled++;
        }

        if (itemsPulled > 0) {
            // Play sound effect
            level.playSound(null, player.blockPosition(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS,
                0.5f, 1.0f + (float) (Math.random() * 0.4 - 0.2));

            // Particle effect around player
            serverLevel.sendParticles(ParticleTypes.PORTAL,
                playerPos.x, playerPos.y + 1.0, playerPos.z,
                15, 0.5, 0.5, 0.5, 0.5);

            // Send feedback message
            player.displayClientMessage(
                Component.literal("§bPulled §f" + itemsPulled + "§b items!"),
                true
            );
        } else {
            player.displayClientMessage(
                Component.literal("§7No items in range to pull"),
                true
            );
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Make the item have enchanted glint
        return true;
    }

    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        // Add subtle particle effects when held
        if (isSelected && entity instanceof Player player && level.isClientSide) {
            if (level.getGameTime() % 20 == 0) { // Every second
                Vec3 pos = player.position();
                level.addParticle(ParticleTypes.ENCHANT,
                    pos.x + (Math.random() - 0.5) * 0.5,
                    pos.y + 1.5 + (Math.random() - 0.5) * 0.3,
                    pos.z + (Math.random() - 0.5) * 0.5,
                    0, 0.05, 0);
            }
        }
    }
}