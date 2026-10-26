package com.jeremykenedy.contourflow;

public final class ContourFlowMotionTest {
    private static void close(float expected, float actual) {
        if (Math.abs(expected - actual) > .00001f)
            throw new AssertionError(expected + " != " + actual);
    }

    public static void main(String[] args) {
        ContourFlowMotion motion = new ContourFlowMotion();
        close(0, motion.advance(1_000_000_000L, 3));
        if (!motion.start() || motion.start()) throw new AssertionError("Duplicate starts");
        motion.advance(1_000_000_000L, 3);
        for (int tick = 1; tick <= 10; tick++)
            motion.advance(1_000_000_000L + tick * 100_000_000L, 3);
        close(.18f, motion.advance(2_000_000_000L, 3));
        motion.stop();
        close(.18f, motion.advance(30_000_000_000L, 3));
        motion.start();
        close(.18f, motion.advance(40_000_000_000L, 3));
        close(.198f, motion.advance(40_100_000_000L, 3));
        close(.198f, motion.advance(39_000_000_000L, 3));
        // A long scheduling stall is bounded; it cannot jump the landscape on return.
        close(.216f, motion.advance(60_000_000_000L, 3));
        float slow = motion.advance(60_000_000_000L, 1);
        float fast = motion.advance(60_000_000_000L, 5);
        if (!(fast > slow * 2)) throw new AssertionError("Speed control is ineffective");
        close(slow, motion.advance(60_000_000_000L, -1));
        close(fast, motion.advance(60_000_000_000L, 9));
        // Phase stays precise through long-running scenes; the shader uses periodic harmonics.
        for (int tick = 1; tick <= 4000; tick++) {
            float value = motion.advance(60_000_000_000L + tick * 100_000_000L, 3);
            if (!(value >= 0 && value < Math.PI * 2))
                throw new AssertionError("Phase left periodic bounds");
        }
        System.out.println(
                "Visible motion rate, speed, pause/resume, duplicate start and bounded clock"
                        + " passed");
    }
}
