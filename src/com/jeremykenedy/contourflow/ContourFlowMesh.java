package com.jeremykenedy.contourflow;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

final class ContourFlowMesh {
    static final int COLUMNS = 256;
    static final int ROWS = 144;
    final int indexCount = COLUMNS * ROWS * 6;
    final FloatBuffer vertices =
            ByteBuffer.allocateDirect((COLUMNS + 1) * (ROWS + 1) * 2 * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
    final ShortBuffer indices =
            ByteBuffer.allocateDirect(indexCount * 2)
                    .order(ByteOrder.nativeOrder())
                    .asShortBuffer();

    ContourFlowMesh() {
        for (int y = 0; y <= ROWS; y++) {
            for (int x = 0; x <= COLUMNS; x++) {
                vertices.put(x * 2f / COLUMNS - 1).put(y * 2f / ROWS - 1);
            }
        }
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLUMNS; x++) {
                int first = y * (COLUMNS + 1) + x;
                int next = first + COLUMNS + 1;
                indices.put((short) first).put((short) (first + 1)).put((short) next);
                indices.put((short) next).put((short) (first + 1)).put((short) (next + 1));
            }
        }
        vertices.position(0);
        indices.position(0);
    }
}
