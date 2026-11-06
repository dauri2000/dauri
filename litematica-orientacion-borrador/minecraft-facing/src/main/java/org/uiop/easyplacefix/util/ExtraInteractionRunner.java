package org.uiop.easyplacefix.util;

import com.tick_ins.tick.RunnableWithCountDown;
import com.tick_ins.tick.TickThread;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.uiop.easyplacefix.config.easyPlacefixConfig;
import org.uiop.easyplacefix.placement.NativePlacementScope;
import org.uiop.easyplacefix.placement.NativeFollowUpAction;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.world.level.Level;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

public final class ExtraInteractionRunner {
   private ExtraInteractionRunner() {
   }

   public static void run(
      Minecraft mc,
      MultiPlayerGameMode interactionManager,
      InteractionHand usedHand,
      RelativeBlockHitResult hitResult,
      int totalClicks,
      Block block,
      BlockPos targetPos,
      Float yaw,
      Float pitch
   ) {
      int extraClicks = Math.max(0, totalClicks - 1);
      if (extraClicks != 0) {
         RelativeBlockHitResult followUpHit = hitResult.getBlockPos().equals(targetPos)
            ? hitResult
            : new RelativeBlockHitResult(hitResult.getLocation(), hitResult.getDirection(), targetPos, false);

         boolean nativeBedrock = NativePlacementScope.isBedrockConnection();
         Level expectedSchematicWorld = nativeBedrock ? SchematicWorldHandler.getSchematicWorld() : null;
         BlockState expectedSchematic = expectedSchematicWorld == null ? null : expectedSchematicWorld.getBlockState(targetPos);
         for (int i = 1; i <= extraClicks; i++) {
            int prepareDelay = nativeBedrock ? i * 2 - 1 : i * 2;
            TickThread.addCountDownTask(new RunnableWithCountDown.Builder().setCount(prepareDelay).build(() -> {
               if (mc.player != null && mc.level != null && !mc.player.isSecondaryUseActive()) {
                  BlockState current = mc.level.getBlockState(targetPos);
                  if (current.getBlock() == block) {
                     if (nativeBedrock) {
                        if (expectedSchematicWorld == null || SchematicWorldHandler.getSchematicWorld() != expectedSchematicWorld
                           || !expectedSchematicWorld.getBlockState(targetPos).equals(expectedSchematic)) return;
                        NativeFollowUpAction.start(mc, interactionManager, usedHand, followUpHit, block, targetPos, yaw, pitch);
                        return;
                     }
                     boolean spoofRotation = yaw != null && pitch != null;
                     if (spoofRotation) {
                        PlayerRotationAction.setServerBoundPlayerRotation(yaw, pitch, mc.player.horizontalCollision);
                     }

                     interactionManager.useItemOn(mc.player, usedHand, followUpHit);
                     mc.player.swing(usedHand, mc.player.getItemInHand(usedHand).getInteractAnimation(), false);
                     if (spoofRotation && easyPlacefixConfig.CLIENT_ROTATION_REVERT.getBooleanValue()) {
                        PlayerRotationAction.restRotation();
                     }
                  }
               }
            }));
         }
      }
   }
}
