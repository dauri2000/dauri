package org.uiop.easyplacefix.placement;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Classifies a real current cell; it never treats a consumed click as an ACK. */
public final class PottedPlantPlan {
    public enum Stage { NOT_POTTED, COMPLETE, PLACE_EMPTY, FILL_EMPTY, BLOCKED }

    private PottedPlantPlan() {}

    public static boolean isFilledGoal(BlockState goal) {
        return goal.getBlock() instanceof FlowerPotBlock pot && pot.getPotted()!=Blocks.AIR;
    }

    public static Item plantItem(BlockState goal) {
        return isFilledGoal(goal) ? ((FlowerPotBlock)goal.getBlock()).getPotted().asItem() : Items.AIR;
    }

    public static Stage stage(BlockState goal, BlockState current) {
        if (!isFilledGoal(goal)) return Stage.NOT_POTTED;
        if (current.equals(goal)) return Stage.COMPLETE;
        if (current.is(Blocks.FLOWER_POT)) return Stage.FILL_EMPTY;
        if (current.getBlock() instanceof FlowerPotBlock || !current.canBeReplaced()) return Stage.BLOCKED;
        return Stage.PLACE_EMPTY;
    }

    public static BlockState emptyGoal() { return Blocks.FLOWER_POT.defaultBlockState(); }
}
