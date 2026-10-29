package android.opengl;

import android.content.Context;

/** Host-only boundary fixture; this does not create or render a GL context. */
public class GLSurfaceView {
    public static final int RENDERMODE_WHEN_DIRTY = 0;
    public int requests, resumes, pauses, detaches;

    public GLSurfaceView(Context context) {}

    public void setEGLContextClientVersion(int version) {}

    public void setEGLConfigChooser(int r, int g, int b, int a, int depth, int stencil) {}

    public void setRenderer(Object renderer) {}

    public void setRenderMode(int mode) {}

    public void setFocusable(boolean focusable) {}

    public void requestRender() {
        requests++;
    }

    public void onResume() {
        resumes++;
    }

    public void onPause() {
        pauses++;
    }

    protected void onDetachedFromWindow() {
        detaches++;
    }
}
