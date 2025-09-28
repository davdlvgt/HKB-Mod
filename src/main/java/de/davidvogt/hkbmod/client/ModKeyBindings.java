package de.davidvogt.hkbmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "hkbmod", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModKeyBindings {

    public static final String CATEGORY_HKBMOD = "key.categories.hkbmod";

    // Research Overview keybind
    public static final KeyMapping OPEN_RESEARCH_OVERVIEW = new KeyMapping(
            "key.hkbmod.research_overview",           // Translation key
            InputConstants.Type.KEYSYM,               // Input type
            GLFW.GLFW_KEY_R,                         // Default key (R)
            CATEGORY_HKBMOD                          // Category
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_RESEARCH_OVERVIEW);
    }
}