package de.davidvogt.hkbmod.client.screen;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.menu.ResearchTableMenu;
import de.davidvogt.hkbmod.research.ResearchClass;
import de.davidvogt.hkbmod.research.ResearchManager;
import de.davidvogt.hkbmod.research.ResearchNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class ResearchTableScreen extends AbstractContainerScreen<ResearchTableMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HkbMod.MOD_ID,
            "textures/gui/research_table.png");

    // UI state
    private enum UIMode {
        CLASS_SELECTION,
        RESEARCH_TREE,
        RESEARCH_DETAIL
    }

    private UIMode currentMode = UIMode.CLASS_SELECTION;
    private ResearchClass selectedClass = null;
    private ResearchNode selectedNode = null;

    public ResearchTableScreen(ResearchTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        // Larger GUI to accommodate research system
        this.imageWidth = 360;
        this.imageHeight = 250;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();
        addClassSelectionButtons();
    }

    private void addClassSelectionButtons() {
        int panelX = this.leftPos + 160;
        int panelY = this.topPos + 20;
        int buttonWidth = 45;
        int buttonHeight = 20;
        int spacing = 2;

        ResearchClass[] classes = ResearchClass.values();
        for (int i = 0; i < classes.length; i++) {
            ResearchClass researchClass = classes[i];
            int buttonX = panelX + 5 + (i % 2) * (buttonWidth + spacing);
            int buttonY = panelY + (i / 2) * (buttonHeight + spacing);

            Button classButton = Button.builder(
                    Component.literal(researchClass.getDisplayName()),
                    button -> openResearchTree(researchClass)
            )
                    .bounds(buttonX, buttonY, buttonWidth, buttonHeight)
                    .build();

            this.addRenderableWidget(classButton);
        }
    }

    private void openResearchTree(ResearchClass researchClass) {
        this.selectedClass = researchClass;
        this.currentMode = UIMode.RESEARCH_TREE;
        this.clearWidgets();
        addResearchTreeButtons();
    }

    private void addResearchTreeButtons() {
        // Back button
        Button backButton = Button.builder(
                Component.literal("Back"),
                button -> {
                    this.currentMode = UIMode.CLASS_SELECTION;
                    this.selectedClass = null;
                    this.clearWidgets();
                    addClassSelectionButtons();
                }
        )
                .bounds(this.leftPos + 165, this.topPos + 15, 40, 15)
                .build();
        this.addRenderableWidget(backButton);

        // Research node buttons
        if (this.selectedClass != null) {
            List<ResearchNode> availableNodes = menu.getBlockEntity()
                    .getAvailableNodesForClass(this.selectedClass);

            int panelX = this.leftPos + 165;
            int panelY = this.topPos + 40;
            int buttonHeight = 18;
            int spacing = 2;

            for (int i = 0; i < availableNodes.size() && i < 6; i++) { // Limit to 6 visible nodes
                ResearchNode node = availableNodes.get(i);
                Button nodeButton = Button.builder(
                        Component.literal(node.getName()),
                        button -> selectResearchNode(node)
                )
                        .bounds(panelX, panelY + i * (buttonHeight + spacing), 180, buttonHeight)
                        .build();
                this.addRenderableWidget(nodeButton);
            }
        }
    }

    private void selectResearchNode(ResearchNode node) {
        this.selectedNode = node;
        this.currentMode = UIMode.RESEARCH_DETAIL;
        this.clearWidgets();
        addResearchDetailButtons();
    }

    private void addResearchDetailButtons() {
        // Back button
        Button backButton = Button.builder(
                Component.literal("Back"),
                button -> {
                    this.currentMode = UIMode.RESEARCH_TREE;
                    this.selectedNode = null;
                    this.clearWidgets();
                    addResearchTreeButtons();
                }
        )
                .bounds(this.leftPos + 165, this.topPos + 15, 40, 15)
                .build();
        this.addRenderableWidget(backButton);

        // Start Research button
        if (this.selectedNode != null) {
            Button startButton = Button.builder(
                    Component.literal("Start Research"),
                    button -> startResearch()
            )
                    .bounds(this.leftPos + 165, this.topPos + 120, 100, 20)
                    .build();
            this.addRenderableWidget(startButton);
        }
    }

    private void startResearch() {
        if (this.selectedNode != null) {
            boolean started = menu.getBlockEntity().startResearch(this.selectedNode.getId());
            if (started) {
                // Return to class selection view after starting research
                this.currentMode = UIMode.CLASS_SELECTION;
                this.selectedClass = null;
                this.selectedNode = null;
                this.clearWidgets();
                addClassSelectionButtons();
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Draw main background
        int bgColor = 0xFF8B8989;
        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, bgColor);

        // Draw borders
        guiGraphics.fill(x - 1, y - 1, x + this.imageWidth + 1, y, 0xFF000000);
        guiGraphics.fill(x - 1, y + this.imageHeight, x + this.imageWidth + 1, y + this.imageHeight + 1, 0xFF000000);
        guiGraphics.fill(x - 1, y, x, y + this.imageHeight, 0xFF000000);
        guiGraphics.fill(x + this.imageWidth, y, x + this.imageWidth + 1, y + this.imageHeight, 0xFF000000);

        // Draw crafting area
        renderCraftingArea(guiGraphics, x, y);

        // Draw research area
        renderResearchArea(guiGraphics, x, y);

        // Draw player inventory background
        renderPlayerInventoryBackground(guiGraphics, x, y);
    }

    private void renderCraftingArea(GuiGraphics guiGraphics, int x, int y) {
        int craftingBgColor = 0xFF555555;
        guiGraphics.fill(x + 8, y + 25, x + 150, y + 90, craftingBgColor);

        // Draw crafting slots outlines
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotX = x + 30 + col * 18;
                int slotY = y + 35 + row * 18;
                renderSlotBackground(guiGraphics, slotX, slotY);
            }
        }

        // Draw result slot outline
        renderSlotBackground(guiGraphics, x + 124, y + 53);

        // Draw research input slots
        for (int i = 0; i < 6; i++) {
            int slotX = x + 30 + (i % 3) * 18;
            int slotY = y + 110 + (i / 3) * 18;
            renderSlotBackground(guiGraphics, slotX, slotY);
        }
    }

    private void renderResearchArea(GuiGraphics guiGraphics, int x, int y) {
        int researchBgColor = 0xFF3A3A44;
        int panelX = x + 160;
        int panelWidth = this.imageWidth - 168;

        guiGraphics.fill(panelX, y + 6, panelX + panelWidth, y + 147, researchBgColor);

        // Draw border
        guiGraphics.fill(panelX - 1, y + 5, panelX + panelWidth + 1, y + 6, 0xFF2A2A33);
        guiGraphics.fill(panelX - 1, y + 147, panelX + panelWidth + 1, y + 148, 0xFF6A6A77);
        guiGraphics.fill(panelX - 1, y + 6, panelX, y + 147, 0xFF2A2A33);
        guiGraphics.fill(panelX + panelWidth, y + 6, panelX + panelWidth + 1, y + 147, 0xFF6A6A77);
    }

    private void renderPlayerInventoryBackground(GuiGraphics guiGraphics, int x, int y) {
        int invBgColor = 0xFF555555;
        int invY = y + 155;
        int invHeight = this.imageHeight - 161;

        guiGraphics.fill(x + 6, invY, x + this.imageWidth - 6, y + this.imageHeight - 6, invBgColor);

        // Draw player inventory slot backgrounds
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotX = x + 8 + col * 18;
                int slotY = y + 165 + row * 18;
                renderSlotBackground(guiGraphics, slotX, slotY);
            }
        }

        // Draw hotbar slot backgrounds
        for (int col = 0; col < 9; col++) {
            int slotX = x + 8 + col * 18;
            int slotY = y + 223;
            renderSlotBackground(guiGraphics, slotX, slotY);
        }
    }

    private void renderSlotBackground(GuiGraphics guiGraphics, int x, int y) {
        int darkColor = 0xFF222222;
        int lightColor = 0xFFAAAAAA;
        int bgColor = 0xFF555555;

        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, bgColor);
        guiGraphics.fill(x, y, x + 18, y + 1, darkColor);
        guiGraphics.fill(x, y, x + 1, y + 18, darkColor);
        guiGraphics.fill(x, y + 17, x + 18, y + 18, lightColor);
        guiGraphics.fill(x + 17, y, x + 18, y + 18, lightColor);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int titleColor = 0xFFFFFFFF;

        // Draw the title
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, titleColor, false);

        // Draw section labels
        guiGraphics.drawString(this.font, Component.literal("Crafting"), 18, 20, titleColor, false);
        guiGraphics.drawString(this.font, Component.literal("Materials"), 25, 95, titleColor, false);
        guiGraphics.drawString(this.font, Component.literal("Research"), 165, 15, titleColor, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, titleColor, false);

        // Draw content based on current UI mode
        switch (currentMode) {
            case CLASS_SELECTION:
                guiGraphics.drawString(this.font, Component.literal("Select a Class:"), 170, 30, 0xFFFFAA00, false);
                break;

            case RESEARCH_TREE:
                if (selectedClass != null) {
                    guiGraphics.drawString(this.font,
                        Component.literal(selectedClass.getDisplayName() + " Research"),
                        170, 30, selectedClass.getColor(), false);
                }
                break;

            case RESEARCH_DETAIL:
                if (selectedNode != null) {
                    guiGraphics.drawString(this.font,
                        Component.literal(selectedNode.getName()),
                        170, 35, 0xFFFFFFFF, false);
                    guiGraphics.drawString(this.font,
                        Component.literal("Tier " + selectedNode.getTier()),
                        170, 50, 0xFFCCCCCC, false);

                    // Show costs
                    int yOffset = 65;
                    guiGraphics.drawString(this.font, Component.literal("Required:"), 170, yOffset, 0xFFFFAA00, false);
                    yOffset += 12;

                    for (int i = 0; i < selectedNode.getCosts().size() && i < 4; i++) {
                        var cost = selectedNode.getCosts().get(i);
                        String costText = cost.getCount() + "x " + cost.getItemId().getPath();
                        guiGraphics.drawString(this.font, Component.literal(costText), 170, yOffset, 0xFFCCCCCC, false);
                        yOffset += 10;
                    }
                }
                break;
        }

        // Show current research if active
        if (menu.isResearching()) {
            float progress = menu.getResearchProgress() * 100;
            guiGraphics.drawString(this.font,
                Component.literal("Progress: " + Math.round(progress) + "%"),
                170, 130, 0xFF55AA55, false);
        }
    }
}