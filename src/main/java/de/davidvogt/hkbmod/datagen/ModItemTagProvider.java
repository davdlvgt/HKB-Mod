package de.davidvogt.hkbmod.datagen;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.item.ModItems;
import de.davidvogt.hkbmod.util.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Class for generating item tags for the mod.
 */

public class ModItemTagProvider extends TagsProvider<Item> {

    public ModItemTagProvider(PackOutput output,
                              CompletableFuture<HolderLookup.Provider> lookupProvider,
                              @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.ITEM, lookupProvider, HkbMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModTags.Items.TRANSFORMABLE_ITEMS)
                .addElement(BuiltInRegistries.ITEM.getKey(ModItems.ALEXANDRITE.get()))
                .addElement(BuiltInRegistries.ITEM.getKey(ModItems.RAW_ALEXANDRITE.get()))
                .addElement(BuiltInRegistries.ITEM.getKey(Items.COAL))
                .addElement(BuiltInRegistries.ITEM.getKey(Items.STICK))
                .addElement(BuiltInRegistries.ITEM.getKey(Items.COMPASS));
    }

    protected TagBuilder tag(TagKey<Item> tagKey) {
        return this.getOrCreateRawBuilder(tagKey);
    }

    @Override
    public String getName() {
        return "HkbMod Item Tags";
    }
}
