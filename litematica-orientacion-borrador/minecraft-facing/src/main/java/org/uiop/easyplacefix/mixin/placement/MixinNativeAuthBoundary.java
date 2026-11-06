package org.uiop.easyplacefix.mixin.placement;

import com.llamalad7.mixinextras.sugar.Local;
import com.tick_ins.tick.TickThread;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Observe only the genuine tick-end send; no packet or extra tick is generated. */
@Mixin(Minecraft.class)
public abstract class MixinNativeAuthBoundary {
    @Inject(method="tick",at=@At(value="INVOKE",
        target="Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V",
        shift=At.Shift.AFTER),
        slice=@Slice(from=@At(value="FIELD",
            target="Lnet/minecraft/network/protocol/game/ServerboundClientTickEndPacket;INSTANCE:Lnet/minecraft/network/protocol/game/ServerboundClientTickEndPacket;",
            opcode=Opcodes.GETSTATIC),
            to=@At("RETURN")),require=1,allow=1)
    private void easyplacefix$observeAuthBoundary(CallbackInfo ci,
        @Local(ordinal=0) ClientPacketListener connection) {
        TickThread.onClientAuthBoundary((Minecraft)(Object)this,connection);
    }
}
