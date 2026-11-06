package org.uiop.easyplacefix.util;

import com.tick_ins.tick.RunnableWithLast;
import com.tick_ins.tick.TickThread;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager.PlacementPart;
import fi.dy.masa.litematica.util.EntityUtils;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.util.WorldUtils;
import fi.dy.masa.litematica.util.RayTraceUtils.RayTraceWrapper;
import fi.dy.masa.litematica.util.RayTraceUtils.RayTraceWrapper.HitType;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CoralFanBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import org.uiop.easyplacefix.EasyPlaceFix;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.IClientPlayerInteractionManager;
import org.uiop.easyplacefix.LookAt;
import org.uiop.easyplacefix.placement.NativeEasyPlaceAction;
import org.uiop.easyplacefix.placement.NativePlacementScope;
import org.uiop.easyplacefix.placement.NativePottedPlantAction;
import org.uiop.easyplacefix.placement.PottedPlantPlan;
import org.uiop.easyplacefix.config.easyPlacefixConfig;
import org.uiop.easyplacefix.data.LoosenModeData;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;
import oshi.util.tuples.Pair;

public class EasyPlaceHandler {
   private static final String[] BOX_CONTAINS_METHOD_NAMES = new String[]{"contains", "containsPos"};
   private static final Method PLACEMENT_PART_GET_BOX = findNoArgMethod(PlacementPart.class, "getBox", "getBoundingBox");
   private static final ConcurrentMap<Class<?>, Method> BOX_CONTAINS_METHODS = new ConcurrentHashMap<>();

   private static Method findNoArgMethod(Class<?> owner, String... methodNames) {
      for (String methodName : methodNames) {
         try {
            return owner.getMethod(methodName);
         } catch (NoSuchMethodException var7) {
         }
      }

      throw unsupportedApi(owner, methodNames);
   }

   private static Method findBoxContainsMethod(Class<?> boxClass) {
      for (String methodName : BOX_CONTAINS_METHOD_NAMES) {
         for (Method method : boxClass.getMethods()) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (method.getName().equals(methodName)
               && parameterTypes.length == 1
               && parameterTypes[0].isAssignableFrom(BlockPos.class)
               && (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
               return method;
            }
         }
      }

      throw unsupportedApi(boxClass, BOX_CONTAINS_METHOD_NAMES);
   }

   private static IllegalStateException unsupportedApi(Class<?> owner, String... methodNames) {
      return new IllegalStateException("Unsupported Litematica/MaLiLib API: " + owner.getName() + "#" + String.join("/", methodNames));
   }

   private static boolean placementContains(PlacementPart placementPart, BlockPos pos) {
      try {
         Object box = PLACEMENT_PART_GET_BOX.invoke(placementPart);
         if (box == null) {
            return false;
         }

         Method containsMethod = BOX_CONTAINS_METHODS.computeIfAbsent(box.getClass(), EasyPlaceHandler::findBoxContainsMethod);
         return Boolean.TRUE.equals(containsMethod.invoke(box, pos));
      } catch (ReflectiveOperationException exception) {
         throw new IllegalStateException("Could not inspect a Litematica placement bounding box", exception);
      }
   }

   public static boolean shouldAllowVanillaInteraction(Minecraft mc, RayTraceWrapper traceWrapper) {
      if (easyPlacefixConfig.Allow_Interaction.getBooleanValue() && mc.level != null) {
         BlockHitResult trace = traceWrapper.getBlockHitResult();
         Level schematicWorld = SchematicWorldHandler.getSchematicWorld();
         if (trace != null && schematicWorld != null) {
            BlockPos pos = trace.getBlockPos();
            BlockState stateClient = mc.level.getBlockState(pos);
            BlockState stateSchematic = schematicWorld.getBlockState(pos);
            return ((IBlock)stateClient.getBlock()).isWorldTermination(pos, stateSchematic, stateClient) == InteractionResult.PASS;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean isSchematicBlock(BlockPos pos) {
      SchematicPlacementManager schematicPlacementManager = DataManager.getSchematicPlacementManager();

      for (PlacementPart placementPart : schematicPlacementManager.getAllPlacementsTouchingChunk(pos)) {
         if (placementContains(placementPart, pos)) {
            return true;
         }
      }

      return false;
   }

   public static ItemStack loosenMode2() {
      for (int i = 0; i < Minecraft.getInstance().player.getInventory().getContainerSize(); i++) {
         ItemStack stack = Minecraft.getInstance().player.getInventory().getItem(i);
         stack = stack.copy();
         if (!stack.isEmpty() && LoosenModeData.items.contains(stack.getItem())) {
            return stack;
         }
      }

      return null;
   }

   public static ItemStack loosenMode(ItemStack stack, BlockState stateSchema) {
      if (stack == null && easyPlacefixConfig.LOOSEN_MODE.getBooleanValue() && !EntityUtils.isCreativeMode(Minecraft.getInstance().player)) {
         Block ReplacedBlock = stateSchema.getBlock();
         Predicate<Block> predicate = null;
         if (ReplacedBlock instanceof WallBlock) {
            predicate = block -> block instanceof WallBlock;
         } else if (ReplacedBlock instanceof FenceGateBlock) {
            predicate = block -> block instanceof FenceGateBlock;
         } else if (ReplacedBlock instanceof TrapDoorBlock) {
            predicate = block -> block instanceof TrapDoorBlock;
         } else if (ReplacedBlock instanceof CoralFanBlock) {
            predicate = block -> block instanceof CoralFanBlock;
         }

         ItemStack stack1 = null;
         if (predicate != null) {
            Inventory playerInventory = Minecraft.getInstance().player.getInventory();
            stack1 = EasyPlaceFix.findBlockInInventory(playerInventory, predicate);
         }

         return stack1 == null ? loosenMode2() : stack1;
      } else {
         return stack;
      }
   }

   public static InteractionResult doEasyPlace2(Minecraft mc, RayTraceWrapper traceWrapper) {
      BlockHitResult trace = traceWrapper.getBlockHitResult();
      Level schematicWorld = SchematicWorldHandler.getSchematicWorld();
      if (schematicWorld == null) {
         PlacementDiagnostics.report("easyplacefix.diagnostic.no_schematic_world");
         return InteractionResult.PASS;
      }

      BlockPos pos = trace.getBlockPos();
      if (PlayerBlockAction.useItemOnAction.isGlobalPlacementCooling()) {
         PlacementDiagnostics.report("easyplacefix.diagnostic.global_cooldown", easyPlacefixConfig.getEffectivePlacementDelayTicks());
         return InteractionResult.FAIL;
      }

      if (PlayerBlockAction.useItemOnAction.isPlacementCooling(pos)) {
         PlacementDiagnostics.report("easyplacefix.diagnostic.position_cooldown", pos.toShortString());
         return InteractionResult.FAIL;
      }

      BlockState stateClient = mc.level.getBlockState(pos);
      BlockState stateSchematic = schematicWorld.getBlockState(pos);
      boolean nativeBedrock = NativePlacementScope.isBedrockConnection();
      InteractionResult isTermination = ((IBlock)stateClient.getBlock()).isWorldTermination(pos, stateSchematic, stateClient);
      if (isTermination != null) {
         PlacementDiagnostics.report("easyplacefix.diagnostic.world_termination", pos.toShortString());
         return isTermination;
      }

      isTermination = ((IBlock)stateSchematic.getBlock()).isSchemaTermination(pos, stateSchematic, stateClient);
      if (isTermination != null) {
         PlacementDiagnostics.report("easyplacefix.diagnostic.schema_termination", pos.toShortString());
         return isTermination;
      }

      HitResult traceVanilla = RayTraceUtils.getRayTraceFromEntity(mc.level, mc.player, false, WorldUtils.getValidBlockRange(mc));
      if (traceVanilla.getType() == Type.ENTITY) {
         PlacementDiagnostics.report("easyplacefix.diagnostic.entity_in_crosshair");
         return InteractionResult.PASS;
      }

      if (traceWrapper.getHitType() == HitType.SCHEMATIC_BLOCK) {
         BlockState placementGoal=stateSchematic;
         if (nativeBedrock && PottedPlantPlan.isFilledGoal(stateSchematic)) {
            PottedPlantPlan.Stage stage=PottedPlantPlan.stage(stateSchematic,stateClient);
            if (stage==PottedPlantPlan.Stage.COMPLETE) {
               PlacementDiagnostics.report("easyplacefix.diagnostic.already_correct",pos.toShortString());
               return InteractionResult.FAIL;
            }
            if (stage==PottedPlantPlan.Stage.BLOCKED) {
               PlacementDiagnostics.report("easyplacefix.diagnostic.not_replaceable",pos.toShortString());
               return InteractionResult.FAIL;
            }
            if (stage==PottedPlantPlan.Stage.FILL_EMPTY) {
               boolean accepted=NativePottedPlantAction.tryStart(mc,pos,stateSchematic);
               if (accepted) PlayerBlockAction.useItemOnAction.markGlobalPlacement();
               return accepted ? InteractionResult.SUCCESS : InteractionResult.FAIL;
            }
            // Filled pots are two ordinary actions: this activation places only
            // the empty pot; a later activation may interact with an empty cell.
            placementGoal=PottedPlantPlan.emptyGoal();
         }
         ItemStack stack = PlacementItemResolver.getPlacementStack(placementGoal, pos, schematicWorld);
         if (!stack.isEmpty()) {
            BlockState currentState = mc.level.getBlockState(pos);
            if (PlacementStateMatcher.isSatisfied(stateSchematic, currentState)) {
               if (EasyPlaceFix.LOGGER.isDebugEnabled()) {
                  EasyPlaceFix.LOGGER.debug("EasyPlace skip at {} because world state already matches schematic", pos);
               }

               PlacementDiagnostics.report("easyplacefix.diagnostic.already_correct", pos.toShortString());
               return InteractionResult.FAIL;
            }

            if (stateSchematic.getBlock() instanceof NoteBlock && currentState.getBlock() instanceof NoteBlock) {
               int targetNote = stateSchematic.getValue(BlockStateProperties.NOTE);
               int currentNote = currentState.getValue(BlockStateProperties.NOTE);
               if (currentNote != targetNote) {
                  if (!NoteBlockHelper.isTuning(pos)) {
                     NoteBlockHelper.tune(mc, pos, targetNote);
                  }

                  PlacementDiagnostics.report("easyplacefix.diagnostic.tuning_noteblock", pos.toShortString());
                  return InteractionResult.SUCCESS;
               }
            }

            if (nativeBedrock && !PottedPlantPlan.isFilledGoal(stateSchematic)
               && TerrainAutoReplace.isEligible(pos, stateClient, stateSchematic)) {
               TerrainAutoReplace.tryClearThenRetry(mc, traceWrapper, pos, trace.getDirection());
               return InteractionResult.SUCCESS;
            }
            boolean nativeSlabMerge = nativeBedrock && stateSchematic.getBlock() instanceof SlabBlock
               && stateSchematic.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE
               && stateClient.getBlock() == stateSchematic.getBlock();
            if (!nativeSlabMerge && !stateClient.canBeReplaced(new BlockPlaceContext(Minecraft.getInstance().player, InteractionHand.MAIN_HAND, stack, trace))) {
               if (!(nativeBedrock && PottedPlantPlan.isFilledGoal(stateSchematic))
                  && TerrainAutoReplace.isEligible(pos, stateClient, stateSchematic)) {
                  TerrainAutoReplace.tryClearThenRetry(mc, traceWrapper, pos, trace.getDirection());
                  return InteractionResult.SUCCESS;
               }

               PlacementDiagnostics.report("easyplacefix.diagnostic.not_replaceable", pos.toShortString());
               return InteractionResult.FAIL;
            }

            MultiPlayerGameMode interactionManager = Minecraft.getInstance().gameMode;
            ItemStack itemStack2 = PlacementInventory.searchItem(mc, stack);
            itemStack2 = loosenMode(itemStack2, stateSchematic);
            if (itemStack2 == null) {
               PlacementDiagnostics.report("easyplacefix.diagnostic.missing_item", stack.getHoverName());
               return InteractionResult.FAIL;
            }

            Block block = placementGoal.getBlock();
            String placementBlocker = ((IBlock)block).getPlacementBlocker(placementGoal, pos, stateClient);
            if (placementBlocker != null) {
               PlacementDiagnostics.report(placementBlocker, pos.toShortString());
               return InteractionResult.FAIL;
            }

            Tuple<RelativeBlockHitResult, Integer> blockHitResultIntegerPair = ((IBlock)block).getHitResult(placementGoal, trace.getBlockPos(), stateClient);
            if (blockHitResultIntegerPair == null) {
               PlacementDiagnostics.report("easyplacefix.diagnostic.no_hit_result", stateSchematic.getBlock().getName());
               return InteractionResult.FAIL;
            }

            RelativeBlockHitResult offsetBlockHitResult = blockHitResultIntegerPair.getA();
            ItemStack finalStack = itemStack2;
            AtomicReference<InteractionHand> hand = new AtomicReference<>();
            boolean hasSleep = ((IBlock)block).HasSleepTime(placementGoal);
            Tuple<LookAt, LookAt> YawAndPitch = ((IBlock)block).getYawAndPitch(placementGoal);
            boolean hasRotation = YawAndPitch != null;
            float rotationYaw = hasRotation ? YawAndPitch.getA().Value() : (nativeBedrock ? mc.player.getYRot() : 0.0F);
            float rotationPitch = hasRotation ? YawAndPitch.getB().Value() : (nativeBedrock ? mc.player.getXRot() : 0.0F);
            boolean lockRotation = hasRotation || nativeBedrock;
            NativeEasyPlaceAction nativeAction = nativeBedrock ? new NativeEasyPlaceAction(mc, interactionManager,
               finalStack, pos, stateSchematic, placementGoal, trace, offsetBlockHitResult, blockHitResultIntegerPair.getB(),
               true, rotationYaw, rotationPitch) : null;
            PlayerBlockAction.useItemOnAction.markGlobalPlacement();
            if (nativeAction != null) {
               PlacementDiagnostics.report("easyplacefix.diagnostic.native_placement_queued", stack.getHoverName());
               boolean accepted = TickThread.tryAddNativeTask(new RunnableWithLast.Builder()
                  .setTask(nativeAction::prepare).setYawAndPitch(new Pair<>(rotationYaw, rotationPitch)).build(),
                  new RunnableWithLast.Builder().setTask(nativeAction::place).build(), nativeAction::stillRelevant);
               if (!accepted) PlacementDiagnostics.report("easyplacefix.diagnostic.native_queue_full");
               return accepted ? InteractionResult.SUCCESS : InteractionResult.FAIL;
            }
            if (hasSleep) {
               TickThread.addLastTask(
                  new RunnableWithLast.Builder()
                     .setTask(() -> {
                        if (hasRotation) {
                           PlayerRotationAction.setServerBoundPlayerRotation(rotationYaw, rotationPitch, mc.player.horizontalCollision);
                        }

                        pickItem(mc, finalStack);
                        hand.set(EntityUtils.getUsedHandForItem(mc.player, finalStack));
                        ((IClientPlayerInteractionManager)interactionManager).syn();
                     })
                     .setYawAndPitch(lockRotation ? new Pair<>(rotationYaw, rotationPitch) : null)
                     .cache(
                        () -> {
                           pickItem(mc, finalStack);
                           hand.set(EntityUtils.getUsedHandForItem(mc.player, finalStack));
                           ((IClientPlayerInteractionManager)interactionManager).syn();
                           InteractionHand usedHand = hand.get();
                           if (usedHand != null) {
                              if (hasRotation) {
                                 PlayerRotationAction.setServerBoundPlayerRotation(rotationYaw, rotationPitch, mc.player.horizontalCollision);
                              }

                              ((IBlock)block).firstAction(stateSchematic, trace);
                              if (PlacementStateMatcher.shouldUsePlacementOverride(stateSchematic)) {
                                 PlayerBlockAction.useItemOnAction.armPlacementStateOverride(
                                    trace.getBlockPos(), stateSchematic, offsetBlockHitResult.getDirection()
                                 );
                              }

                              if (stateSchematic.getBlock() instanceof PistonBaseBlock) {
                                 PlayerBlockAction.useItemOnAction.pistonBlockState = stateSchematic;
                                 PlayerBlockAction.useItemOnAction.modifyBoolean = true;
                              }

                              interactionManager.useItemOn(mc.player, usedHand, offsetBlockHitResult);
                              PlayerBlockAction.useItemOnAction.modifyBoolean = false;
                              PlayerBlockAction.useItemOnAction.pistonBlockState = null;
                              mc.player.swing(usedHand, mc.player.getItemInHand(usedHand).getInteractAnimation(), false);
                              ExtraInteractionRunner.run(
                                 mc,
                                 interactionManager,
                                 usedHand,
                                 offsetBlockHitResult,
                                 blockHitResultIntegerPair.getB(),
                                 block,
                                 trace.getBlockPos(),
                                 hasRotation ? rotationYaw : null,
                                 hasRotation ? rotationPitch : null
                              );
                              ((IBlock)block).afterAction(stateSchematic, trace);
                              ((IBlock)block).BlockAction(stateSchematic, trace);
                              if (easyPlacefixConfig.CLIENT_ROTATION_REVERT.getBooleanValue()) {
                                 PlayerRotationAction.restRotation();
                              }
                           }
                        }
                     )
                     .build()
               );
            } else {
               TickThread.addTask(
                  new RunnableWithLast.Builder().setTask(() -> {
                     if (hasRotation) {
                        PlayerRotationAction.setServerBoundPlayerRotation(rotationYaw, rotationPitch, mc.player.horizontalCollision);
                     }

                     pickItem(mc, finalStack);
                     hand.set(EntityUtils.getUsedHandForItem(mc.player, finalStack));
                     ((IClientPlayerInteractionManager)interactionManager).syn();
                  }).setYawAndPitch(lockRotation ? new Pair<>(rotationYaw, rotationPitch) : null).build(),
                  new RunnableWithLast.Builder()
                     .setTask(
                        () -> {
                           pickItem(mc, finalStack);
                           hand.set(EntityUtils.getUsedHandForItem(mc.player, finalStack));
                           ((IClientPlayerInteractionManager)interactionManager).syn();
                           InteractionHand usedHand = hand.get();
                           if (usedHand != null) {
                              if (hasRotation) {
                                 PlayerRotationAction.setServerBoundPlayerRotation(rotationYaw, rotationPitch, mc.player.horizontalCollision);
                              }

                              ((IBlock)block).firstAction(stateSchematic, trace);
                              if (PlacementStateMatcher.shouldUsePlacementOverride(stateSchematic)) {
                                 PlayerBlockAction.useItemOnAction.armPlacementStateOverride(
                                    trace.getBlockPos(), stateSchematic, offsetBlockHitResult.getDirection()
                                 );
                              }

                              if (stateSchematic.getBlock() instanceof PistonBaseBlock) {
                                 PlayerBlockAction.useItemOnAction.pistonBlockState = stateSchematic;
                                 PlayerBlockAction.useItemOnAction.modifyBoolean = true;
                              }

                              interactionManager.useItemOn(mc.player, usedHand, offsetBlockHitResult);
                              PlayerBlockAction.useItemOnAction.modifyBoolean = false;
                              PlayerBlockAction.useItemOnAction.pistonBlockState = null;
                              mc.player.swing(usedHand, mc.player.getItemInHand(usedHand).getInteractAnimation(), false);
                              ExtraInteractionRunner.run(
                                 mc,
                                 interactionManager,
                                 usedHand,
                                 offsetBlockHitResult,
                                 blockHitResultIntegerPair.getB(),
                                 block,
                                 trace.getBlockPos(),
                                 hasRotation ? rotationYaw : null,
                                 hasRotation ? rotationPitch : null
                              );
                              ((IBlock)block).afterAction(stateSchematic, trace);
                              ((IBlock)block).BlockAction(stateSchematic, trace);
                              if (easyPlacefixConfig.CLIENT_ROTATION_REVERT.getBooleanValue()) {
                                 PlayerRotationAction.restRotation();
                              }
                           }
                        }
                     )
                     .build()
               );
            }

            PlacementDiagnostics.report("easyplacefix.diagnostic.placing", stack.getHoverName(), pos.toShortString());
         } else {
            PlacementDiagnostics.report("easyplacefix.diagnostic.no_block_item", stateSchematic.getBlock().getName());
         }

         return InteractionResult.SUCCESS;
      } else if (placementRestrictionInEffect(pos)) {
         PlacementDiagnostics.report("easyplacefix.diagnostic.restricted_area", pos.toShortString());
         return InteractionResult.FAIL;
      } else {
         PlacementDiagnostics.report("easyplacefix.diagnostic.no_schematic_hit");
         return InteractionResult.PASS;
      }
   }

   public static ItemStack searchItem(Minecraft mc, ItemStack stack) {
      return PlacementInventory.searchItem(mc, stack);
   }

   public static int getSlotWithStackWithOutNbt(ItemStack stack, Inventory inv) {
      return PlacementInventory.getSlotWithStackWithoutNbt(stack, inv);
   }

   public static int getSlotWithStack(ItemStack stack, Inventory inv) {
      return PlacementInventory.getSlotWithStack(stack, inv);
   }

   public static void pickItem(Minecraft mc, ItemStack stack) {
      PlacementInventory.pickItem(mc, stack);
   }

   private static boolean placementRestrictionInEffect(BlockPos pos) {
      return WorldUtils.isPositionWithinRangeOfSchematicRegions(pos, 2);
   }
}
