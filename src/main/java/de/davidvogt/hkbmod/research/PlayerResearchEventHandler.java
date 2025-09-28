package de.davidvogt.hkbmod.research;

import de.davidvogt.hkbmod.HkbMod;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Event handler for automatic saving/loading of player research data
 */
@Mod.EventBusSubscriber(modid = HkbMod.MOD_ID)
public class PlayerResearchEventHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        // Load research data when player logs in
        PlayerResearchDataManager.getPlayerResearchData(event.getEntity());
        HkbMod.LOGGER.info("[ResearchEventHandler] Player logged in: " + event.getEntity().getName().getString() + " - Research data loaded");
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        // Save research data and clear cache when player logs out
        PlayerResearchData data = PlayerResearchDataManager.getPlayerResearchData(event.getEntity());
        PlayerResearchDataManager.savePlayerResearchData(event.getEntity(), data);
        PlayerResearchDataManager.clearCache(event.getEntity().getUUID());
        HkbMod.LOGGER.info("[ResearchEventHandler] Player logged out: " + event.getEntity().getName().getString() + " - Research data saved and cache cleared");
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // Handle player respawn - copy research data from old player to new player
        if (!event.isWasDeath()) {
            return; // Only handle death respawn
        }

        PlayerResearchData oldData = PlayerResearchDataManager.getPlayerResearchData(event.getOriginal());
        PlayerResearchDataManager.savePlayerResearchData(event.getEntity(), oldData);

        HkbMod.LOGGER.info("[ResearchEventHandler] Player respawned: " + event.getEntity().getName().getString() + " - Research data copied");
    }
}
