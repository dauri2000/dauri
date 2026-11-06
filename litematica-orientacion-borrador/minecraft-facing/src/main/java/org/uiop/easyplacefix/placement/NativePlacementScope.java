package org.uiop.easyplacefix.placement;

import java.lang.reflect.Method;
import net.minecraft.world.item.context.BlockPlaceContext;

/** A real input context for one Bedrock action; never changes camera or world. */
public final class NativePlacementScope implements AutoCloseable {
    private static final ThreadLocal<NativePlacementScope> ACTIVE = new ThreadLocal<>();
    private final NativePlacementScope previous;
    private final float yaw;
    private final float pitch;
    private boolean secondary;
    private boolean closed;

    private NativePlacementScope(float yaw, float pitch, boolean secondary) {
        if (!Float.isFinite(yaw) || !Float.isFinite(pitch)) throw new IllegalArgumentException("Finite look required");
        previous=ACTIVE.get();
        this.yaw=yaw;
        this.pitch=pitch;
        this.secondary=secondary;
        ACTIVE.set(this);
    }

    public static NativePlacementScope open(float yaw, float pitch, boolean secondary) {
        return new NativePlacementScope(yaw,pitch,secondary);
    }

    public static boolean isActive() { return ACTIVE.get()!=null; }
    public static boolean secondaryUseActive(boolean original) {
        NativePlacementScope scope=ACTIVE.get();
        return scope==null ? original : scope.secondary;
    }
    public float yaw() { return yaw; }
    public float pitch() { return pitch; }
    public boolean secondary() { return secondary; }
    public void setSecondary(boolean value) { secondary=value; }

    public static BlockPlaceContext project(BlockPlaceContext original) {
        NativePlacementScope scope=ACTIVE.get();
        return original==null || scope==null ? original
            : new ProjectedPlacementContext(original,scope.yaw,scope.pitch,scope.secondary);
    }

    public static boolean isBedrockConnection() {
        Method connection=BedrockDetection.CONNECTION;
        if (connection==null) return false;
        try {
            return Boolean.TRUE.equals(connection.invoke(null));
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    private static final class BedrockDetection {
        private static final Method CONNECTION=findBedrockConnection();
    }

    private static Method findBedrockConnection() {
        try {
            return Class.forName("com.viaversion.viafabricplus.bedrock.input.BedrockInputPermissions",false,
                NativePlacementScope.class.getClassLoader()).getMethod("isBedrockConnection");
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    @Override
    public void close() {
        if (!closed) {
            if (ACTIVE.get()!=this) throw new IllegalStateException("Unbalanced native placement scope");
            closed=true;
            if (previous==null) ACTIVE.remove(); else ACTIVE.set(previous);
        }
    }
}
