package de.davidvogt.hkbmod.entities;

import de.davidvogt.hkbmod.item.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrowingKnifeEntity extends ThrowableItemProjectile {

    public ThrowingKnifeEntity(EntityType<? extends ThrowingKnifeEntity> entityType, Level level) {
        super(entityType, level);
    }

    public ThrowingKnifeEntity(Level level, LivingEntity shooter) {
        super(ModEntityTypes.THROWING_KNIFE.get(), level);
        this.setOwner(shooter);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.THROWING_KNIFE.get(); // Referenz zu deinem Wurfmesser-Item
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();

        // Debug-Nachricht
        System.out.println("Wurfmesser getroffen: " + target);

        if (target instanceof LivingEntity livingTarget) {
            float damage = 4.0F;
            livingTarget.hurt(this.damageSources().thrown(this, this.getOwner()), damage);

            // Partikel-Effekt
            this.level().addParticle(ParticleTypes.CRIT,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    0.0, 0.0, 0.0);
        }

        // Sound-Effekt
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ARROW_HIT, SoundSource.NEUTRAL, 1.0F, 1.0F);

        // Lösche das Entity nur, wenn es wirklich nicht mehr benötigt wird
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        // Debug-Nachricht
        System.out.println("Wurfmesser auf Block oder andere Entität getroffen");

        if (result.getType() == HitResult.Type.BLOCK) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ARROW_HIT, SoundSource.NEUTRAL, 1.0F, 1.2F);
        }

        if (!this.level().isClientSide) {
            this.discard();
        }
    }
}