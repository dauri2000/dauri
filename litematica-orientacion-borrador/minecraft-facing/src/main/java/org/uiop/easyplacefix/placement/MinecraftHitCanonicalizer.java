package org.uiop.easyplacefix.placement;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;
import org.uiop.easyplacefix.placement.LegalSupportSelector.Cell;
import org.uiop.easyplacefix.placement.LegalSupportSelector.Face;
import org.uiop.easyplacefix.placement.LegalSupportSelector.Hit;
import org.uiop.easyplacefix.placement.LegalSupportSelector.Point;

/** Source-only genuine-Minecraft bridge. Conversion is NOT placement validation. */
public final class MinecraftHitCanonicalizer {
    private MinecraftHitCanonicalizer() {}

    public static Hit read(BlockHitResult input) {
        Objects.requireNonNull(input, "input");
        BlockPos pos=input.getBlockPos();
        Vec3 location=input.getLocation();
        return LegalSupportSelector.canonicalize(new Cell(pos.getX(),pos.getY(),pos.getZ()),
            Face.valueOf(input.getDirection().name()),new Point(location.x,location.y,location.z),
            input instanceof RelativeBlockHitResult);
    }

    /** Only for a selected hit already validated from this actual native ray hit. */
    public static BlockHitResult writeSelected(Hit selected, BlockHitResult actualNativeHit) {
        if (actualNativeHit.getType()!=HitResult.Type.BLOCK || actualNativeHit.isWorldBorderHit()
            || !selected.equals(read(actualNativeHit))) {
            throw new IllegalArgumentException("Selected hit must be the actual validated non-border ray hit");
        }
        return new BlockHitResult(new Vec3(selected.location().x(),selected.location().y(),selected.location().z()),
            Direction.valueOf(selected.face().name()),
            new BlockPos(selected.clicked().x(),selected.clicked().y(),selected.clicked().z()),
            actualNativeHit.isInside());
    }
}
