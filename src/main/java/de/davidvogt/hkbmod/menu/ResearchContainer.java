package de.davidvogt.hkbmod.menu;

import de.davidvogt.hkbmod.block.entity.ResearchTableBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ResearchContainer implements Container {
    private final ResearchTableBlockEntity blockEntity;

    public ResearchContainer(ResearchTableBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public int getContainerSize() {
        return ResearchTableBlockEntity.RESEARCH_SLOTS;
    }

    @Override
    public boolean isEmpty() {
        return blockEntity.areResearchSlotsEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return blockEntity.getResearchItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        return blockEntity.removeResearchItem(slot, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = blockEntity.getResearchItem(slot);
        blockEntity.setResearchItem(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        blockEntity.setResearchItem(slot, stack);
    }

    @Override
    public void setChanged() {
        blockEntity.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
    }

    @Override
    public void clearContent() {
        blockEntity.clearResearchItems();
    }
}