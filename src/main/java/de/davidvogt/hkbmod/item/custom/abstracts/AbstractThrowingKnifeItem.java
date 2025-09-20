package de.davidvogt.hkbmod.item.custom.abstracts;

import de.davidvogt.hkbmod.entities.ThrowingKnifeEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractThrowingKnifeItem extends Item {

    protected final float damage;
    protected final float velocity;
    protected final float soundPitch;

    public AbstractThrowingKnifeItem(Properties properties, float damage, float velocity, float soundPitch) {
        super(properties);
        this.damage = damage;
        this.velocity = velocity;
        this.soundPitch = soundPitch;
    }

    public AbstractThrowingKnifeItem(Properties properties) {
        this(properties, 4.0F, 1.5F, 0.4F);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // Erstelle das Wurfmesser-Projektil
            ThrowingKnifeEntity throwingKnife = createProjectile(level, player, itemStack);

            // Setze die Position des Wurfmessers
            throwingKnife.setPos(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());

            // Setze die Bewegungsrichtung und Geschwindigkeit des Wurfmessers
            Vec3 direction = player.getLookAngle();
            throwingKnife.setDeltaMovement(direction.x * velocity, direction.y * velocity, direction.z * velocity);

            // Konfiguriere das Projektil basierend auf dem Item-Typ
            configureProjectile(throwingKnife, itemStack, player);

            // Füge das Wurfmesser der Welt hinzu
            level.addFreshEntity(throwingKnife);

            // Spiele den Wurf-Sound ab
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    getSoundEvent(), SoundSource.NEUTRAL,
                    0.5F, soundPitch / (level.getRandom().nextFloat() * 0.4F + 0.8F));

            // Führe zusätzliche Aktionen nach dem Wurf aus
            onThrow(level, player, itemStack);
        }

        // Verbrauche das Item (wenn nicht im Kreativmodus)
        if (!player.getAbilities().instabuild && shouldConsumeItem(itemStack, player)) {
            itemStack.shrink(1);
        }

        return getUseResult(itemStack, player);
    }

    /**
     * Erstellt das Projektil-Entity. Kann überschrieben werden für spezielle Projektil-Typen.
     */
    protected ThrowingKnifeEntity createProjectile(Level level, Player player, ItemStack itemStack) {
        return new ThrowingKnifeEntity(level, player);
    }

    /**
     * Konfiguriert das Projektil nach der Erstellung. Hier können spezifische Eigenschaften gesetzt werden.
     */
    protected abstract void configureProjectile(ThrowingKnifeEntity projectile, ItemStack itemStack, Player player);

    /**
     * Wird nach dem erfolgreichen Wurf aufgerufen. Für zusätzliche Effekte oder Aktionen.
     */
    protected void onThrow(Level level, Player player, ItemStack itemStack) {
        // Standard: keine zusätzlichen Aktionen
    }

    /**
     * Bestimmt das Sound-Event beim Werfen. Kann überschrieben werden für verschiedene Sounds.
     */
    protected net.minecraft.sounds.SoundEvent getSoundEvent() {
        return SoundEvents.SNOWBALL_THROW;
    }

    /**
     * Bestimmt ob das Item verbraucht werden soll. Kann für unendliche Items überschrieben werden.
     */
    protected boolean shouldConsumeItem(ItemStack itemStack, Player player) {
        return true;
    }

    /**
     * Das Ergebnis der use() Methode. Kann für verschiedene Verhalten überschrieben werden.
     */
    protected InteractionResult getUseResult(ItemStack itemStack, Player player) {
        return InteractionResult.SUCCESS;
    }

    // Getter für geschützte Felder
    public float getDamage() {
        return damage;
    }

    public float getVelocity() {
        return velocity;
    }

    public float getSoundPitch() {
        return soundPitch;
    }
}
