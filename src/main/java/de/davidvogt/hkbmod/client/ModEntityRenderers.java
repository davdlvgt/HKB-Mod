package de.davidvogt.hkbmod.client;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.client.renderer.ThrowingKnifeRenderer;
import de.davidvogt.hkbmod.entity.ModEntityTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HkbMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEntityRenderers {

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.THROWING_KNIFE.get(), ThrowingKnifeRenderer::new);
    }
}