package de.davidvogt.hkbmod.client.gui.components;

import de.davidvogt.hkbmod.research.Research;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ResearchButton extends Button {
    private final Research research;
    private final boolean canUnlock;
    private final boolean isUnlocked;
    private boolean isSelected;

    public ResearchButton(Research research, int x, int y, int width, int height,
                         boolean canUnlock, boolean isUnlocked, OnPress onPress) {
        super(x, y, width, height, Component.literal(research.getName()), onPress, DEFAULT_NARRATION);
        this.research = research;
        this.canUnlock = canUnlock;
        this.isUnlocked = isUnlocked;
        this.isSelected = false;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Determine button color based on state
        int backgroundColor;
        int textColor;

        if (isUnlocked) {
            backgroundColor = 0xFF4CAF50; // Green for unlocked
            textColor = 0xFFFFFFFF; // White text
        } else if (canUnlock) {
            backgroundColor = 0xFF2196F3; // Blue for available
            textColor = 0xFFFFFFFF; // White text
        } else {
            backgroundColor = 0xFF757575; // Gray for locked
            textColor = 0xFFBDBDBD; // Light gray text
        }

        // Draw button background
        guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), backgroundColor);

        // Draw border - highlight if selected
        int borderColor;
        if (isSelected) {
            borderColor = 0xFFFFD700; // Gold for selected
        } else if (isHoveredOrFocused()) {
            borderColor = 0xFFFFFFFF; // White for hovered
        } else {
            borderColor = 0xFF424242; // Gray for normal
        }

        // Draw thicker border if selected
        int borderThickness = isSelected ? 2 : 1;
        for (int i = 0; i < borderThickness; i++) {
            guiGraphics.fill(getX() - i, getY() - i, getX() + getWidth() + i, getY() - i + 1, borderColor); // Top
            guiGraphics.fill(getX() - i, getY() + getHeight() - 1 + i, getX() + getWidth() + i, getY() + getHeight() + i, borderColor); // Bottom
            guiGraphics.fill(getX() - i, getY() - i, getX() - i + 1, getY() + getHeight() + i, borderColor); // Left
            guiGraphics.fill(getX() + getWidth() - 1 + i, getY() - i, getX() + getWidth() + i, getY() + getHeight() + i, borderColor); // Right
        }

        // Draw text
        Font font = Minecraft.getInstance().font;
        String text = research.getName();
        if (text.length() > 12) {
            text = text.substring(0, 9) + "...";
        }

        int textX = getX() + (getWidth() - font.width(text)) / 2;
        int textY = getY() + (getHeight() - 8) / 2;
        guiGraphics.drawString(font, text, textX, textY, textColor, false);

        // Draw tier indicator
        String tierText = "T" + research.getTier();
        int tierX = getX() + 2;
        int tierY = getY() + 2;
        guiGraphics.drawString(font, tierText, tierX, tierY, 0xFFFFFF00, false);
    }

    public Research getResearch() {
        return research;
    }

    public boolean canUnlock() {
        return canUnlock;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }
}