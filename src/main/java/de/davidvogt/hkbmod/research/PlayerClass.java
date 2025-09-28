package de.davidvogt.hkbmod.research;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public enum PlayerClass implements StringRepresentable {
    KNIGHT("knight", ChatFormatting.RED, "Knight", "Masters of melee combat and heavy armor"),
    ARCHER("archer", ChatFormatting.GREEN, "Archer", "Specialists in ranged combat and precision"),
    CAVALIER("cavalier", ChatFormatting.BLUE, "Cavalier", "Experts in mounted combat and mobility"),
    MAGICIAN("magician", ChatFormatting.LIGHT_PURPLE, "Magician", "Wielders of magic and enchantments");

    private final String name;
    private final ChatFormatting color;
    private final String displayName;
    private final String description;

    PlayerClass(String name, ChatFormatting color, String displayName, String description) {
        this.name = name;
        this.color = color;
        this.displayName = displayName;
        this.description = description;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public String getName() {
        return name;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public MutableComponent getDisplayComponent() {
        return Component.literal(displayName).withStyle(color);
    }

    public MutableComponent getDescriptionComponent() {
        return Component.literal(description).withStyle(ChatFormatting.GRAY);
    }

    public ResourceLocation getIconLocation() {
        // old:
        // return new ResourceLocation("hkbmod", "textures/gui/class/" + name + ".png");

        //new:
        return ResourceLocation.fromNamespaceAndPath("hkbmod", "textures/gui/class/" + name + ".png");
    }

    public static PlayerClass fromString(String name) {
        for (PlayerClass playerClass : values()) {
            if (playerClass.getSerializedName().equals(name)) {
                return playerClass;
            }
        }
        return KNIGHT; // Default fallback
    }

    public static PlayerClass byId(int id) {
        PlayerClass[] values = values();
        if (id >= 0 && id < values.length) {
            return values[id];
        }
        return KNIGHT; // Default fallback
    }
}