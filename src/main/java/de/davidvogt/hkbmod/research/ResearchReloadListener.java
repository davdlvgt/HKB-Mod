package de.davidvogt.hkbmod.research;

import de.davidvogt.hkbmod.HkbMod;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = HkbMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ResearchReloadListener {

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new PreparableReloadListener() {
            @Override
            public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier,
                                                  ResourceManager resourceManager,
                                                  java.util.concurrent.Executor backgroundExecutor,
                                                  java.util.concurrent.Executor gameExecutor) {
                return preparationBarrier.wait(null).thenRunAsync(() -> {
                    ModResearches.initializeResearches(resourceManager);
                }, gameExecutor);
            }
        });
    }
}
