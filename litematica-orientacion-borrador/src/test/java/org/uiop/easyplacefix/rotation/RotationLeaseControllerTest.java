package org.uiop.easyplacefix.rotation;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/** Pure-JDK regression tests; these do not launch Minecraft or transform Mixins. */
public final class RotationLeaseControllerTest {
    private static int assertions;

    public static void main(String[] args) throws Exception {
        allSixDirectionsAndArbitraryAngles();
        overlappingJobsKeepTheirPacketAngles();
        nestedAndOrientationlessScopes();
        explicitLookAndCameraRestoration();
        expiryAndDisconnect();
        staleCleanupAndResetDuringScope();
        explicitCannotReviveAnOldUnscopedPacket();
        cancellationAndExceptions();
        legacyGlobalUnlock();
        boundedQueue();
        encoderAndSubmissionOnSeparateThreads();
        System.out.println("PASS pure-JDK ownership/snapshot regression: " + assertions + " assertions");
        System.out.println("LIMIT: no Minecraft classes, Mixin transformation, actual client ticks, Bedrock server or in-game placement verified");
    }

    private static RotationLeaseController controller(AtomicLong clock) {
        return new RotationLeaseController(512, 1_500_000_000L, clock::get);
    }

    private static void allSixDirectionsAndArbitraryAngles() {
        RotationLeaseController c = controller(new AtomicLong());
        float[][] values = {{0,0}, {90,0}, {180,0}, {270,0}, {0,-90}, {0,90},
            {-45, 44.999f}, {721.25f,-31.75f}, {-179.99f,45.001f}};
        for (float[] pair : values) {
            var token = c.begin(new RotationLeaseController.Angles(pair[0], pair[1]));
            AtomicReference<RotationPacketState> packet = new AtomicReference<>();
            require(c.runScoped(token, () -> packet.set(new RotationPacketState(c))), "phase executes");
            c.release(token);
            angles(packet.get(), pair[0], pair[1]);
        }
        require(c.pendingJobs() == 0, "all directional jobs released");
    }

    private static void overlappingJobsKeepTheirPacketAngles() {
        RotationLeaseController c = controller(new AtomicLong());
        var a = c.begin(new RotationLeaseController.Angles(0, 0));
        var packetA = new RotationPacketState(c);
        var b = c.begin(new RotationLeaseController.Angles(90, 90));
        var packetB = new RotationPacketState(c);
        angles(packetA, 0, 0);
        angles(packetB, 90, 90);
        c.runScoped(a, () -> {
            angles(new RotationPacketState(c), 0, 0);
            c.releaseCurrent();
        });
        require(c.isCurrent(b), "A cleanup preserves B");
        angles(new RotationPacketState(c), 90, 90);
        c.release(a);
        require(c.isCurrent(b), "A repeated cleanup preserves B");
        c.release(b);
        angles(packetA, 0, 0);
        angles(packetB, 90, 90);
        require(new RotationPacketState(c).capturedAngles() == null, "manual packet remains raw after jobs");
    }

    private static void nestedAndOrientationlessScopes() {
        RotationLeaseController c = controller(new AtomicLong());
        var a = c.begin(new RotationLeaseController.Angles(-90, 0));
        var b = c.begin(new RotationLeaseController.Angles(0, -90));
        var unrotated = c.begin(null);
        c.runScoped(a, () -> {
            angles(new RotationPacketState(c), -90, 0);
            c.runScoped(b, () -> angles(new RotationPacketState(c), 0, -90));
            angles(new RotationPacketState(c), -90, 0);
            c.runScoped(unrotated, () -> {
                require(new RotationPacketState(c).capturedAngles() == null,
                    "orientationless job masks unrelated default lock");
                c.releaseCurrent();
            });
            angles(new RotationPacketState(c), -90, 0);
        });
        require(c.isCurrent(a) && c.isCurrent(b), "orientationless cleanup releases neither rotated job");
        angles(new RotationPacketState(c), 0, -90);
        c.release(a);
        c.release(b);
    }

    private static void explicitLookAndCameraRestoration() {
        RotationLeaseController c = controller(new AtomicLong());
        var b = c.begin(new RotationLeaseController.Angles(90, 90));
        var extraInteractionLook = new RotationPacketState(c);
        extraInteractionLook.markExplicit(-90, 0);
        var restoreCamera = new RotationPacketState(c);
        restoreCamera.markExplicit(13.5f, -21.25f);
        c.releaseCurrent();
        require(c.isCurrent(b), "unscoped restore cannot release B");
        angles(extraInteractionLook, -90, 0);
        angles(restoreCamera, 13.5f, -21.25f);
        angles(new RotationPacketState(c), 90, 90);
        c.release(b);
        // With revert=false, this controller sends no camera LOOK. The actual
        // server's retained orientation still requires a genuine-runtime test.
        require(new RotationPacketState(c).capturedAngles() == null,
            "revert=false does not invent an unsolicited restoration packet");
    }

    private static void expiryAndDisconnect() {
        AtomicLong now = new AtomicLong();
        RotationLeaseController c = controller(now);
        var a = c.begin(new RotationLeaseController.Angles(180, 0));
        var old = new RotationPacketState(c);
        now.set(1_500_000_000L);
        require(c.defaultAngles() == null, "default lock expires at 1500ms");
        require(new RotationPacketState(c).capturedAngles() == null, "new manual packet remains raw after expiry");
        angles(old, 180, 0);
        require(c.runScoped(a, () -> angles(new RotationPacketState(c), 180, 0)),
            "lagged but owned second phase still valid before reset");
        var explicit = new RotationPacketState(c);
        explicit.markExplicit(45, 0);
        c.reset();
        require(old.capturedAngles() == null && explicit.capturedAngles() == null,
            "disconnect invalidates queued normal and explicit snapshots");
        require(!c.runScoped(a, () -> { throw new AssertionError("stale callback ran"); }),
            "old callback cannot run in next generation");
    }

    private static void staleCleanupAndResetDuringScope() {
        RotationLeaseController c = controller(new AtomicLong());
        var a = c.begin(new RotationLeaseController.Angles(0, 90));
        c.runScoped(a, () -> {
            c.reset();
            var b = c.begin(new RotationLeaseController.Angles(180, 0));
            require(new RotationPacketState(c).capturedAngles() == null,
                "stale executing scope cannot inherit new connection B");
            var staleExplicit = new RotationPacketState(c);
            staleExplicit.markExplicit(0, 90);
            require(staleExplicit.capturedAngles() == null,
                "stale explicit LOOK cannot promote itself into new connection generation");
            c.releaseCurrent();
            c.release(a);
            require(c.isCurrent(b), "stale current and explicit cleanup preserve new generation B");
        });
        angles(new RotationPacketState(c), 180, 0);
    }

    private static void cancellationAndExceptions() {
        RotationLeaseController c = controller(new AtomicLong());
        var a = c.begin(new RotationLeaseController.Angles(33, 44));
        var b = c.begin(new RotationLeaseController.Angles(66, 77));
        try {
            c.runScoped(a, () -> { throw new IllegalStateException("expected"); });
            throw new AssertionError("exception swallowed");
        } catch (IllegalStateException expected) {
            require("expected".equals(expected.getMessage()), "exception propagated");
        } finally {
            c.release(a);
        }
        angles(new RotationPacketState(c), 66, 77);
        require(c.isCurrent(b), "exception releases no unrelated lock");
        c.release(b);
        require(!c.runScoped(b, () -> { throw new AssertionError("canceled job ran"); }),
            "canceled callback skipped");
        try {
            new RotationLeaseController.Angles(Float.NaN, 0);
            throw new AssertionError("NaN accepted");
        } catch (IllegalArgumentException expected) {
            assertions++;
        }
    }

    private static void explicitCannotReviveAnOldUnscopedPacket() {
        RotationLeaseController c = controller(new AtomicLong());
        var originallyRaw = new RotationPacketState(c);
        var owner = c.begin(new RotationLeaseController.Angles(90, 0));
        var originallyLocked = new RotationPacketState(c);
        c.release(owner);
        c.reset();
        originallyRaw.markExplicit(20, 30);
        originallyLocked.markExplicit(40, 50);
        require(originallyRaw.capturedAngles() == null,
            "explicit tagging cannot revive old unscoped packet with originally null snapshot");
        require(originallyLocked.capturedAngles() == null,
            "explicit tagging cannot revive old unscoped packet with originally locked snapshot");
        var newPacket = new RotationPacketState(c);
        newPacket.markExplicit(60, 70);
        angles(newPacket, 60, 70);
    }

    private static void boundedQueue() {
        RotationLeaseController c = controller(new AtomicLong());
        RotationLeaseController.Token[] tokens = new RotationLeaseController.Token[512];
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = c.begin(new RotationLeaseController.Angles(i, 0));
            require(tokens[i] != null, "job within bound accepted");
        }
        require(c.begin(null) == null, "513th job rejected");
        require(c.pendingJobs() == 512, "bound stays 512");
        c.release(tokens[0]);
        require(c.begin(null) != null, "release restores admission");
        c.reset();
        require(c.pendingJobs() == 0, "reset frees bounded queue");
    }

    private static void legacyGlobalUnlock() {
        RotationLeaseController c = controller(new AtomicLong());
        var a = c.begin(new RotationLeaseController.Angles(40, 30));
        var packet = new RotationPacketState(c);
        c.clearRotations();
        require(c.isCurrent(a), "global unlock does not discard admitted job");
        require(c.defaultAngles() == null, "global unlock releases default rotation");
        c.runScoped(a, () -> require(new RotationPacketState(c).capturedAngles() == null,
            "global unlock releases scoped rotation too"));
        angles(packet, 40, 30);
        c.release(a);
    }

    private static void encoderAndSubmissionOnSeparateThreads() throws Exception {
        RotationLeaseController c = controller(new AtomicLong());
        var a = c.begin(new RotationLeaseController.Angles(12, 34));
        var packetA = new RotationPacketState(c);
        CountDownLatch start = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread encoder = new Thread(() -> {
            try {
                start.await();
                for (int i = 0; i < 20_000; i++) {
                    var angles = packetA.capturedAngles();
                    if (angles == null || angles.yaw() != 12 || angles.pitch() != 34) {
                        throw new AssertionError("angle pair changed during submission");
                    }
                }
            } catch (Throwable problem) {
                failure.set(problem);
            }
        }, "simulated-encoder");
        encoder.start();
        start.countDown();
        for (int i = 0; i < 20_000; i++) {
            var b = c.begin(new RotationLeaseController.Angles(i, -i));
            c.release(b);
        }
        encoder.join();
        if (failure.get() != null) {
            throw new AssertionError("encoder thread failed", failure.get());
        }
        c.release(a);
        angles(packetA, 12, 34);
        require(true, "packet immutable under concurrent submissions");
    }

    private static void angles(RotationPacketState state, float yaw, float pitch) {
        var angles = state.capturedAngles();
        require(angles != null && angles.yaw() == yaw && angles.pitch() == pitch,
            "expected angles " + yaw + "," + pitch + " got " + angles);
    }

    private static void require(boolean value, String message) {
        assertions++;
        if (!value) throw new AssertionError(message);
    }
}
