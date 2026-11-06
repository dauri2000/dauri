package org.uiop.easyplacefix.placement;

import com.tick_ins.tick.RunnableWithLast;
import com.tick_ins.tick.TickThread;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.data.DataManager;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;
import org.uiop.easyplacefix.util.PlayerInputAction;
import org.uiop.easyplacefix.util.PlayerRotationAction;
import org.uiop.easyplacefix.util.PlacementStateMatcher;
import org.uiop.easyplacefix.util.EasyPlaceHandler;
import oshi.util.tuples.Pair;

/** Every extra click owns a normal prepare/auth/use split, independently of A. */
public final class NativeFollowUpAction {
    private static final Logger LOGGER=LoggerFactory.getLogger("easyplacefix");
    private final Minecraft mc;
    private final MultiPlayerGameMode gameMode;
    private final InteractionHand hand;
    private final BlockPos target;
    private final Block expectedBlock;
    private final RelativeBlockHitResult initialRequest;
    private final float yaw,pitch;
    private final Level schematicWorld;
    private final BlockState expectedSchematic;
    private RelativeBlockHitResult requested;
    private NativePlacementAdapter.Prepared placement;
    private Item heldItem;
    private boolean ready;

    private NativeFollowUpAction(Minecraft mc, MultiPlayerGameMode gameMode, InteractionHand hand,
        RelativeBlockHitResult requested, Block block, BlockPos target, float yaw, float pitch) {
        this.mc=mc;this.gameMode=gameMode;this.hand=hand;this.initialRequest=requested;
        this.expectedBlock=block;this.target=target.immutable();this.yaw=yaw;this.pitch=pitch;
        this.schematicWorld=SchematicWorldHandler.getSchematicWorld();
        this.expectedSchematic=schematicWorld==null ? null : schematicWorld.getBlockState(target);
    }

    public static void start(Minecraft mc, MultiPlayerGameMode gameMode, InteractionHand hand,
        RelativeBlockHitResult requested, Block block, BlockPos target, Float yaw, Float pitch) {
        if (mc.player==null || mc.level==null || mc.getConnection()==null) return;
        float actionYaw=yaw==null ? mc.player.getYRot() : yaw;
        float actionPitch=pitch==null ? mc.player.getXRot() : pitch;
        NativeFollowUpAction action=new NativeFollowUpAction(mc,gameMode,hand,requested,block,target,actionYaw,actionPitch);
        TickThread.tryAddNativeTask(new RunnableWithLast.Builder().setTask(action::prepare)
            .setYawAndPitch(new Pair<>(actionYaw,actionPitch)).build(),
            new RunnableWithLast.Builder().setTask(action::place).build(),action::stillRelevant);
    }

    private boolean stillRelevant() {
        return NativeEasyPlaceAction.settingsEnabled() && schematicWorld!=null
            && SchematicWorldHandler.getSchematicWorld()==schematicWorld
            && DataManager.getRenderLayerRange().isPositionWithinRange(target)
            && EasyPlaceHandler.isSchematicBlock(target)
            && schematicWorld.getBlockState(target).equals(expectedSchematic);
    }

    private boolean usable() {
        return mc.player!=null && mc.level!=null && mc.getConnection()!=null
            && !mc.player.isSecondaryUseActive() && !mc.player.input.keyPresses.shift() && mc.level.isLoaded(target)
            && mc.level.getBlockState(target).getBlock()==expectedBlock;
    }

    private void prepare() {
        try {
            if (!usable() || !stillRelevant()) return;
            BlockState wanted=SchematicWorldHandler.getSchematicWorld().getBlockState(target);
            if (wanted.getBlock()!=expectedBlock) return;
            if (PlacementStateMatcher.isSatisfied(wanted,mc.level.getBlockState(target))) return;
            heldItem=mc.player.getItemInHand(hand).getItem();
            requested=initialRequest;
            if (wanted.getBlock() instanceof SlabBlock) {
                Tuple<RelativeBlockHitResult,Integer> fresh=((IBlock)wanted.getBlock()).getHitResult(
                    wanted,target,mc.level.getBlockState(target));
                if (fresh==null) return;
                requested=fresh.getA();
            }
            Optional<NativePlacementAdapter.Prepared> candidate=NativePlacementAdapter.prepare(
                mc,hand,target,wanted,requested,yaw,pitch);
            if (candidate.isPresent()) placement=candidate.get();
            else if (NativeSupportActions.isKnownStackable(wanted.getBlock())
                || NativePlacementAdapter.interactionHit(mc,target,requested).isEmpty()) return;
            boolean secondary=placement!=null && placement.secondary();
            NativeEasyPlaceAction.registerPreparedInputs(yaw,pitch,secondary);
            PlayerInputAction.SetShift(secondary);
            PlayerRotationAction.setServerBoundPlayerRotation(yaw,pitch,mc.player.horizontalCollision);
            ready=true;
        } catch (RuntimeException error) {
            LOGGER.debug("Native EasyPlace follow-up preparation failed",error);
        } finally {
            if (!ready) NativeEasyPlaceAction.finishInputs(mc,true);
        }
    }

    private void place() {
        boolean completed=false;
        try {
            if (!ready || !usable() || !stillRelevant() || mc.player.getItemInHand(hand).getItem()!=heldItem) return;
            BlockHitResult hit;
            boolean secondary;
            if (placement!=null) {
                Optional<NativePlacementAdapter.Prepared> checked=NativePlacementAdapter.revalidate(mc,hand,placement);
                if (checked.isEmpty()) return;
                hit=checked.get().hit();secondary=checked.get().secondary();
            } else {
                Optional<BlockHitResult> interaction=NativePlacementAdapter.interactionHit(mc,target,requested);
                if (interaction.isEmpty()) return;
                hit=interaction.get();secondary=false;
            }
            NativePlacementScope scope=NativePlacementScope.open(yaw,pitch,secondary);
            try (scope) {
                PlayerInputAction.SetShift(secondary);
                PlayerRotationAction.setServerBoundPlayerRotation(yaw,pitch,mc.player.horizontalCollision);
                InteractionResult result=gameMode.useItemOn(mc.player,hand,hit);
                completed=result.consumesAction();
                if (completed) mc.player.swing(hand,mc.player.getItemInHand(hand).getInteractAnimation(),false);
            }
        } catch (RuntimeException error) {
            LOGGER.debug("Native EasyPlace follow-up failed",error);
        } finally {
            NativeEasyPlaceAction.finishInputs(mc,!completed);
        }
    }
}
