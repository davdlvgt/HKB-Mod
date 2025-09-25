package de.davidvogt.hkbmod.research;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public enum ResearchClass {
    MAGICIAN("magician", "Magician", 0xFF4A90E2, "textures/gui/research/class_magician.png"),
    ARCHER("archer", "Archer", 0xFF7ED321, "textures/gui/research/class_archer.png"),
    KNIGHT("knight", "Knight", 0xFFD0021B, "textures/gui/research/class_knight.png"),
    CAVALIER("cavalier", "Cavalier", 0xFFB8860B, "textures/gui/research/class_cavalier.png");

    private final String id;
    private final String displayName;
    private final int color;
    private final String iconPath;

    ResearchClass(String id, String displayName, int color, String iconPath) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.iconPath = iconPath;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Component getDisplayComponent() { return Component.literal(displayName); }
    public int getColor() { return color; }
    public String getIconPath() { return iconPath; }

    public ResourceLocation getIconLocation(String modId) {
        return ResourceLocation.fromNamespaceAndPath(modId, iconPath);
    }

    public static ResearchClass fromId(String id) {
        for (ResearchClass clazz : values()) {
            if (clazz.id.equals(id)) {
                return clazz;
            }
        }
        return MAGICIAN; // default fallback
    }

    public String getDescription() {
        return switch (this) {
            case MAGICIAN -> "Masters of elemental magic and arcane arts";
            case ARCHER -> "Swift hunters specializing in ranged combat";
            case KNIGHT -> "Heavy armor defenders with melee prowess";
            case CAVALIER -> "Mounted warriors with speed and cavalry tactics";
        };
    }
}