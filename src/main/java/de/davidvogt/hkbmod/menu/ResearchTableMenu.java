package de.davidvogt.hkbmod.menu;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.block.ModBlocks;
import de.davidvogt.hkbmod.block.entity.ResearchTableBlockEntity;
import de.davidvogt.hkbmod.client.gui.layout.ResearchTableLayout;
import de.davidvogt.hkbmod.research.PlayerClass;
import de.davidvogt.hkbmod.research.PlayerResearchData;
import de.davidvogt.hkbmod.research.PlayerResearchDataManager;
import de.davidvogt.hkbmod.research.Research;
import de.davidvogt.hkbmod.research.ResearchManager;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public class ResearchTableMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final Player player;
    private final ResearchTableBlockEntity blockEntity;
    private PlayerResearchData playerResearchData;
    private PlayerClass selectedClass = PlayerClass.KNIGHT;
    private Research selectedResearch = null;

    // Client constructor
    public ResearchTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getClientData(playerInventory, extraData));
    }

    private static class ClientData {
        ResearchTableBlockEntity blockEntity;
        ContainerLevelAccess access;

        ClientData(ResearchTableBlockEntity blockEntity, ContainerLevelAccess access) {
            this.blockEntity = blockEntity;
            this.access = access;
        }
    }

    private ResearchTableMenu(int containerId, Inventory playerInventory, ClientData clientData) {
        this(containerId, playerInventory, clientData.blockEntity, clientData.access);
    }

    private static ClientData getClientData(Inventory playerInventory, FriendlyByteBuf extraData) {
        // The server should send the block position in the extra data
        // For now, let's try to find it in the world around the player
        Player player = playerInventory.player;
        if (player != null && player.level() != null) {
            // Look for research table block entities near the player
            for (int x = -3; x <= 3; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -3; z <= 3; z++) {
                        var pos = player.blockPosition().offset(x, y, z);
                        var blockEntity = player.level().getBlockEntity(pos);
                        if (blockEntity instanceof ResearchTableBlockEntity researchTable) {
                            HkbMod.LOGGER.info("[ResearchTableMenu] Found ResearchTableBlockEntity at client: " + pos);
                            // Create proper ContainerLevelAccess for the found position
                            ContainerLevelAccess access = ContainerLevelAccess.create(player.level(), pos);
                            return new ClientData(researchTable, access);
                        }
                    }
                }
            }
        }
        HkbMod.LOGGER.warn("[ResearchTableMenu] Could not find ResearchTableBlockEntity on client side");
        return new ClientData(null, ContainerLevelAccess.NULL);
    }

    // Server constructor
    public ResearchTableMenu(int containerId, Inventory playerInventory, ResearchTableBlockEntity blockEntity, ContainerLevelAccess access) {
        super(ModMenuTypes.RESEARCH_TABLE.get(), containerId);
        this.access = access;
        this.player = playerInventory.player;
        this.blockEntity = blockEntity;
        this.playerResearchData = PlayerResearchDataManager.getPlayerResearchData(this.player);

        // Initialize research system
        ResearchManager.initialize();

        // Synchronize initial class selection with player research data
        this.playerResearchData.setPlayerClass(this.selectedClass);

        // Add research material slots on the left side
        addResearchSlots();

        // Add player inventory slots - positioned for new UI layout
        addPlayerInventory(playerInventory);
    }

    private void addResearchSlots() {
        int columns = 3; // 3 Spalten
        int rows = 3;    // 3 Reihen
        int slotSize = 18; // Standard Slot-Größe in Pixel

        // Materials panel: adjusted positions for smaller window
        int panelWidth = 90;
        int gridWidth = columns * slotSize + (columns - 1) * 2; // 3 slots + 2 gaps

        int startX = 5 + (panelWidth - gridWidth) / 2; // Centered horizontally
        int startY = 55 + 15; // Fixed offset from panel top (below label)

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int index = row * columns + col;
                int x = startX + col * (slotSize + 2);
                int y = startY + row * (slotSize + 2);

                if (blockEntity != null) {
                    addSlot(new ResearchSlot(blockEntity, index, x, y));
                } else {
                    addSlot(new Slot(new SimpleContainer(ResearchTableBlockEntity.RESEARCH_SLOTS), index, x, y));
                }
            }
        }
    }


    private void addPlayerInventory(Inventory playerInventory) {
        // Calculate centered positions using the same logic as visual rendering
        ResearchTableLayout.InventoryGridPositions positions = ResearchTableLayout.calculateInventoryGridPositions(0, 0);

        // Player inventory (3x9) - centered in inventory panel
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory,
                    col + row * 9 + 9,
                    positions.mainInventoryX + col * 18,
                    positions.mainInventoryY + row * 18));
            }
        }

        // Player hotbar (1x9) - centered below main inventory
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory,
                col,
                positions.hotbarX + col * 18,
                positions.hotbarY));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < ResearchTableBlockEntity.RESEARCH_SLOTS) {
                // Moving from research slots to player inventory
                if (!this.moveItemStackTo(itemstack1, ResearchTableBlockEntity.RESEARCH_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < this.slots.size()) {
                // Moving from player inventory to research slots
                if (!this.moveItemStackTo(itemstack1, 0, ResearchTableBlockEntity.RESEARCH_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, getResearchTableBlock());
    }

    private Block getResearchTableBlock() {
        return ModBlocks.RESEARCH_TABLE.get();
    }

    // Research-specific methods
    public PlayerClass getSelectedClass() {
        return selectedClass;
    }

    public void setSelectedClass(PlayerClass playerClass) {
        this.selectedClass = playerClass;
        // Synchronize the player research data class
        if (this.playerResearchData != null) {
            this.playerResearchData.setPlayerClass(playerClass);
        }
    }

    public PlayerResearchData getPlayerResearchData() {
        return playerResearchData;
    }

    public void setPlayerResearchData(PlayerResearchData data) {
        this.playerResearchData = data;
    }

    public Research getSelectedResearch() {
        return selectedResearch;
    }

    public void setSelectedResearch(Research research) {
        this.selectedResearch = research;
    }

    // Check if research button should be enabled (available to select)
    public boolean isResearchAvailable(Research research) {
        return ResearchManager.isResearchAvailable(research, playerResearchData);
    }

    // Check if research can be started with current items in slots (removed inventory checking)
    public boolean canUnlockResearch(Research research) {
        if (blockEntity == null) return false;

        // Check if research is available and has all required items in slots
        return isResearchAvailable(research) && hasAllRequiredItemsInSlots(research);
    }

    // Start research process (with timer) - called from network packet handler
    public void startResearch(Research research) {
        HkbMod.LOGGER.info("[ResearchTableMenu] Starting research: " + research.getName());
        HkbMod.LOGGER.info("[ResearchTableMenu] BlockEntity is: " + (blockEntity != null ? "available" : "NULL"));
        HkbMod.LOGGER.info("[ResearchTableMenu] Player level isClientSide: " + player.level().isClientSide);

        // CRITICAL FIX: Only allow research to start on server side
        if (player.level().isClientSide) {
            HkbMod.LOGGER.info("[ResearchTableMenu] Called from client side - skipping research start (should use networking)");
            return;
        }

        if (blockEntity != null) {
            // Get items from slots for server-side checking
            NonNullList<ItemStack> slotItems = NonNullList.withSize(ResearchTableBlockEntity.RESEARCH_SLOTS, ItemStack.EMPTY);
            for (int i = 0; i < ResearchTableBlockEntity.RESEARCH_SLOTS; i++) {
                slotItems.set(i, this.slots.get(i).getItem().copy());
                HkbMod.LOGGER.info("[ResearchTableMenu] Slot " + i + ": " + slotItems.get(i));
            }

            if (ResearchManager.canUnlockResearchWithItems(research, playerResearchData, slotItems)) {
                // Start the research timer - we're guaranteed to be on server side now
                HkbMod.LOGGER.info("[ResearchTableMenu] Starting research timer on SERVER side: " + research.getName());
                blockEntity.startResearch(research);
            } else {
                HkbMod.LOGGER.warn("[ResearchTableMenu] Cannot start research - requirements not met: " + research.getName());
            }
        } else {
            HkbMod.LOGGER.warn("[ResearchTableMenu] Cannot start research - blockEntity is null");
        }
    }

    // Check if research completed and unlock it
    public void checkResearchCompletion() {
        if (blockEntity != null && blockEntity.getCurrentResearchId() != null && !blockEntity.isResearching()) {
            HkbMod.LOGGER.info("[ResearchTableMenu] Research completed, attempting to unlock: " + blockEntity.getCurrentResearchId());

            // Research completed, unlock it
            Research research = ResearchManager.getResearch(blockEntity.getCurrentResearchId());
            if (research != null) {
                HkbMod.LOGGER.info("[ResearchTableMenu] Attempting to unlock research with items: " + research.getName());

                if (ResearchManager.unlockResearchWithItems(research, playerResearchData, blockEntity.getResearchItems())) {
                    HkbMod.LOGGER.info("[ResearchTableMenu] Research successfully unlocked! Items consumed: " + research.getName());

                    // CRITICAL: Save the updated research data to persistent storage
                    PlayerResearchDataManager.savePlayerResearchData(player, playerResearchData);

                    // Research was successfully unlocked, items were consumed
                    blockEntity.cancelResearch(); // Clear research state
                    blockEntity.setChanged();
                } else {
                    HkbMod.LOGGER.warn("[ResearchTableMenu] Failed to unlock research: " + research.getName());
                }
            } else {
                HkbMod.LOGGER.warn("[ResearchTableMenu] Research not found: " + blockEntity.getCurrentResearchId());
            }
        }
    }

    public boolean hasAllRequiredItemsInSlots(Research research) {
        HkbMod.LOGGER.info("blockEntity: " + blockEntity + ", research: " + research);
        if (research == null) {
            return false;
        }

        List<ItemStack> requiredItems = research.getCosts();
        // Get items from slots instead of blockEntity (works on both client and server)
        List<ItemStack> availableItems = new ArrayList<>();
        for (int i = 0; i < ResearchTableBlockEntity.RESEARCH_SLOTS; i++) {
            availableItems.add(this.slots.get(i).getItem());
        }

        // Debug output
        System.out.println("DEBUG hasAllRequiredItemsInSlots for " + research.getName() + ":");
        System.out.println("  Required items: " + requiredItems.size());
        System.out.println("  Available slots: " + availableItems.size());

        // Check for each required item if enough is available in slots
        for (ItemStack required : requiredItems) {
            int neededAmount = required.getCount();
            int availableAmount = 0;

            System.out.println("  Looking for: " + neededAmount + "x " + required.getDisplayName().getString());

            // Count available amount in all slots using same comparison as ResearchManager
            for (int i = 0; i < availableItems.size(); i++) {
                ItemStack available = availableItems.get(i);
                if (!available.isEmpty()) {
                    System.out.println("    Slot " + i + ": " + available.getCount() + "x " + available.getDisplayName().getString());
                    if (ItemStack.isSameItemSameComponents(available, required)) {
                        availableAmount += available.getCount();
                        System.out.println("      -> MATCH! Total available now: " + availableAmount);
                    }
                }
            }

            // If not enough available, return false
            if (availableAmount < neededAmount) {
                System.out.println("  -> NOT ENOUGH! Need " + neededAmount + ", have " + availableAmount);
                return false;
            }
            System.out.println("  -> OK! Need " + neededAmount + ", have " + availableAmount);
        }

        System.out.println("  -> ALL ITEMS AVAILABLE!");
        return true;
    }

    // Legacy method for immediate unlocking (kept for compatibility)
    public void unlockResearch(Research research) {
        if (blockEntity != null && ResearchManager.unlockResearchWithItems(research, playerResearchData, blockEntity.getResearchItems())) {
            // Research was successfully unlocked, items were consumed
            blockEntity.setChanged();
        }
    }

    public List<Research> getAvailableResearches() {
        return ResearchManager.getAvailableResearches(selectedClass, playerResearchData);
    }

    public List<Research> getResearchesForCurrentClass() {
        return ResearchManager.getResearchesForClass(selectedClass);
    }

    public List<Research> getResearchesByTier(int tier) {
        return ResearchManager.getResearchesByTier(selectedClass, tier);
    }

    public ResearchManager.ResearchProgress getResearchProgress() {
        return ResearchManager.getResearchProgress(selectedClass, playerResearchData);
    }

    public ResearchTableBlockEntity getBlockEntity() {
        return blockEntity;
    }

    // Sync research state from server to client (for singleplayer)
    public void syncResearchState() {
        if (player.level().isClientSide && minecraft.getSingleplayerServer() != null) {
            // Get server-side player and menu
            var server = minecraft.getSingleplayerServer();
            var serverPlayer = server.getPlayerList().getPlayer(player.getUUID());
            if (serverPlayer != null && serverPlayer.containerMenu instanceof ResearchTableMenu serverMenu) {
                var serverBlockEntity = serverMenu.getBlockEntity();
                if (serverBlockEntity != null && blockEntity != null) {

                    // IMPORTANT: Check research completion on server side first
                    serverMenu.checkResearchCompletion();

                    // CRITICAL: Sync PlayerResearchData from server to client
                    var serverPlayerData = serverMenu.getPlayerResearchData();
                    if (serverPlayerData != null) {
                        // Copy unlocked researches from server to client
                        for (var researchId : serverPlayerData.getUnlockedResearches()) {
                            if (!this.playerResearchData.hasUnlockedResearch(researchId)) {
                                HkbMod.LOGGER.info("[ResearchTableMenu] Syncing unlocked research to client: " + researchId);
                                this.playerResearchData.unlockResearch(researchId);
                            }
                        }
                    }

                    // Sync research state to client-side block entity
                    if (serverBlockEntity.isResearching()) {
                        blockEntity.setResearchProgress(
                            serverBlockEntity.getResearchProgress(),
                            serverBlockEntity.getResearchDuration(),
                            serverBlockEntity.getCurrentResearchId()
                        );
                        HkbMod.LOGGER.debug("[ResearchTableMenu] Synced research state to client: " +
                            serverBlockEntity.getResearchProgress() + "/" + serverBlockEntity.getResearchDuration());
                    } else {
                        blockEntity.clearClientResearchState();
                    }
                }
            }
        }
    }

    // Add minecraft field access
    private final net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
}