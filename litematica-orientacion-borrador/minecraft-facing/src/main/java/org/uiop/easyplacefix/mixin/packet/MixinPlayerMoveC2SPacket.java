package org.uiop.easyplacefix.mixin.packet;

import org.uiop.easyplacefix.rotation.RotationBoundPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerboundMovePlayerPacket.class)
public interface MixinPlayerMoveC2SPacket {
   @Mutable
   @Accessor
   void setYRot(float var1);

   @Mutable
   @Accessor
   void setXRot(float var1);

   @Mixin(PosRot.class)
   class Full {
      @Inject(method = "write", at = @At("HEAD"))
      private void lockLook(FriendlyByteBuf buf, CallbackInfo ci) {
         ((RotationBoundPacket)this).easyplacefix$applyRotationSnapshot();
      }
   }

   @Mixin(Rot.class)
   class LookAndOnGround {
      @Inject(method = "write", at = @At("HEAD"))
      private void lockLook(FriendlyByteBuf buf, CallbackInfo ci) {
         ((RotationBoundPacket)this).easyplacefix$applyRotationSnapshot();
      }
   }
}
