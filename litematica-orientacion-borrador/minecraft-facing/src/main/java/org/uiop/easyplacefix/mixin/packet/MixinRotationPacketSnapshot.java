package org.uiop.easyplacefix.mixin.packet;

import com.tick_ins.tick.TickThread;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.uiop.easyplacefix.rotation.RotationBoundPacket;
import org.uiop.easyplacefix.rotation.RotationLeaseController;
import org.uiop.easyplacefix.rotation.RotationPacketState;

/** Source candidate; actual target constructor transformation is not yet tested. */
@Mixin(ServerboundMovePlayerPacket.class)
public abstract class MixinRotationPacketSnapshot implements RotationBoundPacket {
    @Unique
    private RotationPacketState easyplacefix$rotationSnapshot;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void easyplacefix$captureRotation(CallbackInfo ci) {
        easyplacefix$rotationSnapshot = TickThread.newRotationPacketState();
    }

    @Override
    @Unique
    public void easyplacefix$markExplicitRotation(float yaw, float pitch) {
        easyplacefix$rotationSnapshot.markExplicit(yaw, pitch);
    }

    @Override
    @Unique
    public void easyplacefix$applyRotationSnapshot() {
        RotationLeaseController.Angles angles = easyplacefix$rotationSnapshot.capturedAngles();
        if (angles != null) {
            ((MixinPlayerMoveC2SPacket) this).setYRot(angles.yaw());
            ((MixinPlayerMoveC2SPacket) this).setXRot(angles.pitch());
        }
    }
}
