package org.uiop.easyplacefix.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Conservative native interaction classification; no interaction is executed. */
public final class NativeSupportActions {
    private NativeSupportActions() {}

    private static final ClassValue<Boolean> INTERACTIVE_CLASSES = new ClassValue<>() {
        @Override
        protected Boolean computeValue(Class<?> type) {
            try {
                for (Class<?> current = type;
                     current != null && current != Block.class && current != BlockBehaviour.class;
                     current = current.getSuperclass()) {
                    if (declares(current, "useItemOn", ItemStack.class, BlockState.class,
                            Level.class, BlockPos.class, Player.class, InteractionHand.class,
                            BlockHitResult.class)
                        || declares(current, "useWithoutItem", BlockState.class, Level.class,
                            BlockPos.class, Player.class, BlockHitResult.class)) {
                        return true;
                    }
                }
                return false;
            } catch (LinkageError | SecurityException unavailable) {
                return true;
            }
        }
    };

    /** Inherited overrides are included, including SignBlock/HangingSignBlock. */
    public static boolean mayConsumeBlockInteraction(Block block) {
        if (block == null) return true;
        try {
            return INTERACTIVE_CLASSES.get(block.getClass());
        } catch (LinkageError | SecurityException unavailable) {
            return true;
        }
    }

    /**
     * These native families merge/add a layer through BlockPlaceContext. This is
     * only a family guard: native item/context/preview checks remain mandatory.
     */
    public static boolean isKnownStackable(Block block) {
        try {
            return block instanceof SlabBlock || block instanceof CandleBlock
                || block instanceof SeaPickleBlock || block instanceof TurtleEggBlock
                || block instanceof SnowLayerBlock;
        } catch (LinkageError unavailable) {
            return false;
        }
    }

    private static boolean declares(Class<?> type, String name, Class<?>... parameters) {
        try {
            type.getDeclaredMethod(name, parameters);
            return true;
        } catch (NoSuchMethodException absent) {
            return false;
        }
    }
}
