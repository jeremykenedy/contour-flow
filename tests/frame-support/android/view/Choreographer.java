package android.view;

import java.util.ArrayList;
import java.util.List;

/** Deterministic host scheduler that allows duplicate posts, like the real API. */
public final class Choreographer {
    public interface FrameCallback {
        void doFrame(long nanos);
    }

    private static final Choreographer INSTANCE = new Choreographer();
    private final List<FrameCallback> pending = new ArrayList<>();

    public static Choreographer getInstance() {
        return INSTANCE;
    }

    public void postFrameCallback(FrameCallback callback) {
        pending.add(callback);
    }

    public void removeFrameCallback(FrameCallback callback) {
        pending.removeIf(value -> value == callback);
    }

    public int pendingCount() {
        return pending.size();
    }

    public void frame(long nanos) {
        List<FrameCallback> current = new ArrayList<>(pending);
        pending.clear();
        for (FrameCallback callback : current) callback.doFrame(nanos);
    }
}
