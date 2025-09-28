package de.davidvogt.hkbmod.menu;

import de.davidvogt.hkbmod.block.entity.ResearchTableBlockEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ResearchSlot extends Slot {
    private final ResearchTableBlockEntity blockEntity;
    private final int slotIndex;

    public ResearchSlot(ResearchTableBlockEntity blockEntity, int slotIndex, int x, int y) {
        super(new ResearchContainer(blockEntity), slotIndex, x, y);
        this.blockEntity = blockEntity;
        this.slotIndex = slotIndex;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // Allow all items to be placed in research slots
        return !stack.isEmpty();
    }

    @Override
    public ItemStack getItem() {
        return blockEntity.getResearchItem(slotIndex);
    }

    @Override
    public void set(ItemStack stack) {
        blockEntity.setResearchItem(slotIndex, stack);
        this.setChanged();
    }

    @Override
    public void setChanged() {
        blockEntity.setChanged();
    }

    @Override
    public ItemStack remove(int count) {
        return blockEntity.removeResearchItem(slotIndex, count);
    }

    @Override
    public boolean hasItem() {
        return !blockEntity.getResearchItem(slotIndex).isEmpty();
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Math.min(this.getMaxStackSize(), stack.getMaxStackSize());
    }
}