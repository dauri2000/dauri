package org.uiop.easyplacefix.mixin.placement;

import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.uiop.easyplacefix.placement.NativeEasyPlaceAction;
import org.uiop.easyplacefix.placement.NativeInputMask;

/** Snapshot once at construction; no global state is consulted by the encoder. */
@Mixin(ServerboundPlayerInputPacket.class)
public abstract class MixinNativeInputPacket {
    @Shadow @Final @Mutable
    private Input input;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void easyplacefix$preservePreparedShift(CallbackInfo ci) {
        input=NativeInputMask.preserveShift(input,NativeEasyPlaceAction.requiresPreparedShift());
    }
}
