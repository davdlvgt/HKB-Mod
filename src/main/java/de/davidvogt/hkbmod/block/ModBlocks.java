package de.davidvogt.hkbmod.block;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.block.custom.FeatherBlock;
import de.davidvogt.hkbmod.block.custom.JumpBlock;
import de.davidvogt.hkbmod.block.custom.MagicBlock;
import de.davidvogt.hkbmod.block.custom.ResearchTableBlock;
import de.davidvogt.hkbmod.block.custom.StringBlock;
import de.davidvogt.hkbmod.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, HkbMod.MOD_ID);

    public static final RegistryObject<Block> ALEXANDRITE_BLOCK = registerBlock("alexandrite_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(4f).requiresCorrectToolForDrops().sound(SoundType.AMETHYST)
                    .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.parse("hkbmod:alexandrite_block")))));

    public static final RegistryObject<Block> RAW_ALEXANDRITE_BLOCK = registerBlock("raw_alexandrite_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(3f).requiresCorrectToolForDrops()
                    .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.parse("hkbmod:raw_alexandrite_block")))));

    public static final RegistryObject<Block> ALEXANDRITE_ORE = registerBlock("alexandrite_ore",
            () -> new DropExperienceBlock(UniformInt.of(2, 4), BlockBehaviour.Properties.of()
                    .strength(4f).requiresCorrectToolForDrops()
                    .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.parse("hkbmod:alexandrite_ore")))));

    public static final RegistryObject<Block> ALEXANDRITE_DEEPSLATE_ORE = registerBlock("alexandrite_deepslate_ore",
            () -> new DropExperienceBlock(UniformInt.of(3, 5), BlockBehaviour.Properties.of()
                    .strength(5f).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)
                    .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.parse("hkbmod:alexandrite_deepslate_ore")))));

    public static final RegistryObject<Block> MAGIC_BLOCK = registerBlock("magic_block",
            () -> new MagicBlock(BlockBehaviour.Properties.of()
                    .strength(2f).requiresCorrectToolForDrops().sound(SoundType.AMETHYST)
                    .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.parse("hkbmod:magic_block")))));

    public static final RegistryObject<Block> JUMP_BLOCK = registerBlock("jump_block",
            () -> new JumpBlock(BlockBehaviour.Properties.of()
                    .strength(2f).requiresCorrectToolForDrops().sound(SoundType.AMETHYST)
                    .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.parse("hkbmod:jump_block")))));

    public static final RegistryObject<Block> STRING_BLOCK = registerBlock("string_block",
            () -> new StringBlock(BlockBehaviour.Properties.of()
                    .strength(0.5f).requiresCorrectToolForDrops().sound(SoundType.WOOL)
                    .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.parse("hkbmod:string_block")))));

    public static final RegistryObject<Block> FEATHER_BLOCK = registerBlock("feather_block",
            () -> new FeatherBlock(BlockBehaviour.Properties.of()
                    .strength(0.5f).requiresCorrectToolForDrops().sound(SoundType.WOOL)
                    .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.parse("hkbmod:feather_block")))));

    public static final RegistryObject<Block> RESEARCH_TABLE = registerBlock("research_table",
            () -> new ResearchTableBlock(BlockBehaviour.Properties.of()
                    .strength(2.5f).requiresCorrectToolForDrops().sound(SoundType.WOOD)
                    .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.parse("hkbmod:research_table")))));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().useItemDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.parse("hkbmod:" + name)))));
    }

    public static void register(BusGroup busGroup) {
        BLOCKS.register(busGroup);
    }
}
