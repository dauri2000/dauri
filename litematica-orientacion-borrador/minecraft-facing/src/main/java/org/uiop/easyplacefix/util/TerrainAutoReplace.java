package org.uiop.easyplacefix.util;

import com.tick_ins.tick.RunnableWithCountDown;
import com.tick_ins.tick.TickThread;
import fi.dy.masa.litematica.config.Configs.Generic;
import fi.dy.masa.litematica.util.RayTraceUtils.RayTraceWrapper;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.uiop.easyplacefix.config.easyPlacefixConfig;
import org.uiop.easyplacefix.rotation.TaskAdmission;

public final class TerrainAutoReplace {
   private static final Set<Block> TERRAIN = Set.of(Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.COARSE_DIRT, Blocks.DIRT_PATH);
   private static final int TERRAIN_MAX_TICKS = 40;
   private static final int GENERAL_MAX_TICKS = 600;
   private static final int GENERAL_GRACE_TICKS = 20;
   private static final TaskAdmission.Reservations<BlockPos> CLEARING = new TaskAdmission.Reservations<>();

   private TerrainAutoReplace() {
   }

   public static boolean isEligible(BlockPos pos, BlockState worldState, BlockState schematicState) {
      return !Generic.EASY_PLACE_MODE.getBooleanValue() ? false : isTerrainSwap(worldState, schematicState) || isWrongBlock(pos, worldState, schematicState);
   }

   private static boolean isTerrainSwap(BlockState worldState, BlockState schematicState) {
      return easyPlacefixConfig.TERRAIN_AUTO_REPLACE.getBooleanValue()
         && worldState.getBlock() != schematicState.getBlock()
         && TERRAIN.contains(worldState.getBlock())
         && TERRAIN.contains(schematicState.getBlock());
   }

   private static boolean isWrongBlock(BlockPos pos, BlockState worldState, BlockState schematicState) {
      if (!easyPlacefixConfig.BREAK_WRONG_BLOCKS.getBooleanValue() || schematicState.isAir()) {
         return false;
      }

      if (worldState.isAir() || worldState.getBlock() instanceof LiquidBlock) {
         return false;
      }

      if (!PlacementStateMatcher.isStructurallyWrong(schematicState, worldState)) {
         return false;
      }

      Level level = Minecraft.getInstance().level;
      if (level == null) {
         return false;
      }

      if (pos != null) {
         if (worldState.getDestroySpeed(level, pos) < 0.0F) {
            return false;
         }

         if (level.getBlockEntity(pos) instanceof Container) {
            return false;
         }
      }

      return true;
   }

   public static void clear() {
      CLEARING.clear();
   }

   public static boolean tryClearThenRetry(Minecraft mc, RayTraceWrapper traceWrapper, BlockPos pos, Direction direction) {
      if (!TickThread.prepareClientContext(mc)) return false;

      BlockPos key = pos.immutable();
      TaskAdmission.Reservation reservation = CLEARING.acquire(key);
      if (reservation == null) {
         return true;
      }

      BlockState worldState = mc.level.getBlockState(key);
      boolean terrain = TERRAIN.contains(worldState.getBlock());
      PlacementDiagnostics.report(terrain ? "easyplacefix.diagnostic.terrain_clearing" : "easyplacefix.diagnostic.wrong_block_clearing", key.toShortString());
      mc.gameMode.startDestroyBlock(key, direction);
      int maxTicks = 40;
      if (!terrain) {
         float progress = mc.level.getBlockState(key).getDestroyProgress(mc.player, mc.level, key);
         maxTicks = progress <= 0.0F ? 600 : Math.min(600, (int)Math.ceil(1.0F / progress) + 20);
      }

      step(mc, traceWrapper, key, direction, maxTicks, reservation);
      return true;
   }

   private static boolean isCleared(BlockState state) {
      return state.isAir() || state.canBeReplaced();
   }

   private static void step(Minecraft mc, RayTraceWrapper traceWrapper, BlockPos pos, Direction direction, int ticksLeft, TaskAdmission.Reservation reservation) {
      if (!CLEARING.owns(pos, reservation)) return;
      if (mc.level != null && mc.player != null && mc.gameMode != null) {
         BlockState current = mc.level.getBlockState(pos);
         if (isCleared(current)) {
            if (PlayerBlockAction.useItemOnAction.isGlobalPlacementCooling() && ticksLeft > 0) {
               scheduleStep(mc, traceWrapper, pos, direction, ticksLeft - 1, reservation);
            } else {
               CLEARING.release(pos, reservation);
               PlayerBlockAction.useItemOnAction.lastPlacementTimeMap.remove(pos);
               EasyPlaceHandler.doEasyPlace2(mc, traceWrapper);
            }
         } else if (ticksLeft <= 0) {
            mc.gameMode.stopDestroyBlock();
            CLEARING.release(pos, reservation);
            PlacementDiagnostics.report("easyplacefix.diagnostic.terrain_clear_timeout", pos.toShortString());
         } else {
            mc.gameMode.continueDestroyBlock(pos, direction);
            mc.player.swing(InteractionHand.MAIN_HAND, mc.player.getItemInHand(InteractionHand.MAIN_HAND).getInteractAnimation(), false);
            scheduleStep(mc, traceWrapper, pos, direction, ticksLeft - 1, reservation);
         }
      } else {
         CLEARING.release(pos, reservation);
      }
   }

   private static void scheduleStep(Minecraft mc, RayTraceWrapper traceWrapper, BlockPos pos, Direction direction, int ticksLeft, TaskAdmission.Reservation reservation) {
      TaskAdmission.trySubmit(
         () -> TickThread.tryAddCountDownTask(new RunnableWithCountDown.Builder().setCount(1).build(() -> step(mc, traceWrapper, pos, direction, ticksLeft, reservation))),
         () -> CLEARING.release(pos, reservation)
      );
   }
}
