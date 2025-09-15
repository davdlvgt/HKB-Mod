package de.davidvogt.hkbmod.entities;

import de.davidvogt.hkbmod.HkbMod;
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
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "yourmodid"); // Ersetze "yourmodid"

    public static final RegistryObject<EntityType<ThrowingKnifeEntity>> THROWING_KNIFE =
            ENTITY_TYPES.register("throwing_knife", () -> EntityType.Builder.<ThrowingKnifeEntity>of(
                            ThrowingKnifeEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(HkbMod.MOD_ID, "throwing_knife"))));

    public static void register(BusGroup eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}