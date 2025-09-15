package de.davidvogt.hkbmod.item.custom;

import de.davidvogt.hkbmod.entities.ThrowingKnifeEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class ThrowingKnifeItem extends Item {

    public ThrowingKnifeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // Debug-Nachricht
        System.out.println("Wurfmesser use() aufgerufen! ClientSide: " + level.isClientSide);

        if (!level.isClientSide) {
            // Erstelle das Wurfmesser-Projektil
            ThrowingKnifeEntity throwingKnife = new ThrowingKnifeEntity(level, player);

            // Setze die Position des Wurfmessers
            throwingKnife.setPos(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());

            // Setze die Bewegungsrichtung und Geschwindigkeit des Wurfmessers
            Vec3 direction = player.getLookAngle();
            throwingKnife.setDeltaMovement(direction.x * 1.5, direction.y * 1.5, direction.z * 1.5);

            // Debug-Nachricht
            System.out.println("Wurfmesser Richtung: " + direction);
            System.out.println("Wurfmesser Position: " + throwingKnife.position());

            // Füge das Wurfmesser der Welt hinzu
            level.addFreshEntity(throwingKnife);

            // Spiele den Wurf-Sound ab
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL,
                    0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

            System.out.println("Wurfmesser Entity erstellt und hinzugefügt!");
        }

        // Verbrauche das Item
        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }


}