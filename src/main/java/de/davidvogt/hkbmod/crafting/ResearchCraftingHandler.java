package de.davidvogt.hkbmod.crafting;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.item.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Event handler to check research requirements for crafting
 */
@Mod.EventBusSubscriber(modid = HkbMod.MOD_ID)
public class ResearchCraftingHandler {

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack craftedItem = event.getCrafting();
        Player player = event.getEntity();

        // Check if the crafted item is a throwing knife
        if (craftedItem.getItem() == ModItems.THROWING_KNIFE.get()) {
            if (!ResearchHelper.canCraftThrowingKnife(player)) {
                preventCrafting(player, craftedItem, "You need to complete the 'Basic Archery' research to craft this item!");
                return;
            }
        }

        // Check if the crafted item is a mystical wand
        if (craftedItem.getItem() == ModItems.MYSTICAL_WAND.get()) {
            if (!ResearchHelper.canCraftMysticalWand(player)) {
                preventCrafting(player, craftedItem, "You need to complete the 'Elemental Magic' research to craft this item!");
                return;
            }
        }
    }

    // Continuously monitor crafting result slots to prevent restricted items from appearing
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Skip if on client side - we only want server-side processing
        if (event.player.level().isClientSide) {
            return;
        }

        Player player = event.player;

        // Check if player has a crafting menu open
        if (player.containerMenu instanceof CraftingMenu craftingMenu) {
            // Get the result slot (slot 0)
            Slot resultSlot = craftingMenu.getSlot(0);
            ItemStack resultItem = resultSlot.getItem();

            if (!resultItem.isEmpty()) {
                boolean shouldPrevent = false;
                String message = "";

                // Check for mystical wand
                if (resultItem.getItem() == ModItems.MYSTICAL_WAND.get()) {
                    if (!ResearchHelper.canCraftMysticalWand(player)) {
                        shouldPrevent = true;
                        message = "You need to complete the 'Elemental Magic' research to craft this item!";
                    }
                }
                // Check for throwing knife
                else if (resultItem.getItem() == ModItems.THROWING_KNIFE.get()) {
                    if (!ResearchHelper.canCraftThrowingKnife(player)) {
                        shouldPrevent = true;
                        message = "You need to complete the 'Basic Archery' research to craft this item!";
                    }
                }

                if (shouldPrevent) {
                    // Immediately clear the result slot to prevent any interaction
                    resultSlot.set(ItemStack.EMPTY);

                    // Return all crafting materials
                    returnAllCraftingMaterials(player, craftingMenu);

                    // Send message to player (with cooldown to prevent spam)
                    sendMessageWithCooldown(player, message);

                    HkbMod.LOGGER.debug("[ResearchCraftingHandler] Prevented result slot item for player: " +
                        player.getName().getString() + " - Research requirement not met");
                }
            }
        }
    }

    // Simple message cooldown to prevent spam
    private static long lastMessageTime = 0;
    private static final long MESSAGE_COOLDOWN = 2000; // 2 seconds

    private static void sendMessageWithCooldown(Player player, String message) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMessageTime > MESSAGE_COOLDOWN) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(message),
                true
            );
            lastMessageTime = currentTime;
        }
    }

    private static void preventCrafting(Player player, ItemStack craftedItem, String message) {
        // Remove the crafted item
        craftedItem.setCount(0);

        if (!player.level().isClientSide) {
            // Get access to the player's current crafting container
            AbstractContainerMenu container = player.containerMenu;

            if (container instanceof CraftingMenu craftingMenu) {
                returnAllCraftingMaterials(player, craftingMenu);

                // Send message to player
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal(message),
                    true
                );

                HkbMod.LOGGER.info("[ResearchCraftingHandler] Prevented crafting for player: " +
                    player.getName().getString() + " - All materials returned");
            }
        }
    }

    private static void returnAllCraftingMaterials(Player player, CraftingMenu craftingMenu) {
        // Collect ALL items from the crafting grid (slots 1-9) and return them to player
        // Slot 0 is the result slot, slots 1-9 are the crafting grid
        for (int i = 1; i <= 9; i++) {
            ItemStack stackInSlot = craftingMenu.getSlot(i).getItem();
            if (!stackInSlot.isEmpty()) {
                // Give the entire stack back to the player
                ItemStack copy = stackInSlot.copy();
                player.addItem(copy);

                // Clear the slot in the crafting table
                craftingMenu.getSlot(i).set(ItemStack.EMPTY);
            }
        }
    }
}
