package de.davidvogt.hkbmod.datagen;

import de.davidvogt.hkbmod.item.ModItems;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.stream.Stream;

/**
 * Class that provides item models for the mod.
 */

public class ModItemModelProvider extends ModelProvider {

    public ModItemModelProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected ItemModelGenerators getItemModelGenerators(ItemInfoCollector items, SimpleModelCollector models) {
        return new ItemModelGenerators(items, models) {
            @Override
            public void run() {
                basicItem(ModItems.ALEXANDRITE.get());
                basicItem(ModItems.RAW_ALEXANDRITE.get());
                basicItem(ModItems.CHISEL.get());
                basicItem(ModItems.KOHLRABI.get());
                basicItem(ModItems.AURORA_ASHES.get());
            }

            private void basicItem(Item item) {
                ResourceLocation model = ModelLocationUtils.getModelLocation(item);
                items.accept(item, ItemModelUtils.plainModel(model));
            }
        };
    }

    @Override
    protected Stream<Item> getKnownItems() {
        return Stream.of(
                ModItems.ALEXANDRITE.get(),
                ModItems.RAW_ALEXANDRITE.get(),
                ModItems.CHISEL.get(),
                ModItems.KOHLRABI.get(),
                ModItems.AURORA_ASHES.get()
        );
    }

    @Override
    protected Stream<net.minecraft.world.level.block.Block> getKnownBlocks() {
        return Stream.empty();
    }
}
