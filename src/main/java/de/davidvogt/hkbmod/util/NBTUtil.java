package de.davidvogt.hkbmod.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * Utility class to handle NBT operations with proper Optional handling for Forge 57.0.3
 */
public class NBTUtil {

    /**
     * Safely get a CompoundTag from another CompoundTag, handling Optional return type
     */
    public static CompoundTag getCompoundTag(CompoundTag tag, String key) {
        Optional<CompoundTag> optional = tag.getCompound(key);
        return optional.orElse(new CompoundTag());
    }

    /**
     * Safely get a boolean value from CompoundTag, handling Optional return type
     */
    public static boolean getBoolean(CompoundTag tag, String key) {
        Optional<Boolean> optional = tag.getBoolean(key);
        return optional.orElse(false);
    }

    /**
     * Safely get an int value from CompoundTag, handling Optional return type
     */
    public static int getInt(CompoundTag tag, String key) {
        Optional<Integer> optional = tag.getInt(key);
        return optional.orElse(0);
    }

    /**
     * Safely get a string value from CompoundTag, handling Optional return type
     */
    public static String getString(CompoundTag tag, String key) {
        Optional<String> optional = tag.getString(key);
        return optional.orElse("");
    }

    /**
     * Safely save an ItemStack to NBT, handling version-specific API
     * Note: API changed in Forge 57.0.3, needs proper implementation
     */
    public static CompoundTag saveItemStack(ItemStack stack) {
        CompoundTag tag = new CompoundTag();
        // TODO: Implement proper ItemStack saving for current API
        return tag;
    }

    /**
     * Safely load an ItemStack from NBT, handling version-specific API
     * Note: API changed in Forge 57.0.3, needs proper implementation
     */
    public static ItemStack loadItemStack(CompoundTag tag) {
        // TODO: Implement proper ItemStack loading for current API
        return ItemStack.EMPTY;
    }

    /**
     * Safely get a ListTag from CompoundTag, handling Optional return type
     */
    public static ListTag getList(CompoundTag tag, String key) {
        Optional<ListTag> optional = tag.getList(key);
        return optional.orElse(new ListTag());
    }

    /**
     * Safely get a string from ListTag at index, handling Optional return type
     */
    public static String getStringFromList(ListTag list, int index) {
        if (index >= 0 && index < list.size()) {
            Optional<String> optional = list.getString(index);
            return optional.orElse("");
        }
        return "";
    }

    /**
     * Safely parse a ResourceLocation from string
     */
    public static ResourceLocation parseResourceLocation(String locationString) {
        if (locationString == null || locationString.isEmpty()) {
            return null;
        }
        return ResourceLocation.tryParse(locationString);
    }
}