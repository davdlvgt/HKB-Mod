package de.davidvogt.hkbmod.item.custom;

import com.electronwill.nightconfig.core.serde.annotations.SerdeAssert;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class MysticalWandItem extends Item {

    public MysticalWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        if (!pLevel.isClientSide && pPlayer != null) {

            //if Control key is pressed, shoot a snowball
            if (pPlayer.isShiftKeyDown()) {
                Snowball snowball = new Snowball(pLevel, pPlayer, new ItemStack(net.minecraft.world.item.Items.SNOWBALL, Integer.MAX_VALUE));

                snowball.setPos(
                        pPlayer.getX(),
                        pPlayer.getEyeY(),
                        pPlayer.getZ()
                );

                snowball.setDeltaMovement(pPlayer.getLookAngle().scale(3f));

                pLevel.addFreshEntity(snowball);

                if (pLevel instanceof ServerLevel serverLevel) {
                    pPlayer.hurtServer(serverLevel, pPlayer.damageSources().magic(), 1.0F);
                }

                pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);

                // if snowball hits fire the fire is extinguished


                return InteractionResult.SUCCESS;
            }

            SmallFireball fireball = new SmallFireball(
                    pLevel,
                    pPlayer,
                    pPlayer.getLookAngle()
            );

            fireball.setPos(
                    pPlayer.getX(),
                    pPlayer.getEyeY(),
                    pPlayer.getZ()
            );

            pLevel.addFreshEntity(fireball);

            if (pLevel instanceof ServerLevel serverLevel) {
                pPlayer.hurtServer(serverLevel, pPlayer.damageSources().magic(), 2.0F);
            }

            pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }
}
