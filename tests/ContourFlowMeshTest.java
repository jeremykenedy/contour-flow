package com.jeremykenedy.contourflow;

public final class ContourFlowMeshTest {
    public static void main(String[] args) {
        ContourFlowMesh mesh = new ContourFlowMesh();
        int vertexCount = mesh.vertices.capacity() / 2;
        if (vertexCount > 65536) throw new AssertionError("Unsigned short index overflow");
        boolean[] used = new boolean[vertexCount];
        double area = 0;
        for (int i = 0; i < mesh.indexCount; i += 3) {
            int[] triangle = new int[3];
            for (int j = 0; j < 3; j++) {
                int index = Short.toUnsignedInt(mesh.indices.get(i + j));
                if (index >= vertexCount) throw new AssertionError("Index outside vertex data");
                used[index] = true;
                triangle[j] = index;
            }
            double ax = mesh.vertices.get(triangle[0] * 2);
            double ay = mesh.vertices.get(triangle[0] * 2 + 1);
            double bx = mesh.vertices.get(triangle[1] * 2);
            double by = mesh.vertices.get(triangle[1] * 2 + 1);
            double cx = mesh.vertices.get(triangle[2] * 2);
            double cy = mesh.vertices.get(triangle[2] * 2 + 1);
            double triangleArea = ((bx - ax) * (cy - ay) - (by - ay) * (cx - ax)) / 2;
            if (!(triangleArea > 0)) throw new AssertionError("Reversed or degenerate triangle");
            area += triangleArea;
        }
        if (Math.abs(area - 4) > .000001) throw new AssertionError("Surface has missing area");
        for (int i = 0; i < vertexCount; i++) {
            if (!used[i]) throw new AssertionError("Unreferenced field sample");
            float x = mesh.vertices.get(i * 2);
            float y = mesh.vertices.get(i * 2 + 1);
            if (!Float.isFinite(x) || !Float.isFinite(y) || Math.abs(x) > 1 || Math.abs(y) > 1)
                throw new AssertionError("Vertex outside the full native surface");
        }
        System.out.println(
                "Full-surface positive triangles, finite bounds and unsigned indices pass");
    }
}
