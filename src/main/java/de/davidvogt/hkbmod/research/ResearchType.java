package de.davidvogt.hkbmod.research;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum ResearchType {
    COMBAT("combat", "Combat", ChatFormatting.RED),
    CRAFTING("crafting", "Crafting", ChatFormatting.YELLOW),
    MAGIC("magic", "Magic", ChatFormatting.LIGHT_PURPLE),
    UTILITY("utility", "Utility", ChatFormatting.BLUE),
    PASSIVE("passive", "Passive", ChatFormatting.GREEN);

    private final String name;
    private final String displayName;
    private final ChatFormatting color;

    ResearchType(String name, String displayName, ChatFormatting color) {
        this.name = name;
        this.displayName = displayName;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public MutableComponent getDisplayComponent() {
        return Component.literal(displayName).withStyle(color);
    }
}