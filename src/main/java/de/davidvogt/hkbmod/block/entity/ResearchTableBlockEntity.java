package de.davidvogt.hkbmod.block.entity;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.menu.ResearchTableMenu;
import de.davidvogt.hkbmod.research.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.*;

public class ResearchTableBlockEntity extends BlockEntity implements MenuProvider {
    // Slot configuration:
    // 0-8: Crafting grid (3x3)
    // 9: Crafting result slot
    // 10-15: Research input slots (6 slots for materials)
    public static final int CRAFTING_GRID_SIZE = 9;
    public static final int CRAFTING_RESULT_SLOT = 9;
    public static final int RESEARCH_INPUT_START = 10;
    public static final int RESEARCH_INPUT_SIZE = 6;
    public static final int TOTAL_SLOTS = 16;

    private final ItemStackHandler itemHandler = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == CRAFTING_RESULT_SLOT) {
                return false; // Result slot is output only
            }
            return true;
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    // Research system data
    private Set<ResourceLocation> unlockedResearches = new HashSet<>();
    private ResourceLocation activeResearchId = null;
    private int researchProgress = 0;
    private int researchMaxProgress = 0;
    private boolean isResearching = false;

    public ResearchTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.RESEARCH_TABLE.get(), pos, blockState);

        // Initialize default research nodes
        if (level != null && !level.isClientSide) {
            ResearchManager.getInstance().initializeDefaultNodes();
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.hkbmod.research_table");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ResearchTableMenu(id, inventory, this);
    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("inventory", itemHandler.serializeNBT(registries));

        // Save research data
        CompoundTag researchTag = new CompoundTag();

        // Save unlocked researches
        ListTag unlockedList = new ListTag();
        for (ResourceLocation research : unlockedResearches) {
            CompoundTag researchCompound = new CompoundTag();
            researchCompound.putString("id", research.toString());
            unlockedList.add(researchCompound);
        }
        researchTag.put("unlocked", unlockedList);

        // Save active research state
        if (activeResearchId != null) {
            researchTag.putString("activeResearch", activeResearchId.toString());
            researchTag.putInt("progress", researchProgress);
            researchTag.putInt("maxProgress", researchMaxProgress);
            researchTag.putBoolean("isResearching", isResearching);
        }

        tag.put("research", researchTag);
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("inventory")) {
            tag.getCompound("inventory").ifPresent(inventoryTag ->
                itemHandler.deserializeNBT(registries, inventoryTag));
        }

        // Load research data
        if (tag.contains("research")) {
            tag.getCompound("research").ifPresent(researchTag -> {
                // Load unlocked researches
                unlockedResearches.clear();
                researchTag.getList("unlocked").ifPresent(unlockedList -> {
                    for (int i = 0; i < unlockedList.size(); i++) {
                        unlockedList.getCompound(i).ifPresent(researchCompound -> {
                            researchCompound.getString("id").ifPresent(researchId -> {
                                try {
                                    unlockedResearches.add(ResourceLocation.parse(researchId));
                                } catch (Exception e) {
                                    HkbMod.LOGGER.warn("Failed to load research id: {}", researchId);
                                }
                            });
                        });
                    }
                });

                // Load active research state
                if (researchTag.contains("activeResearch")) {
                    try {
                        researchTag.getString("activeResearch").ifPresent(activeResearch -> {
                            activeResearchId = ResourceLocation.parse(activeResearch);
                        });
                        researchProgress = researchTag.getInt("progress").orElse(0);
                        researchMaxProgress = researchTag.getInt("maxProgress").orElse(0);
                        isResearching = researchTag.getBoolean("isResearching").orElse(false);
                    } catch (Exception e) {
                        HkbMod.LOGGER.warn("Failed to load active research state");
                        activeResearchId = null;
                        researchProgress = 0;
                        researchMaxProgress = 0;
                        isResearching = false;
                    }
                }
            });
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    // Research system methods
    public Set<ResourceLocation> getUnlockedResearches() {
        return new HashSet<>(unlockedResearches);
    }

    public List<ResearchNode> getAvailableNodesForClass(ResearchClass researchClass) {
        return ResearchManager.getInstance().getAvailableNodes(researchClass, unlockedResearches);
    }

    public boolean isResearchUnlocked(ResourceLocation researchId) {
        return unlockedResearches.contains(researchId);
    }

    public boolean canStartResearch(ResourceLocation researchId) {
        ResearchNode node = ResearchManager.getInstance().getNode(researchId);
        if (node == null) return false;

        if (unlockedResearches.contains(researchId)) return false; // Already unlocked
        if (isResearching && !researchId.equals(activeResearchId)) return false; // Already researching something else

        return ResearchManager.getInstance().canUnlockNode(node, unlockedResearches);
    }

    public boolean startResearch(ResourceLocation researchId) {
        if (!canStartResearch(researchId)) return false;

        ResearchNode node = ResearchManager.getInstance().getNode(researchId);
        if (node == null) return false;

        // Check if we have the required items
        if (!hasRequiredItems(node)) return false;

        // Consume the items
        consumeResearchItems(node);

        // Start research
        activeResearchId = researchId;
        researchProgress = 0;
        researchMaxProgress = calculateResearchTime(node);
        isResearching = true;

        setChanged();
        return true;
    }

    public void cancelResearch() {
        if (!isResearching) return;

        // TODO: Optionally return some items
        activeResearchId = null;
        researchProgress = 0;
        researchMaxProgress = 0;
        isResearching = false;
        setChanged();
    }

    public void completeResearch() {
        if (!isResearching || activeResearchId == null) return;

        ResearchNode node = ResearchManager.getInstance().getNode(activeResearchId);
        if (node != null) {
            unlockedResearches.add(activeResearchId);

            // TODO: Apply unlock reward (recipes, abilities, etc.)
            applyUnlockReward(node.getUnlockReward());

            HkbMod.LOGGER.info("Research completed: {}", activeResearchId);
        }

        activeResearchId = null;
        researchProgress = 0;
        researchMaxProgress = 0;
        isResearching = false;
        setChanged();
    }

    private boolean hasRequiredItems(ResearchNode node) {
        List<ItemStack> availableItems = new ArrayList<>();
        for (int i = RESEARCH_INPUT_START; i < RESEARCH_INPUT_START + RESEARCH_INPUT_SIZE; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                availableItems.add(stack.copy());
            }
        }

        for (ItemCost cost : node.getCosts()) {
            int needed = cost.getCount();
            for (ItemStack stack : availableItems) {
                if (cost.matches(stack)) {
                    int taken = Math.min(needed, stack.getCount());
                    needed -= taken;
                    stack.shrink(taken);
                    if (needed <= 0) break;
                }
            }
            if (needed > 0) return false; // Not enough items
        }

        return true;
    }

    private void consumeResearchItems(ResearchNode node) {
        for (ItemCost cost : node.getCosts()) {
            int needed = cost.getCount();
            for (int i = RESEARCH_INPUT_START; i < RESEARCH_INPUT_START + RESEARCH_INPUT_SIZE && needed > 0; i++) {
                ItemStack stack = itemHandler.getStackInSlot(i);
                if (cost.matches(stack)) {
                    int taken = Math.min(needed, stack.getCount());
                    stack.shrink(taken);
                    needed -= taken;
                    itemHandler.setStackInSlot(i, stack);
                }
            }
        }
    }

    private int calculateResearchTime(ResearchNode node) {
        // Base time increases with tier: 10s per tier
        // Can be made configurable later
        return (node.getTier() + 1) * 200; // 10 seconds in ticks per tier
    }

    private void applyUnlockReward(UnlockReward reward) {
        if (reward == null) return;

        // TODO: Implement reward application based on type
        // For now, just log it
        HkbMod.LOGGER.info("Applying unlock reward: {} -> {}", reward.getType(), reward.getTargetId());
    }

    // Crafting system methods
    private void updateCraftingResult() {
        if (level == null || level.isClientSide) return;

        // TODO: Implement crafting result calculation when crafting API is stable
        // For now, just clear the result slot
        itemHandler.setStackInSlot(CRAFTING_RESULT_SLOT, ItemStack.EMPTY);
    }

    private void updateResearchProgress() {
        // This is called when research input items change
        // Could be used for visual feedback or validation
    }

    // Tick method for research progress
    public void tick() {
        if (level == null || level.isClientSide) return;

        if (isResearching && activeResearchId != null) {
            researchProgress++;

            if (researchProgress >= researchMaxProgress) {
                completeResearch();
            } else {
                // Update every second for sync
                if (researchProgress % 20 == 0) {
                    setChanged();
                }
            }
        }
    }

    // Getters for UI
    @Nullable
    public ResourceLocation getActiveResearchId() {
        return activeResearchId;
    }

    @Nullable
    public ResearchNode getActiveResearchNode() {
        return activeResearchId != null ? ResearchManager.getInstance().getNode(activeResearchId) : null;
    }

    public int getResearchProgress() {
        return researchProgress;
    }

    public int getResearchMaxProgress() {
        return researchMaxProgress;
    }

    public boolean isResearching() {
        return isResearching;
    }

    public float getResearchProgressPercent() {
        return researchMaxProgress > 0 ? (float) researchProgress / researchMaxProgress : 0.0f;
    }
}