package com.jeremykenedy.contourflow;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public final class PreviewActivity extends Activity {
    private ContourFlowSceneView scene;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(5894 | 512 | 1024);
        scene =
                new ContourFlowSceneView(
                        this, new ContourFlowPreferences(this).read().resolve(System.nanoTime()));
        scene.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });
        setContentView(scene);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (scene != null) {
            scene.startAnimation();
        }
    }

    @Override
    protected void onPause() {
        if (scene != null) {
            scene.stopAnimation();
        }
        super.onPause();
    }
}
