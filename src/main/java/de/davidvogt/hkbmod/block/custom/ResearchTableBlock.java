package de.davidvogt.hkbmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ResearchTableBlock extends Block {

    public ResearchTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            level.playSound(null, pos, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Research Table - GUI coming soon!"), true);
        }
        return InteractionResult.SUCCESS;
    }
}