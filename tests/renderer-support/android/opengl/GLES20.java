package android.opengl;

import java.nio.Buffer;
import java.util.*;

/** Host-only call recorder. It does not compile GLSL or simulate a GPU. */
public final class GLES20 {
    public static final int GL_VERTEX_SHADER = 1,
            GL_FRAGMENT_SHADER = 2,
            GL_COMPILE_STATUS = 3,
            GL_LINK_STATUS = 4,
            GL_DEPTH_TEST = 5,
            GL_BLEND = 6,
            GL_FLOAT = 7,
            GL_TRIANGLES = 8,
            GL_ARRAY_BUFFER = 9,
            GL_ELEMENT_ARRAY_BUFFER = 10,
            GL_STATIC_DRAW = 11,
            GL_UNSIGNED_SHORT = 12,
            GL_HIGH_FLOAT = 13;
    public static int precision, arrayDrawCount, indexDrawCount, uploads, width, height;
    public static boolean rejectMeshCompile, rejectMeshLink;
    public static float pixelX, pixelY;
    public static final List<String> sources = new ArrayList<>();
    public static final Set<Integer> deletedShaders = new HashSet<>(),
            deletedPrograms = new HashSet<>();
    private static final Map<Integer, String> shaderText = new HashMap<>();
    private static final Map<Integer, List<Integer>> attached = new HashMap<>();
    private static final Map<Integer, String> uniforms = new HashMap<>();
    private static int next;

    public static void reset(int bits) {
        precision = bits;
        arrayDrawCount = indexDrawCount = uploads = 0;
        rejectMeshCompile = rejectMeshLink = false;
        pixelX = pixelY = 0;
        sources.clear();
        deletedShaders.clear();
        deletedPrograms.clear();
        shaderText.clear();
        attached.clear();
        uniforms.clear();
        next = 1;
    }

    public static int glCreateShader(int type) {
        return next++;
    }

    public static void glShaderSource(int id, String source) {
        shaderText.put(id, source);
        sources.add(source);
    }

    public static void glCompileShader(int id) {}

    public static void glGetShaderiv(int id, int name, int[] value, int offset) {
        value[offset] = rejectMeshCompile && shaderText.get(id).contains("vField") ? 0 : 1;
    }

    public static String glGetShaderInfoLog(int id) {
        return "test driver rejected mesh shader";
    }

    public static void glDeleteShader(int id) {
        deletedShaders.add(id);
    }

    public static int glCreateProgram() {
        int id = next++;
        attached.put(id, new ArrayList<Integer>());
        return id;
    }

    public static void glAttachShader(int program, int shader) {
        attached.get(program).add(shader);
    }

    public static void glLinkProgram(int program) {}

    public static void glGetProgramiv(int id, int name, int[] value, int offset) {
        boolean mesh = false;
        for (int shader : attached.get(id)) mesh |= shaderText.get(shader).contains("vField");
        value[offset] = rejectMeshLink && mesh ? 0 : 1;
    }

    public static String glGetProgramInfoLog(int id) {
        return "test driver rejected mesh link";
    }

    public static void glDeleteProgram(int id) {
        deletedPrograms.add(id);
    }

    public static int glGetAttribLocation(int id, String name) {
        return 0;
    }

    public static int glGetUniformLocation(int id, String name) {
        int location = next++;
        uniforms.put(location, name);
        return location;
    }

    public static void glGetShaderPrecisionFormat(
            int shader, int kind, int[] range, int offset, int[] value, int start) {
        value[start] = precision;
    }

    public static void glDisable(int what) {}

    public static void glGenBuffers(int count, int[] buffers, int start) {
        for (int i = 0; i < count; i++) buffers[start + i] = next++;
    }

    public static void glBindBuffer(int type, int id) {}

    public static void glBufferData(int type, int bytes, Buffer data, int usage) {
        uploads++;
    }

    public static void glViewport(int x, int y, int w, int h) {
        width = w;
        height = h;
    }

    public static void glUseProgram(int id) {}

    public static void glUniform1f(int id, float value) {}

    public static void glUniform2f(int id, float x, float y) {
        if ("uPixel".equals(uniforms.get(id))) {
            pixelX = x;
            pixelY = y;
        }
    }

    public static void glUniform3fv(int id, int count, float[] value, int offset) {}

    public static void glEnableVertexAttribArray(int id) {}

    public static void glVertexAttribPointer(
            int id, int size, int type, boolean normalize, int stride, Buffer data) {}

    public static void glVertexAttribPointer(
            int id, int size, int type, boolean normalize, int stride, int offset) {}

    public static void glDrawArrays(int type, int first, int count) {
        arrayDrawCount = count;
    }

    public static void glDrawElements(int type, int count, int indexType, int offset) {
        indexDrawCount = count;
    }
}
