package de.davidvogt.hkbmod.datagen;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.item.ModItems;
import de.davidvogt.hkbmod.util.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BlockItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static net.minecraft.tags.TagEntry.tag;

public class ModItemTagProvider extends BlockItemTagsProvider {
    public ModItemTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture,
                              CompletableFuture<TagsProvider.TagLookup<Block>> lookupCompletableFuture, @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, completableFuture, lookupCompletableFuture, HkbMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(ModTags.Items.TRANSFORMABLE_ITEMS)
                .add(ModItems.ALEXANDRITE.get())
                .add(ModItems.RAW_ALEXANDRITE.get())
                .add(Items.COAL)
                .add(Items.STICK)
                .add(Items.COMPASS);
    }
}