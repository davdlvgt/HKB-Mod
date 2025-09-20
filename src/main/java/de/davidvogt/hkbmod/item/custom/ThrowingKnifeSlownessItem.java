package de.davidvogt.hkbmod.item.custom;

import de.davidvogt.hkbmod.entities.ThrowingKnifeEntity;
import de.davidvogt.hkbmod.item.custom.abstracts.AbstractThrowingKnifeItem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

public class ThrowingKnifeSlownessItem extends AbstractThrowingKnifeItem {

    private static final float SLOWNESS_DAMAGE = 3.0F;    // Etwas weniger Schaden
    private static final float SLOWNESS_VELOCITY = 1.4F;  // Normale Geschwindigkeit
    private static final float SLOWNESS_SOUND_PITCH = 0.6F; // Mittlerer Sound

    public ThrowingKnifeSlownessItem(Properties properties) {
        super(properties, SLOWNESS_DAMAGE, SLOWNESS_VELOCITY, SLOWNESS_SOUND_PITCH);
    }

    @Override
    protected void configureProjectile(ThrowingKnifeEntity projectile, ItemStack itemStack, Player player) {
        // Setze den Schaden basierend auf dem Item
        projectile.setBaseDamage(getDamage());

        // Aktiviere Slowness-Effekt durch Trank
        projectile.setImbuedPotion(Potions.SLOWNESS);

        // Optional: Zusätzliche Eigenschaften für Slowness-Messer
        projectile.setKnockback(0.5F); // Leichter Knockback
    }

    @Override
    protected void onThrow(Level level, Player player, ItemStack itemStack) {
        // Server-seitige Effekte nach dem Wurf
        if (!level.isClientSide) {
            // Optional: Spezielle Partikel oder Sounds könnten hier hinzugefügt werden
        }
    }

    @Override
    protected SoundEvent getSoundEvent() {
        // Verwende einen passenden Sound für das Slowness-Messer
        return SoundEvents.SPLASH_POTION_THROW; // Sound eines geworfenen Tranks
    }

    @Override
    protected boolean shouldConsumeItem(ItemStack itemStack, Player player) {
        // Slowness-Messer werden normal verbraucht
        return true;
    }
}