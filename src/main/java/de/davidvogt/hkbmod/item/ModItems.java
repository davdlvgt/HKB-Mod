package de.davidvogt.hkbmod.item;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.item.custom.ChiselItem;
import de.davidvogt.hkbmod.item.custom.FuelItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
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
            () -> new Item(new Item.Properties()
                    .durability(64).stacksTo(1)
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.parse("hkbmod:mystical_wand")))));

    public static void register(BusGroup busGroup) {
        ITEMS.register(busGroup);
    }
}
