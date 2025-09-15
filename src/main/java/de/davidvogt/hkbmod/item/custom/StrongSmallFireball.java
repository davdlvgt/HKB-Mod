/*
package de.davidvogt.hkbmod.item.custom;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.EntityHitResult;

public class StrongSmallFireball extends SmallFireball {

    public StrongSmallFireball(Level level, LivingEntity shooter, Vec3 direction) {
        super(level, shooter, shooter.getX(), shooter.getEyeY(), shooter.getZ(), direction);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide && hitResult instanceof EntityHitResult entityHit) {
            entityHit.getEntity().hurt(DamageSource.indirectMagic(this, this.getOwner()), 10.0F);
        }
    }
}
*/
