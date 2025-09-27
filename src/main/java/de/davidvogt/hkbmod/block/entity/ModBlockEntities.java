package de.davidvogt.hkbmod.block.entity;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, HkbMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<ResearchTableBlockEntity>> RESEARCH_TABLE_BE =
            BLOCK_ENTITIES.register("research_table_be", () ->
                    new BlockEntityType<>(ResearchTableBlockEntity::new,
                            java.util.Set.of(ModBlocks.RESEARCH_TABLE.get())));

    public static void register(BusGroup busGroup) {
        BLOCK_ENTITIES.register(busGroup);
    }
}