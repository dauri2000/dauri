package org.uiop.easyplacefix.rotation;

/** Implemented on genuine movement packets by the separate Minecraft-facing mixin. */
public interface RotationBoundPacket {
    void easyplacefix$markExplicitRotation(float yaw, float pitch);
    void easyplacefix$applyRotationSnapshot();
}
