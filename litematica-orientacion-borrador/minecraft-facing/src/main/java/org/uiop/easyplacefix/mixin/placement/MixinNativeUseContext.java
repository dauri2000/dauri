package org.uiop.easyplacefix.mixin.placement;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.uiop.easyplacefix.placement.NativePlacementScope;

/** Align local interaction routing with this action's already planned sneak input. */
@Mixin(MultiPlayerGameMode.class)
public abstract class MixinNativeUseContext {
    @WrapOperation(method = "performUseItemOn", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/player/LocalPlayer;isSecondaryUseActive()Z"))
    private boolean easyplacefix$projectSecondaryUse(LocalPlayer player, Operation<Boolean> original) {
        return NativePlacementScope.secondaryUseActive(original.call(player));
    }
}
