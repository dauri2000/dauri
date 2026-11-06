package org.uiop.easyplacefix.mixin.placement;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.uiop.easyplacefix.placement.NativePlacementScope;

/** Projection is scoped by NativePlacementScope; ordinary calls are unchanged. */
@Mixin(BlockItem.class)
public abstract class MixinNativeBlockItemContext {
    @WrapMethod(method = "place")
    private InteractionResult easyplacefix$projectInitialPlacement(
        BlockPlaceContext context, Operation<InteractionResult> original) {
        return original.call(NativePlacementScope.project(context));
    }

    @WrapOperation(method = "place", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/BlockItem;updatePlacementContext(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/item/context/BlockPlaceContext;"))
    private BlockPlaceContext easyplacefix$projectUpdatedPlacement(
        BlockItem item, BlockPlaceContext context, Operation<BlockPlaceContext> original) {
        return NativePlacementScope.project(original.call(item, context));
    }

    @WrapOperation(method = "useOn", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/BlockItem;place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult easyplacefix$projectVirtualPlacement(
        BlockItem item, BlockPlaceContext context, Operation<InteractionResult> original) {
        return original.call(item, NativePlacementScope.project(context));
    }
}
