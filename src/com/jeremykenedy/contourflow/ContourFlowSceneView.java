package com.jeremykenedy.contourflow;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.Choreographer;

final class ContourFlowSceneView extends GLSurfaceView implements Choreographer.FrameCallback {
    private final Choreographer choreographer = Choreographer.getInstance();
    private final ContourFlowMotion motion = new ContourFlowMotion();
    private final ContourFlowRenderer scene;
    private ContourFlowOptions options;
    private boolean running;

    ContourFlowSceneView(Context context, ContourFlowOptions options) {
        super(context);
        this.options = options;
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 0, 0, 0);
        scene = new ContourFlowRenderer(context, options);
        setRenderer(scene);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        setFocusable(true);
    }

    void configure(ContourFlowOptions value) {
        options = value;
        scene.options = value;
        requestRender();
    }

    void startAnimation() {
        if (running) return;
        running = true;
        motion.start();
        onResume();
        choreographer.postFrameCallback(this);
    }

    void stopAnimation() {
        if (!running) return;
        running = false;
        motion.stop();
        choreographer.removeFrameCallback(this);
        onPause();
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (!running) return;
        scene.phase = motion.advance(frameTimeNanos, options.speed);
        requestRender();
        choreographer.postFrameCallback(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAnimation();
        super.onDetachedFromWindow();
    }
}
