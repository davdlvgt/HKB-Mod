package de.davidvogt.hkbmod.client.gui.layout;

/**
 * Layout constants and utility methods for the Research Table GUI.
 * Centralizes all positioning, sizing, and color information.
 */
public class ResearchTableLayout {

    // Main window dimensions - increased height for slightly bigger bottom panels
    public static final int WINDOW_WIDTH = 280;
    public static final int WINDOW_HEIGHT = 248; // Increased from 240 for bigger panels

    // Color constants for modern design
    public static final int COLOR_BACKGROUND = 0xFF1A1A1A;
    public static final int COLOR_PANEL = 0xFF2D2D30;
    public static final int COLOR_PANEL_LIGHT = 0xFF3E3E42;
    public static final int COLOR_BORDER = 0xFF4A4A4A;
    public static final int COLOR_ACCENT = 0xFF007ACC;
    public static final int COLOR_SUCCESS = 0xFF00AA00;
    public static final int COLOR_WARNING = 0xFFFF8800;
    public static final int COLOR_ERROR = 0xFFAA0000;
    public static final int COLOR_TEXT = 0xFFFFFFFF;
    public static final int COLOR_TEXT_DIM = 0xFFAAAAAA;
    public static final int COLOR_CONNECTIONS = 0xFF8A5A83;

    // Panel dimensions and positions - adjusted for consistent padding
    public static final int HEADER_PANEL_HEIGHT = 30;
    public static final int MATERIALS_PANEL_WIDTH = 90;
    public static final int MATERIALS_PANEL_HEIGHT = 75;
    public static final int RESEARCH_TREE_PANEL_WIDTH = 175;
    public static final int RESEARCH_TREE_PANEL_HEIGHT = 75;
    public static final int INVENTORY_PANEL_HEIGHT = 90; // Increased from 85 for better proportions
    public static final int RESEARCH_CONTROL_PANEL_HEIGHT = 90; // Increased to match inventory panel height

    // Panel positions (relative to window) - adjusted for consistent spacing between title and header
    public static final int PANEL_MARGIN = 5;
    public static final int TITLE_Y = PANEL_MARGIN; // Title at top with 5px margin
    public static final int HEADER_Y = TITLE_Y + 12 + PANEL_MARGIN; // 5px spacing between title and header panel (12 is text height)
    public static final int MAIN_PANELS_Y = HEADER_Y + HEADER_PANEL_HEIGHT + PANEL_MARGIN; // 5px below header
    public static final int INVENTORY_Y = MAIN_PANELS_Y + MATERIALS_PANEL_HEIGHT + PANEL_MARGIN; // 5px below main panels

    // Inventory grid settings
    public static final int INVENTORY_SLOT_SIZE = 18;
    public static final int INVENTORY_SLOTS_PER_ROW = 9;
    public static final int INVENTORY_ROWS = 3;
    public static final int HOTBAR_SLOTS = 9;

    // Player inventory layout - centered in inventory panel
    public static final int PLAYER_INVENTORY_X = PANEL_MARGIN + 3; // Base X position
    public static final int PLAYER_INVENTORY_Y = INVENTORY_Y + 15; // Centered vertically in panel
    public static final int PLAYER_HOTBAR_X = PLAYER_INVENTORY_X;
    public static final int PLAYER_HOTBAR_Y = PLAYER_INVENTORY_Y + (3 * INVENTORY_SLOT_SIZE) + 4; // Small gap between inventory and hotbar
    public static final int INVENTORY_PANEL_WIDTH = 162; // Exact width for 9 slots (9 * 18 = 162)

    // Research control panel - consistent spacing matching inventory panel
    public static final int RESEARCH_CONTROL_X = PLAYER_INVENTORY_X + INVENTORY_PANEL_WIDTH + 6 + PANEL_MARGIN; // 5px gap between panels
    public static final int RESEARCH_CONTROL_Y = INVENTORY_Y;
    public static final int RESEARCH_CONTROL_PANEL_WIDTH = WINDOW_WIDTH - RESEARCH_CONTROL_X - PANEL_MARGIN; // Extend to right edge with 5px margin

    // Materials panel - consistent spacing from left edge
    public static final int MATERIALS_X = PANEL_MARGIN;
    public static final int MATERIALS_Y = MAIN_PANELS_Y;

    // Research tree panel - consistent spacing from materials panel
    public static final int RESEARCH_TREE_X = MATERIALS_X + MATERIALS_PANEL_WIDTH + PANEL_MARGIN; // 5px gap between panels
    public static final int RESEARCH_TREE_Y = MAIN_PANELS_Y;

    // 3x3 grid settings
    public static final int GRID_COLUMNS = 3;
    public static final int GRID_ROWS = 3;
    public static final int SLOT_SIZE = 18;
    public static final int SLOT_SPACING = 2;

    // Research tree settings
    public static final int RESEARCH_BUTTON_WIDTH = 150; // Increased from 50 to 65
    public static final int RESEARCH_BUTTON_HEIGHT = 18;
    public static final int TIER_SPACING = 25;
    public static final int RESEARCH_SPACING = 4;
    public static final int MAX_SCROLL_Y = 120;

    // Button settings
    public static final int CLASS_BUTTON_WIDTH = 62;
    public static final int CLASS_BUTTON_HEIGHT = 22;
    public static final int CLASS_BUTTON_SPACING = 4;
    public static final int START_BUTTON_WIDTH = 80;
    public static final int START_BUTTON_HEIGHT = 18;

    // Scroll bar settings
    public static final int SCROLL_BAR_WIDTH = 4;
    public static final int SCROLL_BAR_X_OFFSET = 270;

    /**
     * Calculate the center position for the 3x3 grid within the materials panel
     */
    public static GridPosition calculateGridPosition(int windowX, int windowY) {
        int gridWidth = GRID_COLUMNS * SLOT_SIZE + (GRID_COLUMNS - 1) * SLOT_SPACING;
        int gridHeight = GRID_ROWS * SLOT_SIZE + (GRID_ROWS - 1) * SLOT_SPACING;

        int startX = windowX + MATERIALS_X + (MATERIALS_PANEL_WIDTH - gridWidth) / 2;
        int startY = windowY + MATERIALS_Y + 15; // Fixed offset from top instead of centering

        return new GridPosition(startX, startY, gridWidth, gridHeight);
    }

    /**
     * Calculate research tree content area position
     */
    public static PanelArea calculateResearchTreeArea(int windowX, int windowY) {
        return new PanelArea(
            windowX + RESEARCH_TREE_X + 5,  // Content area with padding
            windowY + RESEARCH_TREE_Y + 15, // Below the "Research Tree" label
            RESEARCH_TREE_PANEL_WIDTH - 15, // Minus padding and scroll bar
            RESEARCH_TREE_PANEL_HEIGHT - 20  // Minus top and bottom padding
        );
    }

    /**
     * Calculate class button positions - centered in header panel
     */
    public static ButtonLayout calculateClassButtonLayout(int windowX, int windowY) {
        // Calculate total width needed for all 4 buttons
        int totalButtonWidth = 4 * CLASS_BUTTON_WIDTH + 3 * CLASS_BUTTON_SPACING; // 4 buttons + 3 gaps
        int availableWidth = WINDOW_WIDTH - (2 * PANEL_MARGIN); // Full width minus side margins

        // Center the button group horizontally
        int startX = windowX + PANEL_MARGIN + (availableWidth - totalButtonWidth) / 2;

        // Center buttons vertically in header panel
        int startY = windowY + HEADER_Y + (HEADER_PANEL_HEIGHT - CLASS_BUTTON_HEIGHT) / 2;

        return new ButtonLayout(startX, startY, CLASS_BUTTON_WIDTH, CLASS_BUTTON_HEIGHT, CLASS_BUTTON_SPACING);
    }

    /**
     * Calculate start research button position - consistent spacing within panel
     */
    public static ButtonPosition calculateStartButtonPosition(int windowX, int windowY) {
        return new ButtonPosition(
            windowX + RESEARCH_CONTROL_X + PANEL_MARGIN,
            windowY + RESEARCH_CONTROL_Y + PANEL_MARGIN + 10, // Below "Research" label
            START_BUTTON_WIDTH,
            START_BUTTON_HEIGHT
        );
    }

    /**
     * Calculate research info area position - consistent spacing below button
     */
    public static PanelArea calculateResearchInfoArea(int windowX, int windowY) {
        return new PanelArea(
            windowX + RESEARCH_CONTROL_X + PANEL_MARGIN,
            windowY + RESEARCH_CONTROL_Y + PANEL_MARGIN + 35, // Below button with 5px gap
            RESEARCH_CONTROL_PANEL_WIDTH - (2 * PANEL_MARGIN), // Full width minus left and right margins
            40
        );
    }

    /**
     * Calculate research control panel area - properly sized to match inventory panel positioning
     */
    public static PanelArea calculateResearchControlArea(int windowX, int windowY) {
        return new PanelArea(
            windowX + RESEARCH_CONTROL_X,
            windowY + RESEARCH_CONTROL_Y,
            RESEARCH_CONTROL_PANEL_WIDTH,
            RESEARCH_CONTROL_PANEL_HEIGHT
        );
    }

    /**
     * Calculate inventory panel area - sized to exactly fit inventory content
     */
    public static PanelArea calculateInventoryPanelArea(int windowX, int windowY) {
        return new PanelArea(
            windowX + PLAYER_INVENTORY_X - 3, // Small padding
            windowY + INVENTORY_Y,
            INVENTORY_PANEL_WIDTH + 6, // Add padding on both sides
            INVENTORY_PANEL_HEIGHT
        );
    }

    /**
     * Calculate inventory slot grid positions for visual rendering - centered in panel
     */
    public static InventoryGridPositions calculateInventoryGridPositions(int windowX, int windowY) {
        // Calculate center positions within the inventory panel
        int inventoryPanelX = windowX + PLAYER_INVENTORY_X - 3; // Panel start position
        int inventoryPanelWidth = INVENTORY_PANEL_WIDTH + 6; // Panel width with padding
        int inventoryPanelY = windowY + INVENTORY_Y;

        // Calculate total inventory content size
        int totalInventoryWidth = 9 * INVENTORY_SLOT_SIZE; // 9 slots * 18px
        int totalInventoryHeight = 3 * INVENTORY_SLOT_SIZE; // 3 rows * 18px
        int hotbarHeight = INVENTORY_SLOT_SIZE; // 1 row * 18px
        int gapBetweenInventoryAndHotbar = 4; // Small gap
        int totalContentHeight = totalInventoryHeight + gapBetweenInventoryAndHotbar + hotbarHeight;

        // Center horizontally in panel
        int centeredX = inventoryPanelX + (inventoryPanelWidth - totalInventoryWidth) / 2;

        // Center vertically in panel
        int centeredStartY = inventoryPanelY + (INVENTORY_PANEL_HEIGHT - totalContentHeight) / 2;

        // Main inventory (3x9) positions - centered
        int mainInventoryStartX = centeredX;
        int mainInventoryStartY = centeredStartY;

        // Hotbar (1x9) positions - centered below main inventory
        int hotbarStartX = centeredX;
        int hotbarStartY = centeredStartY + totalInventoryHeight + gapBetweenInventoryAndHotbar;

        return new InventoryGridPositions(
            mainInventoryStartX, mainInventoryStartY,
            hotbarStartX, hotbarStartY
        );
    }

    // Helper classes for layout data
    public static class GridPosition {
        public final int startX, startY, width, height;

        public GridPosition(int startX, int startY, int width, int height) {
            this.startX = startX;
            this.startY = startY;
            this.width = width;
            this.height = height;
        }
    }

    public static class PanelArea {
        public final int x, y, width, height;

        public PanelArea(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    public static class ButtonLayout {
        public final int startX, startY, buttonWidth, buttonHeight, spacing;

        public ButtonLayout(int startX, int startY, int buttonWidth, int buttonHeight, int spacing) {
            this.startX = startX;
            this.startY = startY;
            this.buttonWidth = buttonWidth;
            this.buttonHeight = buttonHeight;
            this.spacing = spacing;
        }
    }

    public static class ButtonPosition {
        public final int x, y, width, height;

        public ButtonPosition(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    public static class InventoryGridPositions {
        public final int mainInventoryX, mainInventoryY;
        public final int hotbarX, hotbarY;

        public InventoryGridPositions(int mainInventoryX, int mainInventoryY, int hotbarX, int hotbarY) {
            this.mainInventoryX = mainInventoryX;
            this.mainInventoryY = mainInventoryY;
            this.hotbarX = hotbarX;
            this.hotbarY = hotbarY;
        }
    }
}
