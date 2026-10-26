package com.jeremykenedy.contourflow;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Calendar;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

final class ContourFlowRenderer implements GLSurfaceView.Renderer {
    private static final float[][] LOW = {
        {.018f, .075f, .105f}, {.075f, .105f, .14f}, {.16f, .095f, .055f}, {.48f, .61f, .59f}
    };
    private static final float[][] HIGH = {
        {.11f, .48f, .50f}, {.32f, .48f, .56f}, {.69f, .40f, .17f}, {.80f, .89f, .80f}
    };
    private static final float[][] INK = {
        {.40f, .94f, .86f}, {.70f, .86f, .90f}, {1f, .82f, .50f}, {.045f, .28f, .24f}
    };
    private final Context context;
    private final FloatBuffer quad =
            ByteBuffer.allocateDirect(12 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    volatile ContourFlowOptions options;
    volatile float phase;
    private int program,
            position,
            phaseLocation,
            shape,
            levels,
            weight,
            pixel,
            low,
            high,
            ink,
            light;
    private int width = 1, height = 1;
    private long lightingMinute = -1;
    private boolean night;
    private ContourFlowMesh mesh;
    private boolean fieldMesh;
    private int vertexBuffer, indexBuffer;

    ContourFlowRenderer(Context context, ContourFlowOptions options) {
        this.context = context;
        this.options = options;
        quad.put(new float[] {-1, -1, 1, -1, -1, 1, -1, 1, 1, -1, 1, 1}).position(0);
    }

    private String source(int resource) {
        try (InputStream in = context.getResources().openRawResource(resource);
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] data = new byte[4096];
            int length;
            while ((length = in.read(data)) != -1) out.write(data, 0, length);
            return out.toString("UTF-8");
        } catch (IOException error) {
            throw new IllegalStateException("Cannot read contour scene", error);
        }
    }

    private int compile(int kind, String source) {
        int shader = GLES20.glCreateShader(kind);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] status = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            String reason = GLES20.glGetShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);
            throw new IllegalStateException(reason);
        }
        return shader;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertex = 0, fragment = 0, linked = 0;
        try {
            vertex = compile(GLES20.GL_VERTEX_SHADER, vertexSource);
            fragment = compile(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
            linked = GLES20.glCreateProgram();
            GLES20.glAttachShader(linked, vertex);
            GLES20.glAttachShader(linked, fragment);
            GLES20.glLinkProgram(linked);
            int[] status = new int[1];
            GLES20.glGetProgramiv(linked, GLES20.GL_LINK_STATUS, status, 0);
            if (status[0] == 0) throw new IllegalStateException(GLES20.glGetProgramInfoLog(linked));
            return linked;
        } catch (RuntimeException error) {
            if (linked != 0) GLES20.glDeleteProgram(linked);
            throw error;
        } finally {
            if (vertex != 0) GLES20.glDeleteShader(vertex);
            if (fragment != 0) GLES20.glDeleteShader(fragment);
        }
    }

    private int originalProgram() {
        return createProgram(
                "attribute vec2 aPosition;void main(){gl_Position=vec4(aPosition,0.,1.);}",
                source(R.raw.contours));
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        int[] range = new int[2];
        int[] precision = new int[1];
        GLES20.glGetShaderPrecisionFormat(
                GLES20.GL_FRAGMENT_SHADER, GLES20.GL_HIGH_FLOAT, range, 0, precision, 0);
        fieldMesh = precision[0] >= 23;
        if (fieldMesh) {
            try {
                program = createProgram(source(R.raw.contours_vertex), source(R.raw.contours_mesh));
            } catch (IllegalStateException unsupported) {
                fieldMesh = false;
                Log.w("ContourFlow", "Gradient field unavailable; using original fragment scene");
                program = originalProgram();
            }
        } else {
            program = originalProgram();
        }
        position = GLES20.glGetAttribLocation(program, "aPosition");
        phaseLocation = GLES20.glGetUniformLocation(program, "uPhase");
        shape = GLES20.glGetUniformLocation(program, "uRelief");
        levels = GLES20.glGetUniformLocation(program, "uLevels");
        weight = GLES20.glGetUniformLocation(program, "uWeight");
        pixel = GLES20.glGetUniformLocation(program, "uPixel");
        low = GLES20.glGetUniformLocation(program, "uLow");
        high = GLES20.glGetUniformLocation(program, "uHigh");
        ink = GLES20.glGetUniformLocation(program, "uInk");
        light = GLES20.glGetUniformLocation(program, "uLight");
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_BLEND);
        if (fieldMesh) {
            if (mesh == null) mesh = new ContourFlowMesh();
            int[] buffers = new int[2];
            GLES20.glGenBuffers(2, buffers, 0);
            vertexBuffer = buffers[0];
            indexBuffer = buffers[1];
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffer);
            mesh.vertices.position(0);
            GLES20.glBufferData(
                    GLES20.GL_ARRAY_BUFFER,
                    mesh.vertices.capacity() * 4,
                    mesh.vertices,
                    GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
            mesh.indices.position(0);
            GLES20.glBufferData(
                    GLES20.GL_ELEMENT_ARRAY_BUFFER,
                    mesh.indices.capacity() * 2,
                    mesh.indices,
                    GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
        Log.i(
                "ContourFlow",
                "Field rendering: "
                        + (fieldMesh ? "256x144 gradient mesh" : "original fragment fallback")
                        + "; fragment precision "
                        + precision[0]);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        GLES20.glViewport(0, 0, this.width, this.height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        ContourFlowOptions current = options;
        long minute = System.currentTimeMillis() / 60000;
        if (lightingMinute != minute) {
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            night = hour < 7 || hour >= 19;
            lightingMinute = minute;
        }
        boolean dark = current.lighting == 2 || (current.lighting == 0 && night);
        GLES20.glUseProgram(program);
        GLES20.glUniform1f(phaseLocation, phase);
        GLES20.glUniform1f(shape, current.relief);
        GLES20.glUniform1f(levels, 8 + current.density * 3);
        GLES20.glUniform1f(weight, .65f + current.weight * .42f);
        GLES20.glUniform2f(pixel, 1f / width, 1f / height);
        GLES20.glUniform3fv(low, 1, LOW[current.palette], 0);
        GLES20.glUniform3fv(high, 1, HIGH[current.palette], 0);
        GLES20.glUniform3fv(ink, 1, INK[current.palette], 0);
        GLES20.glUniform1f(light, (.55f + current.brightness * .15f) * (dark ? .72f : 1));
        GLES20.glEnableVertexAttribArray(position);
        if (fieldMesh) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffer);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
            GLES20.glVertexAttribPointer(position, 2, GLES20.GL_FLOAT, false, 0, 0);
            GLES20.glDrawElements(
                    GLES20.GL_TRIANGLES, mesh.indexCount, GLES20.GL_UNSIGNED_SHORT, 0);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        } else {
            quad.position(0);
            GLES20.glVertexAttribPointer(position, 2, GLES20.GL_FLOAT, false, 0, quad);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
        }
    }
}
