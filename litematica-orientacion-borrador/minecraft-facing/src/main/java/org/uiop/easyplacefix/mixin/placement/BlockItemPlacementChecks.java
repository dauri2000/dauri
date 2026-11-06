package org.uiop.easyplacefix.mixin.placement;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Native virtual item checks; subclasses retain their own placement behavior. */
@Mixin(BlockItem.class)
public interface BlockItemPlacementChecks {
    @Invoker("getPlacementState")
    BlockState easyplacefix$getPlacementState(BlockPlaceContext context);

    @Invoker("canPlace")
    boolean easyplacefix$canPlace(BlockPlaceContext context, BlockState state);
}
