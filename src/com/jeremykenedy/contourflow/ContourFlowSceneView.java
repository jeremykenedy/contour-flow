package com.jeremykenedy.contourflow;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;

final class ContourFlowSceneView extends GLSurfaceView {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ContourFlowMotion motion = new ContourFlowMotion();
    private final ContourFlowRenderer scene;
    private ContourFlowOptions options;
    private boolean running;
    private final Runnable frame =
            new Runnable() {
                @Override
                public void run() {
                    if (!running) return;
                    scene.phase = motion.advance(System.nanoTime(), options.speed);
                    requestRender();
                    handler.postDelayed(this, 33);
                }
            };

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
        handler.post(frame);
    }

    void stopAnimation() {
        if (!running) return;
        running = false;
        motion.stop();
        handler.removeCallbacks(frame);
        onPause();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAnimation();
        super.onDetachedFromWindow();
    }
}
