package de.davidvogt.hkbmod.menu;

import de.davidvogt.hkbmod.HkbMod;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, HkbMod.MOD_ID);

    public static final RegistryObject<MenuType<ResearchTableMenu>> RESEARCH_TABLE_MENU =
            MENUS.register("research_table_menu", () ->
                    IForgeMenuType.create(ResearchTableMenu::new));

    public static void register(BusGroup busGroup) {
        MENUS.register(busGroup);
    }
}