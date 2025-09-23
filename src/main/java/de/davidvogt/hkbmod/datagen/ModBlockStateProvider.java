package de.davidvogt.hkbmod.datagen;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.block.ModBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.stream.Stream;

/**
 * Class that provides block models and blockstates for the mod.
 */

public class ModBlockStateProvider extends ModelProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output);
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
                String name = ForgeRegistries.BLOCKS.getKey(block).getPath();
                ResourceLocation texture = ResourceLocation.parse(HkbMod.MOD_ID + ":block/" + name);

                TextureMapping mapping = TextureMapping.cube(texture);

                ModelTemplates.CUBE_ALL.create(
                        block,
                        mapping,
                        models
                );

                // Funktionierender ItemModel-Eintrag:
                items.accept(
                        block.asItem(),
                        ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(block))
                );
            }

        };
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

    @Override
    protected Stream<net.minecraft.world.item.Item> getKnownItems() {
        // BlockItems müssen ebenfalls als Items registriert werden
        return getKnownBlocks()
                .map(block -> block.asItem()) // Block -> Item
                .filter(item -> item instanceof BlockItem); // nur BlockItems
    }
}
