package de.davidvogt.hkbmod.client.gui;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.client.gui.components.ResearchButton;
import de.davidvogt.hkbmod.client.gui.layout.ResearchTableLayout;
import de.davidvogt.hkbmod.client.gui.panels.MaterialsPanel;
import de.davidvogt.hkbmod.client.gui.panels.ResearchInfoPanel;
import de.davidvogt.hkbmod.client.gui.panels.ResearchTreePanel;
import de.davidvogt.hkbmod.menu.ResearchTableMenu;
import de.davidvogt.hkbmod.research.PlayerClass;
import de.davidvogt.hkbmod.research.Research;
import de.davidvogt.hkbmod.research.ResearchManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class ResearchTableScreen extends AbstractContainerScreen<ResearchTableMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("hkbmod",
            "textures/gui/research_table.png");

    // Use layout constants from modular layout class
    private static final int IMAGE_WIDTH = ResearchTableLayout.WINDOW_WIDTH;
    private static final int IMAGE_HEIGHT = ResearchTableLayout.WINDOW_HEIGHT; // Now 248 for bigger panels

    // UI Komponenten
    private Button knightButton, archerButton, cavalierButton, magicianButton;
    private Button startResearchButton;
    private String statusMessage = "";
    private int statusMessageColor = ResearchTableLayout.COLOR_TEXT;

    // Animation
    private float animationTick = 0;

    // Modular UI panels
    private MaterialsPanel materialsPanel;
    private ResearchTreePanel researchTreePanel;
    private ResearchInfoPanel researchInfoPanel;

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

        // Initialize modular panels
        initializeModularPanels(x, y);

        // Verbesserte Klassen-Auswahl Buttons mit modernem Styling
        createClassButtons(x, y);

        // Start/Cancel Research Button mit verbessertem Design
        ResearchTableLayout.ButtonPosition startButtonPos = ResearchTableLayout.calculateStartButtonPosition(x, y);
        startResearchButton = addRenderableWidget(Button.builder(
                        Component.literal("Start Research"),
                        button -> startSelectedResearch())
                .bounds(startButtonPos.x, startButtonPos.y, startButtonPos.width, startButtonPos.height)
                .build());

        updateButtonStates();
        updateResearchButtons();
    }

    private void initializeModularPanels(int x, int y) {
        // Initialize materials panel
        materialsPanel = new MaterialsPanel(x, y);
        addRenderableWidget(materialsPanel);

        // Initialize research tree panel with callbacks
        researchTreePanel = new ResearchTreePanel(x, y);
        researchTreePanel.setIsResearchAvailable(research -> menu.isResearchAvailable(research));
        researchTreePanel.setIsResearchCompleted(research -> menu.getPlayerResearchData().hasUnlockedResearch(research.getId()));
        researchTreePanel.setIsResearchSelected(research -> research.equals(menu.getSelectedResearch()));
        researchTreePanel.setOnResearchSelected(research -> {
            menu.setSelectedResearch(research);
            updateResearchButtons();
        });
        addRenderableWidget(researchTreePanel);

        // Initialize research info panel with data providers
        researchInfoPanel = new ResearchInfoPanel(x, y);
        researchInfoPanel.setSelectedResearchProvider(() -> menu.getSelectedResearch());
        researchInfoPanel.setCurrentResearchProvider(() -> {
            if (menu.getBlockEntity() != null && menu.getBlockEntity().getCurrentResearchId() != null) {
                return ResearchManager.getResearch(menu.getBlockEntity().getCurrentResearchId());
            }
            return null;
        });
        researchInfoPanel.setIsResearchingProvider(() -> menu.getBlockEntity() != null && menu.getBlockEntity().isResearching());
        researchInfoPanel.setResearchProgressProvider(() -> menu.getBlockEntity() != null ? menu.getBlockEntity().getResearchProgressPercentage() : 0.0f);
        researchInfoPanel.setRemainingTimeProvider(() -> menu.getBlockEntity() != null ? menu.getBlockEntity().getRemainingResearchSeconds() : 0);
        addRenderableWidget(researchInfoPanel);
    }

    private void createClassButtons(int x, int y) {
        ResearchTableLayout.ButtonLayout layout = ResearchTableLayout.calculateClassButtonLayout(x, y);
        int buttonWidth = layout.buttonWidth;
        int buttonHeight = layout.buttonHeight;
        int spacing = layout.spacing;
        int startX = layout.startX;
        int startY = layout.startY;

        knightButton = addRenderableWidget(Button.builder(
                        Component.literal("⚔ Knight"),
                        button -> selectClass(PlayerClass.KNIGHT))
                .bounds(startX, startY, buttonWidth, buttonHeight)
                .build());

        archerButton = addRenderableWidget(Button.builder(
                        Component.literal("🏹 Archer"),
                        button -> selectClass(PlayerClass.ARCHER))
                .bounds(startX + (buttonWidth + spacing), startY, buttonWidth, buttonHeight)
                .build());

        cavalierButton = addRenderableWidget(Button.builder(
                        Component.literal("🐎 Cavalier"),
                        button -> selectClass(PlayerClass.CAVALIER))
                .bounds(startX + 2 * (buttonWidth + spacing), startY, buttonWidth, buttonHeight)
                .build());

        magicianButton = addRenderableWidget(Button.builder(
                        Component.literal("🔮 Magician"),
                        button -> selectClass(PlayerClass.MAGICIAN))
                .bounds(startX + 3 * (buttonWidth + spacing), startY, buttonWidth, buttonHeight)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        animationTick += partialTick;

        // Sync research state from server to client
        menu.syncResearchState();
        menu.checkResearchCompletion();
        updateButtonStates();
        updateResearchButtons();

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Erweiterte Tooltip-Funktionalität
        renderAdvancedTooltips(guiGraphics, mouseX, mouseY);
    }

    private void renderAdvancedTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Verbesserte Tooltips für Research Buttons
        if (researchTreePanel != null) {
            for (ResearchButton button : researchTreePanel.getResearchButtons()) {
            if (button.isHovered() && mouseX >= button.getX() && mouseY >= button.getY() &&
                    mouseX < button.getX() + button.getWidth() && mouseY < button.getY() + button.getHeight()) {

                Research research = button.getResearch();
                List<Component> tooltip = new ArrayList<>();

                // Forschungsname mit Farbe nach Typ - FIXED: Verwende ChatFormatting direkt
                tooltip.add(Component.literal(research.getName())
                        .withStyle(research.getType().getColor()));

                // Tier und Typ
                tooltip.add(Component.literal("Tier " + research.getTier() + " " + research.getType().name())
                        .withStyle(s -> s.withColor(ResearchTableLayout.COLOR_TEXT_DIM)));

                // Status
                boolean isCompleted = menu.getPlayerResearchData().hasUnlockedResearch(research.getId());
                boolean isAvailable = menu.isResearchAvailable(research);

                if (isCompleted) {
                    tooltip.add(Component.literal("✓ Completed").withStyle(s -> s.withColor(ResearchTableLayout.COLOR_SUCCESS)));
                } else if (isAvailable) {
                    tooltip.add(Component.literal("Available").withStyle(s -> s.withColor(ResearchTableLayout.COLOR_ACCENT)));
                } else {
                    tooltip.add(Component.literal("Locked").withStyle(s -> s.withColor(ResearchTableLayout.COLOR_ERROR)));
                }

                // Beschreibung
                tooltip.add(Component.literal(""));
                String[] descLines = research.getDescription().split("\\n");
                for (String line : descLines) {
                    tooltip.add(Component.literal(line).withStyle(s -> s.withColor(ResearchTableLayout.COLOR_TEXT_DIM)));
                }

                // Anforderungen - FIXED: getCosts() returns List<ItemStack>, not Map
                if (!research.getCosts().isEmpty()) {
                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.literal("Requirements:").withStyle(s -> s.withColor(ResearchTableLayout.COLOR_WARNING)));
                    for (ItemStack costItem : research.getCosts()) {
                        tooltip.add(Component.literal("• " + costItem.getCount() + "x " + costItem.getHoverName().getString())
                                .withStyle(s -> s.withColor(ResearchTableLayout.COLOR_TEXT_DIM)));
                    }
                }

                // FIXED: Use the correct overload that accepts List<Component>
                guiGraphics.renderTooltip(this.font, tooltip.stream().map(Component::getVisualOrderText).map(text -> ClientTooltipComponent.create(text)).toList(), mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
                break;
            }
        }
        }
    }

    private void selectClass(PlayerClass playerClass) {
        menu.setSelectedClass(playerClass);
        if (researchTreePanel != null) {
            researchTreePanel.resetScroll(); // Reset scroll when changing class
        }
        updateButtonStates();
        updateResearchButtons();
    }

    private void updateButtonStates() {
        PlayerClass selected = menu.getSelectedClass();

        // Klassen-Button Styling
        updateClassButtonStyle(knightButton, selected == PlayerClass.KNIGHT);
        updateClassButtonStyle(archerButton, selected == PlayerClass.ARCHER);
        updateClassButtonStyle(cavalierButton, selected == PlayerClass.CAVALIER);
        updateClassButtonStyle(magicianButton, selected == PlayerClass.MAGICIAN);

        // Start Research Button Update
        Research selectedResearch = menu.getSelectedResearch();
        boolean isResearching = menu.getBlockEntity() != null && menu.getBlockEntity().isResearching();

        if (isResearching) {
            startResearchButton.setMessage(Component.literal("⏹ Cancel Research"));
            startResearchButton.visible = true;
            startResearchButton.active = true;
            statusMessage = "Research in progress...";
            statusMessageColor = ResearchTableLayout.COLOR_ACCENT;
        } else if (selectedResearch != null) {
            startResearchButton.setMessage(Component.literal("▶ Start Research"));
            startResearchButton.visible = true;
            startResearchButton.active = menu.isResearchAvailable(selectedResearch);

            if (menu.canUnlockResearch(selectedResearch)) {
                statusMessage = "Ready to start research";
                statusMessageColor = ResearchTableLayout.COLOR_SUCCESS;
            } else if (!menu.hasAllRequiredItemsInSlots(selectedResearch)) {
                statusMessage = "Missing required materials";
                statusMessageColor = ResearchTableLayout.COLOR_ERROR;
            } else {
                statusMessage = "Prerequisites not met";
                statusMessageColor = ResearchTableLayout.COLOR_WARNING;
            }
        } else {
            startResearchButton.visible = true;
            startResearchButton.active = false;
            startResearchButton.setMessage(Component.literal("Select Research"));
            statusMessage = "Select a research to begin";
            statusMessageColor = ResearchTableLayout.COLOR_TEXT_DIM;
        }
    }

    private void updateClassButtonStyle(Button button, boolean isSelected) {
        // Visual feedback für ausgewählte Klasse - wird in renderBg() gehandhabt
        button.active = !isSelected;
    }

    private void startSelectedResearch() {
        boolean isResearching = menu.getBlockEntity() != null && menu.getBlockEntity().isResearching();

        if (isResearching) {
            // Cancel research logic (existing implementation)
            HkbMod.LOGGER.info("Cancelling research directly");

            if (menu.getBlockEntity() != null) {
                menu.getBlockEntity().cancelResearch();
            }

            if (minecraft.getSingleplayerServer() != null) {
                var server = minecraft.getSingleplayerServer();
                var serverPlayer = server.getPlayerList().getPlayer(minecraft.player.getUUID());
                if (serverPlayer != null && serverPlayer.containerMenu instanceof ResearchTableMenu serverMenu) {
                    if (serverMenu.getBlockEntity() != null) {
                        serverMenu.getBlockEntity().cancelResearch();
                    }
                }
            }
            statusMessage = "Research cancelled";
            statusMessageColor = ResearchTableLayout.COLOR_WARNING;
        } else {
            // Start research logic (existing implementation with improved feedback)
            Research selectedResearch = menu.getSelectedResearch();
            if (selectedResearch != null && menu.isResearchAvailable(selectedResearch)) {
                if (menu.hasAllRequiredItemsInSlots(selectedResearch)) {
                    menu.startResearch(selectedResearch);

                    if (minecraft.getSingleplayerServer() != null) {
                        var server = minecraft.getSingleplayerServer();
                        var serverPlayer = server.getPlayerList().getPlayer(minecraft.player.getUUID());
                        if (serverPlayer != null && serverPlayer.containerMenu instanceof ResearchTableMenu serverMenu) {
                            serverMenu.startResearch(selectedResearch);
                        }
                    }
                    statusMessage = "Research started: " + selectedResearch.getName();
                    statusMessageColor = ResearchTableLayout.COLOR_SUCCESS;
                } else {
                    statusMessage = "Missing required materials!";
                    statusMessageColor = ResearchTableLayout.COLOR_ERROR;
                }
            }
        }
    }

    private void updateResearchButtons() {
        List<Research> researches = menu.getResearchesForCurrentClass();

        if (researchTreePanel != null) {
            researchTreePanel.updateResearchButtons(
                researches,
                this::addRenderableWidget,
                this::removeWidget
            );
        }
    }

    // This method is now handled by ResearchTreePanel

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Haupthintergrund
        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, ResearchTableLayout.COLOR_BACKGROUND);

        // Header Panel - consistent 5px margin from top
        drawModernPanel(guiGraphics, x + ResearchTableLayout.PANEL_MARGIN, y + ResearchTableLayout.HEADER_Y,
                        imageWidth - 2 * ResearchTableLayout.PANEL_MARGIN, ResearchTableLayout.HEADER_PANEL_HEIGHT,
                        ResearchTableLayout.COLOR_PANEL);

        // Materials Panel - consistent spacing
        drawModernPanel(guiGraphics, x + ResearchTableLayout.MATERIALS_X, y + ResearchTableLayout.MATERIALS_Y,
                        ResearchTableLayout.MATERIALS_PANEL_WIDTH, ResearchTableLayout.MATERIALS_PANEL_HEIGHT,
                        ResearchTableLayout.COLOR_PANEL);

        // Research Tree Panel - consistent spacing
        drawModernPanel(guiGraphics, x + ResearchTableLayout.RESEARCH_TREE_X, y + ResearchTableLayout.RESEARCH_TREE_Y,
                        ResearchTableLayout.RESEARCH_TREE_PANEL_WIDTH, ResearchTableLayout.RESEARCH_TREE_PANEL_HEIGHT,
                        ResearchTableLayout.COLOR_PANEL_LIGHT);

        // Player Inventory Panel - consistent spacing
        ResearchTableLayout.PanelArea inventoryArea = ResearchTableLayout.calculateInventoryPanelArea(x, y);
        drawModernPanel(guiGraphics, inventoryArea.x, inventoryArea.y, inventoryArea.width, inventoryArea.height,
                        ResearchTableLayout.COLOR_PANEL);

        // Draw inventory slot grids
        drawInventorySlotGrids(guiGraphics, x, y);

        // Research Control Panel - consistent spacing
        ResearchTableLayout.PanelArea controlArea = ResearchTableLayout.calculateResearchControlArea(x, y);
        drawModernPanel(guiGraphics, controlArea.x, controlArea.y, controlArea.width, controlArea.height,
                        ResearchTableLayout.COLOR_PANEL);

        // Render research tree connections
        if (researchTreePanel != null) {
            List<Research> researches = menu.getResearchesForCurrentClass();
            researchTreePanel.renderConnections(guiGraphics, researches);
        }

        // Zeichne ausgewählte Klasse Highlight
        highlightSelectedClass(guiGraphics, x, y);
    }

    private void drawModernPanel(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        // Panel Hintergrund
        guiGraphics.fill(x, y, x + width, y + height, color);

        // Subtiler Rahmen
        guiGraphics.fill(x, y, x + width, y + 1, ResearchTableLayout.COLOR_BORDER); // Top
        guiGraphics.fill(x, y + height - 1, x + width, y + height, ResearchTableLayout.COLOR_BORDER); // Bottom
        guiGraphics.fill(x, y, x + 1, y + height, ResearchTableLayout.COLOR_BORDER); // Left
        guiGraphics.fill(x + width - 1, y, x + width, y + height, ResearchTableLayout.COLOR_BORDER); // Right
    }

    private void drawInventorySlotGrids(GuiGraphics guiGraphics, int x, int y) {
        ResearchTableLayout.InventoryGridPositions positions = ResearchTableLayout.calculateInventoryGridPositions(x, y);

        // Draw main inventory slots (3x9 grid)
        for (int row = 0; row < ResearchTableLayout.INVENTORY_ROWS; row++) {
            for (int col = 0; col < ResearchTableLayout.INVENTORY_SLOTS_PER_ROW; col++) {
                int slotX = positions.mainInventoryX + col * ResearchTableLayout.INVENTORY_SLOT_SIZE;
                int slotY = positions.mainInventoryY + row * ResearchTableLayout.INVENTORY_SLOT_SIZE;

                drawInventorySlot(guiGraphics, slotX, slotY);
            }
        }

        // Draw hotbar slots (1x9 grid)
        for (int col = 0; col < ResearchTableLayout.HOTBAR_SLOTS; col++) {
            int slotX = positions.hotbarX + col * ResearchTableLayout.INVENTORY_SLOT_SIZE;
            int slotY = positions.hotbarY;

            drawInventorySlot(guiGraphics, slotX, slotY);
        }
    }

    private void drawInventorySlot(GuiGraphics guiGraphics, int slotX, int slotY) {
        // Slot border (slightly darker than panel border)
        guiGraphics.fill(
            slotX - 1, slotY - 1,
            slotX + ResearchTableLayout.INVENTORY_SLOT_SIZE + 1,
            slotY + ResearchTableLayout.INVENTORY_SLOT_SIZE + 1,
            ResearchTableLayout.COLOR_BORDER
        );

        // Slot interior (dark background)
        guiGraphics.fill(
            slotX, slotY,
            slotX + ResearchTableLayout.INVENTORY_SLOT_SIZE,
            slotY + ResearchTableLayout.INVENTORY_SLOT_SIZE,
            0xFF000000
        );
    }

    // This method is now handled by ResearchTreePanel

    private void highlightSelectedClass(GuiGraphics guiGraphics, int x, int y) {
        PlayerClass selected = menu.getSelectedClass();
        int buttonIndex = switch (selected) {
            case KNIGHT -> 0;
            case ARCHER -> 1;
            case CAVALIER -> 2;
            case MAGICIAN -> 3;
        };

        // Use the same layout calculation as createClassButtons
        ResearchTableLayout.ButtonLayout layout = ResearchTableLayout.calculateClassButtonLayout(x, y);
        int buttonWidth = layout.buttonWidth;
        int buttonHeight = layout.buttonHeight;
        int spacing = layout.spacing;
        int startX = layout.startX;
        int startY = layout.startY;

        // Calculate the exact button position
        int buttonX = startX + buttonIndex * (buttonWidth + spacing);
        int buttonY = startY;

        // Draw highlight border around the actual button
        guiGraphics.fill(buttonX - 2, buttonY - 2, buttonX + buttonWidth + 2, buttonY, ResearchTableLayout.COLOR_ACCENT); // Top
        guiGraphics.fill(buttonX - 2, buttonY + buttonHeight, buttonX + buttonWidth + 2, buttonY + buttonHeight + 2, ResearchTableLayout.COLOR_ACCENT); // Bottom
        guiGraphics.fill(buttonX - 2, buttonY, buttonX, buttonY + buttonHeight, ResearchTableLayout.COLOR_ACCENT); // Left
        guiGraphics.fill(buttonX + buttonWidth, buttonY, buttonX + buttonWidth + 2, buttonY + buttonHeight, ResearchTableLayout.COLOR_ACCENT); // Right
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Research Table title - positioned at top with consistent spacing to window edge
        Component title = Component.literal("Research Table").withStyle(s -> s.withColor(ResearchTableLayout.COLOR_TEXT));
        int titleWidth = this.font.width(title);
        int titleX = (imageWidth - titleWidth) / 2;
        int titleY = ResearchTableLayout.TITLE_Y; // Same spacing as panel margins

        // Shadow
        guiGraphics.drawString(this.font, title, titleX + 1, titleY + 1, 0x55000000, false);
        // Text
        guiGraphics.drawString(this.font, title, titleX, titleY, ResearchTableLayout.COLOR_TEXT, false);

        // Materials label - consistent spacing from panel top
        Component materialsLabel = Component.literal("Materials");
        int materialsLabelWidth = this.font.width(materialsLabel);
        int materialsPanelX = ResearchTableLayout.MATERIALS_X;
        int materialsPanelWidth = ResearchTableLayout.MATERIALS_PANEL_WIDTH;
        int materialsCenterX = materialsPanelX + (materialsPanelWidth - materialsLabelWidth) / 2;
        guiGraphics.drawString(this.font, materialsLabel, materialsCenterX, ResearchTableLayout.MATERIALS_Y + ResearchTableLayout.PANEL_MARGIN, ResearchTableLayout.COLOR_TEXT_DIM, false);

        // Research Tree label - consistent spacing from panel top
        guiGraphics.drawString(this.font, Component.literal("Research Tree"), ResearchTableLayout.RESEARCH_TREE_X + ResearchTableLayout.PANEL_MARGIN, ResearchTableLayout.RESEARCH_TREE_Y + ResearchTableLayout.PANEL_MARGIN, ResearchTableLayout.COLOR_TEXT_DIM, false);

        // Research Control label - consistent spacing from panel top
        guiGraphics.drawString(this.font, Component.literal("Research"), ResearchTableLayout.RESEARCH_CONTROL_X + ResearchTableLayout.PANEL_MARGIN, ResearchTableLayout.RESEARCH_CONTROL_Y + ResearchTableLayout.PANEL_MARGIN, ResearchTableLayout.COLOR_TEXT_DIM, false);

        // Status Message - positioned at bottom of Research Tree Panel with consistent spacing
        if (!statusMessage.isEmpty()) {
            int statusX = ResearchTableLayout.RESEARCH_TREE_X + ResearchTableLayout.PANEL_MARGIN;
            int statusY = ResearchTableLayout.RESEARCH_TREE_Y + ResearchTableLayout.RESEARCH_TREE_PANEL_HEIGHT - 12;
            guiGraphics.drawString(this.font, Component.literal(statusMessage), statusX, statusY, statusMessageColor, false);
        }
    }

    // This method is now handled by ResearchInfoPanel

    // Color conversion is now handled by ResearchInfoPanel

    // Utility method für moderne Rahmen
    private void drawBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + 1, color); // Top
        guiGraphics.fill(x, y + height - 1, x + width, y + height, color); // Bottom
        guiGraphics.fill(x, y, x + 1, y + height, color); // Left
        guiGraphics.fill(x + width - 1, y, x + width, y + height, color); // Right
    }

    // Scroll bar is now handled by ResearchTreePanel

    // Scroll-Funktionalität für große Forschungsbäume
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (researchTreePanel != null && researchTreePanel.handleScroll(mouseX, mouseY, scrollY)) {
            updateResearchButtons();
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
