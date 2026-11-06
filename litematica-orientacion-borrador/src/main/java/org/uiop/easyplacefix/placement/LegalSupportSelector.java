package org.uiop.easyplacefix.placement;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Bounded geometry selection. The validator is a required genuine-world boundary;
 * pure tests do not establish the truth of its Minecraft/Bedrock observations.
 */
public final class LegalSupportSelector {
    private LegalSupportSelector() {}
    public static final int MAX_PROBES = 13;

    public enum Face {
        DOWN(0,-1,0), UP(0,1,0), NORTH(0,0,-1), SOUTH(0,0,1), WEST(-1,0,0), EAST(1,0,0);
        public final int x, y, z;
        Face(int x, int y, int z) { this.x=x; this.y=y; this.z=z; }
    }

    public enum Intent { PRESERVE_FACE, PREVIEW_VALIDATED_ANY_FACE, SLAB_TOP, SLAB_BOTTOM, SLAB_DOUBLE_FIRST, SLAB_MERGE_DOUBLE }
    public enum SlabKind { NOT_SLAB, TOP, BOTTOM, DOUBLE }

    public record Cell(int x, int y, int z) {
        public Cell supportFor(Face clickedFace) {
            return new Cell(Math.subtractExact(x, clickedFace.x),
                Math.subtractExact(y, clickedFace.y), Math.subtractExact(z, clickedFace.z));
        }
    }

    public record Point(double x, double y, double z) {
        public Point {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z))
                throw new IllegalArgumentException("Finite geometry required");
        }
        public double distanceSquared(Point other) {
            double dx=x-other.x, dy=y-other.y, dz=z-other.z;
            return dx*dx+dy*dy+dz*dz;
        }
    }

    /** Location is always in WORLD coordinates. */
    public record Hit(Cell clicked, Face face, Point location) {
        public Hit {
            Objects.requireNonNull(clicked); Objects.requireNonNull(face); Objects.requireNonNull(location);
        }
        public Point local() {
            return new Point(location.x-clicked.x, location.y-clicked.y, location.z-clicked.z);
        }
    }

    public record Request(Cell physicalAnchor, Hit original, Point eye, double reach,
                          Intent intent, boolean expectedWaterlogged, boolean sameCellSurfaces) {
        public Request {
            Objects.requireNonNull(physicalAnchor); Objects.requireNonNull(original);
            Objects.requireNonNull(eye); Objects.requireNonNull(intent);
            if (!Double.isFinite(reach) || reach < 0) throw new IllegalArgumentException("Finite reach required");
        }
        public Request(Cell anchor, Hit hit, Point eye, double reach, Intent intent, boolean waterlogged) {
            this(anchor,hit,eye,reach,intent,waterlogged,false);
        }
    }

    public record Probe(Hit requested, boolean original) {}

    /**
     * Every field must come from actual native checks, not a fabricated success.
     * Mandatory order: first ray hit -> context built from THAT hit -> preview
     * and canPlace. Previewing the proposed point and returning another actual
     * point violates this contract and can reproduce a top/bottom mismatch.
     */
    public record Assessment(Hit actualFirstHit, Cell contextDestination, boolean chunkLoaded,
                             boolean realSupport, boolean actionSafe, boolean canPlace,
                             SlabKind computedSlabKind, boolean computedWaterlogged,
                             boolean completePreviewMatches) {
        public Assessment {
            Objects.requireNonNull(actualFirstHit); Objects.requireNonNull(contextDestination);
            Objects.requireNonNull(computedSlabKind);
        }
        public Assessment(Hit hit, Cell destination, boolean loaded, boolean support,
                          boolean safe, boolean placeable, SlabKind slabKind, boolean waterlogged) {
            this(hit,destination,loaded,support,safe,placeable,slabKind,waterlogged,false);
        }
    }

    @FunctionalInterface
    public interface Validator {
        Optional<Assessment> assess(Probe probe);
    }

    /** Convert only explicitly local input. An ordinary world hit stays unchanged. */
    public static Hit canonicalize(Cell clicked, Face face, Point location, boolean relativeSubtype) {
        return new Hit(clicked, face, relativeSubtype
            ? new Point(clicked.x+location.x, clicked.y+location.y, clicked.z+location.z) : location);
    }

    public static Optional<Hit> select(Request request, Validator validator) {
        Objects.requireNonNull(validator);
        Hit best = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (Probe probe : proposals(request)) {
            if (!legalLocal(probe.requested.local())) continue;
            if (minimumCellDistanceSquared(request.eye, probe.requested.clicked) > request.reach*request.reach)
                continue;
            Optional<Assessment> assessed = validator.assess(probe);
            if (assessed.isEmpty()) continue;
            Assessment result = assessed.get();
            Hit actual = result.actualFirstHit;
            if (!result.chunkLoaded || !result.realSupport || !result.actionSafe || !result.canPlace
                || !result.contextDestination.equals(request.physicalAnchor)
                || !actual.clicked.equals(probe.requested.clicked)
                || actual.face != probe.requested.face || !legalLocal(actual.local())
                || (request.intent == Intent.PRESERVE_FACE
                    && !sameTangential(probe.requested, actual))
                || !intentMatches(request, result)) continue;
            double distance = actual.location.distanceSquared(request.eye);
            if (distance > request.reach*request.reach) continue;
            if (probe.original) return Optional.of(actual);
            if (distance < bestDistance) {
                best = actual;
                bestDistance = distance;
            }
        }
        return Optional.ofNullable(best);
    }

    public static List<Probe> proposals(Request request) {
        LinkedHashSet<Hit> unique = new LinkedHashSet<>();
        unique.add(request.original);
        Point local = request.original.local();
        if (!legalLocal(local)) return List.of();
        for (Face face : Face.values()) {
            if (request.intent == Intent.PRESERVE_FACE && face != request.original.face) continue;
            if (request.intent == Intent.SLAB_DOUBLE_FIRST && face != Face.UP) continue;
            Cell support = request.physicalAnchor.supportFor(face);
            unique.add(faceHit(support, face, local, request.intent));
        }
        if (request.intent == Intent.SLAB_MERGE_DOUBLE || request.sameCellSurfaces) {
            for (Face face : Face.values())
                unique.add(faceHit(request.physicalAnchor, face, local, request.intent));
        }
        List<Probe> result = new ArrayList<>();
        for (Hit hit : unique) result.add(new Probe(hit, hit.equals(request.original)));
        if (result.size() > MAX_PROBES) throw new IllegalStateException("Probe bound exceeded");
        return List.copyOf(result);
    }

    private static Hit faceHit(Cell support, Face face, Point preferred, Intent intent) {
        double x=preferred.x, y=preferred.y, z=preferred.z;
        if (face.x != 0) x=face.x > 0 ? 1 : 0;
        if (face.y != 0) y=face.y > 0 ? 1 : 0;
        if (face.z != 0) z=face.z > 0 ? 1 : 0;
        if (face.y == 0) {
            if (intent == Intent.SLAB_TOP) y=.75;
            if (intent == Intent.SLAB_BOTTOM || intent == Intent.SLAB_DOUBLE_FIRST) y=.25;
        }
        return new Hit(support, face, new Point(support.x+x, support.y+y, support.z+z));
    }

    private static boolean intentMatches(Request request, Assessment result) {
        if (request.intent == Intent.PRESERVE_FACE) return true;
        if (request.intent == Intent.PREVIEW_VALIDATED_ANY_FACE) return result.completePreviewMatches;
        // The first half of a double slab is an intermediate native state. Its
        // water is not forged to match the final double slab before the merge.
        if (request.intent != Intent.SLAB_DOUBLE_FIRST
            && result.computedWaterlogged != request.expectedWaterlogged) return false;
        return switch (request.intent) {
            case SLAB_TOP -> result.computedSlabKind == SlabKind.TOP;
            case SLAB_BOTTOM -> result.computedSlabKind == SlabKind.BOTTOM;
            case SLAB_DOUBLE_FIRST -> result.computedSlabKind == SlabKind.BOTTOM
                && result.actualFirstHit.face == Face.UP;
            case SLAB_MERGE_DOUBLE -> result.computedSlabKind == SlabKind.DOUBLE;
            case PRESERVE_FACE, PREVIEW_VALIDATED_ANY_FACE -> throw new IllegalStateException("Handled above");
        };
    }

    private static boolean legalLocal(Point local) {
        return local.x >= 0 && local.x <= 1 && local.y >= 0 && local.y <= 1
            && local.z >= 0 && local.z <= 1;
    }

    private static boolean sameTangential(Hit requested, Hit actual) {
        Point a=requested.local(), b=actual.local();
        return (requested.face.x != 0 || Math.abs(a.x-b.x)<=1e-6)
            && (requested.face.y != 0 || Math.abs(a.y-b.y)<=1e-6)
            && (requested.face.z != 0 || Math.abs(a.z-b.z)<=1e-6);
    }

    private static double minimumCellDistanceSquared(Point eye, Cell cell) {
        double x=Math.max(cell.x-eye.x, Math.max(0, eye.x-(cell.x+1.0)));
        double y=Math.max(cell.y-eye.y, Math.max(0, eye.y-(cell.y+1.0)));
        double z=Math.max(cell.z-eye.z, Math.max(0, eye.z-(cell.z+1.0)));
        return x*x+y*y+z*z;
    }
}
