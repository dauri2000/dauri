package org.uiop.easyplacefix.placement;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

/**
 * Main-thread FIFO of prepare/use actions. Waiting entries execute no callbacks
 * and own no game inputs. The caller supplies real tick observations only.
 */
public final class NativeActionQueue {
    private enum Phase { FIRST, PREPARED, SECOND, CLEANUP, DONE }
    private static final class Entry {
        final BooleanSupplier first;
        final Runnable second;
        final Runnable cleanup;
        Phase phase = Phase.FIRST;
        boolean running;
        boolean cancelled;
        boolean cleanupQueued;
        boolean cleaned;
        long dueTick;
        long preparedAtEnd;

        Entry(BooleanSupplier first, Runnable second, Runnable cleanup) {
            this.first=first; this.second=second; this.cleanup=cleanup;
        }
    }
    private record Work(Entry entry, Phase phase) {}

    private final Object lock = new Object();
    private final int capacity;
    private final LongSupplier currentTick;
    private final Consumer<RuntimeException> errors;
    private final ArrayDeque<Entry> waiting = new ArrayDeque<>();
    private final ArrayDeque<Entry> cleanups = new ArrayDeque<>();
    private Entry active;
    private boolean driving;
    private boolean observedEnd;
    private long latestTick;
    private long endSequence;

    public NativeActionQueue(int capacity, LongSupplier currentTick,
                             Consumer<RuntimeException> errors) {
        if (capacity < 1) throw new IllegalArgumentException("Positive capacity required");
        this.capacity=capacity;
        this.currentTick=Objects.requireNonNull(currentTick,"currentTick");
        this.errors=Objects.requireNonNull(errors,"errors");
    }

    /** False rejects ownership: no callback, including cleanup, is then run. */
    public boolean submit(BooleanSupplier first, Runnable second, Runnable cleanup) {
        Objects.requireNonNull(first,"first");
        Objects.requireNonNull(second,"second");
        Objects.requireNonNull(cleanup,"cleanup");
        synchronized (lock) {
            if (waiting.size()+(active==null ? 0 : 1)>=capacity) return false;
            waiting.addLast(new Entry(first,second,cleanup));
        }
        drive();
        return true;
    }

    /** Call before the generic delayed callbacks for this actual END tick. */
    public void onTickEnd(long tick) {
        synchronized (lock) {
            if (observedEnd && tick<=latestTick) return;
            observedEnd=true;
            latestTick=tick;
            endSequence++;
        }
        drive();
    }

    /** Accepted cancelled entries clean up once, including entries not yet started. */
    public void clear() {
        synchronized (lock) {
            Entry previous=active;
            active=null;
            if (previous!=null) {
                previous.cancelled=true;
                queueCleanup(previous);
            }
            while (!waiting.isEmpty()) {
                Entry entry=waiting.removeFirst();
                entry.cancelled=true;
                queueCleanup(entry);
            }
        }
        drive();
    }

    public int size() {
        synchronized (lock) {
            return waiting.size()+(active==null ? 0 : 1);
        }
    }

    private void queueCleanup(Entry entry) {
        if (!entry.running && !entry.cleanupQueued && !entry.cleaned) {
            entry.phase=Phase.CLEANUP;
            entry.cleanupQueued=true;
            cleanups.addLast(entry);
        }
    }

    /** Claims state under the lock; all supplied callbacks run after release. */
    private Work nextWork() {
        synchronized (lock) {
            if (!cleanups.isEmpty()) {
                Entry entry=cleanups.removeFirst();
                entry.cleanupQueued=false;
                entry.running=true;
                return new Work(entry,Phase.CLEANUP);
            }
            if (active==null && !waiting.isEmpty()) {
                active=waiting.removeFirst();
                active.running=true;
                return new Work(active,Phase.FIRST);
            }
            if (active!=null && active.phase==Phase.PREPARED && observedEnd
                && active.preparedAtEnd<endSequence && active.dueTick<=latestTick) {
                active.phase=Phase.SECOND;
                active.running=true;
                return new Work(active,Phase.SECOND);
            }
            return null;
        }
    }

    private void drive() {
        synchronized (lock) {
            if (driving) return;
            driving=true;
        }
        try {
            Work work;
            while ((work=nextWork())!=null) execute(work);
        } finally {
            synchronized (lock) { driving=false; }
        }
    }

    private void execute(Work work) {
        Entry entry=work.entry;
        if (work.phase==Phase.CLEANUP) {
            try {
                entry.cleanup.run();
            } catch (RuntimeException error) {
                report(error);
            } finally {
                synchronized (lock) {
                    entry.running=false;
                    entry.cleaned=true;
                    entry.phase=Phase.DONE;
                    if (active==entry) active=null;
                }
            }
            return;
        }
        boolean prepared=false;
        long due=0;
        Error fatal=null;
        try {
            if (work.phase==Phase.FIRST) {
                prepared=entry.first.getAsBoolean();
                if (prepared) due=Math.addExact(currentTick.getAsLong(),1L);
            } else {
                entry.second.run();
            }
        } catch (RuntimeException error) {
            prepared=false;
            report(error);
        } catch (Error error) {
            fatal=error;
        } finally {
            synchronized (lock) {
                entry.running=false;
                if (work.phase==Phase.FIRST && prepared && fatal==null
                    && !entry.cancelled && active==entry) {
                    entry.dueTick=due;
                    entry.preparedAtEnd=endSequence;
                    entry.phase=Phase.PREPARED;
                } else {
                    queueCleanup(entry);
                }
            }
        }
        if (fatal!=null) {
            // Preserve the fatal error while still unwinding this accepted job.
            synchronized (lock) {
                cleanups.remove(entry);
                entry.cleanupQueued=false;
                entry.running=true;
            }
            try {
                execute(new Work(entry,Phase.CLEANUP));
            } catch (Error cleanupFailure) {
                fatal.addSuppressed(cleanupFailure);
            }
            throw fatal;
        }
    }

    private void report(RuntimeException error) {
        try {
            errors.accept(error);
        } catch (RuntimeException ignored) {
            // A broken reporter must not strand the job or suppress cleanup.
        }
    }
}
