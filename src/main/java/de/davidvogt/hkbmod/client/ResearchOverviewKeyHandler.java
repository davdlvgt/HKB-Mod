package de.davidvogt.hkbmod.client;

import de.davidvogt.hkbmod.client.gui.ResearchOverviewScreen;
import de.davidvogt.hkbmod.research.PlayerResearchDataManager;
import de.davidvogt.hkbmod.research.ResearchManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "hkbmod", value = Dist.CLIENT)
public class ResearchOverviewKeyHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();

        // Only handle key press events (not release)
        if (event.getAction() != 1) { // 1 = GLFW_PRESS
            return;
        }

        // Check if player is in game
        if (mc.player == null || mc.level == null) {
            return;
        }

        // Don't handle keys if player is already in a GUI (except our research overview)
        if (mc.screen != null && !(mc.screen instanceof ResearchOverviewScreen)) {
            return;
        }

        // Check if the research overview key was pressed
        if (ModKeyBindings.OPEN_RESEARCH_OVERVIEW.consumeClick()) {
            openResearchOverview();
        }
    }

    private static void openResearchOverview() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) {
            return;
        }

        // Get the research tree and player data
        var researchTree = ResearchManager.getGlobalResearchTree();
        var playerData = PlayerResearchDataManager.getPlayerResearchData(mc.player);

        if (researchTree != null && playerData != null) {
            // Open the research overview screen
            mc.setScreen(new ResearchOverviewScreen(researchTree, playerData));
        }
    }
}