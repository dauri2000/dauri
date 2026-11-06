package org.uiop.easyplacefix.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;

/** The first half of a schematic double chest may be placed before its partner. */
public final class NativeChestPlacement {
    private NativeChestPlacement() {}

    public static boolean allowsFirstSingleHalf(BlockState wanted, BlockState preview,
            BlockPos position, Level schematic, Level world) {
        if (schematic==null || world==null || !(wanted.getBlock() instanceof ChestBlock)
            || wanted.getBlock()!=preview.getBlock()) return false;
        ChestType type=wanted.getValue(BlockStateProperties.CHEST_TYPE);
        if (type==ChestType.SINGLE
            || !wanted.setValue(BlockStateProperties.CHEST_TYPE,ChestType.SINGLE).equals(preview)) return false;
        BlockPos partnerPosition=ChestBlock.getConnectedBlockPos(position,wanted);
        if (!schematic.isLoaded(position) || !schematic.isLoaded(partnerPosition)
            || !world.isLoaded(position) || !world.isLoaded(partnerPosition)
            || !schematic.getBlockState(position).equals(wanted)) return false;
        BlockState partner=schematic.getBlockState(partnerPosition);
        if (partner.getBlock()!=wanted.getBlock()
            || partner.getValue(BlockStateProperties.HORIZONTAL_FACING)!=wanted.getValue(BlockStateProperties.HORIZONTAL_FACING)
            || partner.getValue(BlockStateProperties.CHEST_TYPE)!=type.getOpposite()
            || !ChestBlock.getConnectedBlockPos(partnerPosition,partner).equals(position)) return false;
        BlockState currentPartner=world.getBlockState(partnerPosition);
        if (currentPartner.getBlock() instanceof ChestBlock) return false;
        return currentPartner.isAir() || currentPartner.canBeReplaced();
    }
}
