package org.uiop.easyplacefix.placement;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.uiop.easyplacefix.ICanUse;
import org.uiop.easyplacefix.mixin.placement.BlockItemPlacementChecks;
import org.uiop.easyplacefix.placement.LegalSupportSelector.Assessment;
import org.uiop.easyplacefix.placement.LegalSupportSelector.Cell;
import org.uiop.easyplacefix.placement.LegalSupportSelector.Hit;
import org.uiop.easyplacefix.placement.LegalSupportSelector.Intent;
import org.uiop.easyplacefix.placement.LegalSupportSelector.Point;
import org.uiop.easyplacefix.placement.LegalSupportSelector.Probe;
import org.uiop.easyplacefix.placement.LegalSupportSelector.Request;
import org.uiop.easyplacefix.placement.LegalSupportSelector.SlabKind;

/** Genuine native ray/context/item checks. Does not write a block or send an ACK. */
public final class NativePlacementAdapter {
    private static final List<Property<?>> PLACEMENT_PROPERTIES = List.of(
        BlockStateProperties.FACING, BlockStateProperties.FACING_HOPPER,
        BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.AXIS,
        BlockStateProperties.HORIZONTAL_AXIS, BlockStateProperties.HALF,
        BlockStateProperties.ATTACH_FACE, BlockStateProperties.ROTATION_16,
        BlockStateProperties.DOOR_HINGE, BlockStateProperties.ORIENTATION,
        BlockStateProperties.VERTICAL_DIRECTION, BlockStateProperties.BELL_ATTACHMENT,
        BlockStateProperties.HANGING, BlockStateProperties.BED_PART,
        BlockStateProperties.DOUBLE_BLOCK_HALF, BlockStateProperties.CHEST_TYPE);

    private NativePlacementAdapter() {}

    /** Existing-block follow-up, not an asserted successful block placement. */
    public static Optional<BlockHitResult> interactionHit(Minecraft mc, BlockPos target, BlockHitResult original) {
        if (mc==null || mc.player==null || mc.level==null || !loadedRegion(mc.level,target)
            || mc.level.getBlockState(target).isAir()) return Optional.empty();
        Hit wanted=MinecraftHitCanonicalizer.read(original);
        Vec3 eye=mc.player.getEyePosition();
        Direction side=Direction.valueOf(wanted.face().name());
        boolean refined=false;
        for (double penetration:new double[]{1e-7,.501}) {
            Vec3 end=new Vec3(wanted.location().x()-side.getStepX()*penetration,
                wanted.location().y()-side.getStepY()*penetration,wanted.location().z()-side.getStepZ()*penetration);
            if (!chunksLoaded(mc.level,BlockPos.containing(eye),BlockPos.containing(end))) continue;
            BlockHitResult hit=mc.level.clip(new ClipContext(eye,end,ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,mc.player));
            if (hit.getType()!=HitResult.Type.BLOCK || hit.isWorldBorderHit()
                || !hit.getBlockPos().equals(target) || hit.getDirection()!=side
                || eye.distanceToSqr(hit.getLocation())>mc.player.blockInteractionRange()*mc.player.blockInteractionRange()) continue;
            Hit actual=MinecraftHitCanonicalizer.read(hit);
            Point a=wanted.local(),b=actual.local();
            if ((side.getStepX()==0 && Math.abs(a.x()-b.x())>1e-6)
                || (side.getStepY()==0 && Math.abs(a.y()-b.y())>1e-6)
                || (side.getStepZ()==0 && Math.abs(a.z()-b.z())>1e-6)) {
                if (refined) continue;
                refined=true;
                // A thin block's real surface can be below the original aim.
                // Keep the requested tangents; use only the observed surface's
                // normal coordinate, then obtain and validate another real hit.
                Vec3 surface=hit.getLocation();
                Vec3 refinedEnd=new Vec3(
                    (side.getStepX()!=0 ? surface.x : wanted.location().x())-side.getStepX()*1e-7,
                    (side.getStepY()!=0 ? surface.y : wanted.location().y())-side.getStepY()*1e-7,
                    (side.getStepZ()!=0 ? surface.z : wanted.location().z())-side.getStepZ()*1e-7);
                if (!chunksLoaded(mc.level,BlockPos.containing(eye),BlockPos.containing(refinedEnd))) continue;
                BlockHitResult refinedHit=mc.level.clip(new ClipContext(eye,refinedEnd,
                    ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,mc.player));
                if (refinedHit.getType()!=HitResult.Type.BLOCK || refinedHit.isWorldBorderHit()
                    || !refinedHit.getBlockPos().equals(target) || refinedHit.getDirection()!=side
                    || eye.distanceToSqr(refinedHit.getLocation())>
                        mc.player.blockInteractionRange()*mc.player.blockInteractionRange()) continue;
                Point refinedLocal=MinecraftHitCanonicalizer.read(refinedHit).local();
                if ((side.getStepX()==0 && Math.abs(a.x()-refinedLocal.x())>1e-6)
                    || (side.getStepY()==0 && Math.abs(a.y()-refinedLocal.y())>1e-6)
                    || (side.getStepZ()==0 && Math.abs(a.z()-refinedLocal.z())>1e-6)) continue;
                return Optional.of(refinedHit);
            }
            return Optional.of(hit);
        }
        return Optional.empty();
    }

    public record Prepared(Level level, LocalPlayer player, InteractionHand hand,
                           BlockPos schematicPos, BlockPos anchor, BlockState wanted,
                           BlockHitResult hit, BlockState preview, boolean secondary,
                           float yaw, float pitch, Intent intent, Item heldItem) {}
    private record Anchor(BlockPos pos, BlockState state) {}
    private record Checked(Assessment assessment, BlockHitResult nativeHit,
                           BlockState preview, boolean secondary) {}

    public static Optional<Prepared> prepare(Minecraft mc, InteractionHand hand,
            BlockPos schematicPos, BlockState schematic, BlockHitResult original,
            float yaw, float pitch) {
        if (mc==null || mc.player==null || mc.level==null || hand==null || original==null)
            return Optional.empty();
        Optional<Anchor> anchored=anchor(schematicPos,schematic);
        if (anchored.isEmpty()) return Optional.empty();
        Anchor anchor=anchored.get();
        if (!loadedRegion(mc.level,anchor.pos)) return Optional.empty();
        ItemStack held=mc.player.getItemInHand(hand);
        if (!(held.getItem() instanceof BlockItem item)
            || !((Object)item instanceof BlockItemPlacementChecks)) return Optional.empty();
        Intent intent=intent(anchor.state,mc.level.getBlockState(anchor.pos));
        boolean water=waterlogged(anchor.state);
        Hit originalWorld=MinecraftHitCanonicalizer.read(original);
        Vec3 eye=mc.player.getEyePosition();
        Request request=new Request(cell(anchor.pos),originalWorld,point(eye),
            mc.player.blockInteractionRange(),intent,water,!mc.level.getBlockState(anchor.pos).isAir());
        Map<Hit,Checked> checked=new HashMap<>();
        try (NativePlacementScope scope=NativePlacementScope.open(yaw,pitch,mc.player.isSecondaryUseActive())) {
            Optional<Hit> selected=LegalSupportSelector.select(request,probe -> {
                Optional<Checked> result=assess(mc,hand,item,anchor,request,probe,scope);
                result.ifPresent(value -> checked.put(value.assessment.actualFirstHit(),value));
                return result.map(Checked::assessment);
            });
            if (selected.isEmpty()) return Optional.empty();
            Checked result=checked.get(selected.get());
            if (result==null) return Optional.empty();
            return Optional.of(new Prepared(mc.level,mc.player,hand,schematicPos.immutable(),
                anchor.pos.immutable(),anchor.state,
                MinecraftHitCanonicalizer.writeSelected(selected.get(),result.nativeHit),
                result.preview,result.secondary,yaw,pitch,intent,held.getItem()));
        }
    }

    /** Recheck the prepared hit, not a different support requiring different input. */
    public static Optional<Prepared> revalidate(Minecraft mc, InteractionHand hand, Prepared prepared) {
        if (mc==null || prepared==null || mc.player!=prepared.player || mc.level!=prepared.level
            || hand!=prepared.hand || mc.player.getItemInHand(hand).getItem()!=prepared.heldItem
            || !loadedRegion(mc.level,prepared.anchor)) return Optional.empty();
        if (!(mc.player.getItemInHand(hand).getItem() instanceof BlockItem item)) return Optional.empty();
        Request request=new Request(cell(prepared.anchor),MinecraftHitCanonicalizer.read(prepared.hit),
            point(mc.player.getEyePosition()),mc.player.blockInteractionRange(),prepared.intent,
            waterlogged(prepared.wanted));
        try (NativePlacementScope scope=NativePlacementScope.open(prepared.yaw,prepared.pitch,prepared.secondary)) {
            Optional<Checked> checked=assess(mc,hand,item,new Anchor(prepared.anchor,prepared.wanted),
                request,new Probe(request.original(),true),scope);
            if (checked.isEmpty() || checked.get().secondary!=prepared.secondary) return Optional.empty();
            Checked result=checked.get();
            // Reuse the pure policy for the original prepared hit only: never
            // permit a new fallback whose look/shift was not prepared last tick.
            Optional<Hit> selected=LegalSupportSelector.select(request,probe -> probe.original()
                ? Optional.of(result.assessment) : Optional.empty());
            if (selected.isEmpty()) return Optional.empty();
            return Optional.of(new Prepared(prepared.level,prepared.player,hand,prepared.schematicPos,
                prepared.anchor,prepared.wanted,
                MinecraftHitCanonicalizer.writeSelected(selected.get(),result.nativeHit),
                result.preview,result.secondary,prepared.yaw,prepared.pitch,prepared.intent,prepared.heldItem));
        }
    }

    private static Optional<Checked> assess(Minecraft mc, InteractionHand hand, BlockItem item,
            Anchor anchor, Request request, Probe probe, NativePlacementScope scope) {
        BlockPos support=pos(probe.requested().clicked());
        if (!mc.level.isLoaded(support) || !loadedRegion(mc.level,anchor.pos)) return Optional.empty();
        BlockState supportState=mc.level.getBlockState(support);
        if (supportState.isAir()) return Optional.empty();
        Vec3 eye=mc.player.getEyePosition();
        Point target=probe.requested().location();
        Direction face=Direction.valueOf(probe.requested().face().name());
        BlockHitResult actual=null;
        // Tiny penetration first. Slab-specific deep probing also reaches a
        // half-slab's real surface; all results still require exact ray support/face.
        int attempts=2;
        for (int attempt=0;attempt<attempts;attempt++) {
            double penetration=attempt==0 ? 1e-7 : .501;
            Vec3 end=new Vec3(target.x()-face.getStepX()*penetration,
                target.y()-face.getStepY()*penetration,target.z()-face.getStepZ()*penetration);
            if (!chunksLoaded(mc.level,BlockPos.containing(eye),BlockPos.containing(end))) continue;
            BlockHitResult ray=mc.level.clip(new ClipContext(eye,end,ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,mc.player));
            if (ray.getType()==HitResult.Type.BLOCK && !ray.isWorldBorderHit()
                && ray.getBlockPos().equals(support) && ray.getDirection()==face) {
                actual=ray;
                break;
            }
        }
        if (actual==null || eye.distanceToSqr(actual.getLocation())>
            mc.player.blockInteractionRange()*mc.player.blockInteractionRange()) return Optional.empty();
        boolean physicalSecondary=mc.player.isSecondaryUseActive() || mc.player.input.keyPresses.shift();
        scope.setSecondary(physicalSecondary);
        BlockPlaceContext physicalContext=NativePlacementScope.project(new BlockPlaceContext(mc.player,hand,
            mc.player.getItemInHand(hand),actual));
        boolean sameCellReplacement=physicalContext.replacingClickedOnBlock()
            && support.equals(anchor.pos) && supportState.getBlock()==anchor.state.getBlock();
        boolean interactive=supportState.getBlock() instanceof ICanUse
            || NativeSupportActions.mayConsumeBlockInteraction(supportState.getBlock());
        if (sameCellReplacement && interactive
            && !NativeSupportActions.isKnownStackable(supportState.getBlock())) return Optional.empty();
        boolean supportRequiresSecondary=interactive && !sameCellReplacement;
        for (boolean secondary:NativeSecondaryUsePolicy.candidates(physicalSecondary,supportRequiresSecondary)) {
            scope.setSecondary(secondary);
            BlockPlaceContext initial=secondary==physicalSecondary ? physicalContext
                : NativePlacementScope.project(new BlockPlaceContext(mc.player,hand,mc.player.getItemInHand(hand),actual));
            if (!initial.canPlace() || !initial.getClickedPos().equals(anchor.pos)) continue;
            BlockPlaceContext context=NativePlacementScope.project(item.updatePlacementContext(initial));
            if (context==null || !context.getClickedPos().equals(anchor.pos) || !context.canPlace()) continue;
            BlockItemPlacementChecks checks=(BlockItemPlacementChecks)(Object)item;
            BlockState initialPreview=checks.easyplacefix$getPlacementState(context);
            if (initialPreview==null) continue;
            // Vanilla applies this component after its native permission check.
            BlockState preview=applyItemProperties(mc.player.getItemInHand(hand),initialPreview);
            boolean compatible=placementMatchesInContext(mc.level,anchor.pos,anchor.state,preview);
            if (request.intent()==Intent.PREVIEW_VALIDATED_ANY_FACE && !compatible) continue;
            if (request.intent()!=Intent.PREVIEW_VALIDATED_ANY_FACE && !sameSlabBlock(anchor.state,preview)) continue;
            Hit actualWorld=MinecraftHitCanonicalizer.read(actual);
            Assessment assessment=new Assessment(actualWorld,cell(context.getClickedPos()),true,true,true,true,
                slabKind(preview),waterlogged(preview),compatible);
            return Optional.of(new Checked(assessment,actual,preview,secondary));
        }
        return Optional.empty();
    }

    private static Optional<Anchor> anchor(BlockPos schematicPos, BlockState schematic) {
        BlockPos anchor=schematicPos;
        if (schematic.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
            && schematic.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)==DoubleBlockHalf.UPPER) anchor=anchor.below();
        else if (schematic.hasProperty(BlockStateProperties.BED_PART)
            && schematic.getValue(BlockStateProperties.BED_PART)==BedPart.HEAD)
            anchor=anchor.relative(schematic.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite());
        if (anchor.equals(schematicPos)) return Optional.of(new Anchor(anchor,schematic));
        Level schematicWorld=SchematicWorldHandler.getSchematicWorld();
        if (schematicWorld==null) return Optional.empty();
        BlockState lower=schematicWorld.getBlockState(anchor);
        return lower.getBlock()==schematic.getBlock() ? Optional.of(new Anchor(anchor,lower)) : Optional.empty();
    }

    private static Intent intent(BlockState wanted, BlockState current) {
        if (!(wanted.getBlock() instanceof SlabBlock)) return Intent.PREVIEW_VALIDATED_ANY_FACE;
        return switch(wanted.getValue(BlockStateProperties.SLAB_TYPE)) {
            case TOP -> Intent.SLAB_TOP;
            case BOTTOM -> Intent.SLAB_BOTTOM;
            case DOUBLE -> current.getBlock()==wanted.getBlock()
                && current.getValue(BlockStateProperties.SLAB_TYPE)!=SlabType.DOUBLE
                ? Intent.SLAB_MERGE_DOUBLE : Intent.SLAB_DOUBLE_FIRST;
        };
    }

    private static boolean placementMatches(BlockState wanted, BlockState preview) {
        if (wanted.getBlock()!=preview.getBlock()) return false;
        if (wanted.getBlock() instanceof CeilingHangingSignBlock
            && (!preview.hasProperty(BlockStateProperties.ATTACHED)
                || wanted.getValue(BlockStateProperties.ATTACHED)!=preview.getValue(BlockStateProperties.ATTACHED))) return false;
        if (wanted.getBlock() instanceof MultifaceBlock || wanted.getBlock() instanceof VineBlock) {
            // A native placement may add one desired attachment at a time, but
            // must not introduce an attachment absent from the schematic.
            for (Direction face:Direction.values()) {
                var property=MultifaceBlock.getFaceProperty(face);
                if (wanted.hasProperty(property) && preview.hasProperty(property)
                    && preview.getValue(property) && !wanted.getValue(property)) return false;
            }
        }
        for (Property<?> property:PLACEMENT_PROPERTIES) {
            if (wanted.hasProperty(property) && (!preview.hasProperty(property)
                || !wanted.getValue(property).equals(preview.getValue(property)))) return false;
        }
        return !wanted.hasProperty(BlockStateProperties.WATERLOGGED)
            || waterlogged(wanted)==waterlogged(preview);
    }

    /** A verified first chest half is an intermediate preview, not a final match. */
    private static boolean placementMatchesInContext(Level world, BlockPos position,
            BlockState wanted, BlockState preview) {
        if (placementMatches(wanted,preview)) return true;
        if (!(wanted.getBlock() instanceof ChestBlock)) return false;
        return NativeChestPlacement.allowsFirstSingleHalf(wanted,preview,position,
            SchematicWorldHandler.getSchematicWorld(),world);
    }

    private static boolean sameSlabBlock(BlockState wanted, BlockState preview) {
        return wanted.getBlock() instanceof SlabBlock && wanted.getBlock()==preview.getBlock();
    }
    /** Same immutable component application that vanilla performs after placement. */
    public static BlockState applyItemProperties(ItemStack stack, BlockState state) {
        BlockItemStateProperties properties=stack.get(DataComponents.BLOCK_STATE);
        return properties==null ? state : properties.apply(state);
    }
    private static SlabKind slabKind(BlockState state) {
        if (!(state.getBlock() instanceof SlabBlock)) return SlabKind.NOT_SLAB;
        return switch(state.getValue(BlockStateProperties.SLAB_TYPE)) {
            case TOP -> SlabKind.TOP; case BOTTOM -> SlabKind.BOTTOM; case DOUBLE -> SlabKind.DOUBLE;
        };
    }
    private static boolean waterlogged(BlockState state) {
        return state.hasProperty(BlockStateProperties.WATERLOGGED)
            && state.getValue(BlockStateProperties.WATERLOGGED);
    }
    private static boolean loadedRegion(Level level, BlockPos anchor) {
        return level.isLoaded(anchor) && chunksLoaded(level,anchor.offset(-8,0,-8),anchor.offset(8,0,8));
    }
    private static boolean chunksLoaded(Level level, BlockPos first, BlockPos last) {
        int minX=Math.min(first.getX()>>4,last.getX()>>4), maxX=Math.max(first.getX()>>4,last.getX()>>4);
        int minZ=Math.min(first.getZ()>>4,last.getZ()>>4), maxZ=Math.max(first.getZ()>>4,last.getZ()>>4);
        for (int x=minX;x<=maxX;x++) for (int z=minZ;z<=maxZ;z++) if (!level.hasChunk(x,z)) return false;
        return true;
    }
    private static Cell cell(BlockPos pos) { return new Cell(pos.getX(),pos.getY(),pos.getZ()); }
    private static BlockPos pos(Cell cell) { return new BlockPos(cell.x(),cell.y(),cell.z()); }
    private static Point point(Vec3 point) { return new Point(point.x,point.y,point.z); }
}
