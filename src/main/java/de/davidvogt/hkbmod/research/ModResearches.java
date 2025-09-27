package de.davidvogt.hkbmod.research;

import de.davidvogt.hkbmod.research.data.ResearchDataLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public class ModResearches {

    private static final Map<ResourceLocation, Research> RESEARCHES = new HashMap<>();

    // Research IDs
    // Tier 0 - Starting researches (no prerequisites)
    public static final ResourceLocation KNIGHT_FOUNDATION = ResourceLocation.fromNamespaceAndPath("hkbmod", "knight_foundation");
    public static final ResourceLocation ARCHER_FOUNDATION = ResourceLocation.fromNamespaceAndPath("hkbmod", "archer_foundation");
    public static final ResourceLocation CAVALIER_FOUNDATION = ResourceLocation.fromNamespaceAndPath("hkbmod", "cavalier_foundation");
    public static final ResourceLocation MAGICIAN_FOUNDATION = ResourceLocation.fromNamespaceAndPath("hkbmod", "magician_foundation");

    // Tier 1 - Basic training (requires foundation)
    public static final ResourceLocation KNIGHT_BASIC = ResourceLocation.fromNamespaceAndPath("hkbmod", "knight_basic");
    public static final ResourceLocation ARCHER_BASIC = ResourceLocation.fromNamespaceAndPath("hkbmod", "archer_basic");
    public static final ResourceLocation CAVALIER_BASIC = ResourceLocation.fromNamespaceAndPath("hkbmod", "cavalier_basic");
    public static final ResourceLocation MAGICIAN_BASIC = ResourceLocation.fromNamespaceAndPath("hkbmod", "magician_basic");

    // Tier 2 - Advanced skills
    public static final ResourceLocation KNIGHT_ARMOR_MASTERY = ResourceLocation.fromNamespaceAndPath("hkbmod", "knight_armor_mastery");
    public static final ResourceLocation KNIGHT_SHIELD_WALL = ResourceLocation.fromNamespaceAndPath("hkbmod", "knight_shield_wall");
    public static final ResourceLocation ARCHER_PRECISION = ResourceLocation.fromNamespaceAndPath("hkbmod", "archer_precision");
    public static final ResourceLocation ARCHER_MULTISHOT = ResourceLocation.fromNamespaceAndPath("hkbmod", "archer_multishot");
    public static final ResourceLocation CAVALIER_MOUNT_MASTERY = ResourceLocation.fromNamespaceAndPath("hkbmod", "cavalier_mount_mastery");
    public static final ResourceLocation CAVALIER_CHARGE = ResourceLocation.fromNamespaceAndPath("hkbmod", "cavalier_charge");
    public static final ResourceLocation MAGICIAN_ELEMENTAL = ResourceLocation.fromNamespaceAndPath("hkbmod", "magician_elemental");
    public static final ResourceLocation MAGICIAN_ENCHANTING = ResourceLocation.fromNamespaceAndPath("hkbmod", "magician_enchanting");

    // Tier 3 - Master skills
    public static final ResourceLocation KNIGHT_HEAVY_STRIKE = ResourceLocation.fromNamespaceAndPath("hkbmod", "knight_heavy_strike");
    public static final ResourceLocation ARCHER_EXPLOSIVE_ARROW = ResourceLocation.fromNamespaceAndPath("hkbmod", "archer_explosive_arrow");
    public static final ResourceLocation CAVALIER_MOBILITY = ResourceLocation.fromNamespaceAndPath("hkbmod", "cavalier_mobility");
    public static final ResourceLocation MAGICIAN_ARCANE_POWER = ResourceLocation.fromNamespaceAndPath("hkbmod", "magician_arcane_power");

    public static void initializeResearches(ResourceManager resourceManager) {
        RESEARCHES.clear();
        ResearchDataLoader.loadResearches(resourceManager);
        RESEARCHES.putAll(ResearchDataLoader.getLoadedResearches());
    }


    private static void registerResearch(Research research) {
        RESEARCHES.put(research.getId(), research);
    }

    public static Research getResearch(ResourceLocation id) {
        return RESEARCHES.get(id);
    }

    public static Map<ResourceLocation, Research> getAllResearches() {
        return new HashMap<>(RESEARCHES);
    }

    public static void populateResearchTree(ResearchTree tree) {
        for (Research research : RESEARCHES.values()) {
            tree.addResearch(research);
        }
    }
}