package de.davidvogt.hkbmod.client.gui.panels;

import de.davidvogt.hkbmod.client.gui.layout.ResearchTableLayout;
import de.davidvogt.hkbmod.research.Research;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/**
 * Handles the research information display panel.
 * Shows selected research details, progress, and requirements.
 */
public class ResearchInfoPanel extends AbstractWidget {

    private final int windowX;
    private final int windowY;
    private final ResearchTableLayout.PanelArea infoArea;
    private final Font font;

    // Animation
    private float animationTick = 0;

    // Data providers
    private Supplier<Research> selectedResearchProvider;
    private Supplier<Research> currentResearchProvider;
    private Supplier<Boolean> isResearchingProvider;
    private Supplier<Float> researchProgressProvider;
    private Supplier<Integer> remainingTimeProvider;

    public ResearchInfoPanel(int windowX, int windowY) {
        super(
            windowX + ResearchTableLayout.RESEARCH_CONTROL_X + 5,
            windowY + ResearchTableLayout.RESEARCH_CONTROL_Y + 35,
            ResearchTableLayout.RESEARCH_CONTROL_PANEL_WIDTH - 10,
            50,
            Component.literal("Research Info Panel")
        );
        this.windowX = windowX;
        this.windowY = windowY;
        this.infoArea = ResearchTableLayout.calculateResearchInfoArea(windowX, windowY);
        this.font = Minecraft.getInstance().font;

        // Make this panel non-interactive - it should not capture mouse events
        this.active = false;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        animationTick += partialTick;

        // Determine what to display based on current state
        boolean isResearching = isResearchingProvider != null && isResearchingProvider.get();

        if (isResearching) {
            renderResearchProgress(guiGraphics);
        } else {
            renderSelectedResearchInfo(guiGraphics);
        }
    }

    private void renderResearchProgress(GuiGraphics guiGraphics) {
        Research currentResearch = currentResearchProvider != null ? currentResearchProvider.get() : null;
        if (currentResearch == null) return;

        // Show progress info in the research control panel
        int progressAreaY = infoArea.y;

        // Research name (compact)
        String researchName = "Researching: " + currentResearch.getName();
        if (font.width(researchName) > infoArea.width - 5) {
            while (font.width(researchName + "...") > infoArea.width - 5 && researchName.length() > 15) {
                researchName = researchName.substring(0, researchName.length() - 1);
            }
            researchName += "...";
        }
        guiGraphics.drawString(font, Component.literal(researchName),
                infoArea.x, progressAreaY, ResearchTableLayout.COLOR_ACCENT, false);

        // Progress bar - adjusted to fit in research control panel
        int barWidth = infoArea.width - 5; // Use available width minus small margin
        int barHeight = 8; // Slightly taller for better visibility
        int barX = infoArea.x;
        int barY = progressAreaY + 12;

        float progress = researchProgressProvider != null ? researchProgressProvider.get() : 0.0f;

        // Background with border
        guiGraphics.fill(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, ResearchTableLayout.COLOR_BORDER);
        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, ResearchTableLayout.COLOR_PANEL);

        // Progress fill with gradient effect
        int progressWidth = (int) (barWidth * progress);
        if (progressWidth > 0) {
            // Animate progress bar color
            float hue = (animationTick * 0.01f) % 1.0f;
            int animColor = net.minecraft.util.Mth.hsvToRgb(hue, 0.6f, 0.9f);
            guiGraphics.fill(barX, barY, barX + progressWidth, barY + barHeight, 0xFF000000 | animColor);
        }

        // Progress percentage text (centered on progress bar)
        String progressText = String.format("%.1f%%", progress * 100);
        int progressTextWidth = font.width(progressText);
        int progressTextX = barX + (barWidth - progressTextWidth) / 2;
        int progressTextY = barY + 1; // Slightly offset from top of bar

        // Draw text shadow for better readability
        guiGraphics.drawString(font, Component.literal(progressText), progressTextX + 1, progressTextY + 1, 0x55000000, false);
        guiGraphics.drawString(font, Component.literal(progressText), progressTextX, progressTextY, ResearchTableLayout.COLOR_TEXT, false);

        // Time display below progress bar
        if (remainingTimeProvider != null) {
            int remainingSeconds = remainingTimeProvider.get();
            int minutes = remainingSeconds / 60;
            int seconds = remainingSeconds % 60;
            String timeText = String.format("Time: %d:%02d", minutes, seconds);
            guiGraphics.drawString(font, Component.literal(timeText), barX, barY + barHeight + 5, ResearchTableLayout.COLOR_TEXT_DIM, false);
        }
    }

    private void renderSelectedResearchInfo(GuiGraphics guiGraphics) {
        Research selectedResearch = selectedResearchProvider != null ? selectedResearchProvider.get() : null;
        if (selectedResearch == null) return;

        // Show selected research info in the compact research control panel
        int infoAreaY = infoArea.y;

        // Research name with type color (truncated if necessary)
        int typeColor = getColorFromChatFormatting(selectedResearch.getType().getColor());
        String researchName = selectedResearch.getName();
        if (font.width(researchName) > infoArea.width - 5) {
            while (font.width(researchName + "...") > infoArea.width - 5 && researchName.length() > 1) {
                researchName = researchName.substring(0, researchName.length() - 1);
            }
            researchName += "...";
        }
        guiGraphics.drawString(font, Component.literal(researchName), infoArea.x, infoAreaY, typeColor, false);

        // Tier and type info (compact)
        String tierInfo = "T" + selectedResearch.getTier() + " " + selectedResearch.getType().name().substring(0, 3);
        guiGraphics.drawString(font, Component.literal(tierInfo), infoArea.x, infoAreaY + 10, ResearchTableLayout.COLOR_TEXT_DIM, false);

        // Requirements (compact list)
        if (!selectedResearch.getCosts().isEmpty()) {
            guiGraphics.drawString(font, Component.literal("Requires:"), infoArea.x, infoAreaY + 20, ResearchTableLayout.COLOR_WARNING, false);
            int yOffset = infoAreaY + 30;
            for (ItemStack costItem : selectedResearch.getCosts()) {
                if (yOffset > infoAreaY + 45) break; // Prevent overflow
                String costText = costItem.getCount() + "x " + costItem.getHoverName().getString();
                if (font.width(costText) > infoArea.width - 5) {
                    while (font.width(costText + "...") > infoArea.width - 5 && costText.length() > 8) {
                        costText = costText.substring(0, costText.length() - 1);
                    }
                    costText += "...";
                }
                guiGraphics.drawString(font, Component.literal(costText), infoArea.x, yOffset, ResearchTableLayout.COLOR_TEXT_DIM, false);
                yOffset += 10;
            }
        }
    }

    /**
     * Convert ChatFormatting to color integer
     */
    private int getColorFromChatFormatting(ChatFormatting formatting) {
        return switch (formatting) {
            case RED -> 0xFFFF5555;
            case YELLOW -> 0xFFFFFF55;
            case LIGHT_PURPLE -> 0xFFFF55FF;
            case BLUE -> 0xFF5555FF;
            case GREEN -> 0xFF55FF55;
            case AQUA -> 0xFF55FFFF;
            case WHITE -> 0xFFFFFFFF;
            case GRAY -> 0xFFAAAAAA;
            case DARK_RED -> 0xFFAA0000;
            case GOLD -> 0xFFFFAA00;
            case DARK_PURPLE -> 0xFFAA00AA;
            case DARK_BLUE -> 0xFF0000AA;
            case DARK_GREEN -> 0xFF00AA00;
            case DARK_AQUA -> 0xFF00AAAA;
            case BLACK -> 0xFF000000;
            case DARK_GRAY -> 0xFF555555;
            default -> ResearchTableLayout.COLOR_TEXT; // Fallback to white
        };
    }

    private void drawBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + 1, color); // Top
        guiGraphics.fill(x, y + height - 1, x + width, y + height, color); // Bottom
        guiGraphics.fill(x, y, x + 1, y + height, color); // Left
        guiGraphics.fill(x + width - 1, y, x + width, y + height, color); // Right
    }

    /**
     * Check if a point is within the info panel
     */
    public boolean isPointInPanel(int x, int y) {
        return x >= getX() && x < getX() + getWidth() && y >= getY() && y < getY() + getHeight();
    }

    /**
     * Get the info area for external use
     */
    public ResearchTableLayout.PanelArea getInfoArea() {
        return infoArea;
    }

    // Data provider setters
    public void setSelectedResearchProvider(Supplier<Research> provider) {
        this.selectedResearchProvider = provider;
    }

    public void setCurrentResearchProvider(Supplier<Research> provider) {
        this.currentResearchProvider = provider;
    }

    public void setIsResearchingProvider(Supplier<Boolean> provider) {
        this.isResearchingProvider = provider;
    }

    public void setResearchProgressProvider(Supplier<Float> provider) {
        this.researchProgressProvider = provider;
    }

    public void setRemainingTimeProvider(Supplier<Integer> provider) {
        this.remainingTimeProvider = provider;
    }

    @Override
    protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE, "Research Information Panel");
    }
}

