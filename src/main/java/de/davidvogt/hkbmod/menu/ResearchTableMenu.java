package de.davidvogt.hkbmod.menu;

import de.davidvogt.hkbmod.block.ModBlocks;
import de.davidvogt.hkbmod.block.entity.ResearchTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ResearchTableMenu extends AbstractContainerMenu {
    private final ResearchTableBlockEntity blockEntity;
    private final Level level;
    private final ContainerLevelAccess access;

    public ResearchTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, (BlockEntity) null);
    }

    public ResearchTableMenu(int containerId, Inventory playerInventory, BlockEntity entity) {
        super(ModMenuTypes.RESEARCH_TABLE_MENU.get(), containerId);

        this.level = playerInventory.player.level();

        if (entity instanceof ResearchTableBlockEntity researchTableEntity) {
            this.blockEntity = researchTableEntity;
            this.access = ContainerLevelAccess.create(level, blockEntity.getBlockPos());
            createBlockEntityInventory(blockEntity);
        } else if (entity == null) {
            // Client-side construction without block entity (create dummy)
            this.blockEntity = new ResearchTableBlockEntity(BlockPos.ZERO,
                ModBlocks.RESEARCH_TABLE.get().defaultBlockState());
            this.access = ContainerLevelAccess.NULL;
            createDummyInventory();
        } else {
            throw new IllegalStateException("Incorrect block entity class (%s) passed into ResearchTableMenu!".formatted(
                entity.getClass().getCanonicalName()));
        }

        createPlayerInventory(playerInventory);
        createPlayerHotbar(playerInventory);
    }

    private void createBlockEntityInventory(ResearchTableBlockEntity entity) {
        entity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(itemHandler -> {
            // Crafting grid slots (3x3) - positioned on the left side
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    int index = row * 3 + col;
                    addSlot(new SlotItemHandler(itemHandler, index, 30 + col * 18, 35 + row * 18));
                }
            }

            // Crafting result slot - positioned to the right of crafting grid
            addSlot(new SlotItemHandler(itemHandler, ResearchTableBlockEntity.CRAFTING_RESULT_SLOT, 124, 53));

            // Research input slots - positioned below crafting area
            for (int i = 0; i < ResearchTableBlockEntity.RESEARCH_INPUT_SIZE; i++) {
                int slotIndex = ResearchTableBlockEntity.RESEARCH_INPUT_START + i;
                int x = 30 + (i % 3) * 18;
                int y = 110 + (i / 3) * 18;
                addSlot(new SlotItemHandler(itemHandler, slotIndex, x, y));
            }
        });
    }

    private void createDummyInventory() {
        // Create dummy slots for client-side rendering
        ItemStackHandler dummyHandler = new ItemStackHandler(ResearchTableBlockEntity.TOTAL_SLOTS);

        // Crafting grid slots (3x3)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int index = row * 3 + col;
                addSlot(new SlotItemHandler(dummyHandler, index, 30 + col * 18, 35 + row * 18));
            }
        }

        // Crafting result slot
        addSlot(new SlotItemHandler(dummyHandler, ResearchTableBlockEntity.CRAFTING_RESULT_SLOT, 124, 53));

        // Research input slots
        for (int i = 0; i < ResearchTableBlockEntity.RESEARCH_INPUT_SIZE; i++) {
            int slotIndex = ResearchTableBlockEntity.RESEARCH_INPUT_START + i;
            int x = 30 + (i % 3) * 18;
            int y = 110 + (i / 3) * 18;
            addSlot(new SlotItemHandler(dummyHandler, slotIndex, x, y));
        }
    }

    private void createPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 165 + row * 18));
            }
        }
    }

    private void createPlayerHotbar(Inventory playerInventory) {
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 223));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot fromSlot = getSlot(index);
        ItemStack fromStack = fromSlot.getItem();

        if (fromStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack copyFromStack = fromStack.copy();
        int blockEntitySlots = ResearchTableBlockEntity.TOTAL_SLOTS;
        int playerInventoryStart = blockEntitySlots;
        int playerInventoryEnd = playerInventoryStart + 36; // 27 inventory + 9 hotbar

        // Moving from block entity slots to player inventory
        if (index < blockEntitySlots) {
            // Don't allow quick move from result slot if it's empty or from input slots during research
            if (index == ResearchTableBlockEntity.CRAFTING_RESULT_SLOT) {
                // Special handling for crafting result
                if (!moveItemStackTo(fromStack, playerInventoryStart, playerInventoryEnd, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= ResearchTableBlockEntity.RESEARCH_INPUT_START) {
                // Research input slots - move to player inventory
                if (!moveItemStackTo(fromStack, playerInventoryStart, playerInventoryEnd, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Crafting grid slots - move to player inventory
                if (!moveItemStackTo(fromStack, playerInventoryStart, playerInventoryEnd, true)) {
                    return ItemStack.EMPTY;
                }
            }
        }
        // Moving from player inventory to block entity
        else if (index >= playerInventoryStart && index < playerInventoryEnd) {
            // Try to move to research input first, then crafting grid
            if (!moveItemStackTo(fromStack, ResearchTableBlockEntity.RESEARCH_INPUT_START,
                    ResearchTableBlockEntity.RESEARCH_INPUT_START + ResearchTableBlockEntity.RESEARCH_INPUT_SIZE, false)) {
                if (!moveItemStackTo(fromStack, 0, ResearchTableBlockEntity.CRAFTING_GRID_SIZE, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else {
            return ItemStack.EMPTY;
        }

        fromSlot.setChanged();
        if (fromStack.isEmpty()) {
            fromSlot.set(ItemStack.EMPTY);
        } else {
            fromSlot.setChanged();
        }

        return fromStack.getCount() == copyFromStack.getCount() ? ItemStack.EMPTY : copyFromStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, blockEntity.getBlockState().getBlock());
    }

    public ResearchTableBlockEntity getBlockEntity() {
        return blockEntity;
    }

    // Research-specific methods for UI
    public boolean isResearching() {
        return blockEntity.isResearching();
    }

    public float getResearchProgress() {
        return blockEntity.getResearchProgressPercent();
    }
}