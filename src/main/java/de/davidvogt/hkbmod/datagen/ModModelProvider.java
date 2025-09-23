package de.davidvogt.hkbmod.datagen;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.block.ModBlocks;
import de.davidvogt.hkbmod.item.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.stream.Stream;

/**
 * Combined provider for both item models and block models/blockstates.
 */
public class ModModelProvider extends ModelProvider {

    public ModModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output);
    }

    @Override
    protected ItemModelGenerators getItemModelGenerators(ItemInfoCollector items, SimpleModelCollector models) {
        return new ItemModelGenerators(items, models) {
            @Override
            public void run() {
                // Basic items
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

                // Handheld items (tools/weapons)
                handheldItem(ModItems.LIGHTNING_AXE.get());
                handheldItem(ModItems.LIGHTNING_WAND.get());
            }

            private void basicItem(Item item) {
                ResourceLocation model = ModelLocationUtils.getModelLocation(item);
                items.accept(item, ItemModelUtils.plainModel(model));
            }

            private void handheldItem(Item item) {
                ResourceLocation model = ModelLocationUtils.getModelLocation(item);
                items.accept(item, ItemModelUtils.plainModel(model.withPrefix("item/")));
            }
        };
    }

    @Override
    protected BlockModelGenerators getBlockModelGenerators(BlockStateGeneratorCollector blocks, ItemInfoCollector items, SimpleModelCollector models) {
        return new BlockModelGenerators(blocks, items, models) {
            @Override
            public void run() {
                cubeAll(ModBlocks.ALEXANDRITE_BLOCK.get());
                cubeAll(ModBlocks.RAW_ALEXANDRITE_BLOCK.get());
                cubeAll(ModBlocks.ALEXANDRITE_ORE.get());
                cubeAll(ModBlocks.ALEXANDRITE_DEEPSLATE_ORE.get());
                cubeAll(ModBlocks.MAGIC_BLOCK.get());
                cubeAll(ModBlocks.JUMP_BLOCK.get());
                cubeAll(ModBlocks.STRING_BLOCK.get());
                cubeAll(ModBlocks.FEATHER_BLOCK.get());
            }

            private void cubeAll(Block block) {
                // Create the blockstate and models using the built-in method
                createTrivialCube(block);
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
                ModItems.LIGHTNING_WAND.get()
        );
    }

    @Override
    protected Stream<Block> getKnownBlocks() {
        return Stream.of(
                ModBlocks.ALEXANDRITE_BLOCK.get(),
                ModBlocks.RAW_ALEXANDRITE_BLOCK.get(),
                ModBlocks.ALEXANDRITE_ORE.get(),
                ModBlocks.ALEXANDRITE_DEEPSLATE_ORE.get(),
                ModBlocks.MAGIC_BLOCK.get(),
                ModBlocks.JUMP_BLOCK.get(),
                ModBlocks.STRING_BLOCK.get(),
                ModBlocks.FEATHER_BLOCK.get()
        );
    }
}