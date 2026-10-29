package com.jeremykenedy.contourflow;

import android.content.Context;
import android.view.Choreographer;

public final class ContourFlowFrameTest {
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static void near(float expected, float actual) {
        require(Math.abs(expected - actual) < .00001f, "Scene time changed unexpectedly");
    }

    public static void main(String[] args) {
        Choreographer scheduler = Choreographer.getInstance();
        ContourFlowSceneView view =
                new ContourFlowSceneView(new Context(), ContourFlowOptions.defaults());
        view.startAnimation();
        view.startAnimation();
        require(scheduler.pendingCount() == 1 && view.resumes == 1, "Duplicate starts posted work");
        for (int frame = 0; frame <= 60; frame++)
            scheduler.frame(1_000_000_000L + Math.round(frame * 1_000_000_000.0 / 60));
        require(
                view.requests == 61 && scheduler.pendingCount() == 1,
                "Visible vsyncs did not request frames");
        near(.18f, ContourFlowRenderer.latest.phase);
        view.stopAnimation();
        view.stopAnimation();
        require(scheduler.pendingCount() == 0 && view.pauses == 1, "Pause left queued callbacks");
        view.doFrame(30_000_000_000L);
        require(
                view.requests == 61 && scheduler.pendingCount() == 0,
                "A late stopped callback rendered or reposted");
        near(.18f, ContourFlowRenderer.latest.phase);
        view.startAnimation();
        scheduler.frame(40_000_000_000L);
        near(.18f, ContourFlowRenderer.latest.phase);
        scheduler.frame(40_016_666_667L);
        near(.183f, ContourFlowRenderer.latest.phase);
        view.onDetachedFromWindow();
        require(
                view.pauses == 2 && view.detaches == 1 && scheduler.pendingCount() == 0,
                "Detach did not stop frame work");
        int last = view.requests;
        view.doFrame(50_000_000_000L);
        require(view.requests == last && scheduler.pendingCount() == 0, "Detached view rendered");
        System.out.println(
                "Actual SceneView visible-vsync, duplicate start, late-stop, resume-clock and"
                        + " detach tests passed");
    }
}
