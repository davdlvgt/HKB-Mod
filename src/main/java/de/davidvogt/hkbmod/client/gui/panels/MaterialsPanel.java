package de.davidvogt.hkbmod.client.gui.panels;

import de.davidvogt.hkbmod.client.gui.layout.ResearchTableLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

/**
 * Handles the materials panel rendering and slot management for the Research Table GUI.
 * Contains the 3x3 grid where research materials are placed.
 */
public class MaterialsPanel extends AbstractWidget {

    private final int windowX;
    private final int windowY;
    private final ResearchTableLayout.GridPosition gridPosition;

    public MaterialsPanel(int windowX, int windowY) {
        super(
            windowX + ResearchTableLayout.MATERIALS_X,
            windowY + ResearchTableLayout.MATERIALS_Y,
            ResearchTableLayout.MATERIALS_PANEL_WIDTH,
            ResearchTableLayout.MATERIALS_PANEL_HEIGHT,
            Component.literal("Materials Panel")
        );
        this.windowX = windowX;
        this.windowY = windowY;
        this.gridPosition = ResearchTableLayout.calculateGridPosition(windowX, windowY);

        // Make this panel non-interactive - it should not capture mouse events
        this.active = false;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Draw panel background
        drawModernPanel(guiGraphics);

        // Draw panel label
        drawLabel(guiGraphics);

        // Draw research slots
        drawResearchSlots(guiGraphics);
    }

    private void drawModernPanel(GuiGraphics guiGraphics) {
        // Panel background
        guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), ResearchTableLayout.COLOR_PANEL);

        // Panel border
        drawBorder(guiGraphics, getX(), getY(), getWidth(), getHeight(), ResearchTableLayout.COLOR_BORDER);
    }

    private void drawLabel(GuiGraphics guiGraphics) {
        // Use absolute positioning for text relative to window
        int labelX = windowX + 8;
        int labelY = windowY + 57;

        // Note: Font rendering should be done by the parent screen, this is just positioning
        // The actual text rendering will be handled in the main screen class
    }

    private void drawResearchSlots(GuiGraphics guiGraphics) {
        for (int row = 0; row < ResearchTableLayout.GRID_ROWS; row++) {
            for (int col = 0; col < ResearchTableLayout.GRID_COLUMNS; col++) {
                int slotX = gridPosition.startX + col * (ResearchTableLayout.SLOT_SIZE + ResearchTableLayout.SLOT_SPACING);
                int slotY = gridPosition.startY + row * (ResearchTableLayout.SLOT_SIZE + ResearchTableLayout.SLOT_SPACING);

                // Slot background border
                guiGraphics.fill(
                    slotX - 1, slotY - 1,
                    slotX + ResearchTableLayout.SLOT_SIZE + 1,
                    slotY + ResearchTableLayout.SLOT_SIZE + 1,
                    ResearchTableLayout.COLOR_BORDER
                );

                // Slot interior
                guiGraphics.fill(
                    slotX, slotY,
                    slotX + ResearchTableLayout.SLOT_SIZE,
                    slotY + ResearchTableLayout.SLOT_SIZE,
                    0xFF000000
                );
            }
        }
    }

    private void drawBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + 1, color); // Top
        guiGraphics.fill(x, y + height - 1, x + width, y + height, color); // Bottom
        guiGraphics.fill(x, y, x + 1, y + height, color); // Left
        guiGraphics.fill(x + width - 1, y, x + width, y + height, color); // Right
    }

    /**
     * Get the calculated grid position for slot placement
     */
    public ResearchTableLayout.GridPosition getGridPosition() {
        return gridPosition;
    }

    /**
     * Get the slot position for a specific grid coordinate
     */
    public SlotPosition getSlotPosition(int row, int col) {
        if (row < 0 || row >= ResearchTableLayout.GRID_ROWS || col < 0 || col >= ResearchTableLayout.GRID_COLUMNS) {
            throw new IllegalArgumentException("Invalid grid position: " + row + ", " + col);
        }

        int slotX = gridPosition.startX + col * (ResearchTableLayout.SLOT_SIZE + ResearchTableLayout.SLOT_SPACING);
        int slotY = gridPosition.startY + row * (ResearchTableLayout.SLOT_SIZE + ResearchTableLayout.SLOT_SPACING);

        return new SlotPosition(slotX, slotY);
    }

    /**
     * Get all slot positions for the 3x3 grid
     */
    public SlotPosition[] getAllSlotPositions() {
        SlotPosition[] positions = new SlotPosition[ResearchTableLayout.GRID_ROWS * ResearchTableLayout.GRID_COLUMNS];
        int index = 0;

        for (int row = 0; row < ResearchTableLayout.GRID_ROWS; row++) {
            for (int col = 0; col < ResearchTableLayout.GRID_COLUMNS; col++) {
                positions[index++] = getSlotPosition(row, col);
            }
        }

        return positions;
    }

    /**
     * Check if a point is within the materials panel
     */
    public boolean isPointInPanel(int x, int y) {
        return x >= getX() && x < getX() + getWidth() && y >= getY() && y < getY() + getHeight();
    }

    public static class SlotPosition {
        public final int x, y;

        public SlotPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    @Override
    protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput narrationElementOutput) {
        // Narration for accessibility
        narrationElementOutput.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE, "Research Materials Panel");
    }
}

