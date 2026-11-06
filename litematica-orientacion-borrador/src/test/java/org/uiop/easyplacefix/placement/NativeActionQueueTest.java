package org.uiop.easyplacefix.placement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/** Queue lifecycle only: no Minecraft tick, packet or authentication replacement. */
public final class NativeActionQueueTest {
    private static int assertions;

    public static void main(String[] args) {
        fifoKeepsPrepareAndUseSeparated();
        aNewEndJobCannotUseThatSameEnd();
        falseAndThrownCallbacksCleanOnce();
        clearDuringFirstPreservesReplacement();
        clearDuringSecondPreservesReplacement();
        cleanupMayClearAndReplace();
        fullQueueRejectsWithoutTakingOwnership();
        callbacksAndClockRunOutsideMonitor();
        reporterFailureDoesNotStrandWork();
        fatalCallbackStillCleansItsEntry();
        System.out.println("PASS pure-JDK native prepare/use FIFO regression: "+assertions+" assertions");
        System.out.println("LIMIT: queue and supplied tick observations only; no Minecraft client tick, Mixin, packet codec, AUTH generation or Bedrock server executed");
    }

    private static NativeActionQueue queue(int cap, AtomicLong now, List<RuntimeException> errors) {
        return new NativeActionQueue(cap,now::get,errors::add);
    }
    private static void end(NativeActionQueue queue, AtomicLong now, long tick) {
        now.set(tick);
        queue.onTickEnd(tick);
    }
    private static boolean add(NativeActionQueue queue,List<String> log,String name) {
        return queue.submit(() -> {log.add(name+".prepare");return true;},
            () -> log.add(name+".use"),() -> log.add(name+".cleanup"));
    }

    private static void fifoKeepsPrepareAndUseSeparated() {
        AtomicLong now=new AtomicLong();
        List<String> log=new ArrayList<>();
        List<RuntimeException> errors=new ArrayList<>();
        NativeActionQueue q=queue(512,now,errors);
        require(add(q,log,"A") && add(q,log,"B"),"both requests admitted");
        require(log.equals(List.of("A.prepare")) && q.size()==2,
            "waiting B performs no prepare/input callback");
        end(q,now,1);
        require(log.equals(List.of("A.prepare","A.use","A.cleanup","B.prepare")),
            "A use and cleanup precede B prepare on the same END");
        require(q.size()==1,"B stays active until a later END");
        end(q,now,2);
        require(log.equals(List.of("A.prepare","A.use","A.cleanup","B.prepare","B.use","B.cleanup")),
            "B uses only after its own intervening tick");
        require(q.size()==0 && errors.isEmpty(),"FIFO drains without errors");
    }

    private static void aNewEndJobCannotUseThatSameEnd() {
        AtomicLong now=new AtomicLong(5);
        List<String> log=new ArrayList<>();
        NativeActionQueue q=queue(512,now,new ArrayList<>());
        q.onTickEnd(5); // generic callbacks run after this observation
        add(q,log,"late");
        q.onTickEnd(5);
        require(log.equals(List.of("late.prepare")),"generic callback submission cannot use on the same END");
        end(q,now,6);
        require(log.equals(List.of("late.prepare","late.use","late.cleanup")),"late job uses next tick");

        AtomicLong staleClock=new AtomicLong();
        NativeActionQueue guarded=queue(512,staleClock,new ArrayList<>());
        List<String> guardedLog=new ArrayList<>();
        add(guarded,guardedLog,"A"); add(guarded,guardedLog,"B");
        guarded.onTickEnd(100);
        guarded.onTickEnd(100);
        require(guardedLog.equals(List.of("A.prepare","A.use","A.cleanup","B.prepare")),
            "new B cannot use in the same END even if supplied clock is stale");
        guarded.onTickEnd(101);
        require(guarded.size()==0,"later real END permits B to use");
    }

    private static void falseAndThrownCallbacksCleanOnce() {
        AtomicLong now=new AtomicLong();
        List<String> log=new ArrayList<>();
        List<RuntimeException> errors=new ArrayList<>();
        NativeActionQueue q=queue(512,now,errors);
        q.submit(() -> {log.add("false.prepare");return false;},
            () -> {throw new AssertionError("false preparation used");},() -> log.add("false.cleanup"));
        q.submit(() -> {throw new IllegalStateException("first");},
            () -> {throw new AssertionError("failed preparation used");},() -> log.add("first.cleanup"));
        q.submit(() -> true,() -> {throw new IllegalArgumentException("second");},
            () -> log.add("second.cleanup"));
        add(q,log,"next");
        end(q,now,1); end(q,now,2); q.clear();
        require(log.equals(List.of("false.prepare","false.cleanup","first.cleanup","second.cleanup",
            "next.prepare","next.use","next.cleanup")),"false and thrown phases clean once and preserve later work");
        require(errors.size()==2 && q.size()==0,"both runtime failures reported without stranding queue");

        NativeActionQueue badClock=new NativeActionQueue(1,() -> {throw new IllegalStateException("clock");},errors::add);
        int[] cleaned={0};
        badClock.submit(() -> true,() -> {throw new AssertionError("failed clock used");},() -> cleaned[0]++);
        require(cleaned[0]==1 && badClock.size()==0,"clock failure cannot prepare a zero-deadline job");
    }

    private static void clearDuringFirstPreservesReplacement() {
        AtomicLong now=new AtomicLong(); List<String> log=new ArrayList<>();
        NativeActionQueue q=queue(512,now,new ArrayList<>());
        q.submit(() -> {
            log.add("A.prepare"); q.clear(); add(q,log,"B"); log.add("A.return"); return true;
        },() -> {throw new AssertionError("cancelled A used");},() -> log.add("A.cleanup"));
        require(log.equals(List.of("A.prepare","A.return","A.cleanup","B.prepare")),
            "reentrant replacement waits for old callback and cleanup");
        require(q.size()==1,"old preparation completion cannot clear B");
        end(q,now,1); q.clear();
        require(log.equals(List.of("A.prepare","A.return","A.cleanup","B.prepare","B.use","B.cleanup")),
            "replacement finishes once without stale A cleanup");
    }

    private static void clearDuringSecondPreservesReplacement() {
        AtomicLong now=new AtomicLong(); List<String> log=new ArrayList<>();
        NativeActionQueue q=queue(512,now,new ArrayList<>());
        q.submit(() -> true,() -> {log.add("A.use");q.clear();add(q,log,"B");},() -> log.add("A.cleanup"));
        q.submit(() -> {throw new AssertionError("cancelled waiter prepared");},
            () -> {throw new AssertionError("cancelled waiter used");},() -> log.add("waiting.cleanup"));
        end(q,now,1);
        require(log.contains("A.cleanup") && log.contains("waiting.cleanup") && log.getLast().equals("B.prepare"),
            "clear cleans both active and waiting before replacement prepare");
        require(q.size()==1,"old use completion preserves B identity");
        end(q,now,2); q.clear();
        require(log.stream().filter("A.cleanup"::equals).count()==1
            && log.stream().filter("waiting.cleanup"::equals).count()==1
            && log.stream().filter("B.cleanup"::equals).count()==1,"all cancellation cleanup callbacks run once");
    }

    private static void cleanupMayClearAndReplace() {
        AtomicLong now=new AtomicLong(); List<String> log=new ArrayList<>();
        List<RuntimeException> errors=new ArrayList<>(); NativeActionQueue q=queue(512,now,errors);
        q.submit(() -> true,() -> {},() -> {
            log.add("A.cleanup");q.clear();add(q,log,"B");throw new IllegalStateException("cleanup");
        });
        end(q,now,1);
        require(q.size()==1 && log.equals(List.of("A.cleanup","B.prepare")),
            "old throwing cleanup cannot clear or recursively run B");
        end(q,now,2);
        require(errors.size()==1 && q.size()==0,"cleanup exception reported and FIFO progresses");
    }

    private static void fullQueueRejectsWithoutTakingOwnership() {
        AtomicLong now=new AtomicLong(); List<RuntimeException> errors=new ArrayList<>();
        NativeActionQueue q=queue(512,now,errors); int[] first={0},second={0},clean={0},rejected={0};
        int accepted=0;
        for(int i=0;i<512;i++) if(q.submit(() -> {first[0]++;return true;},() -> second[0]++,() -> clean[0]++)) accepted++;
        require(accepted==512 && q.size()==512 && first[0]==1,"capacity counts one active plus all waiting");
        require(!q.submit(() -> {rejected[0]++;return true;},() -> rejected[0]++,() -> rejected[0]++),
            "513th request rejected before callback ownership");
        q.clear();q.clear();end(q,now,1);
        require(rejected[0]==0 && first[0]==1 && second[0]==0 && clean[0]==512,
            "clear cleans all accepted requests once, never a rejected request");
        require(q.size()==0 && errors.isEmpty(),"capacity reusable after cancellation");
        require(q.submit(() -> false,() -> {},() -> {}),"new request admitted after clear");
    }

    private static void callbacksAndClockRunOutsideMonitor() {
        AtomicLong now=new AtomicLong(); NativeActionQueue[] holder=new NativeActionQueue[1];
        List<RuntimeException> errors=new ArrayList<>();
        holder[0]=new NativeActionQueue(512,() -> {outside(holder[0]);return now.get();},error -> {outside(holder[0]);errors.add(error);});
        NativeActionQueue q=holder[0];
        q.submit(() -> {outside(q);return true;},() -> {outside(q);throw new IllegalStateException("expected");},() -> outside(q));
        end(q,now,1);
        require(q.size()==0 && errors.size()==1,"callbacks, reporter and clock run without queue monitor held");
    }

    private static void outside(NativeActionQueue queue) {
        FutureTask<Integer> observation=new FutureTask<>(queue::size);
        Thread worker=new Thread(observation,"fifo-monitor-probe"); worker.setDaemon(true); worker.start();
        try {observation.get(3,TimeUnit.SECONDS);} catch(Exception blocked) {throw new AssertionError("queue monitor held across callback",blocked);}
    }

    private static void reporterFailureDoesNotStrandWork() {
        AtomicLong now=new AtomicLong(); int[] cleaned={0};
        NativeActionQueue q=new NativeActionQueue(1,now::get,error -> {throw new IllegalStateException("reporter");});
        q.submit(() -> {throw new IllegalArgumentException("first");},() -> {},() -> cleaned[0]++);
        require(cleaned[0]==1 && q.size()==0,"throwing error reporter cannot suppress cleanup");
    }

    private static void fatalCallbackStillCleansItsEntry() {
        NativeActionQueue q=queue(1,new AtomicLong(),new ArrayList<>());int[] cleaned={0};
        AssertionError fatal=new AssertionError("fatal");
        try {
            q.submit(() -> {throw fatal;},() -> {},() -> cleaned[0]++);
            throw new AssertionError("fatal error swallowed");
        } catch(AssertionError expected) {require(expected==fatal,"original fatal error preserved");}
        require(cleaned[0]==1 && q.size()==0,"fatal first callback still cleans accepted job once");
        q.clear();require(cleaned[0]==1,"fatal cleanup is not repeated by clear");
    }

    private static void require(boolean condition,String message) {
        assertions++;if(!condition)throw new AssertionError(message);
    }
}
