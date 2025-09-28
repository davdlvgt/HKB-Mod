package de.davidvogt.hkbmod.block.entity;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.research.Research;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ResearchTableBlockEntity extends BlockEntity {

    public static final int RESEARCH_SLOTS = 9; // 5 slots for research materials
    private final NonNullList<ItemStack> researchItems = NonNullList.withSize(RESEARCH_SLOTS, ItemStack.EMPTY);

    // Research progress fields
    private ResourceLocation currentResearchId = null;
    private int researchProgress = 0; // in ticks
    private int researchDuration = 0; // total duration in ticks
    private boolean isResearching = false;

    public ResearchTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.RESEARCH_TABLE_BE.get(), pos, blockState);
    }

    public NonNullList<ItemStack> getResearchItems() {
        return researchItems;
    }

    public ItemStack getResearchItem(int slot) {
        if (slot >= 0 && slot < researchItems.size()) {
            return researchItems.get(slot);
        }
        return ItemStack.EMPTY;
    }

    public void setResearchItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < researchItems.size()) {
            researchItems.set(slot, stack);
            setChanged();
        }
    }

    public ItemStack removeResearchItem(int slot, int count) {
        if (slot >= 0 && slot < researchItems.size()) {
            ItemStack stack = researchItems.get(slot);
            if (!stack.isEmpty()) {
                ItemStack result = stack.split(count);
                if (stack.isEmpty()) {
                    researchItems.set(slot, ItemStack.EMPTY);
                }
                setChanged();
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    public void clearResearchItems() {
        for (int i = 0; i < researchItems.size(); i++) {
            researchItems.set(i, ItemStack.EMPTY);
        }
        setChanged();
    }

    public boolean areResearchSlotsEmpty() {
        for (ItemStack stack : researchItems) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void dropContents(Level level, BlockPos pos) {
        SimpleContainer container = new SimpleContainer(researchItems.size());
        for (int i = 0; i < researchItems.size(); i++) {
            container.setItem(i, researchItems.get(i));
        }
        Containers.dropContents(level, pos, container);
    }

    public boolean stillValid(Player player) {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return player.distanceToSqr((double)this.worldPosition.getX() + 0.5D,
                                      (double)this.worldPosition.getY() + 0.5D,
                                      (double)this.worldPosition.getZ() + 0.5D) <= 64.0D;
        }
    }

    // Research progress methods
    public void startResearch(Research research) {
        HkbMod.LOGGER.info("[ResearchTableBlockEntity] Starting research: " + research.getId() +
            " (Client side: " + (level != null ? level.isClientSide : "unknown") + ")");
        this.currentResearchId = research.getId();
        this.researchDuration = 10 * 20; // 10 seconds for all research, converted to ticks (20 ticks = 1 second)
        this.researchProgress = 0;
        this.isResearching = true;
        HkbMod.LOGGER.info("[ResearchTableBlockEntity] Research state set - isResearching: " + this.isResearching +
            ", progress: " + this.researchProgress + "/" + this.researchDuration);
        setChanged();
    }

    public void cancelResearch() {
        this.currentResearchId = null;
        this.researchProgress = 0;
        this.researchDuration = 0;
        this.isResearching = false;
        setChanged();
    }

    public boolean isResearching() {
        return isResearching;
    }

    public ResourceLocation getCurrentResearchId() {
        return currentResearchId;
    }

    public int getResearchProgress() {
        return researchProgress;
    }

    public int getResearchDuration() {
        return researchDuration;
    }

    public float getResearchProgressPercentage() {
        if (researchDuration == 0) return 0f;
        return (float) researchProgress / researchDuration;
    }

    public int getRemainingResearchTime() {
        return Math.max(0, researchDuration - researchProgress);
    }

    public int getRemainingResearchSeconds() {
        return getRemainingResearchTime() / 20; // Convert ticks to seconds
    }

    // Client-side research state methods (for networking sync)
    public void setResearchProgress(int progress, int duration, ResourceLocation researchId) {
        this.researchProgress = progress;
        this.researchDuration = duration;
        this.currentResearchId = researchId;
        this.isResearching = true;
    }

    public void clearClientResearchState() {
        this.researchProgress = 0;
        this.researchDuration = 0;
        this.currentResearchId = null;
        this.isResearching = false;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ResearchTableBlockEntity blockEntity) {
        if (blockEntity.isResearching && !level.isClientSide) {
            blockEntity.researchProgress++;

            // Debug logging every 20 ticks (1 second)
            if (blockEntity.researchProgress % 20 == 0) {
                HkbMod.LOGGER.info("[ResearchTableBlockEntity] Tick: Research progress " +
                    blockEntity.researchProgress + "/" + blockEntity.researchDuration +
                    " (Research: " + blockEntity.currentResearchId + ")");
                blockEntity.sendProgressUpdate();
            }

            // Check if research is complete
            if (blockEntity.researchProgress >= blockEntity.researchDuration) {
                HkbMod.LOGGER.info("[ResearchTableBlockEntity] Research completed: " + blockEntity.currentResearchId);
                // Research completed - the menu will handle the actual unlock logic
                blockEntity.isResearching = false;
                blockEntity.sendProgressUpdate(); // Final update to show completion
                blockEntity.setChanged();
            }
        } else if (blockEntity.isResearching && level.isClientSide) {
            // Debug: client side tick
            if (blockEntity.researchProgress % 40 == 0) { // Every 2 seconds on client
                HkbMod.LOGGER.info("[ResearchTableBlockEntity] CLIENT Tick: Research progress " +
                    blockEntity.researchProgress + "/" + blockEntity.researchDuration);
            }
        }
    }

    private void sendProgressUpdate() {
        // Networking temporarily disabled - progress updates are handled locally
    }

    // NBT persistence temporarily disabled - API needs research for Forge 57.0.3
    // Research progress and items will be lost on world reload until proper NBT implementation
    // TODO: Research correct NBT persistence API for this Forge version
}