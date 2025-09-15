package de.davidvogt.hkbmod.item.custom;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "hkbmod")
public class FeatherWingsItem extends Item {
    public FeatherWingsItem(Properties properties) {
        super(properties.stacksTo(12));
    }

    @Override
    public InteractionResult use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        if (!pLevel.isClientSide) {
            // Blickrichtung des Spielers
            var lookVec = pPlayer.getLookAngle(); // Einheitlicher Vektor in Blickrichtung

            // Stärke des Boosts, z.B. 1.5 für moderates Flug
            double speed = 15;

            // Bewegung setzen: Vektor * Geschwindigkeit
            pPlayer.setDeltaMovement(
                    pPlayer.getDeltaMovement().add(
                            lookVec.x * speed,
                            lookVec.y * speed,
                            lookVec.z * speed
                    )
            );

            // Spieler-Update erzwingen
            pPlayer.hurtMarked = true;

            // Item verbrauchen
            pPlayer.getItemInHand(pHand).shrink(1);

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }


    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent.Post event) {
        Player player = event.player;
        if (player.level().isClientSide) return;

        ItemStack mainHand = player.getMainHandItem();
        if (!(mainHand.getItem() instanceof FeatherWingsItem)) return;

        if (!player.onGround() && player.fallDistance > 3.0f) {
            if (!player.isFallFlying()) {
                player.startFallFlying();
            }
            player.getPersistentData().putBoolean("FeatherWingsFlying", true);
        } else {
            boolean wasFlying = player.getPersistentData().getBoolean("FeatherWingsFlying").orElse(false);

            if (wasFlying && player.onGround()) {
                mainHand.shrink(1);
                player.getPersistentData().putBoolean("FeatherWingsFlying", false);
            }
        }
    }
}
