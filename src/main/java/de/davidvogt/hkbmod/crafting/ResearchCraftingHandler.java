package de.davidvogt.hkbmod.crafting;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.item.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Event handler to check research requirements for throwing knife crafting
 */
@Mod.EventBusSubscriber(modid = HkbMod.MOD_ID)
public class ResearchCraftingHandler {

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack craftedItem = event.getCrafting();
        Player player = event.getEntity();

        // Check if the crafted item is a throwing knife
        if (craftedItem.getItem() == ModItems.THROWING_KNIFE.get()) {
            // Check if player has the required research
            if (!ResearchHelper.canCraftThrowingKnife(player)) {
                // Remove the crafted item from inventory and return ingredients
                craftedItem.setCount(0); // Clear the result

                // Return the crafting ingredients to the player
                if (!player.level().isClientSide) {
                    // Give back the ingredients
                    player.addItem(new ItemStack(Items.IRON_NUGGET, 1));
                    player.addItem(new ItemStack(Items.IRON_INGOT, 1));
                    player.addItem(new ItemStack(Items.STICK, 1));

                    // Send a message to the player
                    player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("You need to complete the 'Basic Archery' research to craft this item!"),
                        true
                    );
                }
            }
        }
    }
}
