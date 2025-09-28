package de.davidvogt.hkbmod.item.custom;

import de.davidvogt.hkbmod.entities.ThrowingKnifeEntity;
import de.davidvogt.hkbmod.item.custom.abstracts.AbstractThrowingKnifeItem;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

public class ThrowingKnifeInstantDamageItem extends AbstractThrowingKnifeItem {

    private static final float INSTANT_DAMAGE_DAMAGE = 2.0F;    // Niedriger base damage
    private static final float INSTANT_DAMAGE_VELOCITY = 1.6F;  // Schneller
    private static final float INSTANT_DAMAGE_SOUND_PITCH = 0.8F; // Höherer Sound

    public ThrowingKnifeInstantDamageItem(Properties properties) {
        super(properties, INSTANT_DAMAGE_DAMAGE, INSTANT_DAMAGE_VELOCITY, INSTANT_DAMAGE_SOUND_PITCH);
    }

    @Override
    protected void configureProjectile(ThrowingKnifeEntity projectile, ItemStack itemStack, Player player) {
        // Setze den Schaden basierend auf dem Item
        projectile.setBaseDamage(getDamage());

        // Aktiviere Instant Damage-Effekt durch Trank
        projectile.setImbuedPotion(Potions.HARMING);

        // Optional: Zusätzliche Eigenschaften für Instant Damage-Messer
        projectile.setKnockback(1.0F); // Mittlerer Knockback
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
        // Verwende einen aggressiveren Sound für das Instant Damage-Messer
        return SoundEvents.LINGERING_POTION_THROW; // Sound eines geworfenen Verweiltranks
    }

    @Override
    protected boolean shouldConsumeItem(ItemStack itemStack, Player player) {
        // Instant Damage-Messer sind mächtig und werden immer verbraucht
        return true;
    }
}