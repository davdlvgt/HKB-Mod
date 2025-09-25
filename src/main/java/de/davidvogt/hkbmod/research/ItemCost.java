package de.davidvogt.hkbmod.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemCost {
    private final ResourceLocation itemId;
    private final int count;

    public ItemCost(ResourceLocation itemId, int count) {
        this.itemId = itemId;
        this.count = count;
    }

    public ItemCost(Item item, int count) {
        this(ForgeRegistries.ITEMS.getKey(item), count);
    }

    public ResourceLocation getItemId() { return itemId; }
    public int getCount() { return count; }

    public Item getItem() {
        return ForgeRegistries.ITEMS.getValue(itemId);
    }

    public ItemStack createItemStack() {
        Item item = getItem();
        return item != null ? new ItemStack(item, count) : ItemStack.EMPTY;
    }

    public boolean matches(ItemStack stack) {
        Item item = getItem();
        return item != null && stack.is(item) && stack.getCount() >= count;
    }

    // NBT serialization
    public CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("item", itemId.toString());
        tag.putInt("count", count);
        return tag;
    }

    public static ItemCost loadFromNBT(CompoundTag tag) {
        String itemString = tag.getString("item").orElse("minecraft:air");
        ResourceLocation itemId = ResourceLocation.parse(itemString);
        int count = tag.getInt("count").orElse(1);
        return new ItemCost(itemId, count);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ItemCost itemCost = (ItemCost) obj;
        return count == itemCost.count && itemId.equals(itemCost.itemId);
    }

    @Override
    public int hashCode() {
        return itemId.hashCode() * 31 + count;
    }

    @Override
    public String toString() {
        return count + "x " + itemId;
    }
}