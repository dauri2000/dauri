package org.uiop.easyplacefix.placement;

import com.tick_ins.tick.TickThread;
import fi.dy.masa.litematica.util.EntityUtils;
import fi.dy.masa.litematica.config.Configs.Generic;
import fi.dy.masa.litematica.util.EasyPlaceProtocol;
import fi.dy.masa.litematica.util.PlacementHandler;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.data.DataManager;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.IClientPlayerInteractionManager;
import org.uiop.easyplacefix.config.easyPlacefixConfig;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;
import org.uiop.easyplacefix.rotation.RotationLeaseController;
import org.uiop.easyplacefix.util.ExtraInteractionRunner;
import org.uiop.easyplacefix.util.EasyPlaceHandler;
import org.uiop.easyplacefix.util.PlacementDiagnostics;
import org.uiop.easyplacefix.util.PlacementInventory;
import org.uiop.easyplacefix.util.PlayerInputAction;
import org.uiop.easyplacefix.util.PlayerRotationAction;

/** Owns the existing first/second tick phases of one native Bedrock placement. */
public final class NativeEasyPlaceAction {
    private static final Logger LOGGER=LoggerFactory.getLogger("easyplacefix");
    private record InputPlan(float yaw, float pitch, boolean secondary) {}
    private static final Map<RotationLeaseController.Token,InputPlan> INPUTS=new ConcurrentHashMap<>();
    private final Minecraft mc;
    private final MultiPlayerGameMode gameMode;
    private final ItemStack stack;
    private final BlockPos schematicPos;
    private final BlockState schematic;
    private final Block block;
    private final BlockHitResult schematicTrace;
    private final RelativeBlockHitResult requested;
    private final int totalClicks;
    private final boolean hasRotation;
    private final float yaw;
    private final float pitch;
    private final Level schematicWorld;
    private NativePlacementAdapter.Prepared prepared;

    public NativeEasyPlaceAction(Minecraft mc, MultiPlayerGameMode gameMode, ItemStack stack,
            BlockPos schematicPos, BlockState schematic, BlockHitResult schematicTrace,
            RelativeBlockHitResult requested, int totalClicks, boolean hasRotation,
            float yaw, float pitch) {
        this.mc=mc; this.gameMode=gameMode; this.stack=stack.copy();
        this.schematicPos=schematicPos.immutable(); this.schematic=schematic;
        this.block=schematic.getBlock(); this.schematicTrace=schematicTrace;
        this.requested=requested; this.totalClicks=totalClicks;
        this.hasRotation=hasRotation; this.yaw=yaw; this.pitch=pitch;
        this.schematicWorld=SchematicWorldHandler.getSchematicWorld();
    }

    public static boolean settingsEnabled() {
        return easyPlacefixConfig.ENABLE_FIX.getBooleanValue() && Generic.EASY_PLACE_MODE.getBooleanValue()
            && !Generic.EASY_PLACE_POST_REWRITE.getBooleanValue()
            && PlacementHandler.getEffectiveProtocolVersion()==EasyPlaceProtocol.SLAB_ONLY
            && NativePlacementScope.isBedrockConnection();
    }

    public boolean stillRelevant() {
        return settingsEnabled() && schematicWorld!=null && SchematicWorldHandler.getSchematicWorld()==schematicWorld
            && DataManager.getRenderLayerRange().isPositionWithinRange(schematicPos)
            && EasyPlaceHandler.isSchematicBlock(schematicPos)
            && schematicWorld.getBlockState(schematicPos).equals(schematic);
    }

    public void prepare() {
        try {
            if (mc.getConnection()==null || !stillRelevant()) return;
            PlacementInventory.pickItem(mc,stack);
            InteractionHand hand=EntityUtils.getUsedHandForItem(mc.player,stack);
            ((IClientPlayerInteractionManager)gameMode).syn();
            if (hand==null) return;
            float actionYaw=hasRotation ? yaw : mc.player.getYRot();
            float actionPitch=hasRotation ? pitch : mc.player.getXRot();
            Optional<NativePlacementAdapter.Prepared> plan=NativePlacementAdapter.prepare(mc,hand,
                schematicPos,schematic,requested,actionYaw,actionPitch);
            if (plan.isEmpty()) return;
            prepared=plan.get();
            registerPreparedInputs(prepared.yaw(),prepared.pitch(),prepared.secondary());
            PlacementDiagnostics.report("easyplacefix.diagnostic.placing",stack.getHoverName(),schematicPos.toShortString());
            // These input/look packets are prepared before the existing normal
            // CLIENT_TICK_END, not flushed by inventing another auth tick.
            PlayerInputAction.SetShift(prepared.secondary());
            PlayerRotationAction.setServerBoundPlayerRotation(prepared.yaw(),prepared.pitch(),mc.player.horizontalCollision);
        } catch (RuntimeException error) {
            LOGGER.debug("Native EasyPlace preparation failed",error);
            prepared=null;
        } finally {
            if (prepared==null) {
                PlacementDiagnostics.report("easyplacefix.diagnostic.native_placement_rejected",schematic.getBlock().getName());
                finishInputs(mc,true);
            }
        }
    }

    public void place() {
        if (prepared==null) return;
        if (mc.getConnection()==null || !stillRelevant()) { finishInputs(mc,true); return; }
        boolean completed=false;
        boolean started=false;
        boolean afterCalled=false;
        try {
            PlacementInventory.pickItem(mc,stack);
            InteractionHand hand=EntityUtils.getUsedHandForItem(mc.player,stack);
            ((IClientPlayerInteractionManager)gameMode).syn();
            Optional<NativePlacementAdapter.Prepared> checked=NativePlacementAdapter.revalidate(mc,hand,prepared);
            if (checked.isEmpty()) {
                PlacementDiagnostics.report("easyplacefix.diagnostic.native_placement_rejected",schematic.getBlock().getName());
                return;
            }
            NativePlacementAdapter.Prepared plan=checked.get();
            NativePlacementScope placementScope=NativePlacementScope.open(plan.yaw(),plan.pitch(),plan.secondary());
            try (placementScope) {
                started=true;
                ((IBlock)block).firstAction(schematic,schematicTrace);
                PlayerInputAction.SetShift(plan.secondary());
                PlayerRotationAction.setServerBoundPlayerRotation(plan.yaw(),plan.pitch(),mc.player.horizontalCollision);
                InteractionResult result=gameMode.useItemOn(mc.player,hand,plan.hit());
                if (result.consumesAction()) {
                    mc.player.swing(hand,mc.player.getItemInHand(hand).getInteractAnimation(),false);
                    ExtraInteractionRunner.run(mc,gameMode,hand,requested,totalClicks,block,schematicPos,
                        plan.yaw(),plan.pitch());
                    completed=true;
                }
                afterCalled=true;
                ((IBlock)block).afterAction(schematic,schematicTrace);
                if (completed) ((IBlock)block).BlockAction(schematic,schematicTrace);
            }
        } catch (RuntimeException error) {
            LOGGER.debug("Native EasyPlace action failed",error);
        } finally {
            try {
                if (started && !afterCalled) ((IBlock)block).afterAction(schematic,schematicTrace);
            } finally {
                finishInputs(mc,!completed);
            }
        }
    }

    /** Unwind only this job; a newer prepared job retains its own wire input. */
    public static void finishInputs(Minecraft mc, boolean cancelled) {
        RotationLeaseController.Token owner=TickThread.currentRotationOwner();
        if (owner!=null) INPUTS.remove(owner);
        TickThread.releaseCurrentLookLock();
        // A native wire-only look must not outlive its action and poison the
        // next manual click. Java's configurable behavior remains in its path.
        restorePreparedInputs(mc,true);
    }

    public static void registerPreparedInputs(float yaw, float pitch, boolean secondary) {
        RotationLeaseController.Token owner=TickThread.currentRotationOwner();
        if (owner!=null) INPUTS.put(owner,new InputPlan(yaw,pitch,secondary));
    }

    /** Main thread refreshes identity; worker constructors only observe the epoch. */
    public static boolean requiresPreparedShift() {
        TickThread.refreshPlacementContext();
        InputPlan latest=null;
        long latestId=Long.MIN_VALUE;
        for (Map.Entry<RotationLeaseController.Token,InputPlan> entry:INPUTS.entrySet()) {
            if (entry.getKey().id()>latestId && TickThread.isCurrentRotationOwner(entry.getKey())) {
                latestId=entry.getKey().id(); latest=entry.getValue();
            }
        }
        return latest!=null && latest.secondary();
    }

    public static void restorePreparedInputs(Minecraft mc, boolean restoreCamera) {
        if (mc==null || mc.player==null || mc.level==null || mc.getConnection()==null) return;
        INPUTS.entrySet().removeIf(entry -> !TickThread.isCurrentRotationOwner(entry.getKey()));
        InputPlan next=null;
        long latest=Long.MIN_VALUE;
        for (Map.Entry<RotationLeaseController.Token,InputPlan> entry:INPUTS.entrySet()) {
            if (entry.getKey().id()>latest) { latest=entry.getKey().id(); next=entry.getValue(); }
        }
        PlayerInputAction.SetShift(next!=null && next.secondary());
        if (next!=null) {
            PlayerRotationAction.setServerBoundPlayerRotation(next.yaw(),next.pitch(),mc.player.horizontalCollision);
        } else if (restoreCamera) {
            RotationLeaseController.Angles pending=TickThread.pendingRotation();
            float restoreYaw=pending==null ? mc.player.getYRot() : pending.yaw();
            float restorePitch=pending==null ? mc.player.getXRot() : pending.pitch();
            PlayerRotationAction.setServerBoundPlayerRotation(restoreYaw,restorePitch,mc.player.horizontalCollision);
        }
    }

    public static void clearPreparedInputs() { INPUTS.clear(); }
}
