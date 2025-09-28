package de.davidvogt.hkbmod.item.custom;

import de.davidvogt.hkbmod.entities.ThrowingKnifeEntity;
import de.davidvogt.hkbmod.item.custom.abstracts.AbstractThrowingKnifeItem;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Ein explosives Wurfmesser, das beim Aufprall eine Explosion verursacht.
 * Verursacht mehr Schaden als normale Wurfmesser, fliegt aber etwas langsamer.
 */
public class ThrowingKnifeExplosiveItem extends AbstractThrowingKnifeItem {

    private static final float EXPLOSIVE_DAMAGE = 8.0F;    // Höherer Schaden
    private static final float EXPLOSIVE_VELOCITY = 1.3F;  // Etwas langsamer
    private static final float EXPLOSIVE_SOUND_PITCH = 0.2F; // Tieferer Sound

    public ThrowingKnifeExplosiveItem(Properties properties) {
        super(properties, EXPLOSIVE_DAMAGE, EXPLOSIVE_VELOCITY, EXPLOSIVE_SOUND_PITCH);
    }

    @Override
    protected void configureProjectile(ThrowingKnifeEntity projectile, ItemStack itemStack, Player player) {
        // Basis-Konfiguration
        projectile.setBaseDamage(getDamage());

        // Aktiviere Explosions-Modus
        projectile.setExplosive(true);

        // Optional: Zusätzliche Eigenschaften für explosives Messer
        projectile.setKnockback(1.5F); // Starker Knockback durch Explosion
    }

    @Override
    protected void onThrow(Level level, Player player, ItemStack itemStack) {
        // Warnung für den Spieler (nur auf Client-Seite für Chat-Nachrichten)
        if (level.isClientSide) {
            // Hier könntest du eine Warnung anzeigen oder Partikel spawnen
        }

        // Server-seitige Effekte nach dem Wurf
        if (!level.isClientSide) {
            // Optional: Rauch-Partikel am Wurfpunkt
            // level.addParticle könnte hier verwendet werden
        }
    }

    @Override
    protected SoundEvent getSoundEvent() {
        // Verwende einen dramatischeren Sound für das explosive Messer
        return SoundEvents.TNT_PRIMED; // Sound einer zündenden TNT
    }

    @Override
    protected boolean shouldConsumeItem(ItemStack itemStack, Player player) {
        // Explosive Messer sind wertvoller und werden immer verbraucht,
        // auch im Kreativmodus (außer explizit gewünscht)
        return true;
    }

}