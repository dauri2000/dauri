package org.uiop.easyplacefix.rotation;

import java.util.HashSet;
import java.util.Set;

/** Pure lifecycle tests. Helper state here is a model, not Minecraft classes. */
public final class ClientContextTrackerTest {
    private static int assertions;

    public static void main(String[] args) {
        replacementCancelsOldWorkAndKeepsFirstNewWork();
        sameContextKeepsPendingWork();
        playerAndWorldIdentityBothMatter();
        disconnectAndShutdownResetEvenEmptyContext();
        resetMustCompleteBeforePublishingContext();
        System.out.println("PASS pure-JDK context/admission regression: " + assertions + " assertions");
        System.out.println("LIMIT: helper flags/queues are explicit models; actual NoteBlockHelper/TerrainAutoReplace, Minecraft lifecycle and Mixin integration unverified");
    }

    private static final class Harness {
        final RotationLeaseController rotations = new RotationLeaseController(512, 1_500_000_000L, () -> 0L);
        final Set<String> pending = new HashSet<>();
        final Set<String> noteJobs = new HashSet<>();
        final Set<String> terrainClearing = new HashSet<>();
        boolean pumpScheduled;
        int resets;
        final ClientContextTracker contexts = new ClientContextTracker(() -> {
            pending.clear();
            rotations.reset();
            noteJobs.clear();
            pumpScheduled = false;
            terrainClearing.clear();
            resets++;
        });

        void prepareAndTune(Object player, Object level, String job) {
            contexts.observe(player, level);
            noteJobs.add(job);
            pumpScheduled = true;
            // Scheduler observes again after tune has set its scheduling flag.
            contexts.observe(player, level);
            pending.add(job);
        }

        void prepareAndClearTerrain(Object player, Object level, String job) {
            contexts.observe(player, level);
            terrainClearing.add(job);
            contexts.observe(player, level);
            pending.add(job);
        }
    }

    private static void replacementCancelsOldWorkAndKeepsFirstNewWork() {
        Harness h = new Harness();
        Object player = new Object(), worldA = new Object(), worldB = new Object();
        h.prepareAndTune(player, worldA, "old-note");
        h.prepareAndClearTerrain(player, worldA, "old-terrain");
        var oldOwner = h.rotations.begin(new RotationLeaseController.Angles(0, 90));
        var oldPacket = new RotationPacketState(h.rotations);
        h.prepareAndTune(player, worldB, "new-note");
        require(!h.rotations.isCurrent(oldOwner), "old phase cannot run after world replacement");
        require(oldPacket.capturedAngles() == null, "world replacement invalidates old frozen override");
        require(h.pending.equals(Set.of("new-note")), "only new-world callback remains admitted");
        require(h.noteJobs.equals(Set.of("new-note")) && h.pumpScheduled,
            "first new-world note job and its pump survive scheduler observation");
        require(h.terrainClearing.isEmpty(), "canceled terrain callback cannot leave old clearing marker");
        h.prepareAndClearTerrain(player, worldB, "new-terrain");
        require(h.terrainClearing.equals(Set.of("new-terrain")), "terrain may start again in new world");
        require(h.noteJobs.contains("new-note") && h.pumpScheduled, "same-context terrain admission preserves note pump");
    }

    private static void sameContextKeepsPendingWork() {
        Harness h = new Harness();
        Object player = new Object(), world = new Object();
        h.prepareAndTune(player, world, "note");
        h.prepareAndClearTerrain(player, world, "terrain");
        var owner = h.rotations.begin(new RotationLeaseController.Angles(90, 0));
        int before = h.resets;
        for (int i = 0; i < 10; i++) h.contexts.observe(player, world);
        require(h.resets == before, "ordinary repeated context observations perform no cleanup");
        require(h.rotations.isCurrent(owner), "normal tick/packet observations preserve admitted phase");
        require(h.pending.size() == 2 && h.noteJobs.contains("note") && h.pumpScheduled
            && h.terrainClearing.contains("terrain"), "normal observations preserve all helper state");
    }

    private static void playerAndWorldIdentityBothMatter() {
        Harness h = new Harness();
        Object playerA = new String("equal-player"), playerB = new String("equal-player");
        Object worldA = new String("equal-world"), worldB = new String("equal-world");
        h.prepareAndTune(playerA, worldA, "before-respawn");
        h.contexts.observe(playerB, worldA);
        require(h.noteJobs.isEmpty() && !h.pumpScheduled && h.pending.isEmpty(),
            "replacement player identity cancels old work even with equal value");
        h.prepareAndClearTerrain(playerB, worldA, "before-world-change");
        h.contexts.observe(playerB, worldB);
        require(h.terrainClearing.isEmpty() && h.pending.isEmpty(),
            "replacement world identity clears pending terrain state even with equal value");
    }

    private static void disconnectAndShutdownResetEvenEmptyContext() {
        Harness h = new Harness();
        Object player = new Object(), world = new Object();
        h.prepareAndTune(player, world, "old");
        h.prepareAndClearTerrain(player, world, "old-terrain");
        h.contexts.reset();
        require(h.contexts.current().player() == null && h.contexts.current().level() == null,
            "disconnect releases player/world references");
        require(h.pending.isEmpty() && h.noteJobs.isEmpty() && !h.pumpScheduled
            && h.terrainClearing.isEmpty(), "disconnect clears helpers together with callbacks");
        var packet = new RotationPacketState(h.rotations);
        packet.markExplicit(30, 0);
        h.contexts.reset();
        require(packet.capturedAngles() == null, "shutdown/reset invalidates explicit packet even in empty context");
        h.prepareAndTune(new Object(), new Object(), "reconnected");
        require(h.noteJobs.contains("reconnected") && h.pumpScheduled, "reconnect admits first new helper job");
    }

    private static void resetMustCompleteBeforePublishingContext() {
        boolean[] rejectReset = {false};
        ClientContextTracker tracker = new ClientContextTracker(() -> {
            if (rejectReset[0]) throw new IllegalStateException("cleanup failed");
        });
        Object player = new Object(), worldA = new Object(), worldB = new Object();
        tracker.observe(player, worldA);
        rejectReset[0] = true;
        try {
            tracker.observe(player, worldB);
            throw new AssertionError("cleanup failure swallowed");
        } catch (IllegalStateException expected) {
            require(tracker.current().level() == worldA, "failed cleanup does not publish a replacement context");
        }
        rejectReset[0] = false;
        tracker.observe(player, worldB);
        require(tracker.current().level() == worldB, "successful retry publishes replacement after cleanup");
    }

    private static void require(boolean condition, String message) {
        assertions++;
        if (!condition) throw new AssertionError(message);
    }
}
