package org.uiop.easyplacefix.rotation;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

/** Admission and owner-specific rollback; no scheduling, ticking or Minecraft API. */
public final class TaskAdmission {
    private TaskAdmission() {}

    /** Caller holds its queue lock throughout this call, including enqueue. */
    public static boolean tryBounded(int size, int capacity, Runnable enqueue) {
        if (size < 0 || capacity < 1) throw new IllegalArgumentException("Invalid queue bounds");
        Objects.requireNonNull(enqueue, "enqueue");
        if (size >= capacity) return false;
        enqueue.run();
        return true;
    }

    /** Undo only a reservation made by this submission, including thrown rejection. */
    public static boolean trySubmit(BooleanSupplier submission, Runnable rollback) {
        Objects.requireNonNull(submission, "submission");
        Objects.requireNonNull(rollback, "rollback");
        boolean accepted = false;
        try {
            accepted = submission.getAsBoolean();
            return accepted;
        } finally {
            if (!accepted) rollback.run();
        }
    }

    /** Identity token intentionally has Object identity, not value equality. */
    public static final class Reservation {
        private Reservation() {}
    }

    public static final class Reservations<K> {
        private final ConcurrentHashMap<K, Reservation> owners = new ConcurrentHashMap<>();

        public Reservation acquire(K key) {
            Reservation reservation = new Reservation();
            return owners.putIfAbsent(key, reservation) == null ? reservation : null;
        }

        public boolean owns(K key, Reservation reservation) {
            return reservation != null && owners.get(key) == reservation;
        }

        public boolean release(K key, Reservation reservation) {
            return reservation != null && owners.remove(key, reservation);
        }

        public void clear() {
            owners.clear();
        }
    }
}
