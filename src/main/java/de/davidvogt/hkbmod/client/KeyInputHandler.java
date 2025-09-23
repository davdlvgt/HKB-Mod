package de.davidvogt.hkbmod.client;

import de.davidvogt.hkbmod.util.KeySequenceManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class KeyInputHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        // Only handle key press events (not release)
        if (event.getAction() != 1) { // 1 = GLFW_PRESS
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        // Don't handle keys if player is in a GUI
        if (mc.screen != null) {
            return;
        }

        KeySequenceManager manager = KeySequenceManager.getInstance();
        if (!manager.isSequenceActive()) {
            return;
        }

        // Convert GLFW key code to character
        char keyChar = glfwKeyToChar(event.getKey());
        if (keyChar != 0) {
            // Handle the key input
            manager.handleKeyInput(keyChar);
        }
    }

    private static char glfwKeyToChar(int glfwKey) {
        // Map GLFW key codes to characters for G, H, J, U
        return switch (glfwKey) {
            case 71 -> 'g';  // GLFW_KEY_G
            case 72 -> 'h';  // GLFW_KEY_H
            case 74 -> 'j';  // GLFW_KEY_J
            case 85 -> 'u';  // GLFW_KEY_U
            default -> 0;    // Invalid key
        };
    }
}