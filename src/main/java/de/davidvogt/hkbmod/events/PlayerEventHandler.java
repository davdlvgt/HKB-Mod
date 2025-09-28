package de.davidvogt.hkbmod.events;

import de.davidvogt.hkbmod.research.PlayerResearchDataManager;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "hkbmod")
public class PlayerEventHandler {

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        // Clear cached research data when player logs out
        PlayerResearchDataManager.clearCache(event.getEntity().getUUID());
    }
}