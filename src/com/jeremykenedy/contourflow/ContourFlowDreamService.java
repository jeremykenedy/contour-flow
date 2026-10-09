package com.jeremykenedy.contourflow;

import android.service.dreams.DreamService;

public final class ContourFlowDreamService extends DreamService {
    private ContourFlowSceneView scene;

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setInteractive(false);
        setFullscreen(true);
        setScreenBright(false);
        scene =
                new ContourFlowSceneView(
                        this, new ContourFlowPreferences(this).read().resolve(System.nanoTime()));
        setContentView(scene);
    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();
        if (scene != null) {
            scene.configure(new ContourFlowPreferences(this).read().resolve(System.nanoTime()));
            scene.startAnimation();
        }
    }

    @Override
    public void onDreamingStopped() {
        stopScene();
        super.onDreamingStopped();
    }

    @Override
    public void onDetachedFromWindow() {
        stopScene();
        scene = null;
        super.onDetachedFromWindow();
    }

    private void stopScene() {
        if (scene != null) {
            scene.stopAnimation();
        }
    }
}
