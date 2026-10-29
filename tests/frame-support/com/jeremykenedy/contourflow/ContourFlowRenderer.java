package com.jeremykenedy.contourflow;

import android.content.Context;

/** Captures scene-clock submissions only; real shader compilation needs native QA. */
final class ContourFlowRenderer {
    static ContourFlowRenderer latest;
    volatile ContourFlowOptions options;
    volatile float phase;

    ContourFlowRenderer(Context context, ContourFlowOptions options) {
        this.options = options;
        latest = this;
    }
}
