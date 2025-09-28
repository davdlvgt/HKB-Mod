package de.davidvogt.hkbmod.datagen;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.block.ModBlocks;
import de.davidvogt.hkbmod.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Class that provides recipes for the mod.
 */

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(HolderLookup.Provider pProvider, RecipeOutput pOutput) {
        super(pProvider, pOutput);
    }

    @Override
    protected void buildRecipes() {
        List<ItemLike> ALEXANDRITE_SMELTABLES = List.of(ModItems.RAW_ALEXANDRITE.get(),
                ModBlocks.ALEXANDRITE_ORE.get(), ModBlocks.ALEXANDRITE_DEEPSLATE_ORE.get());

        this.shaped(RecipeCategory.MISC, ModBlocks.ALEXANDRITE_BLOCK.get(), 1)
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.ALEXANDRITE.get())
                .unlockedBy(getHasName(ModItems.ALEXANDRITE.get()), has(ModItems.ALEXANDRITE.get())).save(this.output);

        this.shaped(RecipeCategory.MISC, ModBlocks.STRING_BLOCK.get(), 1)
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', Items.STRING)
                .unlockedBy(getHasName(Items.STRING), has(Items.STRING)).save(this.output);

        this.shaped(RecipeCategory.MISC, ModBlocks.JUMP_BLOCK.get(), 1)
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModBlocks.STRING_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.STRING_BLOCK.get()), has(ModBlocks.STRING_BLOCK.get())).save(this.output);

        this.shaped(RecipeCategory.MISC, ModBlocks.FEATHER_BLOCK.get(), 1)
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', Items.FEATHER)
                .unlockedBy(getHasName(Items.FEATHER), has(Items.FEATHER)).save(this.output);

        this.shaped(RecipeCategory.MISC, ModItems.FEATHER_WINGS.get(), 1)
                .pattern("AAA")
                .pattern("AAA")
                .pattern(" A ")
                .define('A', ModBlocks.FEATHER_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.FEATHER_BLOCK.get()), has(ModBlocks.FEATHER_BLOCK.get())).save(this.output);

        // Throwing knife recipe - visible but only craftable with archer_basic research
        this.shaped(RecipeCategory.COMBAT, ModItems.THROWING_KNIFE.get(), 1)
                .pattern(" N ")
                .pattern(" I ")
                .pattern(" A ")
                .define('N', Items.IRON_NUGGET)
                .define('I', Items.IRON_INGOT)
                .define('A', Items.STICK)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(this.output);

        this.shapeless(RecipeCategory.COMBAT, ModItems.THROWING_KNIFE_EXPLOSIVE.get(), 1)
                .requires(ModItems.THROWING_KNIFE.get())
                .requires(Items.TNT)
                .unlockedBy(getHasName(ModItems.THROWING_KNIFE.get()), has(ModItems.THROWING_KNIFE.get())).save(this.output);

        this.shaped(RecipeCategory.TOOLS, ModItems.GROWTH_ACCELERATOR_WAND.get(), 1)
                .pattern(" AB")
                .pattern(" CA")
                .pattern("C  ")
                .define('A', ModItems.ALEXANDRITE.get())
                .define('B', Items.BONE_MEAL)
                .define('C', Items.STICK)
                .unlockedBy(getHasName(ModItems.ALEXANDRITE.get()), has(ModItems.ALEXANDRITE.get())).save(this.output);

        this.shaped(RecipeCategory.TOOLS, ModItems.MAGNETIC_BAR.get(), 1)
                .pattern(" A ")
                .pattern("IRI")
                .pattern(" I ")
                .define('A', ModItems.ALEXANDRITE.get())
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .unlockedBy(getHasName(ModItems.ALEXANDRITE.get()), has(ModItems.ALEXANDRITE.get())).save(this.output);

        this.shaped(RecipeCategory.DECORATIONS, ModBlocks.RESEARCH_TABLE.get(), 1)
                .pattern("AAA")
                .pattern("BWB")
                .pattern("B B")
                .define('A', Items.BOOK)
                .define('W', Items.CRAFTING_TABLE)
                .define('B', Items.OAK_PLANKS)
                .unlockedBy(getHasName(Items.CRAFTING_TABLE), has(Items.CRAFTING_TABLE)).save(this.output);

        // Mystical Wand recipe - visible but only craftable with magician_elemental research
        this.shaped(RecipeCategory.COMBAT, ModItems.MYSTICAL_WAND.get(), 1)
                .pattern("PAP")
                .pattern("BSB")
                .pattern(" S ")
                .define('P', Items.BLAZE_POWDER)
                .define('A', ModItems.ALEXANDRITE.get())
                .define('B', Items.BOOK)
                .define('S', Items.STICK)
                .unlockedBy(getHasName(ModItems.ALEXANDRITE.get()), has(ModItems.ALEXANDRITE.get()))
                .unlockedBy(getHasName(Items.BLAZE_POWDER), has(Items.BLAZE_POWDER))
                .save(this.output);

        this.shapeless(RecipeCategory.MISC, ModItems.ALEXANDRITE.get(), 9)
                .requires(ModBlocks.ALEXANDRITE_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.ALEXANDRITE_BLOCK.get()), has(ModBlocks.ALEXANDRITE_BLOCK.get())).save(this.output);

        this.shapeless(RecipeCategory.MISC, ModItems.ALEXANDRITE.get(), 32)
                .requires(ModBlocks.MAGIC_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.ALEXANDRITE_BLOCK.get()), has(ModBlocks.ALEXANDRITE_BLOCK.get()))
                .save(this.output, HkbMod.MOD_ID + ":alexandrite_from_magic_block");

        oreSmelting(this.output, ALEXANDRITE_SMELTABLES, RecipeCategory.MISC, ModItems.ALEXANDRITE.get(), 0.25f, 200, "alexandrite");
        oreBlasting(this.output, ALEXANDRITE_SMELTABLES, RecipeCategory.MISC, ModItems.ALEXANDRITE.get(), 0.25f, 100, "alexandrite");

    }

    protected void oreSmelting(RecipeOutput recipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult,
                               float pExperience, int pCookingTIme, String pGroup) {
        oreCooking(recipeOutput, RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new, pIngredients, pCategory, pResult,
                pExperience, pCookingTIme, pGroup, "_from_smelting");
    }

    protected void oreBlasting(RecipeOutput recipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult,
                               float pExperience, int pCookingTime, String pGroup) {
        oreCooking(recipeOutput, RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new, pIngredients, pCategory, pResult,
                pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    protected <T extends AbstractCookingRecipe> void oreCooking(
            RecipeOutput recipeOutput,
            RecipeSerializer<T> pCookingSerializer,
            AbstractCookingRecipe.Factory<T> factory,
            List<ItemLike> pIngredients,
            RecipeCategory pCategory,
            ItemLike pResult,
            float pExperience,
            int pCookingTime,
            String pGroup,
            String pRecipeName
    ) {
        for (ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult, pExperience, pCookingTime, pCookingSerializer, factory).group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(recipeOutput, HkbMod.MOD_ID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
        }
    }


    public static class Generator extends RecipeProvider.Runner {
        public Generator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected @NotNull RecipeProvider createRecipeProvider(HolderLookup.@NotNull Provider registries, @NotNull RecipeOutput output) {
            return new ModRecipeProvider(registries, output);
        }

        @Override
        public @NotNull String getName() {
            return "";
        }
    }
}