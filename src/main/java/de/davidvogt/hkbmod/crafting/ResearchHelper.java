package de.davidvogt.hkbmod.crafting;

import de.davidvogt.hkbmod.research.PlayerResearchDataManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * Utility class to check research requirements for crafting
 */
public class ResearchHelper {

    /**
     * Check if a player has completed the required research for crafting an item
     */
    public static boolean hasRequiredResearch(Player player, ResourceLocation researchId) {
        if (player == null) {
            return false;
        }
        return PlayerResearchDataManager.isResearchCompleted(player, researchId);
    }

    /**
     * Check if a player can craft the throwing knife (requires archer_basic research)
     */
    public static boolean canCraftThrowingKnife(Player player) {
        ResourceLocation archerBasicResearch = ResourceLocation.fromNamespaceAndPath("hkbmod", "archer_basic");
        return hasRequiredResearch(player, archerBasicResearch);
    }
}
