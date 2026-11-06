package org.uiop.easyplacefix.placement;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.uiop.easyplacefix.placement.LegalSupportSelector.*;

/** Constructed geometry/native observations, with no Minecraft API replacement. */
public final class LegalSupportSelectorTest {
    private static int assertions;
    private static final Cell TARGET = new Cell(-20, 70, 31);
    private static final Point EYE = new Point(-17.5, 72, 33.5);

    public static void main(String[] args) {
        canonicalizationIsExplicitAndSingle();
        automaticSupportDoesNotReadCameraCrosshair();
        topSlabCannotFallBackToBottom();
        bottomAndMergeAndDoubleFirst();
        preserveFaceAndTangentialFractions();
        rejectWrongDestinationOcclusionAndFace();
        rejectUnsafeUnavailableAndCollision();
        enforceActualReachAndBoundProbes();
        translationDoesNotChangeSlabPolicy();
        vanillaCrosshairDoesNotBypassSlabPolicy();
        sideOnlySupportRequiresFullCompatiblePreview();
        actualRayHitControlsPreview();
        System.out.println("PASS pure-JDK support/coordinate/slab-policy regression: " + assertions + " assertions");
        System.out.println("LIMIT: native observations are explicit fixtures; Minecraft compilation, ray tracing, canPlace, slab prediction and Bedrock placement are unverified");
    }

    private static Request request(Intent intent) {
        return new Request(TARGET,
            LegalSupportSelector.canonicalize(TARGET, Face.UP, new Point(.5,.5,.5), true),
            EYE, 6, intent, false);
    }

    private static Assessment valid(Probe probe, SlabKind kind) {
        return new Assessment(probe.requested(), TARGET, true, true, true, true, kind, false);
    }

    private static void canonicalizationIsExplicitAndSingle() {
        Hit relative = LegalSupportSelector.canonicalize(TARGET, Face.DOWN, new Point(.2,1,.8), true);
        require(relative.location().equals(new Point(-19.8,71,31.8)), "relative location becomes absolute once at negative coordinates");
        Hit absolute = LegalSupportSelector.canonicalize(TARGET, Face.DOWN, relative.location(), false);
        require(absolute.equals(relative), "ordinary world hit is not translated twice");
        require(Math.abs(relative.local().x()-.2)<1e-12 && relative.local().y()==1,
            "normal codec can recover intended local fraction from ordinary absolute hit");
    }

    private static void automaticSupportDoesNotReadCameraCrosshair() {
        Request request = request(Intent.PRESERVE_FACE);
        Cell below = TARGET.supportFor(Face.UP);
        Optional<Hit> result = LegalSupportSelector.select(request, probe ->
            probe.requested().clicked().equals(below)
                ? Optional.of(valid(probe, SlabKind.NOT_SLAB)) : Optional.empty());
        require(result.isPresent() && result.get().clicked().equals(below),
            "schematic destination chooses legal adjacent support without a crosshair parameter");
        require(result.get().face()==Face.UP && result.get().local().y()==1,
            "neighbor support hit is on requested real face in its own coordinate frame");
    }

    private static void topSlabCannotFallBackToBottom() {
        Request request = request(Intent.SLAB_TOP);
        Cell below = TARGET.supportFor(Face.UP);
        Optional<Hit> onlyWrongBottom = LegalSupportSelector.select(request, probe ->
            probe.requested().clicked().equals(below)
                ? Optional.of(valid(probe, SlabKind.BOTTOM)) : Optional.empty());
        require(onlyWrongBottom.isEmpty(), "top schematic rejects legal support hit whose native preview is bottom");

        Cell side = TARGET.supportFor(Face.WEST);
        Optional<Hit> correct = LegalSupportSelector.select(request, probe -> {
            if (probe.requested().clicked().equals(below)) return Optional.of(valid(probe, SlabKind.BOTTOM));
            if (probe.requested().clicked().equals(side) && probe.requested().face()==Face.WEST)
                return Optional.of(valid(probe, SlabKind.TOP));
            return Optional.empty();
        });
        require(correct.isPresent() && correct.get().clicked().equals(side),
            "top slab chooses an independently validated side support instead of wrong bottom placement");
        require(correct.get().local().y()==.75, "top slab horizontal proposal aims above half boundary");

        Optional<Hit> badOriginal = LegalSupportSelector.select(request, probe ->
            probe.original() ? Optional.of(valid(probe, SlabKind.BOTTOM)) : Optional.empty());
        require(badOriginal.isEmpty(), "original hit is subject to the same slab policy, with no unsafe legacy fallback");
    }

    private static void bottomAndMergeAndDoubleFirst() {
        Optional<Hit> bottom = LegalSupportSelector.select(request(Intent.SLAB_BOTTOM), probe ->
            probe.requested().clicked().equals(TARGET.supportFor(Face.UP))
                ? Optional.of(valid(probe, SlabKind.BOTTOM)) : Optional.empty());
        require(bottom.isPresent() && bottom.get().face()==Face.UP, "bottom slab accepts bottom-producing support");

        Hit mergeHit = LegalSupportSelector.canonicalize(TARGET, Face.DOWN, new Point(.5,0,.5), true);
        Request merge = new Request(TARGET, mergeHit, EYE, 6, Intent.SLAB_MERGE_DOUBLE, false);
        Optional<Hit> merged = LegalSupportSelector.select(merge, probe ->
            probe.requested().clicked().equals(TARGET) && probe.requested().face()==Face.DOWN
                ? Optional.of(valid(probe, SlabKind.DOUBLE)) : Optional.empty());
        require(merged.isPresent() && merged.get().clicked().equals(TARGET),
            "double-slab merge may use same cell rather than mandatory adjacent neighbor");

        Optional<Hit> wrongFirst = LegalSupportSelector.select(request(Intent.SLAB_DOUBLE_FIRST), probe ->
            Optional.of(valid(probe, SlabKind.TOP)));
        require(wrongFirst.isEmpty(), "double-first stage does not choose top when existing follow-up expects bottom then up-face merge");
        Optional<Hit> first = LegalSupportSelector.select(request(Intent.SLAB_DOUBLE_FIRST), probe ->
            probe.requested().clicked().equals(TARGET.supportFor(Face.UP))
                ? Optional.of(valid(probe, SlabKind.BOTTOM)) : Optional.empty());
        require(first.isPresent(), "bottom-first double stage remains selectable");
        var wetIntermediate=LegalSupportSelector.select(request(Intent.SLAB_DOUBLE_FIRST),probe ->
            probe.requested().clicked().equals(TARGET.supportFor(Face.UP))
                ? Optional.of(new Assessment(probe.requested(),TARGET,true,true,true,true,SlabKind.BOTTOM,true))
                : Optional.empty());
        require(wetIntermediate.isPresent(),"double-first does not forge intermediate water to match final double state");
        var unsafeLateralFirst=LegalSupportSelector.select(request(Intent.SLAB_DOUBLE_FIRST),probe ->
            probe.requested().face()==Face.EAST
                ? Optional.of(valid(probe,SlabKind.BOTTOM)) : Optional.empty());
        require(unsafeLateralFirst.isEmpty(),"double-first does not reuse a low lateral hit as an unsafe second-click merge plan");
    }

    private static void preserveFaceAndTangentialFractions() {
        for (Face face : Face.values()) {
            Hit original = LegalSupportSelector.canonicalize(TARGET, face, new Point(.2,.6,.8), true);
            Request request = new Request(TARGET, original, EYE, 6, Intent.PRESERVE_FACE, false);
            Cell support = TARGET.supportFor(face);
            Optional<Hit> result = LegalSupportSelector.select(request, probe ->
                probe.requested().clicked().equals(support) ? Optional.of(valid(probe, SlabKind.NOT_SLAB)) : Optional.empty());
            require(result.isPresent() && result.get().face()==face, "generic sensitive-state fallback preserves face "+face);
            Point local=result.get().local();
            require((face.x!=0 || Math.abs(local.x()-.2)<1e-12)
                && (face.y!=0 || Math.abs(local.y()-.6)<1e-12)
                && (face.z!=0 || Math.abs(local.z()-.8)<1e-12), "tangential fractions preserved "+face);
        }
        Request special = new Request(TARGET,
            LegalSupportSelector.canonicalize(TARGET.supportFor(Face.UP), Face.UP, new Point(.8,1,.2), true),
            EYE, 6, Intent.PRESERVE_FACE, false);
        var result=LegalSupportSelector.select(special, probe -> Optional.of(valid(probe, SlabKind.NOT_SLAB)));
        require(result.orElseThrow().equals(special.original()), "already legal special support hit keeps its exact coordinates");
    }

    private static void rejectWrongDestinationOcclusionAndFace() {
        Request request=request(Intent.PRESERVE_FACE);
        require(LegalSupportSelector.select(request, probe -> probe.original() ? Optional.empty() : Optional.of(new Assessment(
            probe.requested(), probe.requested().clicked(), true,true,true,true,SlabKind.NOT_SLAB,false))).isEmpty(),
            "replaceable support that resolves context to itself is rejected");
        require(LegalSupportSelector.select(request, probe -> Optional.of(new Assessment(
            new Hit(new Cell(0,0,0), probe.requested().face(),new Point(.5,.5,.5)), TARGET,
            true,true,true,true,SlabKind.NOT_SLAB,false))).isEmpty(), "occluding first ray block cannot be treated as intended support");
        require(LegalSupportSelector.select(request, probe -> Optional.of(new Assessment(
            new Hit(probe.requested().clicked(),Face.DOWN,probe.requested().location()), TARGET,
            true,true,true,true,SlabKind.NOT_SLAB,false))).isEmpty(), "ray hitting another face is not silently relabeled");
    }

    private static void rejectUnsafeUnavailableAndCollision() {
        Request request=request(Intent.PRESERVE_FACE);
        require(LegalSupportSelector.select(request, probe -> Optional.of(new Assessment(
            probe.requested(),TARGET,false,true,true,true,SlabKind.NOT_SLAB,false))).isEmpty(), "unloaded support rejected");
        require(LegalSupportSelector.select(request, probe -> Optional.of(new Assessment(
            probe.requested(),TARGET,true,false,true,true,SlabKind.NOT_SLAB,false))).isEmpty(), "air/fluid-only support rejected");
        require(LegalSupportSelector.select(request, probe -> Optional.of(new Assessment(
            probe.requested(),TARGET,true,true,false,true,SlabKind.NOT_SLAB,false))).isEmpty(), "unsafe interactive support rejected");
        require(LegalSupportSelector.select(request, probe -> Optional.of(new Assessment(
            probe.requested(),TARGET,true,true,true,false,SlabKind.NOT_SLAB,false))).isEmpty(), "native collision/placement failure rejected");
        require(LegalSupportSelector.select(request(Intent.SLAB_TOP), probe -> Optional.of(new Assessment(
            probe.requested(),TARGET,true,true,true,true,SlabKind.TOP,true))).isEmpty(),
            "waterlogged preview cannot be presented as dry schematic slab");
    }

    private static void enforceActualReachAndBoundProbes() {
        Request request = new Request(TARGET, request(Intent.PRESERVE_FACE).original(), EYE,.1,Intent.PRESERVE_FACE,false);
        AtomicInteger calls=new AtomicInteger();
        require(LegalSupportSelector.select(request, probe -> {calls.incrementAndGet(); return Optional.of(valid(probe,SlabKind.NOT_SLAB));}).isEmpty(),
            "reach is enforced before accepting candidate");
        require(calls.get()==0, "cells wholly outside actual reach do not trigger native raycasts");
        AtomicInteger probes=new AtomicInteger();
        LegalSupportSelector.select(request(Intent.SLAB_MERGE_DOUBLE), probe -> {probes.incrementAndGet();return Optional.empty();});
        require(probes.get()<=LegalSupportSelector.MAX_PROBES && probes.get()>0,
            "neighbor and merge probes have a fixed upper bound");
    }

    private static void translationDoesNotChangeSlabPolicy() {
        int[][] shifts={{0,0,0},{12000,150,-31000},{-47000,-25,-22000},{-123456,100,234567}};
        for (int[] shift : shifts) {
            Cell target=new Cell(TARGET.x()+shift[0],TARGET.y()+shift[1],TARGET.z()+shift[2]);
            Point eye=new Point(EYE.x()+shift[0],EYE.y()+shift[1],EYE.z()+shift[2]);
            Request query=new Request(target,LegalSupportSelector.canonicalize(target,Face.DOWN,new Point(.5,1,.5),true),
                eye,6,Intent.SLAB_TOP,false);
            Cell side=target.supportFor(Face.WEST);
            Optional<Hit> result=LegalSupportSelector.select(query,probe -> {
                SlabKind nativeFixtureKind=probe.requested().face()==Face.WEST && probe.requested().local().y()>.5
                    ? SlabKind.TOP : SlabKind.BOTTOM;
                return Optional.of(new Assessment(probe.requested(),target,true,true,true,true,nativeFixtureKind,false));
            });
            require(result.isPresent() && result.get().clicked().equals(side),
                "slab policy is translation invariant, including synthetic negative/far X/Z");
            require(result.get().local().y()==.75,"top hit height remains local after translation");
            require(query.original().clicked().equals(target) && query.original().face()==Face.DOWN,
                "selection leaves immutable original input unchanged");
        }
    }

    private static void vanillaCrosshairDoesNotBypassSlabPolicy() {
        Cell side=TARGET.supportFor(Face.WEST);
        Hit vanillaLow=LegalSupportSelector.canonicalize(side,Face.WEST,new Point(0,.25,.5),true);
        Request query=new Request(TARGET,vanillaLow,EYE,6,Intent.SLAB_TOP,false);
        Optional<Hit> result=LegalSupportSelector.select(query,probe -> {
            if (!probe.requested().clicked().equals(side) || probe.requested().face()!=Face.WEST) return Optional.empty();
            return Optional.of(valid(probe,probe.requested().local().y()>.5 ? SlabKind.TOP : SlabKind.BOTTOM));
        });
        require(result.isPresent() && result.get().local().y()==.75,
            "low vanilla crosshair hit cannot bypass top policy; another validated top point is selected");
        require(vanillaLow.local().y()==.25,"old vanilla hit remains unchanged rather than patched in place");
        require(LegalSupportSelector.select(request(Intent.PRESERVE_FACE),probe -> {
            Hit shifted=new Hit(probe.requested().clicked(),probe.requested().face(),
                new Point(probe.requested().location().x()+.1,probe.requested().location().y(),probe.requested().location().z()));
            return Optional.of(new Assessment(shifted,TARGET,true,true,true,true,SlabKind.NOT_SLAB,false));
        }).isEmpty(),"sensitive-state tangent fractions cannot drift under native ray correction");
    }

    private static void sideOnlySupportRequiresFullCompatiblePreview() {
        Request query=request(Intent.PREVIEW_VALIDATED_ANY_FACE);
        Cell side=TARGET.supportFor(Face.EAST);
        var selected=LegalSupportSelector.select(query,probe ->
            probe.requested().clicked().equals(side)
                ? Optional.of(new Assessment(probe.requested(),TARGET,true,true,true,true,SlabKind.NOT_SLAB,false,true))
                : Optional.empty());
        require(selected.isPresent() && selected.get().clicked().equals(side),
            "no-spoof full-preview intent may extend a row using side-only legal support");
        var incompatible=LegalSupportSelector.select(query,probe ->
            Optional.of(new Assessment(probe.requested(),TARGET,true,true,true,true,SlabKind.NOT_SLAB,false,false)));
        require(incompatible.isEmpty(),"alternate faces are unavailable when native complete preview does not match");
    }

    private static void actualRayHitControlsPreview() {
        Request query=request(Intent.SLAB_TOP);
        Cell side=TARGET.supportFor(Face.WEST);
        var result=LegalSupportSelector.select(query,probe -> {
            if (!probe.requested().clicked().equals(side) || probe.requested().face()!=Face.WEST)
                return Optional.empty();
            Hit actualLow=LegalSupportSelector.canonicalize(side,Face.WEST,new Point(0,.25,.5),true);
            // Explicit native-observation fixture: preview was computed from
            // actualLow (BOTTOM), not from the proposed .75 point (TOP).
            return Optional.of(new Assessment(actualLow,TARGET,true,true,true,true,SlabKind.BOTTOM,false));
        });
        require(result.isEmpty(),"top proposal cannot pass when actual first-hit preview produces bottom");
    }

    private static void require(boolean condition,String message) {
        assertions++;
        if (!condition) throw new AssertionError(message);
    }
}
