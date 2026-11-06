package org.uiop.easyplacefix.rotation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Real admission/reservation primitives with an explicit constructed tick queue. */
public final class TaskAdmissionTest {
    private static int assertions;

    public static void main(String[] args) {
        rejectionDoesNotRunOrDiscardAcceptedWork();
        notePumpRetriesAfterCapacityReturns();
        notePumpReschedulingRejectionRecovers();
        resetAndOldRollbackCannotRemoveReplacement();
        terrainRejectedAConservesAcceptedB();
        exceptionalSubmissionRollsBackItsOwnReservation();
        System.out.println("PASS pure-JDK bounded-admission/reservation regression: " + assertions + " assertions");
        System.out.println("LIMIT: tick queue and helper jobs are explicit models; real Minecraft scheduler, helper implementation, Mixin and BDS unverified");
    }

    private record Scheduled(long due, Runnable task) {}

    private static final class QueueModel {
        final List<Scheduled> pending = new ArrayList<>();
        long tick;

        boolean submit(Runnable task, int delay) {
            return TaskAdmission.tryBounded(pending.size(), 512,
                () -> pending.add(new Scheduled(tick + delay, task)));
        }

        void fill() {
            while (pending.size() < 512) {
                if (!submit(() -> {}, 10_000)) throw new AssertionError("failed to fill queue");
            }
        }

        void freeOneFiller() {
            for (int i = 0; i < pending.size(); i++) {
                if (pending.get(i).due() >= 10_000) {
                    pending.remove(i);
                    return;
                }
            }
            throw new AssertionError("no filler to free");
        }

        void advance() {
            tick++;
            List<Scheduled> due = new ArrayList<>();
            pending.removeIf(task -> {
                if (task.due() <= tick) {
                    due.add(task);
                    return true;
                }
                return false;
            });
            for (Scheduled task : due) task.task().run();
        }
    }

    private static final class PumpModel {
        final QueueModel queue;
        final TaskAdmission.Reservations<Boolean> reservations = new TaskAdmission.Reservations<>();
        final Set<String> jobs = new HashSet<>();
        int runs;
        boolean partial;
        Runnable duringRun = () -> {};

        PumpModel(QueueModel queue) { this.queue = queue; }

        void retry() {
            if (jobs.isEmpty()) return;
            var owner = reservations.acquire(Boolean.TRUE);
            if (owner != null) {
                TaskAdmission.trySubmit(() -> queue.submit(() -> run(owner), 2),
                    () -> reservations.release(Boolean.TRUE, owner));
            }
        }

        void run(TaskAdmission.Reservation owner) {
            if (!reservations.release(Boolean.TRUE, owner)) return;
            runs++;
            duringRun.run();
            if (partial) {
                partial = false;
                retry();
            } else {
                jobs.clear();
            }
        }
    }

    private static void rejectionDoesNotRunOrDiscardAcceptedWork() {
        QueueModel queue = new QueueModel();
        queue.fill();
        List<Scheduled> accepted = List.copyOf(queue.pending);
        boolean[] ran = {false};
        require(!queue.submit(() -> ran[0] = true, 1), "513th queued task is rejected");
        require(!ran[0] && queue.pending.equals(accepted), "rejection neither runs nor discards accepted tasks");
        require(queue.pending.size() == 512, "queue bound remains 512");
    }

    private static void notePumpRetriesAfterCapacityReturns() {
        QueueModel queue = new QueueModel();
        queue.fill();
        PumpModel pump = new PumpModel(queue);
        pump.jobs.add("existing-note");
        pump.jobs.add("new-note");
        for (int i = 0; i < 5; i++) pump.retry();
        require(pump.jobs.size() == 2 && pump.runs == 0, "rejected pumps preserve valid queued note jobs");
        require(queue.pending.size() == 512, "retries while full add no tasks");
        queue.freeOneFiller();
        pump.retry();
        require(queue.pending.size() == 512, "pending note pump admitted when one slot becomes free");
        pump.retry();
        require(queue.pending.size() == 512, "already admitted pump is not duplicated");
        queue.advance();
        require(pump.runs == 0 && pump.jobs.size() == 2, "admitted pump keeps original two-tick delay");
        queue.advance();
        require(pump.runs == 1 && pump.jobs.isEmpty(), "pump eventually executes after admission");
        require(queue.pending.size() == 511, "accepted filler callbacks are preserved");
    }

    private static void notePumpReschedulingRejectionRecovers() {
        QueueModel queue = new QueueModel();
        queue.fill();
        queue.freeOneFiller();
        PumpModel pump = new PumpModel(queue);
        pump.jobs.add("partially-tuned-note");
        pump.partial = true;
        pump.duringRun = queue::fill;
        pump.retry();
        queue.advance();
        queue.advance();
        require(pump.runs == 1 && pump.jobs.size() == 1, "partial tuning keeps remaining job when reschedule is full");
        require(queue.pending.size() == 512, "failed internal reschedule respects bound");
        pump.retry();
        require(pump.runs == 1 && queue.pending.size() == 512, "end-tick retry does not run synchronously while full");
        queue.freeOneFiller();
        pump.duringRun = () -> {};
        pump.retry();
        queue.advance();
        require(pump.runs == 1, "recovered reschedule still waits original delay");
        queue.advance();
        require(pump.runs == 2 && pump.jobs.isEmpty(), "internal rescheduling rejection does not permanently strand note job");
    }

    private static void resetAndOldRollbackCannotRemoveReplacement() {
        var reservations = new TaskAdmission.Reservations<String>();
        var old = reservations.acquire("same-position");
        final TaskAdmission.Reservation[] replacement = new TaskAdmission.Reservation[1];
        boolean accepted = TaskAdmission.trySubmit(() -> {
            reservations.clear();
            replacement[0] = reservations.acquire("same-position");
            return false;
        }, () -> reservations.release("same-position", old));
        require(!accepted && reservations.owns("same-position", replacement[0]),
            "late rejected admission rollback cannot remove a newer reservation at same position");
        require(!reservations.release("same-position", old), "old callback cannot claim replacement work");
        require(reservations.release("same-position", replacement[0]), "current owner may claim its own work");

        QueueModel queue = new QueueModel();
        PumpModel pump = new PumpModel(queue);
        pump.jobs.add("old-world-note");
        pump.retry();
        Scheduled oldCallback = queue.pending.get(0);
        queue.pending.clear();
        pump.reservations.clear();
        pump.jobs.clear();
        pump.retry();
        require(queue.pending.isEmpty(), "world reset before retry admits no old note job");
        pump.jobs.add("new-world-note");
        pump.retry();
        oldCallback.task().run();
        require(pump.runs == 0 && pump.jobs.contains("new-world-note"), "old callback cannot consume new-world note work");
        pump.retry();
        require(queue.pending.size() == 1, "old callback did not clear new pump reservation or allow duplicate");
        queue.advance();
        queue.advance();
        require(pump.runs == 1 && pump.jobs.isEmpty(), "new-world pump executes once");
    }

    private static void terrainRejectedAConservesAcceptedB() {
        QueueModel queue = new QueueModel();
        var reservations = new TaskAdmission.Reservations<String>();
        var b = reservations.acquire("B");
        int[] runs = {0, 0};
        require(TaskAdmission.trySubmit(() -> queue.submit(() -> {
            if (reservations.release("B", b)) runs[1]++;
        }, 1), () -> reservations.release("B", b)), "terrain B admitted");
        queue.fill();
        var a = reservations.acquire("A");
        require(!TaskAdmission.trySubmit(() -> queue.submit(() -> runs[0]++, 1),
            () -> reservations.release("A", a)), "terrain A rejected at capacity");
        require(!reservations.owns("A", a) && reservations.owns("B", b),
            "rejecting A releases only A, preserving accepted B marker");
        var retryA = reservations.acquire("A");
        require(retryA != null, "terrain A can be retried after rejection");
        require(!reservations.release("A", a) && reservations.owns("A", retryA),
            "old A cleanup cannot remove retried A");
        queue.advance();
        require(runs[0] == 0 && runs[1] == 1, "accepted B still executes while rejected A never does");
    }

    private static void exceptionalSubmissionRollsBackItsOwnReservation() {
        var reservations = new TaskAdmission.Reservations<String>();
        var a = reservations.acquire("A");
        var b = reservations.acquire("B");
        try {
            TaskAdmission.trySubmit(() -> { throw new IllegalStateException("rejected"); },
                () -> reservations.release("A", a));
            throw new AssertionError("exception swallowed");
        } catch (IllegalStateException expected) {
            require(!reservations.owns("A", a) && reservations.owns("B", b),
                "exceptional admission rolls back only its own reservation");
        }
    }

    private static void require(boolean condition, String message) {
        assertions++;
        if (!condition) throw new AssertionError(message);
    }
}
