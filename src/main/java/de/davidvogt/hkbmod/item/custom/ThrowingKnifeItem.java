package de.davidvogt.hkbmod.item.custom;

import de.davidvogt.hkbmod.entity.ThrowingKnifeEntity;
import de.davidvogt.hkbmod.item.custom.abstracts.AbstractThrowingKnifeItem;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nullable;

public class ThrowingKnifeItem extends AbstractThrowingKnifeItem {

    public ThrowingKnifeItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void configureProjectile(ThrowingKnifeEntity projectile, ItemStack itemStack, Player player) {
        // Setze den Schaden basierend auf dem Item
        projectile.setBaseDamage(getDamage());

        // Übertrage getränkte Tränke auf das Projektil
        Holder<Potion> imbuedPotion = getImbuedPotion(itemStack);
        if (!imbuedPotion.equals(Potions.WATER)) {
            projectile.setImbuedPotion(imbuedPotion);
        }
    }

    // Statische Utility-Methoden für Trank-Tränkung
    public static void imbueWithPotion(ItemStack stack, Holder<Potion> potion) {
        CompoundTag tag = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
        tag.putString("ImbuedPotion", BuiltInRegistries.POTION.getKey(potion.value()).toString());
        stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
    }

    @Nullable
    public static Holder<Potion> getImbuedPotion(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("ImbuedPotion")) {
            String potionString = String.valueOf(tag.getString("ImbuedPotion"));

            // Bekannte Tränke direkt zurückgeben
            return switch (potionString) {
                case "minecraft:poison" -> Potions.POISON;
                case "minecraft:healing" -> Potions.HEALING;
                case "minecraft:harming" -> Potions.HARMING;
                case "minecraft:swiftness" -> Potions.SWIFTNESS;
                case "minecraft:slowness" -> Potions.SLOWNESS;
                case "minecraft:strength" -> Potions.STRENGTH;
                case "minecraft:weakness" -> Potions.WEAKNESS;
                case "minecraft:regeneration" -> Potions.REGENERATION;
                case "minecraft:fire_resistance" -> Potions.FIRE_RESISTANCE;
                case "minecraft:night_vision" -> Potions.NIGHT_VISION;
                case "minecraft:invisibility" -> Potions.INVISIBILITY;
                case "minecraft:leaping" -> Potions.LEAPING;
                case "minecraft:water_breathing" -> Potions.WATER_BREATHING;
                case "minecraft:long_night_vision" -> Potions.LONG_NIGHT_VISION;
                case "minecraft:long_invisibility" -> Potions.LONG_INVISIBILITY;
                case "minecraft:long_leaping" -> Potions.LONG_LEAPING;
                case "minecraft:long_fire_resistance" -> Potions.LONG_FIRE_RESISTANCE;
                case "minecraft:long_swiftness" -> Potions.LONG_SWIFTNESS;
                case "minecraft:long_slowness" -> Potions.LONG_SLOWNESS;
                case "minecraft:long_strength" -> Potions.LONG_STRENGTH;
                case "minecraft:long_weakness" -> Potions.LONG_WEAKNESS;
                case "minecraft:long_regeneration" -> Potions.LONG_REGENERATION;
                case "minecraft:long_water_breathing" -> Potions.LONG_WATER_BREATHING;
                case "minecraft:strong_healing" -> Potions.STRONG_HEALING;
                case "minecraft:strong_harming" -> Potions.STRONG_HARMING;
                case "minecraft:strong_swiftness" -> Potions.STRONG_SWIFTNESS;
                case "minecraft:strong_slowness" -> Potions.STRONG_SLOWNESS;
                case "minecraft:strong_strength" -> Potions.STRONG_STRENGTH;
                case "minecraft:strong_leaping" -> Potions.STRONG_LEAPING;
                case "minecraft:strong_regeneration" -> Potions.STRONG_REGENERATION;
                case "minecraft:strong_poison" -> Potions.STRONG_POISON;
                default -> Potions.WATER; // Fallback
            };
        }
        return Potions.WATER;
    }
}