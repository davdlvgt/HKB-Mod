package de.davidvogt.hkbmod.client.gui.panels;

import de.davidvogt.hkbmod.client.gui.components.ResearchButton;
import de.davidvogt.hkbmod.client.gui.layout.ResearchTableLayout;
import de.davidvogt.hkbmod.research.Research;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Handles the research tree panel rendering and interaction.
 * Manages research buttons, scrolling, and visual connections between tiers.
 */
public class ResearchTreePanel extends AbstractWidget {

    private final int windowX;
    private final int windowY;
    private final ResearchTableLayout.PanelArea contentArea;

    // Scroll state
    private int scrollY = 0;

    // Scroll bar drag state
    private boolean isDraggingScrollBar = false;
    private int dragStartY = 0;
    private int dragStartScrollY = 0;

    // Research buttons managed by this panel
    private final List<ResearchButton> researchButtons = new ArrayList<>();

    // Callback functions provided by parent screen
    private Function<Research, Boolean> isResearchAvailable;
    private Function<Research, Boolean> isResearchCompleted;
    private Function<Research, Boolean> isResearchSelected;
    private Consumer<Research> onResearchSelected;

    public ResearchTreePanel(int windowX, int windowY) {
        super(
            windowX + ResearchTableLayout.RESEARCH_TREE_X,
            windowY + ResearchTableLayout.RESEARCH_TREE_Y,
            ResearchTableLayout.RESEARCH_TREE_PANEL_WIDTH,
            ResearchTableLayout.RESEARCH_TREE_PANEL_HEIGHT,
            Component.literal("Research Tree Panel")
        );
        this.windowX = windowX;
        this.windowY = windowY;
        this.contentArea = ResearchTableLayout.calculateResearchTreeArea(windowX, windowY);

        // Make this panel non-interactive for clicks - it should not capture mouse events
        this.active = false;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Draw panel background
        drawModernPanel(guiGraphics);

        // Draw scroll bar
        drawScrollBar(guiGraphics);

        // Research buttons are rendered by the parent screen system
        // This panel only manages positioning and state
    }

    public void renderConnections(GuiGraphics guiGraphics, List<Research> researches) {
        drawResearchConnections(guiGraphics, researches);
    }

    private void drawModernPanel(GuiGraphics guiGraphics) {
        // Panel background
        guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), ResearchTableLayout.COLOR_PANEL_LIGHT);

        // Panel border
        drawBorder(guiGraphics, getX(), getY(), getWidth(), getHeight(), ResearchTableLayout.COLOR_BORDER);
    }

    private void drawScrollBar(GuiGraphics guiGraphics) {
        int maxScrollY = Math.max(0, ResearchTableLayout.TIER_SPACING * 4 - contentArea.height);
        if (maxScrollY <= 0) return;

        int scrollBarX = windowX + ResearchTableLayout.SCROLL_BAR_X_OFFSET;
        int scrollBarY = contentArea.y;
        int scrollBarHeight = contentArea.height - 10;

        // Scroll track background
        guiGraphics.fill(scrollBarX, scrollBarY, scrollBarX + ResearchTableLayout.SCROLL_BAR_WIDTH, scrollBarY + scrollBarHeight, ResearchTableLayout.COLOR_PANEL);

        // Scroll thumb
        float scrollPercentage = maxScrollY == 0 ? 0f : (float) scrollY / maxScrollY;
        int thumbHeight = Math.max(8, scrollBarHeight / 4);
        int thumbY = scrollBarY + (int) (scrollPercentage * (scrollBarHeight - thumbHeight));

        // Highlight scroll thumb if being dragged
        int thumbColor = isDraggingScrollBar ? ResearchTableLayout.COLOR_ACCENT : ResearchTableLayout.COLOR_BORDER;
        guiGraphics.fill(scrollBarX, thumbY, scrollBarX + ResearchTableLayout.SCROLL_BAR_WIDTH, thumbY + thumbHeight, thumbColor);

        // Scroll track border
        drawBorder(guiGraphics, scrollBarX, scrollBarY, ResearchTableLayout.SCROLL_BAR_WIDTH, scrollBarHeight, ResearchTableLayout.COLOR_BORDER);
    }

    private void drawResearchConnections(GuiGraphics guiGraphics, List<Research> researches) {
        for (Research research : researches) {
            if (research.getTier() > 0) {
                int fromY = contentArea.y + ((research.getTier() - 1) * ResearchTableLayout.TIER_SPACING) + 10 - scrollY;
                int toY = contentArea.y + (research.getTier() * ResearchTableLayout.TIER_SPACING) + 10 - scrollY;
                int lineX = contentArea.x + 22; // Center of first button

                if (fromY < contentArea.y + contentArea.height && toY > contentArea.y) {
                    // Draw vertical connection line
                    guiGraphics.fill(lineX, fromY, lineX + 1, toY, ResearchTableLayout.COLOR_CONNECTIONS);
                }
            }
        }
    }

    /**
     * Update research buttons based on current research list and scroll position
     */
    public void updateResearchButtons(List<Research> researches, Consumer<ResearchButton> addButtonCallback, Consumer<ResearchButton> removeButtonCallback) {
        // Remove old buttons
        for (ResearchButton button : researchButtons) {
            removeButtonCallback.accept(button);
        }
        researchButtons.clear();

        // Group researches by tier
        Map<Integer, List<Research>> researchByTier = new HashMap<>();
        for (Research research : researches) {
            researchByTier.computeIfAbsent(research.getTier(), k -> new ArrayList<>()).add(research);
        }

        // Create buttons for each tier
        for (int tier = 0; tier <= 3; tier++) {
            List<Research> tierResearches = researchByTier.getOrDefault(tier, new ArrayList<>());

            int tierY = contentArea.y + (tier * ResearchTableLayout.TIER_SPACING) - scrollY;
            // Better clipping bounds to stay within panel
            if (tierY < contentArea.y || tierY + ResearchTableLayout.RESEARCH_BUTTON_HEIGHT > contentArea.y + contentArea.height) continue;

            for (int i = 0; i < tierResearches.size() && i < 3; i++) {
                Research research = tierResearches.get(i);

                boolean isCompleted = isResearchCompleted != null && isResearchCompleted.apply(research);
                boolean isAvailable = isResearchAvailable != null && isResearchAvailable.apply(research);
                boolean isSelected = isResearchSelected != null && isResearchSelected.apply(research);

                int buttonX = contentArea.x + (i * (ResearchTableLayout.RESEARCH_BUTTON_WIDTH + ResearchTableLayout.RESEARCH_SPACING));

                // Ensure button doesn't overflow the panel bounds
                if (buttonX + ResearchTableLayout.RESEARCH_BUTTON_WIDTH > contentArea.x + 165) continue;

                ResearchButton researchButton = new ResearchButton(
                    research, buttonX, tierY,
                    ResearchTableLayout.RESEARCH_BUTTON_WIDTH,
                    ResearchTableLayout.RESEARCH_BUTTON_HEIGHT,
                    isAvailable, isCompleted,
                    button -> {
                        // Only allow selection if research is not completed
                        if (!isCompleted && (isAvailable || isCompleted) && onResearchSelected != null) {
                            onResearchSelected.accept(research);
                        }
                    }
                );

                researchButton.setSelected(isSelected);
                researchButtons.add(researchButton);
                addButtonCallback.accept(researchButton);
            }
        }
    }

    /**
     * Handle mouse scrolling within the research tree area
     */
    public boolean handleScroll(double mouseX, double mouseY, double scrollDelta) {
        // Check if mouse is over research tree area
        if (mouseX >= contentArea.x && mouseX <= contentArea.x + contentArea.width &&
            mouseY >= contentArea.y && mouseY <= contentArea.y + contentArea.height) {

            int maxScrollY = Math.max(0, ResearchTableLayout.TIER_SPACING * 4 - contentArea.height);
            this.scrollY = Mth.clamp(this.scrollY - (int) (scrollDelta * 10), 0, maxScrollY);
            return true;
        }
        return false;
    }

    /**
     * Handle mouse click for scroll bar dragging
     */
    public boolean handleMouseClick(double mouseX, double mouseY, int button) {
        if (button != 0) return false; // Only handle left mouse button

        int maxScrollY = Math.max(0, ResearchTableLayout.TIER_SPACING * 4 - contentArea.height);
        if (maxScrollY <= 0) return false;

        int scrollBarX = windowX + ResearchTableLayout.SCROLL_BAR_X_OFFSET;
        int scrollBarY = contentArea.y;
        int scrollBarHeight = contentArea.height - 10;

        // Check if click is on scroll bar area
        if (mouseX >= scrollBarX && mouseX <= scrollBarX + ResearchTableLayout.SCROLL_BAR_WIDTH &&
            mouseY >= scrollBarY && mouseY <= scrollBarY + scrollBarHeight) {

            // Calculate thumb position and size
            float scrollPercentage = maxScrollY == 0 ? 0f : (float) scrollY / maxScrollY;
            int thumbHeight = Math.max(8, scrollBarHeight / 4);
            int thumbY = scrollBarY + (int) (scrollPercentage * (scrollBarHeight - thumbHeight));

            // Check if click is on thumb
            if (mouseY >= thumbY && mouseY <= thumbY + thumbHeight) {
                // Start dragging thumb
                isDraggingScrollBar = true;
                dragStartY = (int) mouseY;
                dragStartScrollY = scrollY;
                return true;
            } else {
                // Click on track - jump to position
                float clickPercentage = (float) (mouseY - scrollBarY) / (scrollBarHeight - thumbHeight);
                clickPercentage = Mth.clamp(clickPercentage, 0.0f, 1.0f);
                this.scrollY = (int) (maxScrollY * clickPercentage);
                return true;
            }
        }

        return false;
    }

    /**
     * Handle mouse drag for scroll bar
     */
    public boolean handleMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!isDraggingScrollBar || button != 0) return false;

        int maxScrollY = Math.max(0, ResearchTableLayout.TIER_SPACING * 4 - contentArea.height);
        if (maxScrollY <= 0) return false;

        int scrollBarHeight = contentArea.height - 10;
        int thumbHeight = Math.max(8, scrollBarHeight / 4);
        int scrollableHeight = scrollBarHeight - thumbHeight;

        // Calculate new scroll position based on drag distance
        int dragDistance = (int) mouseY - dragStartY;
        float dragPercentage = (float) dragDistance / scrollableHeight;
        int newScrollY = dragStartScrollY + (int) (maxScrollY * dragPercentage);

        this.scrollY = Mth.clamp(newScrollY, 0, maxScrollY);
        return true;
    }

    /**
     * Handle mouse release for scroll bar
     */
    public boolean handleMouseRelease(double mouseX, double mouseY, int button) {
        if (isDraggingScrollBar && button == 0) {
            isDraggingScrollBar = false;
            return true;
        }
        return false;
    }

    /**
     * Check if currently dragging scroll bar
     */
    public boolean isDraggingScrollBar() {
        return isDraggingScrollBar;
    }

    /**
     * Reset scroll position (e.g., when changing class)
     */
    public void resetScroll() {
        this.scrollY = 0;
    }

    /**
     * Get current scroll position
     */
    public int getScrollY() {
        return scrollY;
    }

    /**
     * Check if a point is within the research tree panel
     */
    public boolean isPointInPanel(int x, int y) {
        return x >= getX() && x < getX() + getWidth() && y >= getY() && y < getY() + getHeight();
    }

    /**
     * Check if a point is within the scrollable content area
     */
    public boolean isPointInContentArea(int x, int y) {
        return x >= contentArea.x && x < contentArea.x + contentArea.width &&
               y >= contentArea.y && y < contentArea.y + contentArea.height;
    }

    /**
     * Get the content area for external use
     */
    public ResearchTableLayout.PanelArea getContentArea() {
        return contentArea;
    }

    /**
     * Get managed research buttons
     */
    public List<ResearchButton> getResearchButtons() {
        return new ArrayList<>(researchButtons);
    }

    // Callback setters
    public void setIsResearchAvailable(Function<Research, Boolean> callback) {
        this.isResearchAvailable = callback;
    }

    public void setIsResearchCompleted(Function<Research, Boolean> callback) {
        this.isResearchCompleted = callback;
    }

    public void setIsResearchSelected(Function<Research, Boolean> callback) {
        this.isResearchSelected = callback;
    }

    public void setOnResearchSelected(Consumer<Research> callback) {
        this.onResearchSelected = callback;
    }

    private void drawBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + 1, color); // Top
        guiGraphics.fill(x, y + height - 1, x + width, y + height, color); // Bottom
        guiGraphics.fill(x, y, x + 1, y + height, color); // Left
        guiGraphics.fill(x + width - 1, y, x + width, y + height, color); // Right
    }

    @Override
    protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE, "Research Tree Panel");
    }
}
