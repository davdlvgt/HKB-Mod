package de.davidvogt.hkbmod.item;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HkbMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> ALEXANDRITE_ITEMS_TAB = CREATIVE_MODE_TABS.register("alexandrite_items_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.ALEXANDRITE.get()))
                    .title(Component.translatable("creativetab.hkbmod.alexandrite_items"))
                    .displayItems((ItemDisplayParameters, output) -> {
                        output.accept(ModItems.ALEXANDRITE.get());
                        output.accept(ModItems.RAW_ALEXANDRITE.get());

                        output.accept(ModItems.CHISEL.get());

                        output.accept(ModItems.KOHLRABI.get());

                        output.accept(ModItems.AURORA_ASHES.get());

                        output.accept(ModItems.MYSTICAL_WAND.get());
                        output.accept(ModItems.FEATHER_WINGS.get());
                        output.accept(ModItems.GROWTH_ACCELERATOR_WAND.get());

                        output.accept(ModItems.THROWING_KNIFE.get());
                        output.accept(ModItems.THROWING_KNIFE_EXPLOSIVE.get());
                        output.accept(ModItems.THROWING_KNIFE_SLOWNESS.get());
                        output.accept(ModItems.THROWING_KNIFE_INSTANT_DAMAGE.get());

                        output.accept(ModItems.LIGHTNING_AXE.get());
                        output.accept(ModItems.LIGHTNING_WAND.get());
                    }).build());

    public static final RegistryObject<CreativeModeTab> ALEXANDRITE_BLOCKS_TAB = CREATIVE_MODE_TABS.register("alexandrite_blocks_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.ALEXANDRITE_BLOCK.get()))
                    .withTabsBefore(ALEXANDRITE_ITEMS_TAB.getId())
                    .title(Component.translatable("creativetab.hkbmod.alexandrite_blocks"))
                    .displayItems((ItemDisplayParameters, output) -> {
                        output.accept(ModBlocks.ALEXANDRITE_BLOCK.get());
                        output.accept(ModBlocks.RAW_ALEXANDRITE_BLOCK.get());

                        output.accept(ModBlocks.ALEXANDRITE_ORE.get());
                        output.accept(ModBlocks.ALEXANDRITE_DEEPSLATE_ORE.get());

                        output.accept(ModBlocks.MAGIC_BLOCK.get());
                        output.accept(ModBlocks.JUMP_BLOCK.get());
                        output.accept(ModBlocks.STRING_BLOCK.get());
                        output.accept(ModBlocks.FEATHER_BLOCK.get());
                    }).build());

    public static void register(BusGroup busGroup) {
        CREATIVE_MODE_TABS.register(busGroup);
    }

}
