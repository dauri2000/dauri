package org.uiop.easyplacefix.placement;

import java.util.Objects;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;

/**
 * Native placement context with the same finite look that the action sends on
 * the wire. The original player, world, stack, hand and hit remain unchanged.
 */
public final class ProjectedPlacementContext extends BlockPlaceContext {
    private final float projectedYaw;
    private final float projectedPitch;
    private final boolean projectedSecondaryUse;

    public ProjectedPlacementContext(BlockPlaceContext original, float yaw, float pitch,
                                     boolean secondaryUse) {
        super(Objects.requireNonNull(original, "original"));
        if (!Float.isFinite(yaw) || !Float.isFinite(pitch)) {
            throw new IllegalArgumentException("Finite projected rotation required");
        }
        projectedYaw = yaw;
        projectedPitch = pitch;
        projectedSecondaryUse = secondaryUse;
        // The superclass evaluates replacement during construction, before our
        // fields exist. Repeat its native check with the initialized projection
        // and its original pre-check replaceClicked=true state.
        replaceClicked = true;
        replaceClicked = getLevel().getBlockState(getHitResult().getBlockPos()).canBeReplaced(this);
    }

    @Override
    public float getRotation() {
        return projectedYaw;
    }

    @Override
    public Direction getHorizontalDirection() {
        return Direction.fromYRot(projectedYaw);
    }

    @Override
    public boolean isSecondaryUseActive() {
        return projectedSecondaryUse;
    }

    @Override
    public Direction getNearestLookingDirection() {
        return orderedByProjectedLook()[0];
    }

    @Override
    public Direction getNearestLookingVerticalDirection() {
        // Exact Axis.Y branch of native Direction.getFacingAxis(Entity, Axis).
        return projectedPitch < 0.0F ? Direction.UP : Direction.DOWN;
    }

    @Override
    public Direction[] getNearestLookingDirections() {
        Direction[] directions = orderedByProjectedLook();
        if (replaceClicked) return directions;
        Direction oppositeClickedFace = getClickedFace().getOpposite();
        int index = 0;
        while (index < directions.length && directions[index] != oppositeClickedFace) index++;
        if (index > 0) {
            System.arraycopy(directions, 0, directions, 1, index);
            directions[0] = oppositeClickedFace;
        }
        return directions;
    }

    /** Native 26.3 Direction.orderedByNearest branch/tie order, with explicit angles. */
    private Direction[] orderedByProjectedLook() {
        float pitch = projectedPitch * (float) (Math.PI / 180.0);
        float yaw = -projectedYaw * (float) (Math.PI / 180.0);
        float pitchSin = Mth.sin(pitch);
        float pitchCos = Mth.cos(pitch);
        float yawSin = Mth.sin(yaw);
        float yawCos = Mth.cos(yaw);
        boolean xPos = yawSin > 0.0F;
        boolean yPos = pitchSin < 0.0F;
        boolean zPos = yawCos > 0.0F;
        float xYaw = xPos ? yawSin : -yawSin;
        float yMag = yPos ? -pitchSin : pitchSin;
        float zYaw = zPos ? yawCos : -yawCos;
        float xMag = xYaw * pitchCos;
        float zMag = zYaw * pitchCos;
        Direction axisX = xPos ? Direction.EAST : Direction.WEST;
        Direction axisY = yPos ? Direction.UP : Direction.DOWN;
        Direction axisZ = zPos ? Direction.SOUTH : Direction.NORTH;
        if (xYaw > zYaw) {
            if (yMag > xMag) return directionArray(axisY, axisX, axisZ);
            return zMag > yMag ? directionArray(axisX, axisZ, axisY)
                : directionArray(axisX, axisY, axisZ);
        }
        if (yMag > zMag) return directionArray(axisY, axisZ, axisX);
        return xMag > yMag ? directionArray(axisZ, axisX, axisY)
            : directionArray(axisZ, axisY, axisX);
    }

    private static Direction[] directionArray(Direction first, Direction second, Direction third) {
        return new Direction[]{first, second, third, third.getOpposite(), second.getOpposite(), first.getOpposite()};
    }
}
