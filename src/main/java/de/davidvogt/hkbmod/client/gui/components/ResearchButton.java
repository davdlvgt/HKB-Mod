package de.davidvogt.hkbmod.client.gui.components;

import de.davidvogt.hkbmod.research.Research;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class ResearchButton extends Button {
    // Moderne Farbkonstanten
    private static final int COLOR_UNLOCKED = 0xFF00AA00;      // Grün für freigeschaltet
    private static final int COLOR_AVAILABLE = 0xFF007ACC;     // Blau für verfügbar
    private static final int COLOR_LOCKED = 0xFF555555;        // Grau für gesperrt
    private static final int COLOR_SELECTED = 0xFFFFD700;      // Gold für ausgewählt
    private static final int COLOR_HOVER = 0xFFFFFFFF;         // Weiß für Hover
    private static final int COLOR_BORDER = 0xFF404040;        // Standard Rahmen
    private static final int COLOR_TEXT_WHITE = 0xFFFFFFFF;
    private static final int COLOR_TEXT_GRAY = 0xFFAAAAAA;
    private static final int COLOR_TIER = 0xFFFFFF00;          // Gelb für Tier-Anzeige

    private final Research research;
    private final boolean canUnlock;
    private final boolean isUnlocked;
    private boolean isSelected;
    private float animationTick = 0;
    private float hoverAnimation = 0;

    public ResearchButton(Research research, int x, int y, int width, int height,
                         boolean canUnlock, boolean isUnlocked, OnPress onPress) {
        super(x, y, width, height, Component.literal(research.getName()), onPress, DEFAULT_NARRATION);
        this.research = research;
        this.canUnlock = canUnlock;
        this.isUnlocked = isUnlocked;
        this.isSelected = false;

        // Disable button if research is already unlocked
        this.active = !isUnlocked;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        animationTick += partialTick;

        // Smooth hover animation - only if button is active
        boolean isHovering = isHoveredOrFocused() && this.active;
        float targetHover = isHovering ? 1.0f : 0.0f;
        hoverAnimation = Mth.lerp(0.1f, hoverAnimation, targetHover);

        // Determine colors based on state
        int backgroundColor = getBackgroundColor();
        int textColor = getTextColor();
        int borderColor = getBorderColor();

        // Draw main button with subtle gradient effect
        drawModernButton(guiGraphics, backgroundColor);

        // Draw animated border
        drawAnimatedBorder(guiGraphics, borderColor);

        // Draw progress indicator for unlocked research
        if (isUnlocked) {
            drawCompletionIndicator(guiGraphics);
        }

        // Draw text with improved formatting
        drawButtonText(guiGraphics, textColor);
    }

    private int getBackgroundColor() {
        if (isUnlocked) {
            return COLOR_UNLOCKED;
        } else if (canUnlock) {
            // Animate available research slightly
            float pulse = (float) Math.sin(animationTick * 0.05f) * 0.1f + 0.9f;
            int baseColor = COLOR_AVAILABLE;
            int r = (int) ((baseColor >> 16 & 0xFF) * pulse);
            int g = (int) ((baseColor >> 8 & 0xFF) * pulse);
            int b = (int) ((baseColor & 0xFF) * pulse);
            return 0xFF000000 | (r << 16) | (g << 8) | b;
        } else {
            return COLOR_LOCKED;
        }
    }

    private int getTextColor() {
        if (isUnlocked) {
            return COLOR_TEXT_WHITE; // Keep white text for completed research
        } else if (canUnlock) {
            return COLOR_TEXT_WHITE;
        } else {
            return COLOR_TEXT_GRAY;
        }
    }

    private int getBorderColor() {
        if (isSelected && this.active) {
            // Animate selected border only if button is active
            float pulse = (float) Math.sin(animationTick * 0.1f) * 0.3f + 0.7f;
            int baseColor = COLOR_SELECTED;
            int r = (int) ((baseColor >> 16 & 0xFF) * pulse);
            int g = (int) ((baseColor >> 8 & 0xFF) * pulse);
            int b = (int) ((baseColor & 0xFF) * pulse);
            return 0xFF000000 | (r << 16) | (g << 8) | b;
        } else if (hoverAnimation > 0 && this.active) {
            // Blend between border and hover color only if active
            return blendColors(COLOR_BORDER, COLOR_HOVER, hoverAnimation);
        } else {
            return COLOR_BORDER;
        }
    }

    private void drawModernButton(GuiGraphics guiGraphics, int backgroundColor) {
        // Main background
        guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), backgroundColor);

        // Subtle highlight on top for 3D effect
        if (canUnlock || isUnlocked) {
            int highlightColor = 0x33FFFFFF; // Semi-transparent white
            guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + 2, highlightColor);
        }

        // Subtle shadow on bottom
        int shadowColor = 0x33000000; // Semi-transparent black
        guiGraphics.fill(getX(), getY() + getHeight() - 2, getX() + getWidth(), getY() + getHeight(), shadowColor);
    }

    private void drawAnimatedBorder(GuiGraphics guiGraphics, int borderColor) {
        int thickness = isSelected ? 2 : 1;

        // Add hover glow effect
        if (hoverAnimation > 0) {
            int glowAlpha = (int) (hoverAnimation * 128);
            int glowColor = (glowAlpha << 24) | (borderColor & 0xFFFFFF);

            // Draw glow border (slightly larger)
            guiGraphics.fill(getX() - 1, getY() - 1, getX() + getWidth() + 1, getY(), glowColor); // Top
            guiGraphics.fill(getX() - 1, getY() + getHeight(), getX() + getWidth() + 1, getY() + getHeight() + 1, glowColor); // Bottom
            guiGraphics.fill(getX() - 1, getY(), getX(), getY() + getHeight(), glowColor); // Left
            guiGraphics.fill(getX() + getWidth(), getY(), getX() + getWidth() + 1, getY() + getHeight(), glowColor); // Right
        }

        // Draw main border
        for (int i = 0; i < thickness; i++) {
            int offset = i;
            guiGraphics.fill(getX() - offset, getY() - offset, getX() + getWidth() + offset, getY() - offset + 1, borderColor); // Top
            guiGraphics.fill(getX() - offset, getY() + getHeight() - 1 + offset, getX() + getWidth() + offset, getY() + getHeight() + offset, borderColor); // Bottom
            guiGraphics.fill(getX() - offset, getY() - offset, getX() - offset + 1, getY() + getHeight() + offset, borderColor); // Left
            guiGraphics.fill(getX() + getWidth() - 1 + offset, getY() - offset, getX() + getWidth() + offset, getY() + getHeight() + offset, borderColor); // Right
        }
    }

    private void drawCompletionIndicator(GuiGraphics guiGraphics) {
        // Draw checkmark for completed research
        int checkX = getX() + getWidth() - 8;
        int checkY = getY() + 2;

        // Simple checkmark using pixels
        guiGraphics.fill(checkX + 1, checkY + 3, checkX + 2, checkY + 4, COLOR_TEXT_WHITE);
        guiGraphics.fill(checkX + 2, checkY + 4, checkX + 3, checkY + 5, COLOR_TEXT_WHITE);
        guiGraphics.fill(checkX + 3, checkY + 2, checkX + 4, checkY + 3, COLOR_TEXT_WHITE);
        guiGraphics.fill(checkX + 4, checkY + 1, checkX + 5, checkY + 2, COLOR_TEXT_WHITE);
    }

    private void drawButtonText(GuiGraphics guiGraphics, int textColor) {
        Font font = Minecraft.getInstance().font;

        // Build text with format: "T[tier] [symbol] [name]"
        String tierText = "T" + research.getTier();
        String symbol = getTypeSymbol();
        String name = research.getName();

        String fullText = tierText + " " + symbol + " " + name;

        // Truncate text if too long
        int maxWidth = getWidth() - 8; // Leave padding on both sides
        if (font.width(fullText) > maxWidth) {
            // First try to truncate the name
            String truncatedName = name;
            while (font.width(tierText + " " + symbol + " " + truncatedName + "...") > maxWidth && truncatedName.length() > 1) {
                truncatedName = truncatedName.substring(0, truncatedName.length() - 1);
            }
            if (truncatedName.length() > 1) {
                fullText = tierText + " " + symbol + " " + truncatedName + "...";
            } else {
                // If name is too short, just show tier and symbol
                fullText = tierText + " " + symbol;
            }
        }

        // Left-align text instead of centering
        int textX = getX() + 4; // Small left padding
        int textY = getY() + (getHeight() - 8) / 2;

        // Draw text shadow for better readability
        guiGraphics.drawString(font, fullText, textX + 1, textY + 1, 0x55000000, false);
        guiGraphics.drawString(font, fullText, textX, textY, textColor, false);
    }

    private String getTypeSymbol() {
        return switch (research.getType()) {
            case COMBAT -> "⚔";
            case CRAFTING -> "🔨";
            case MAGIC -> "✦";
            case UTILITY -> "🔧";
            case PASSIVE -> "🛡";
        };
    }

    // Utility method to blend two colors
    private int blendColors(int color1, int color2, float ratio) {
        ratio = Mth.clamp(ratio, 0.0f, 1.0f);

        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    // Getter methods
    public Research getResearch() {
        return research;
    }

    public boolean canUnlock() {
        return canUnlock;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        // Only allow clicks if the research is not completed
        if (!isUnlocked) {
            super.onClick(mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Only allow mouse clicks if the research is not completed
        if (!isUnlocked) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }
}
