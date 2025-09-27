package de.davidvogt.hkbmod.client.gui;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.block.entity.ResearchTableBlockEntity;
import de.davidvogt.hkbmod.client.gui.components.ResearchButton;
import de.davidvogt.hkbmod.menu.ResearchTableMenu;
import de.davidvogt.hkbmod.research.PlayerClass;
import de.davidvogt.hkbmod.research.Research;
import de.davidvogt.hkbmod.research.ResearchManager;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ResearchTableScreen extends AbstractContainerScreen<ResearchTableMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("hkbmod",
            "textures/gui/research_table.png");
    private static final int IMAGE_WIDTH = 256; // Wider for split layout
    private static final int IMAGE_HEIGHT = 222;

    private Button knightButton;
    private Button archerButton;
    private Button cavalierButton;
    private Button magicianButton;
    private Button startResearchButton;
    private List<ResearchButton> researchButtons = new ArrayList<>();
    private String statusMessage = "";

    public ResearchTableScreen(ResearchTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = IMAGE_WIDTH;
        this.imageHeight = IMAGE_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Class selection buttons - arranged in a 4x1 grid at the top
        knightButton = addRenderableWidget(Button.builder(
                        Component.literal("Knight"),
                        button -> selectClass(PlayerClass.KNIGHT))
                .bounds(x + 8, y + 20, 58, 18)
                .build());

        archerButton = addRenderableWidget(Button.builder(
                        Component.literal("Archer"),
                        button -> selectClass(PlayerClass.ARCHER))
                .bounds(x + 70, y + 20, 58, 18)
                .build());

        cavalierButton = addRenderableWidget(Button.builder(
                        Component.literal("Cavalier"),
                        button -> selectClass(PlayerClass.CAVALIER))
                .bounds(x + 132, y + 20, 58, 18)
                .build());

        magicianButton = addRenderableWidget(Button.builder(
                        Component.literal("Magician"),
                        button -> selectClass(PlayerClass.MAGICIAN))
                .bounds(x + 194, y + 20, 58, 18)
                .build());

        // Start Research button (positioned below research details)
        startResearchButton = addRenderableWidget(Button.builder(
                        Component.literal("Start Research"),
                        button -> startSelectedResearch())
                .bounds(x + 128, y + 120, 100, 20)
                .build());

        updateButtonStates();
        updateResearchButtons();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Sync research state from server to client
        menu.syncResearchState();
        menu.checkResearchCompletion();
        updateButtonStates(); // Diese Zeile hinzufügen
        updateResearchButtons(); // Refresh research buttons to show newly available researches
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void selectClass(PlayerClass playerClass) {
        menu.setSelectedClass(playerClass);
        updateButtonStates();
        updateResearchButtons();
    }

    private void updateButtonStates() {
        PlayerClass selected = menu.getSelectedClass();

        knightButton.active = selected != PlayerClass.KNIGHT;
        archerButton.active = selected != PlayerClass.ARCHER;
        cavalierButton.active = selected != PlayerClass.CAVALIER;
        magicianButton.active = selected != PlayerClass.MAGICIAN;

        // Update start research button
        Research selectedResearch = menu.getSelectedResearch();
        boolean isResearching = menu.getBlockEntity() != null && menu.getBlockEntity().isResearching();

        if (isResearching) {
            // Show cancel research button when researching
            startResearchButton.setMessage(Component.literal("Cancel Research"));
            startResearchButton.visible = true;
            startResearchButton.active = true;
            statusMessage = "";
        } else if (selectedResearch != null) {
            // Research is selected - button is always visible and clickable for available research
            startResearchButton.setMessage(Component.literal("Start Research"));
            startResearchButton.visible = true;
            startResearchButton.active = menu.isResearchAvailable(selectedResearch);

            // Clear status message when research changes
            statusMessage = "";
        } else {
            startResearchButton.visible = false;
            statusMessage = "";
        }
    }

    private void startSelectedResearch() {
        // Check research status - client-side check
        boolean isResearching = menu.getBlockEntity() != null && menu.getBlockEntity().isResearching();

        if (isResearching) {
            // Cancel current research (direct call for singleplayer)
            HkbMod.LOGGER.info("Cancelling research directly");

            // Cancel on client-side
            if (menu.getBlockEntity() != null) {
                menu.getBlockEntity().cancelResearch();
            }

            // Also cancel on server-side in singleplayer
            if (minecraft.getSingleplayerServer() != null) {
                var server = minecraft.getSingleplayerServer();
                var serverPlayer = server.getPlayerList().getPlayer(minecraft.player.getUUID());
                if (serverPlayer != null && serverPlayer.containerMenu instanceof ResearchTableMenu serverMenu) {
                    HkbMod.LOGGER.info("Cancelling research on server side");
                    if (serverMenu.getBlockEntity() != null) {
                        serverMenu.getBlockEntity().cancelResearch();
                    }
                }
            }

            statusMessage = "";
        } else {
            // Start new research
            Research selectedResearch = menu.getSelectedResearch();
            HkbMod.LOGGER.info("Selected research: " + (selectedResearch != null ? selectedResearch.getName() : "null"));
            if (selectedResearch != null && menu.isResearchAvailable(selectedResearch)) {
                HkbMod.LOGGER.info("Attempting to start research: " + selectedResearch.getName());
                // Check if required items are in slots
                if (menu.hasAllRequiredItemsInSlots(selectedResearch)) {
                    HkbMod.LOGGER.info("Starting research directly: " + selectedResearch.getName());

                    // Call client-side menu first
                    menu.startResearch(selectedResearch);

                    // In singleplayer, also trigger server-side menu
                    if (minecraft.player != null && minecraft.level != null && !minecraft.level.isClientSide) {
                        HkbMod.LOGGER.info("Also calling server-side menu");
                        // This should be the server-side menu
                        if (minecraft.player.containerMenu instanceof ResearchTableMenu serverMenu) {
                            serverMenu.startResearch(selectedResearch);
                        }
                    } else {
                        // In singleplayer, client and server run together, so we need to access the server player
                        HkbMod.LOGGER.info("Accessing server-side through integrated server");
                        var server = minecraft.getSingleplayerServer();
                        if (server != null) {
                            var serverPlayer = server.getPlayerList().getPlayer(minecraft.player.getUUID());
                            if (serverPlayer != null && serverPlayer.containerMenu instanceof ResearchTableMenu serverMenu) {
                                HkbMod.LOGGER.info("Calling server-side menu for research start");
                                serverMenu.startResearch(selectedResearch);
                            }
                        }
                    }

                    statusMessage = "";
                } else {
                    HkbMod.LOGGER.info("Cannot start research, missing materials: " + selectedResearch.getName());
                    // Display missing materials message
                    statusMessage = "Missing required materials!";
                }
            }
        }
    }

    private void updateResearchButtons() {
        // Remove old research buttons
        for (ResearchButton button : researchButtons) {
            removeWidget(button);
        }
        researchButtons.clear();

        // Add research buttons for current class in a better layout
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        List<Research> researches = menu.getResearchesForCurrentClass();

        // Group researches by tier for better layout
        List<Research> tier0 = new ArrayList<>();
        List<Research> tier1 = new ArrayList<>();
        List<Research> tier2 = new ArrayList<>();
        List<Research> tier3 = new ArrayList<>();

        for (Research research : researches) {
            switch (research.getTier()) {
                case 0 -> tier0.add(research);
                case 1 -> tier1.add(research);
                case 2 -> tier2.add(research);
                case 3 -> tier3.add(research);
            }
        }

        // Research buttons on the right side (starting at x + 130)
        // Layout tier 0 research (foundation row)
        layoutTierResearches(tier0, x + 130, y + 50, 0);
        // Layout tier 1 research (basic row)
        layoutTierResearches(tier1, x + 130, y + 75, 1);
        // Layout tier 2 research (advanced row)
        layoutTierResearches(tier2, x + 130, y + 100, 2);
        // Layout tier 3 research (master row)
        layoutTierResearches(tier3, x + 130, y + 125, 3);

        // Update button states after refreshing research buttons
        updateButtonStates();
    }

    private void layoutTierResearches(List<Research> researches, int startX, int startY, int tier) {
        int buttonWidth = 60; // Smaller to fit in right side
        int buttonHeight = 20;
        int spacing = 4;

        for (int i = 0; i < researches.size() && i < 2; i++) { // Max 2 per tier
            Research research = researches.get(i);
            boolean isCompleted = menu.getPlayerResearchData().hasUnlockedResearch(research.getId());
            boolean isAvailable = menu.isResearchAvailable(research);

            // For the ResearchButton constructor:
            // canUnlock parameter determines if research can be started (blue button)
            // isCompleted parameter determines if research is completed (green button)
            boolean canUnlock = isAvailable; // Can research if available (not completed and prerequisites met)

            int buttonX = startX + i * (buttonWidth + spacing);
            int buttonY = startY;

            ResearchButton researchButton = new ResearchButton(
                    research, buttonX, buttonY, buttonWidth, buttonHeight,
                    canUnlock, isCompleted,
                    button -> {
                        if (isAvailable || isCompleted) {
                            // Select the research to show its details (can select both available and completed)
                            menu.setSelectedResearch(research);
                            updateResearchButtons(); // Refresh to show selection
                        }
                    }
            );

            // Set selection state
            researchButton.setSelected(research.equals(menu.getSelectedResearch()));

            researchButtons.add(researchButton);
            addRenderableWidget(researchButton);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Main background - darker, more professional
        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF2C2C2C);

        // Header section for class selection
        guiGraphics.fill(x + 4, y + 16, x + imageWidth - 4, y + 42, 0xFF404040);

        // Left side - Research materials section
        guiGraphics.fill(x + 4, y + 46, x + 120, y + 134, 0xFF505050);

        // Right side - Research tree section
        guiGraphics.fill(x + 124, y + 46, x + imageWidth - 4, y + 134, 0xFF353535);

        // Player inventory section
        guiGraphics.fill(x + 4, y + 138, x + imageWidth - 4, y + imageHeight - 4, 0xFF454545);

        // Draw borders
        drawBorder(guiGraphics, x, y, imageWidth, imageHeight, 0xFF666666);
        drawBorder(guiGraphics, x + 4, y + 16, imageWidth - 8, 26, 0xFF888888); // Header
        drawBorder(guiGraphics, x + 4, y + 46, 116, 88, 0xFF888888); // Left side (materials)
        drawBorder(guiGraphics, x + 124, y + 46, imageWidth - 128, 88, 0xFF888888); // Right side (research)

        int columns = 3;
        int rows = 3;
        int slotSize = 18;
        int startX = x + 26;
        int startY = y + 50;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int slotX = startX + col * slotSize - 1; // -1 wegen Rahmen
                int slotY = startY + row * slotSize - 1;
                guiGraphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF222222);
                drawBorder(guiGraphics, slotX, slotY, 18, 18, 0xFF666666);
            }
        }

    }

    private void drawBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + 1, color); // Top
        guiGraphics.fill(x, y + height - 1, x + width, y + height, color); // Bottom
        guiGraphics.fill(x, y, x + 1, y + height, color); // Left
        guiGraphics.fill(x + width - 1, y, x + width, y + height, color); // Right
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Title
        Component title = Component.literal("Research Table").withStyle(style -> style.withColor(0xFFFFFF));
        int titleWidth = this.font.width(title);
        guiGraphics.drawString(this.font, title, (imageWidth - titleWidth) / 2, 6, 0xFFFFFF, false);

        // Left side labels
        guiGraphics.drawString(this.font, Component.literal("Research Materials"), 8, 48, 0xFFFFFF, false);

        // Right side labels
        guiGraphics.drawString(this.font, Component.literal("Research Tree"), 128, 48, 0xFFFFFF, false);

        // Check if research is in progress and show progress or selection details
        boolean isResearching = menu.getBlockEntity() != null && menu.getBlockEntity().isResearching();

        if (isResearching) {
            // Show research progress
            ResourceLocation currentResearchId = menu.getBlockEntity().getCurrentResearchId();
            Research currentResearch = ResearchManager.getResearch(currentResearchId);

            if (currentResearch != null) {
                guiGraphics.drawString(this.font, Component.literal("Researching:"), 128, 52, 0xFFFFFF, false);

                // Research name
                guiGraphics.drawString(this.font, Component.literal(currentResearch.getName()), 128, 62, 0xFFD700, false);

                // Progress bar
                int barWidth = 100;
                int barHeight = 8;
                int barX = 128;
                int barY = 75;

                // Background
                guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF444444);

                // Progress fill
                float progress = menu.getBlockEntity().getResearchProgressPercentage();
                int progressWidth = (int) (barWidth * progress);
                guiGraphics.fill(barX, barY, barX + progressWidth, barY + barHeight, 0xFF00FF00);

                // Progress border
                drawBorder(guiGraphics, barX, barY, barWidth, barHeight, 0xFFFFFFFF);

                // Time remaining
                int remainingSeconds = menu.getBlockEntity().getRemainingResearchSeconds();
                String timeText = "Time: " + remainingSeconds + "s";
                guiGraphics.drawString(this.font, Component.literal(timeText), 128, 90, 0xFFFFFF, false);
            }
        } else {
            // Show status message if any
            if (!statusMessage.isEmpty()) {
                guiGraphics.drawString(this.font, Component.literal(statusMessage), 128, 145, 0xFF4444, false);
            }
            Research selectedResearch = menu.getSelectedResearch();
            if (selectedResearch != null) {
                // Show selected research details
                guiGraphics.drawString(this.font, Component.literal("Selected Research:"), 128, 52, 0xFFFFFF, false);

                // Research name with tier color
                Component nameComponent = Component.literal(selectedResearch.getName()).withStyle(selectedResearch.getType().getColor());
                guiGraphics.drawString(this.font, nameComponent, 128, 62, 0xFFFFFF, false);

                // Tier and type info
                String tierText = "Tier " + selectedResearch.getTier() + " | " + selectedResearch.getType().name();
                guiGraphics.drawString(this.font, Component.literal(tierText), 128, 72, 0xFFD700, false);

                // Description (wrap text if needed)
                String description = selectedResearch.getDescription();
                if (description.length() > 18) {
                    description = description.substring(0, 15) + "...";
                }
                guiGraphics.drawString(this.font, Component.literal(description), 128, 82, 0xAAAAAA, false);

                // Required materials
                if (!selectedResearch.getCosts().isEmpty()) {
                    guiGraphics.drawString(this.font, Component.literal("Required:"), 128, 95, 0xFFFFFF, false);
                    int yOffset = 105;
                    for (ItemStack cost : selectedResearch.getCosts()) {
                        String costText = cost.getCount() + "x " + cost.getHoverName().getString();
                        if (costText.length() > 16) {
                            costText = costText.substring(0, 13) + "...";
                        }
                        guiGraphics.drawString(this.font, Component.literal("• " + costText), 128, yOffset, 0xCCCCCC, false);
                        yOffset += 10;
                    }
                }
            } else {
                // Show research tier labels when nothing is selected
                guiGraphics.drawString(this.font, Component.literal("Tier 0"), 128, 52, 0xFFFFFFFF, false); // White (Foundation)
                guiGraphics.drawString(this.font, Component.literal("Tier 1"), 128, 77, 0xFFD700, false); // Gold
                guiGraphics.drawString(this.font, Component.literal("Tier 2"), 128, 102, 0xFF87CEEB, false); // Sky blue
                guiGraphics.drawString(this.font, Component.literal("Tier 3"), 128, 127, 0xFFFF6347, false); // Tomato
            }
        }

        // Player inventory label
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 80, 0xFFFFFF, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);

        // Add research button tooltips
        for (ResearchButton button : researchButtons) {
            if (button.isHoveredOrFocused()) {
                List<Component> tooltip = new ArrayList<>();
                Research research = button.getResearch();

                tooltip.add(Component.literal(research.getName()).withStyle(research.getType().getColor()));
                tooltip.add(Component.literal(research.getDescription()).withStyle(style -> style.withColor(0xAAAAAA)));
                tooltip.add(Component.literal("Tier: " + research.getTier()).withStyle(style -> style.withColor(0xFFD700)));

                if (!research.getCosts().isEmpty()) {
                    tooltip.add(Component.literal("Costs:").withStyle(style -> style.withColor(0xFFFFFF)));
                    for (var cost : research.getCosts()) {
                        tooltip.add(Component.literal("• " + cost.getCount() + "x " + cost.getHoverName().getString())
                                .withStyle(style -> style.withColor(0xCCCCCC)));
                    }
                }

                String status;
                int statusColor;
                if (button.isUnlocked()) {
                    status = "✓ Unlocked";
                    statusColor = 0x00FF00;
                } else if (button.canUnlock()) {
                    status = "⚡ Ready to unlock";
                    statusColor = 0x00AAFF;
                } else {
                    status = "✗ Locked";
                    statusColor = 0xFF4444;
                }
                tooltip.add(Component.literal(status).withStyle(style -> style.withColor(statusColor)));

                guiGraphics.renderComponentTooltip(this.font, tooltip, x, y, ItemStack.EMPTY);
                break;
            }
        }
    }
}