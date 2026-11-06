package org.uiop.easyplacefix.rotation;

import java.util.Objects;

/** One instance belongs to one movement packet, never to the current global job. */
public final class RotationPacketState {
    private final RotationLeaseController controller;
    private final long creationGeneration;
    private RotationLeaseController.Snapshot snapshot;

    public RotationPacketState(RotationLeaseController controller) {
        this.controller = Objects.requireNonNull(controller, "controller");
        RotationLeaseController.PacketCapture captured = controller.capturePacket();
        creationGeneration = captured.generation();
        snapshot = captured.snapshot();
    }

    /** Call before send, using the explicit packet's originally requested angles. */
    public void markExplicit(float yaw, float pitch) {
        snapshot = controller.explicit(creationGeneration, yaw, pitch);
    }

    /** No live lock, lease expiry or owner change is consulted during encoding. */
    public RotationLeaseController.Angles capturedAngles() {
        RotationLeaseController.Snapshot captured = snapshot;
        return controller.isCurrent(captured) ? captured.angles() : null;
    }
}
