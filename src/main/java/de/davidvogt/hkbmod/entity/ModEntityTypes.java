package de.davidvogt.hkbmod.entity;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.entity.custom.DeerEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HkbMod.MOD_ID);

    public static final RegistryObject<EntityType<ThrowingKnifeEntity>> THROWING_KNIFE =
            ENTITY_TYPES.register("throwing_knife", () -> EntityType.Builder.<ThrowingKnifeEntity>of(
                            ThrowingKnifeEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(HkbMod.MOD_ID, "throwing_knife"))));

    public static final RegistryObject<EntityType<DeerEntity>> DEER =
            ENTITY_TYPES.register("deer", () -> EntityType.Builder.of(DeerEntity::new, MobCategory.CREATURE)
                    .sized(0.9f, 1.4f) // Width and height
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(HkbMod.MOD_ID, "deer"))));

    public static void register(BusGroup eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}