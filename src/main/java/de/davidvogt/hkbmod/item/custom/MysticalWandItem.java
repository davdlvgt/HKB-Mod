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

        Level level = pLevel;
        Player player = pPlayer;


        if (!level.isClientSide && player != null) {

            //if Control key is pressed, shoot a snowball
            if (player.isShiftKeyDown()) {
                Snowball snowball = new Snowball(level, player, new ItemStack(net.minecraft.world.item.Items.SNOWBALL, Integer.MAX_VALUE));

                snowball.setPos(
                        player.getX(),
                        player.getEyeY(),
                        player.getZ()
                );

                snowball.setDeltaMovement(player.getLookAngle().scale(3f));

                level.addFreshEntity(snowball);

                if (level instanceof ServerLevel serverLevel) {
                    player.hurtServer(serverLevel, player.damageSources().magic(), 1.0F);
                }

                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);

                // if snowball hits fire the fire is extinguished


                return InteractionResult.SUCCESS;
            }

            SmallFireball fireball = new SmallFireball(
                    level,
                    player,
                    player.getLookAngle()
            );

            fireball.setPos(
                    player.getX(),
                    player.getEyeY(),
                    player.getZ()
            );

            level.addFreshEntity(fireball);

            if (level instanceof ServerLevel serverLevel) {
                player.hurtServer(serverLevel, player.damageSources().magic(), 2.0F);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }
}
