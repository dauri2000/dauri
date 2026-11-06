package org.uiop.easyplacefix.placement;

import com.tick_ins.tick.RunnableWithLast;
import com.tick_ins.tick.TickThread;
import fi.dy.masa.litematica.config.Configs.Generic;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.util.EntityUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uiop.easyplacefix.IClientPlayerInteractionManager;
import org.uiop.easyplacefix.util.EasyPlaceHandler;
import org.uiop.easyplacefix.util.PlacementDiagnostics;
import org.uiop.easyplacefix.util.PlacementInventory;
import org.uiop.easyplacefix.util.PlayerInputAction;
import org.uiop.easyplacefix.util.PlayerRotationAction;
import oshi.util.tuples.Pair;

/** A later activation fills an observed empty pot through the ordinary block use. */
public final class NativePottedPlantAction {
    private static final Logger LOGGER=LoggerFactory.getLogger("easyplacefix");
    private static final Direction[] FACES={Direction.UP,Direction.NORTH,Direction.SOUTH,
        Direction.EAST,Direction.WEST,Direction.DOWN};
    private final Minecraft mc;
    private final MultiPlayerGameMode gameMode;
    private final LocalPlayer player;
    private final Level level;
    private final Object connection;
    private final Level schematicWorld;
    private final BlockPos target;
    private final BlockState originalGoal;
    private final ItemStack wantedStack;
    private final boolean postRewrite;
    private final float yaw,pitch;
    private InteractionHand hand;
    private ItemStack preparedStack;
    private boolean ready;

    private NativePottedPlantAction(Minecraft mc, BlockPos target, BlockState originalGoal, ItemStack stack) {
        this.mc=mc;this.gameMode=mc.gameMode;this.player=mc.player;this.level=mc.level;
        this.connection=mc.getConnection();this.schematicWorld=SchematicWorldHandler.getSchematicWorld();
        this.target=target.immutable();this.originalGoal=originalGoal;this.wantedStack=stack.copy();
        this.postRewrite=Generic.EASY_PLACE_POST_REWRITE.getBooleanValue();
        this.yaw=mc.player.getYRot();this.pitch=mc.player.getXRot();
    }

    public static boolean tryStart(Minecraft mc, BlockPos target, BlockState originalGoal) {
        if (mc.player==null || mc.level==null || mc.gameMode==null || mc.getConnection()==null
            || !NativeEasyPlaceAction.settingsEnabled() || !mc.level.isLoaded(target)
            || PottedPlantPlan.stage(originalGoal,mc.level.getBlockState(target))!=PottedPlantPlan.Stage.FILL_EMPTY
            || mc.player.isSecondaryUseActive() || mc.player.input.keyPresses.shift()) return false;
        ItemStack requested=new ItemStack(PottedPlantPlan.plantItem(originalGoal));
        ItemStack found=PlacementInventory.searchItem(mc,requested);
        if (found==null || found.isEmpty()) {
            PlacementDiagnostics.report("easyplacefix.diagnostic.missing_item",requested.getHoverName());
            return false;
        }
        NativePottedPlantAction action=new NativePottedPlantAction(mc,target,originalGoal,found);
        if (!action.isDesiredPlant(found)) return false;
        return TickThread.tryAddNativeTask(new RunnableWithLast.Builder().setTask(action::prepare)
            .setYawAndPitch(new Pair<>(action.yaw,action.pitch)).build(),
            new RunnableWithLast.Builder().setTask(action::use).build(),action::stillRelevant);
    }

    private boolean stillRelevant() {
        return NativeEasyPlaceAction.settingsEnabled() && schematicWorld!=null
            && Generic.EASY_PLACE_POST_REWRITE.getBooleanValue()==postRewrite
            && SchematicWorldHandler.getSchematicWorld()==schematicWorld
            && DataManager.getRenderLayerRange().isPositionWithinRange(target)
            && EasyPlaceHandler.isSchematicBlock(target)
            && schematicWorld.getBlockState(target).equals(originalGoal);
    }

    private boolean usable() {
        return mc.player==player && mc.level==level && mc.gameMode==gameMode && mc.getConnection()==connection
            && connection!=null && level.isLoaded(target) && !player.isSecondaryUseActive()
            && !player.input.keyPresses.shift()
            && PottedPlantPlan.stage(originalGoal,level.getBlockState(target))==PottedPlantPlan.Stage.FILL_EMPTY;
    }

    private boolean isDesiredPlant(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BlockItem item
            && item.getBlock()==((FlowerPotBlock)originalGoal.getBlock()).getPotted();
    }

    /** Aim at the current pot's actual shape; every returned hit is ray validated. */
    private Optional<BlockHitResult> interactionHit() {
        BlockState current=level.getBlockState(target);
        if (!current.is(Blocks.FLOWER_POT)) return Optional.empty();
        VoxelShape shape=current.getShape(level,target);
        if (shape.isEmpty()) return Optional.empty();
        AABB bounds=shape.bounds();
        for (Direction face:FACES) {
            double x=face==Direction.WEST ? bounds.minX : face==Direction.EAST ? bounds.maxX : (bounds.minX+bounds.maxX)/2;
            double y=face==Direction.DOWN ? bounds.minY : face==Direction.UP ? bounds.maxY : (bounds.minY+bounds.maxY)/2;
            double z=face==Direction.NORTH ? bounds.minZ : face==Direction.SOUTH ? bounds.maxZ : (bounds.minZ+bounds.maxZ)/2;
            BlockHitResult request=new BlockHitResult(new Vec3(target.getX()+x,target.getY()+y,target.getZ()+z),face,target,false);
            Optional<BlockHitResult> actual=NativePlacementAdapter.interactionHit(mc,target,request);
            if (actual.isPresent()) return actual;
        }
        return Optional.empty();
    }

    private void prepare() {
        try {
            if (!usable() || !stillRelevant()) return;
            PlacementInventory.pickItem(mc,wantedStack);
            hand=EntityUtils.getUsedHandForItem(player,wantedStack);
            ((IClientPlayerInteractionManager)gameMode).syn();
            if (hand==null || !isDesiredPlant(player.getItemInHand(hand)) || interactionHit().isEmpty()) return;
            preparedStack=player.getItemInHand(hand).copy();
            NativeEasyPlaceAction.registerPreparedInputs(yaw,pitch,false);
            PlayerInputAction.SetShift(false);
            PlayerRotationAction.setServerBoundPlayerRotation(yaw,pitch,player.horizontalCollision);
            ready=true;
        } catch (RuntimeException error) {
            LOGGER.debug("Native potted plant preparation failed",error);
        } finally {
            if (!ready) NativeEasyPlaceAction.finishInputs(mc,true);
        }
    }

    private void use() {
        boolean consumed=false;
        try {
            if (!ready || !usable() || !stillRelevant() || !isDesiredPlant(player.getItemInHand(hand))
                || !ItemStack.isSameItemSameComponents(preparedStack,player.getItemInHand(hand))) return;
            ((IClientPlayerInteractionManager)gameMode).syn();
            Optional<BlockHitResult> hit=interactionHit();
            if (hit.isEmpty()) return;
            NativePlacementScope placementScope=NativePlacementScope.open(yaw,pitch,false);
            try (placementScope) {
                PlayerInputAction.SetShift(false);
                PlayerRotationAction.setServerBoundPlayerRotation(yaw,pitch,player.horizontalCollision);
                InteractionResult result=gameMode.useItemOn(player,hand,hit.get());
                consumed=result.consumesAction();
                if (consumed) player.swing(hand,player.getItemInHand(hand).getInteractAnimation(),false);
                // No completion/ACK is fabricated from this local result. A later
                // activation re-reads the actual translated block state.
            }
        } catch (RuntimeException error) {
            LOGGER.debug("Native potted plant interaction failed",error);
        } finally {
            NativeEasyPlaceAction.finishInputs(mc,!consumed);
        }
    }
}
