package de.davidvogt.hkbmod;

import com.mojang.logging.LogUtils;
import de.davidvogt.hkbmod.block.ModBlocks;
import de.davidvogt.hkbmod.block.entity.ModBlockEntities;
import de.davidvogt.hkbmod.client.gui.ResearchTableScreen;
import de.davidvogt.hkbmod.entity.ModEntityTypes;
import de.davidvogt.hkbmod.entity.client.DeerModel;
import de.davidvogt.hkbmod.entity.client.DeerRenderer;
import de.davidvogt.hkbmod.entity.client.ModModelLayers;
import de.davidvogt.hkbmod.entity.custom.DeerEntity;
import de.davidvogt.hkbmod.item.ModCreativeModeTabs;
import de.davidvogt.hkbmod.item.ModItems;
import de.davidvogt.hkbmod.menu.ModMenuTypes;
import de.davidvogt.hkbmod.network.ModNetworking;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(HkbMod.MOD_ID)
public final class HkbMod {
    public static final String MOD_ID = "hkbmod";

    public static final Logger LOGGER = LogUtils.getLogger();


    public HkbMod(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();
        FMLCommonSetupEvent.getBus(modBusGroup).addListener(this::commonSetup);
        FMLClientSetupEvent.getBus(modBusGroup).addListener(this::clientSetup);

        ModItems.register(modBusGroup);
        ModBlocks.register(modBusGroup);
        ModBlockEntities.register(modBusGroup);
        ModMenuTypes.register(modBusGroup);
        ModEntityTypes.register(modBusGroup);
        ModCreativeModeTabs.register(modBusGroup);

        BuildCreativeModeTabContentsEvent.getBus(modBusGroup).addListener(HkbMod::addCreative);
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Register network packets
        ModNetworking.register();
    }

    private void clientSetup(final FMLClientSetupEvent event) {

    }


    // Add the example block item to the building blocks tab
    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.ALEXANDRITE);
            event.accept(ModItems.RAW_ALEXANDRITE);
        }

        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.MYSTICAL_WAND);
            event.accept(ModItems.CHISEL);
            event.accept(ModItems.FEATHER_WINGS);
            event.accept(ModItems.THROWING_KNIFE);
            event.accept(ModItems.THROWING_KNIFE_EXPLOSIVE);
            event.accept(ModItems.THROWING_KNIFE_SLOWNESS);
            event.accept(ModItems.THROWING_KNIFE_INSTANT_DAMAGE);
            event.accept(ModItems.LIGHTNING_AXE);
            event.accept(ModItems.LIGHTNING_WAND);
            event.accept(ModItems.GROWTH_ACCELERATOR_WAND);
        }

        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModBlocks.ALEXANDRITE_BLOCK);
            event.accept(ModBlocks.RAW_ALEXANDRITE_BLOCK);
            event.accept(ModBlocks.ALEXANDRITE_ORE);
            event.accept(ModBlocks.ALEXANDRITE_DEEPSLATE_ORE);
        }

        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.RESEARCH_TABLE);
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(ModMenuTypes.RESEARCH_TABLE.get(), ResearchTableScreen::new);
            });
        }

        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntityTypes.DEER.get(), DeerRenderer::new);
        }

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(ModModelLayers.DEER_LAYER, DeerModel::createBodyLayer);
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class CommonModEvents {
        @SubscribeEvent
        public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
            event.put(ModEntityTypes.DEER.get(), DeerEntity.createAttributes().build());
        }
    }
}
