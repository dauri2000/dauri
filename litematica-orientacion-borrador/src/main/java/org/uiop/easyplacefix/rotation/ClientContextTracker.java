package org.uiop.easyplacefix.rotation;

import java.util.Objects;

/**
 * Tracks player/world identity and resets dependent work before publishing a
 * replacement context. Callers must observe before admitting work for a context.
 */
public final class ClientContextTracker {
    public record Context(Object player, Object level) {}

    private final Runnable resetDependents;
    private Context current = new Context(null, null);

    public ClientContextTracker(Runnable resetDependents) {
        this.resetDependents = Objects.requireNonNull(resetDependents, "resetDependents");
    }

    public synchronized Context current() {
        return current;
    }

    public synchronized Context observe(Object player, Object level) {
        if (current.player() != player || current.level() != level) {
            resetDependents.run();
            current = new Context(player, level);
        }
        return current;
    }

    /** Disconnect/shutdown invalidate work even if the context is already empty. */
    public synchronized void reset() {
        resetDependents.run();
        current = new Context(null, null);
    }
}
