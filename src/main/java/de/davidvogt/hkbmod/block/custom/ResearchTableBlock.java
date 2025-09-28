package de.davidvogt.hkbmod.block.custom;

import com.mojang.serialization.MapCodec;
import de.davidvogt.hkbmod.block.entity.ModBlockEntities;
import de.davidvogt.hkbmod.block.entity.ResearchTableBlockEntity;
import de.davidvogt.hkbmod.menu.ResearchTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ResearchTableBlock extends BaseEntityBlock {
    public static final MapCodec<ResearchTableBlock> CODEC = simpleCodec(ResearchTableBlock::new);

    public ResearchTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ResearchTableBlockEntity researchTableEntity) {
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (containerId, playerInventory, playerEntity) ->
                                new ResearchTableMenu(containerId, playerInventory, researchTableEntity, ContainerLevelAccess.create(level, pos)),
                        Component.translatable("container.hkbmod.research_table")
                ));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResearchTableBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null; // No client-side ticking needed
        }

        return createTickerHelper(blockEntityType, ModBlockEntities.RESEARCH_TABLE_BE.get(),
                ResearchTableBlockEntity::tick);
    }

    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ResearchTableBlockEntity researchTableEntity) {
                researchTableEntity.dropContents(level, pos);
            }
        }
    }
}