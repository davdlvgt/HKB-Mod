package de.davidvogt.hkbmod.datagen;

import de.davidvogt.hkbmod.item.ModItems;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.stream.Stream;

/**
 * Class that provides item models for the mod.
 */

public class ModItemModelProvider extends ModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
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
                basicItem(ModItems.MYSTICAL_WAND.get());
                basicItem(ModItems.FEATHER_WINGS.get());
                basicItem(ModItems.GROWTH_ACCELERATOR_WAND.get());
                basicItem(ModItems.THROWING_KNIFE.get());
                basicItem(ModItems.THROWING_KNIFE_EXPLOSIVE.get());
                basicItem(ModItems.THROWING_KNIFE_SLOWNESS.get());
                basicItem(ModItems.THROWING_KNIFE_INSTANT_DAMAGE.get());
                basicItem(ModItems.LIGHTNING_AXE.get());
                basicItem(ModItems.LIGHTNING_WAND.get());
                basicItem(ModItems.MAGNETIC_BAR.get());
                basicItem(ModItems.DEER_SPAWN_EGG.get());
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
                ModItems.AURORA_ASHES.get(),
                ModItems.MYSTICAL_WAND.get(),
                ModItems.FEATHER_WINGS.get(),
                ModItems.GROWTH_ACCELERATOR_WAND.get(),
                ModItems.THROWING_KNIFE.get(),
                ModItems.THROWING_KNIFE_EXPLOSIVE.get(),
                ModItems.THROWING_KNIFE_SLOWNESS.get(),
                ModItems.THROWING_KNIFE_INSTANT_DAMAGE.get(),
                ModItems.LIGHTNING_AXE.get(),
                ModItems.LIGHTNING_WAND.get(),
                ModItems.MAGNETIC_BAR.get(),
                ModItems.DEER_SPAWN_EGG.get()
        );
    }

    @Override
    protected Stream<net.minecraft.world.level.block.Block> getKnownBlocks() {
        return Stream.empty();
    }
}
