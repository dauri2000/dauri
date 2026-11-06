package org.uiop.easyplacefix.rotation;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.LongSupplier;

/**
 * Owns placement rotations without sharing two separately mutable angle fields.
 * This class deliberately has no Minecraft, Mixin, packet or clock dependency.
 */
public final class RotationLeaseController {
    public record Angles(float yaw, float pitch) {
        public Angles {
            if (!Float.isFinite(yaw) || !Float.isFinite(pitch)) {
                throw new IllegalArgumentException("Rotation must be finite");
            }
        }
    }

    public record Token(long generation, long id) {}

    /** A packet snapshot outlives its lease, but never a connection generation. */
    public record Snapshot(long generation, Angles angles) {
        public Snapshot {
            Objects.requireNonNull(angles, "angles");
        }
    }

    /** The packet epoch exists even when there is no active angle override. */
    public record PacketCapture(long generation, Snapshot snapshot) {}

    private record Lease(Angles angles, long expiresAt) {}

    private final int capacity;
    private final long defaultLockNanos;
    private final LongSupplier clock;
    private final LinkedHashMap<Token, Lease> jobs = new LinkedHashMap<>();
    private final ThreadLocal<ArrayDeque<Token>> scopes =
        ThreadLocal.withInitial(ArrayDeque::new);
    private long generation;
    private long nextId;

    public RotationLeaseController(int capacity, long defaultLockNanos, LongSupplier clock) {
        if (capacity < 1 || defaultLockNanos < 1) {
            throw new IllegalArgumentException("Positive capacity and lifetime required");
        }
        this.capacity = capacity;
        this.defaultLockNanos = defaultLockNanos;
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /** Null angles represent a job that must not inherit another job's rotation. */
    public synchronized Token begin(Angles angles) {
        if (jobs.size() >= capacity) {
            return null;
        }
        Token token = new Token(generation, ++nextId);
        jobs.put(token, new Lease(angles, clock.getAsLong() + defaultLockNanos));
        return token;
    }

    public synchronized boolean isCurrent(Token token) {
        return token != null && token.generation() == generation && jobs.containsKey(token);
    }

    public synchronized int pendingJobs() {
        return jobs.size();
    }

    public Token currentToken() {
        ArrayDeque<Token> stack=scopes.get();
        if (stack.isEmpty()) { scopes.remove(); return null; }
        Token owner=stack.peek();
        return isCurrent(owner) ? owner : null;
    }

    public synchronized long generation() {
        return generation;
    }

    public synchronized boolean isGeneration(long candidate) {
        return candidate == generation;
    }

    /**
     * A scope is thread-local and nested. An orientationless or stale inner scope
     * masks, rather than accidentally inheriting, the latest unrelated lease.
     */
    public boolean runScoped(Token token, Runnable task) {
        Objects.requireNonNull(task, "task");
        synchronized (this) {
            if (!isCurrent(token)) {
                return false;
            }
        }
        ArrayDeque<Token> stack = scopes.get();
        stack.push(token);
        try {
            task.run();
            return true;
        } finally {
            if (stack.pop() != token) {
                throw new IllegalStateException("Unbalanced rotation scope");
            }
            if (stack.isEmpty()) {
                scopes.remove();
            }
        }
    }

    /** Completing A never releases B, even when B was submitted more recently. */
    public synchronized void release(Token token) {
        if (token != null && token.generation() == generation) {
            jobs.remove(token);
        }
    }

    /** Used by camera restoration inside a job; unscoped work cannot clear B. */
    public void releaseCurrent() {
        ArrayDeque<Token> stack = scopes.get();
        if (!stack.isEmpty()) {
            release(stack.peek());
        } else {
            scopes.remove();
        }
    }

    public synchronized Snapshot capture() {
        ArrayDeque<Token> stack = scopes.get();
        if (!stack.isEmpty()) {
            Token owner = stack.peek();
            Lease lease = owner.generation() == generation ? jobs.get(owner) : null;
            return lease == null || lease.angles() == null ? null
                : new Snapshot(generation, lease.angles());
        }
        scopes.remove();
        Angles angles = defaultAngles();
        return angles == null ? null : new Snapshot(generation, angles);
    }

    public synchronized PacketCapture capturePacket() {
        return new PacketCapture(generation, capture());
    }

    /** Capture only a live default lock; the angle pair is returned atomically. */
    public synchronized Angles defaultAngles() {
        long now = clock.getAsLong();
        Angles latest = null;
        for (Lease lease : jobs.values()) {
            if (lease.angles() != null && now - lease.expiresAt() < 0) {
                latest = lease.angles();
            }
        }
        return latest;
    }

    /** An explicit LOOK or camera restoration never inherits another owner. */
    public synchronized Snapshot explicit(long packetGeneration, float yaw, float pitch) {
        if (packetGeneration != generation) return null;
        ArrayDeque<Token> stack = scopes.get();
        if (!stack.isEmpty() && stack.peek().generation() != generation) {
            return null;
        }
        if (stack.isEmpty()) scopes.remove();
        return new Snapshot(packetGeneration, new Angles(yaw, pitch));
    }

    public synchronized boolean isCurrent(Snapshot snapshot) {
        return snapshot != null && snapshot.generation() == generation;
    }

    /** Legacy global unlock: queued jobs remain admitted, but inherit no lock. */
    public synchronized void clearRotations() {
        jobs.replaceAll((token, lease) -> new Lease(null, lease.expiresAt()));
    }

    /** Clear jobs and invalidate queued packet snapshots and stale callbacks. */
    public synchronized void reset() {
        jobs.clear();
        generation++;
    }
}
