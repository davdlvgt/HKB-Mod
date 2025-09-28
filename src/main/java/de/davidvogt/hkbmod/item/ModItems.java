package de.davidvogt.hkbmod.item;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.item.custom.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, HkbMod.MOD_ID);

    public static final RegistryObject<Item> ALEXANDRITE = ITEMS.register("alexandrite",
            () -> new Item(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.parse("hkbmod:alexandrite")))));

    public static final RegistryObject<Item> RAW_ALEXANDRITE = ITEMS.register("raw_alexandrite",
            () -> new Item(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.parse("hkbmod:raw_alexandrite")))));

    public static final RegistryObject<Item> CHISEL = ITEMS.register("chisel",
            () -> new ChiselItem(new Item.Properties().durability(32)
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.parse("hkbmod:chisel")))));

    public static final RegistryObject<Item> KOHLRABI = ITEMS.register("kohlrabi",
            () -> new Item(new Item.Properties().food(ModFoodProperties.KOHLRABI, ModFoodProperties.KOHLRABI_EFFECT)
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.parse("hkbmod:kohlrabi")))));

    public static final RegistryObject<Item> AURORA_ASHES = ITEMS.register("aurora_ashes",
            () -> new FuelItem(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.parse("hkbmod:aurora_ashes"))), 1200));

    public static final RegistryObject<Item> MYSTICAL_WAND = ITEMS.register("mystical_wand",
            () -> new MysticalWandItem(new Item.Properties()
                    .durability(64).stacksTo(1)
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.parse("hkbmod:mystical_wand")))));

    public static final RegistryObject<Item> FEATHER_WINGS = ITEMS.register("feather_wings",
            () -> new FeatherWingsItem(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.parse("hkbmod:feather_wings")))));

    public static final RegistryObject<Item> THROWING_KNIFE = ITEMS.register("throwing_knife",
            () -> new ThrowingKnifeItem(new Item.Properties()
                    .stacksTo(16)
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.parse("hkbmod:throwing_knife")))));

    public static final RegistryObject<Item> THROWING_KNIFE_EXPLOSIVE = ITEMS.register("throwing_knife_explosive",
            () -> new ThrowingKnifeExplosiveItem(new Item.Properties()
                    .stacksTo(8)
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.parse("hkbmod:throwing_knife_explosive")))));

    public static final RegistryObject<Item> THROWING_KNIFE_SLOWNESS = ITEMS.register("throwing_knife_slowness",
            () -> new ThrowingKnifeSlownessItem(new Item.Properties()
                    .stacksTo(12)
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.parse("hkbmod:throwing_knife_slowness")))));

    public static final RegistryObject<Item> THROWING_KNIFE_INSTANT_DAMAGE = ITEMS.register("throwing_knife_instant_damage",
            () -> new ThrowingKnifeInstantDamageItem(new Item.Properties()
                    .stacksTo(6)
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.parse("hkbmod:throwing_knife_instant_damage")))));

    public static final RegistryObject<Item> LIGHTNING_AXE = ITEMS.register("lightning_axe",
            () -> new LightningAxeItem(new Item.Properties()
                    .stacksTo(1)
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.parse("hkbmod:lightning_axe")))));

    public static final RegistryObject<Item> LIGHTNING_WAND = ITEMS.register("lightning_wand",
            () -> new LightningWandItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(100)
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.parse("hkbmod:lightning_wand")))));

    public static final RegistryObject<Item> GROWTH_ACCELERATOR_WAND = ITEMS.register("growth_accelerator_wand",
            () -> new GrowthAcceleratorWandItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(128)
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.parse("hkbmod:growth_accelerator_wand")))));

    public static final RegistryObject<Item> MAGNETIC_BAR = ITEMS.register("magnetic_bar",
            () -> new MagneticBarItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(256)
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.parse("hkbmod:magnetic_bar")))));

    public static final RegistryObject<Item> DEER_SPAWN_EGG = ITEMS.register("deer_spawn_egg",
            () -> new SpawnEggItem(de.davidvogt.hkbmod.entity.ModEntityTypes.DEER.get(),
                    new Item.Properties()
                            .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.parse("hkbmod:deer_spawn_egg")))));

    public static void register(BusGroup busGroup) {
        ITEMS.register(busGroup);
    }
}
