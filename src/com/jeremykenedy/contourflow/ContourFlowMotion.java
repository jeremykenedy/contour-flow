package com.jeremykenedy.contourflow;

/** Scene time advances only while visible; every speed has a perceptible flow. */
final class ContourFlowMotion {
    private boolean running;
    private long previous;
    private double seconds;

    boolean start() {
        if (running) return false;
        running = true;
        previous = 0;
        return true;
    }

    void stop() {
        running = false;
        previous = 0;
    }

    float advance(long nanos, int speed) {
        if (running) {
            if (previous != 0) {
                seconds += Math.max(0, Math.min(.1, (nanos - previous) / 1_000_000_000.0));
            }
            previous = nanos;
        }
        double rate = .1 + .04 * (ContourFlowOptions.clamp(speed, 1, 5) - 1);
        return (float) ((seconds * rate) % (Math.PI * 2));
    }
}
