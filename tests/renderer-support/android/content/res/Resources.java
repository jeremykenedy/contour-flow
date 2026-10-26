package android.content.res;

import java.io.*;

public final class Resources {
    private final File directory;

    public Resources(File directory) {
        this.directory = directory;
    }

    public InputStream openRawResource(int id) {
        String name =
                id == 1 ? "contours.glsl" : id == 2 ? "contours_vertex.glsl" : "contours_mesh.glsl";
        try {
            return new FileInputStream(new File(directory, name));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
