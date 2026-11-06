package org.uiop.easyplacefix.mixin.placement;

import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.litematica.config.Configs.Generic;
import fi.dy.masa.litematica.util.EasyPlaceUtils;
import fi.dy.masa.litematica.util.RayTraceUtils.RayTraceWrapper;
import fi.dy.masa.litematica.util.RayTraceUtils.RayTraceWrapper.HitType;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.uiop.easyplacefix.entity.SchematicEntityAssist;
import org.uiop.easyplacefix.placement.NativeEasyPlaceAction;
import org.uiop.easyplacefix.util.EasyPlaceHandler;

/** Reuse POST's resolved schematic target, before its pick/cache/direct use path. */
@Mixin(value=EasyPlaceUtils.class,remap=false)
public abstract class MixinNativePostEasyPlace {
    @Inject(method="handleEasyPlace()Lnet/minecraft/world/InteractionResult;",
        at=@At(value="INVOKE_ASSIGN",target="Lfi/dy/masa/litematica/util/EasyPlaceUtils;getTargetPosition(Lfi/dy/masa/litematica/util/RayTraceUtils$RayTraceWrapper;)Lnet/minecraft/world/phys/BlockHitResult;"),
        cancellable=true,remap=false)
    private static void easyplacefix$nativePostTarget(CallbackInfoReturnable<InteractionResult> cir,
        @Local(ordinal=0) Minecraft mc, @Local(ordinal=0) RayTraceWrapper traceWrapper,
        @Local(ordinal=0) BlockHitResult targetPosition) {
        if (!Generic.EASY_PLACE_POST_REWRITE.getBooleanValue() || !NativeEasyPlaceAction.settingsEnabled()) return;
        if (mc.player==null || mc.level==null || mc.gameMode==null) {
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }
        InteractionResult entityResult=SchematicEntityAssist.tryHandle(mc,traceWrapper);
        if (entityResult!=null) {
            cir.setReturnValue(entityResult);
            return;
        }
        if (targetPosition==null) {
            if (traceWrapper!=null && EasyPlaceHandler.shouldAllowVanillaInteraction(mc,traceWrapper)) {
                cir.setReturnValue(InteractionResult.PASS);
            }
            return;
        }
        Level schematicWorld=SchematicWorldHandler.getSchematicWorld();
        if (schematicWorld==null || schematicWorld.getBlockState(targetPosition.getBlockPos()).is(BlockTags.AIR)) {
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }
        // getTargetPosition also resolves the placement-position registry override.
        RayTraceWrapper target=new RayTraceWrapper(HitType.SCHEMATIC_BLOCK,targetPosition);
        cir.setReturnValue(EasyPlaceHandler.shouldAllowVanillaInteraction(mc,target)
            ? InteractionResult.PASS : EasyPlaceHandler.doEasyPlace2(mc,target));
    }
}
