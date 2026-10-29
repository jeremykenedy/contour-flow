package com.jeremykenedy.contourflow;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;

import java.io.File;
import java.nio.file.Files;

/** Exercises the real renderer's dispatch and failure recovery, not a GPU/compiler. */
public final class ContourFlowRendererTest {
    private static ContourFlowRenderer renderer() {
        return new ContourFlowRenderer(
                new Context(new Resources(new File("res/raw"))), ContourFlowOptions.defaults());
    }

    private static void originalScene(ContourFlowRenderer renderer) throws Exception {
        renderer.onSurfaceCreated(null, null);
        renderer.onSurfaceChanged(null, 1920, 1080);
        renderer.onDrawFrame(null);
        String original =
                new String(Files.readAllBytes(new File("res/raw/contours.glsl").toPath()), "UTF-8");
        if (!GLES20.sources.contains(original))
            throw new AssertionError("Fallback replaced the landscape");
        if (GLES20.arrayDrawCount != 6 || GLES20.indexDrawCount != 0)
            throw new AssertionError("Fallback did not draw the original full surface");
        if (GLES20.width != 1920 || GLES20.height != 1080)
            throw new AssertionError("Fallback reduced native resolution");
    }

    public static void main(String[] args) throws Exception {
        for (int precision : new int[] {0, 16}) {
            GLES20.reset(precision);
            originalScene(renderer());
        }
        GLES20.reset(23);
        ContourFlowRenderer optimized = renderer();
        optimized.onSurfaceCreated(null, null);
        optimized.onSurfaceChanged(null, 1920, 1080);
        for (int frame = 0; frame < 5; frame++) {
            optimized.phase = frame * .05f;
            optimized.onDrawFrame(null);
        }
        if (GLES20.indexDrawCount == 0 || GLES20.arrayDrawCount != 0)
            throw new AssertionError("High precision field was not drawn");
        if (GLES20.uploads != 2)
            throw new AssertionError("Static buffers were uploaded every frame");
        if (GLES20.width != 1920
                || GLES20.height != 1080
                || Math.abs(GLES20.pixelX - 1f / 1920) > 1e-9
                || Math.abs(GLES20.pixelY - 1f / 1080) > 1e-9)
            throw new AssertionError("Field sampling changed the full native output footprint");

        GLES20.reset(23);
        GLES20.rejectMeshCompile = true;
        originalScene(renderer());
        if (!GLES20.deletedShaders.contains(1)) throw new AssertionError("Failed shader leaked");

        GLES20.reset(23);
        GLES20.rejectMeshLink = true;
        originalScene(renderer());
        if (GLES20.deletedPrograms.isEmpty()) throw new AssertionError("Failed program leaked");
        System.out.println(
                "Precision/driver-failure fallback, native viewport and reused field buffers pass"
                        + " (host call recorder)");
    }
}
