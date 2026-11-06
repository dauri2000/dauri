package com.tick_ins.tick;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.uiop.easyplacefix.EasyPlaceFix;
import org.uiop.easyplacefix.rotation.ClientContextTracker;
import org.uiop.easyplacefix.rotation.RotationLeaseController;
import org.uiop.easyplacefix.rotation.RotationPacketState;
import org.uiop.easyplacefix.rotation.TaskAdmission;
import org.uiop.easyplacefix.placement.NativeEasyPlaceAction;
import org.uiop.easyplacefix.placement.NativePlacementScope;
import org.uiop.easyplacefix.placement.NativeActionQueue;
import org.uiop.easyplacefix.util.NoteBlockHelper;
import org.uiop.easyplacefix.util.TerrainAutoReplace;
import oshi.util.tuples.Pair;

/** Private candidate source; must be compiled and transformed with genuine MC26.3. */
public final class TickThread {
    private static final int MAX_PENDING_TASKS = 512;
    private static final long LOOK_LOCK_MAX_MS = 1500L;
    private static final Object LOCK = new Object();
    private static final List<ScheduledTask> PENDING = new ArrayList<>();
    private static final RotationLeaseController ROTATIONS = new RotationLeaseController(
        MAX_PENDING_TASKS, LOOK_LOCK_MAX_MS * 1_000_000L, System::nanoTime);
    private static long currentTick;
    private static volatile boolean clientStopping;
    private static volatile long lastNativeActionTick=Long.MIN_VALUE;
    private static final NativeActionQueue NATIVE = new NativeActionQueue(MAX_PENDING_TASKS,
        TickThread::currentTick, error -> EasyPlaceFix.LOGGER.error("Native EasyPlace task failed",error));
    private static final ClientContextTracker CONTEXTS = new ClientContextTracker(() -> {
        PENDING.clear();
        ROTATIONS.reset();
        lastNativeActionTick=Long.MIN_VALUE;
        NATIVE.clear();
        NoteBlockHelper.clear();
        TerrainAutoReplace.clear();
        NativeEasyPlaceAction.clearPreparedInputs();
        refreshLegacyLock();
    });

    // Preserve the public API for existing integrations. These are compatibility
    // mirrors only; packet encoding uses a single immutable angle snapshot.
    public static volatile boolean notChangPlayerLook;
    public static volatile float yawLock;
    public static volatile float pitchLock;

    private TickThread() {}

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(TickThread::onClientTick);
    }

    public static long currentTick() {
        synchronized (LOCK) {
            return currentTick;
        }
    }

    public static int pendingTaskCount() {
        synchronized (LOCK) {
            return PENDING.size()+NATIVE.size();
        }
    }

    public static void addTask(RunnableWithLast first, RunnableWithLast second) {
        if (NativePlacementScope.isBedrockConnection()) {
            tryAddNativeTask(first,second);
            return;
        }
        submitPhases(first == null ? null : first.yawAndPitch(),
            first == null ? null : first.task(), second == null ? null : second.task());
    }

    public static void addLastTask(RunnableWithLast task) {
        if (task != null) {
            if (NativePlacementScope.isBedrockConnection()) {
                submitNative(task.yawAndPitch(),task.task(),task.cache());
                return;
            }
            submitPhases(task.yawAndPitch(), task.task(), task.cache());
        }
    }

    public static boolean tryAddNativeTask(RunnableWithLast first, RunnableWithLast second) {
        return tryAddNativeTask(first,second,() -> true);
    }

    public static boolean tryAddNativeTask(RunnableWithLast first, RunnableWithLast second, BooleanSupplier relevant) {
        return submitNative(first==null ? null : first.yawAndPitch(),first==null ? null : first.task(),
            second==null ? null : second.task(),relevant);
    }

    private static boolean submitNative(Pair<Float,Float> yawAndPitch, Runnable first, Runnable second) {
        return submitNative(yawAndPitch,first,second,() -> true);
    }

    private static boolean submitNative(Pair<Float,Float> yawAndPitch, Runnable first, Runnable second, BooleanSupplier relevant) {
        Minecraft client=Minecraft.getInstance();
        if (client==null || !client.isSameThread()) return false;
        ClientContext context=observeContext(client);
        if (!isUsable(client,context)) return false;
        Object schematicWorld=SchematicWorldHandler.getSchematicWorld();
        RotationLeaseController.Angles angles=yawAndPitch==null ? null
            : new RotationLeaseController.Angles(yawAndPitch.getA(),yawAndPitch.getB());
        AtomicReference<RotationLeaseController.Token> owner=new AtomicReference<>();
        return NATIVE.submit(() -> {
            if (!isUsable(client,context) || !NativeEasyPlaceAction.settingsEnabled()
                || SchematicWorldHandler.getSchematicWorld()!=schematicWorld || !relevant.getAsBoolean()) return false;
            // Waiting entries have no lease, movement override or expiry. The
            // active entry alone prepares the next genuine auth interval.
            RotationLeaseController.Token token=ROTATIONS.begin(angles);
            if (token==null) return false;
            owner.set(token);
            lastNativeActionTick=currentTick();
            refreshLegacyLock();
            return ROTATIONS.runScoped(token,() -> { if (first!=null) first.run(); })
                && ROTATIONS.isCurrent(token) && isUsable(client,context);
        },() -> {
            RotationLeaseController.Token token=owner.get();
            if (isUsable(client,context)) {
                lastNativeActionTick=currentTick();
                if (NativeEasyPlaceAction.settingsEnabled()
                    && SchematicWorldHandler.getSchematicWorld()==schematicWorld && relevant.getAsBoolean())
                    ROTATIONS.runScoped(token,() -> { if (second!=null) second.run(); });
            }
        },() -> {
            RotationLeaseController.Token token=owner.getAndSet(null);
            if (token==null) return;
            try {
                if (ROTATIONS.isCurrent(token) && isUsable(client,context))
                    ROTATIONS.runScoped(token,() -> NativeEasyPlaceAction.finishInputs(client,true));
            } finally {
                release(token);
            }
        });
    }

    private static void submitPhases(Pair<Float, Float> yawAndPitch, Runnable first, Runnable second) {
        Minecraft client = Minecraft.getInstance();
        ClientContext context = observeContext(client);
        if (!isUsable(client, context)) return;
        RotationLeaseController.Angles angles = yawAndPitch == null ? null
            : new RotationLeaseController.Angles(yawAndPitch.getA(), yawAndPitch.getB());
        RotationLeaseController.Token owner = ROTATIONS.begin(angles);
        if (owner == null) {
            EasyPlaceFix.LOGGER.warn("Discarding EasyPlaceFix placement because the bounded queue is full ({})", MAX_PENDING_TASKS);
            return;
        }
        refreshLegacyLock();
        boolean accepted = runNow(client, context, () -> {
            if (!isUsable(client, context) || !ROTATIONS.isCurrent(owner)) {
                release(owner);
                return;
            }
            boolean scheduled = false;
            try {
                if (!ROTATIONS.runScoped(owner, () -> {
                    if (first != null) first.run();
                })) return;
                if (!ROTATIONS.isCurrent(owner)) return;
                // Preserve the existing one-tick split, measured from execution
                // of the first phase rather than submission to a busy executor.
                scheduled = runAfterTick(client, context, () -> {
                    try {
                        if (isUsable(client, context)) {
                            ROTATIONS.runScoped(owner, () -> {
                                if (second != null) second.run();
                            });
                        }
                    } finally {
                        release(owner);
                    }
                }, 1);
            } finally {
                if (!scheduled) release(owner);
            }
        });
        if (!accepted) release(owner);
    }

    public static void addCountDownTask(RunnableWithCountDown task) {
        scheduleCountDownTask(task, true);
    }

    /** Admission result for helpers that must undo an unqueued reservation. */
    public static boolean tryAddCountDownTask(RunnableWithCountDown task) {
        return scheduleCountDownTask(task, false);
    }

    private static boolean scheduleCountDownTask(RunnableWithCountDown task, boolean warnOnFull) {
        if (task == null || clientStopping) return false;
        Minecraft client = Minecraft.getInstance();
        ClientContext context = observeContext(client);
        return runAfterTick(client, context, task.task(), task.count(), warnOnFull);
    }

    private static boolean runNow(Minecraft client, ClientContext context, Runnable runnable) {
        if (runnable == null || !isUsable(client, context)) return false;
        try {
            client.execute(() -> {
                if (isUsable(client, context)) runnable.run();
            });
            return true;
        } catch (RejectedExecutionException error) {
            EasyPlaceFix.LOGGER.debug("Minecraft rejected an EasyPlaceFix task during shutdown", error);
            return false;
        }
    }

    private static boolean runAfterTick(Minecraft client, ClientContext context, Runnable runnable, int ticks) {
        return runAfterTick(client, context, runnable, ticks, true);
    }

    private static boolean runAfterTick(Minecraft client, ClientContext context, Runnable runnable, int ticks, boolean warnOnFull) {
        if (runnable == null || !isUsable(client, context)) return false;
        if (ticks <= 0) return runNow(client, context, runnable);
        synchronized (LOCK) {
            if (!isUsable(client, context)) return false;
            if (!TaskAdmission.tryBounded(PENDING.size(), MAX_PENDING_TASKS,
                () -> PENDING.add(new ScheduledTask(currentTick + ticks, context, runnable)))) {
                if (warnOnFull) {
                    EasyPlaceFix.LOGGER.warn("Discarding EasyPlaceFix delayed task because the bounded queue is full ({})", MAX_PENDING_TASKS);
                }
                return false;
            }
            return true;
        }
    }

    private static void onClientTick(Minecraft client) {
        observeContext(client);
        List<ScheduledTask> due = new ArrayList<>();
        synchronized (LOCK) {
            currentTick++;
            Iterator<ScheduledTask> iterator = PENDING.iterator();
            while (iterator.hasNext()) {
                ScheduledTask task = iterator.next();
                if (task.dueTick() <= currentTick) {
                    iterator.remove();
                    due.add(task);
                }
            }
        }
        // This END occurs after Minecraft's genuine CLIENT_TICK_END packet.
        // Drain native A before generic callbacks can start another native job;
        // B prepares only after A's USE/cleanup, for the following auth interval.
        NATIVE.onTickEnd(currentTick());
        for (ScheduledTask task : due) {
            if (!isUsable(client, task.context())) continue;
            try {
                task.runnable().run();
            } catch (RuntimeException error) {
                EasyPlaceFix.LOGGER.error("EasyPlaceFix delayed task failed", error);
            }
        }
        // A rejected note pump retains valid queued tuning jobs. Retry once
        // after draining due work; successful admission still waits two ticks.
        if (client != null && client.player != null && client.level != null && !clientStopping) {
            NoteBlockHelper.retryPending(client);
        }
        refreshLegacyLock();
    }

    /** Called at packet construction, never from its network encoder. */
    public static RotationPacketState newRotationPacketState() {
        observeContext(Minecraft.getInstance());
        return new RotationPacketState(ROTATIONS);
    }

    /** Same main-thread observation guard used by movement-packet creation. */
    public static void refreshPlacementContext() {
        observeContext(Minecraft.getInstance());
    }

    public static boolean isLookLocked() {
        observeContext(Minecraft.getInstance());
        refreshLegacyLock();
        return notChangPlayerLook;
    }

    /** Existing global API; frozen packets and queued jobs retain their identity. */
    public static void clearLookLock() {
        ROTATIONS.clearRotations();
        refreshLegacyLock();
    }

    /** A camera restoration belonging to A must never clear another job B. */
    public static void releaseCurrentLookLock() {
        ROTATIONS.releaseCurrent();
        refreshLegacyLock();
    }

    public static RotationLeaseController.Token currentRotationOwner() { return ROTATIONS.currentToken(); }
    public static boolean isCurrentRotationOwner(RotationLeaseController.Token owner) { return ROTATIONS.isCurrent(owner); }
    public static RotationLeaseController.Angles pendingRotation() { return ROTATIONS.defaultAngles(); }
    public static boolean nativePlacementBusy() {
        return NATIVE.size()!=0 || lastNativeActionTick==currentTick();
    }

    /** Helpers call this before adding their own jobs or scheduling flags. */
    public static boolean prepareClientContext(Minecraft client) {
        return client != null && client.isSameThread()
            && isUsable(client, observeContext(client));
    }

    private static void release(RotationLeaseController.Token owner) {
        ROTATIONS.release(owner);
        refreshLegacyLock();
    }

    private static void refreshLegacyLock() {
        RotationLeaseController.Angles angles = ROTATIONS.defaultAngles();
        if (angles != null) {
            yawLock = angles.yaw();
            pitchLock = angles.pitch();
        }
        notChangPlayerLook = angles != null;
    }

    private static ClientContext observeContext(Minecraft client) {
        synchronized (LOCK) {
            // Packet constructors can be called by another mod's worker. Such
            // a caller may capture the existing epoch, but must not replace
            // lifecycle state using an off-thread view of player/world fields.
            if (client != null && !client.isSameThread()) {
                ClientContextTracker.Context current = CONTEXTS.current();
                return new ClientContext(current.player(), current.level(), ROTATIONS.generation());
            }
            Object player = client == null ? null : client.player;
            Object level = client == null ? null : client.level;
            CONTEXTS.observe(player, level);
            return new ClientContext(player, level, ROTATIONS.generation());
        }
    }

    private static boolean isUsable(Minecraft client, ClientContext context) {
        if (client != null && context != null
            && (client.player != context.player() || client.level != context.level())) {
            observeContext(client);
            return false;
        }
        return !clientStopping && client != null && context != null
            && context.player() != null && context.level() != null
            && client.player == context.player() && client.level == context.level()
            && ROTATIONS.isGeneration(context.generation());
    }

    private static void clearPendingAndReset() {
        synchronized (LOCK) {
            CONTEXTS.reset();
        }
    }

    public static void onClientDisconnected() {
        clearPendingAndReset();
        clientStopping = false;
    }

    public static void onClientShutdown() {
        clientStopping = true;
        clearPendingAndReset();
    }

    private record ClientContext(Object player, Object level, long generation) {}
    private record ScheduledTask(long dueTick, ClientContext context, Runnable runnable) {}
}
