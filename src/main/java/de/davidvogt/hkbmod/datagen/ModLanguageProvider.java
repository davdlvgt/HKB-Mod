package de.davidvogt.hkbmod.datagen;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.block.ModBlocks;
import de.davidvogt.hkbmod.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

/**
 * Generates language files for the mod.
 */
public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput output, String locale) {
        super(output, HkbMod.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {
        // Items
        add(ModItems.ALEXANDRITE.get(), "Alexandrite");
        add(ModItems.RAW_ALEXANDRITE.get(), "Raw Alexandrite");
        add(ModItems.CHISEL.get(), "Chisel");
        add(ModItems.KOHLRABI.get(), "Kohlrabi");
        add(ModItems.AURORA_ASHES.get(), "Aurora Ashes");
        add(ModItems.MYSTICAL_WAND.get(), "Mystical Wand");
        add(ModItems.FEATHER_WINGS.get(), "Feather Wings");
        add(ModItems.THROWING_KNIFE.get(), "Throwing Knife");
        add(ModItems.THROWING_KNIFE_EXPLOSIVE.get(), "Explosive Throwing Knife");
        add(ModItems.THROWING_KNIFE_SLOWNESS.get(), "Slowness Throwing Knife");
        add(ModItems.THROWING_KNIFE_INSTANT_DAMAGE.get(), "Instant Damage Throwing Knife");
        add(ModItems.LIGHTNING_AXE.get(), "Lightning Axe");
        add(ModItems.LIGHTNING_WAND.get(), "Lightning Wand");
        add(ModItems.GROWTH_ACCELERATOR_WAND.get(), "Growth Accelerator Wand");

        // Blocks
        add(ModBlocks.ALEXANDRITE_BLOCK.get(), "Alexandrite Block");
        add(ModBlocks.RAW_ALEXANDRITE_BLOCK.get(), "Raw Alexandrite Block");
        add(ModBlocks.ALEXANDRITE_ORE.get(), "Alexandrite Ore");
        add(ModBlocks.ALEXANDRITE_DEEPSLATE_ORE.get(), "Deepslate Alexandrite Ore");
        add(ModBlocks.MAGIC_BLOCK.get(), "Magic Block");
        add(ModBlocks.JUMP_BLOCK.get(), "Jump Block");
        add(ModBlocks.STRING_BLOCK.get(), "String Block");
        add(ModBlocks.FEATHER_BLOCK.get(), "Feather Block");
        add(ModBlocks.RESEARCH_TABLE.get(), "Research Table");

        // Creative Tabs
        add("itemgroup.hkbmod.hkb_tab", "HKB Mod");

        // Research Overview GUI
        add("gui.hkbmod.research_overview.title", "Research Overview");
        add("gui.hkbmod.research.status.completed", "Completed");
        add("gui.hkbmod.research.status.in_progress", "In Progress");
        add("gui.hkbmod.research.status.available", "Available");
        add("gui.hkbmod.research.status.locked", "Locked");
        add("gui.hkbmod.research.tier", "Tier: %s");
        add("gui.hkbmod.class.status.unlocked", "Unlocked");
        add("gui.hkbmod.class.status.locked", "Locked");
        add("gui.hkbmod.class.progress", "Progress: %s/%s researches");

        // Key Bindings
        add("key.categories.hkbmod", "HKB Mod");
        add("key.hkbmod.research_overview", "Open Research Overview");
    }
}