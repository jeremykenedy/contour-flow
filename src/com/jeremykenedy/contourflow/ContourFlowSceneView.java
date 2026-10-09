package com.jeremykenedy.contourflow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

final class ContourFlowSceneView extends View {
    private static final int FRAME_DELAY_MS = 33;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable frame =
            new Runnable() {
                @Override
                public void run() {
                    if (!running) return;
                    invalidate();
                    handler.postDelayed(this, FRAME_DELAY_MS);
                }
            };
    private final float[] intersectionsX = new float[4];
    private final float[] intersectionsY = new float[4];
    private ContourFlowOptions options;
    private boolean running;
    private long started = System.nanoTime();
    private float[] heights = new float[0];
    private int columns, rows;

    ContourFlowSceneView(Context context, ContourFlowOptions options) {
        super(context);
        this.options = options;
        setFocusable(true);
    }

    void configure(ContourFlowOptions value) {
        options = value;
        invalidate();
    }

    void startAnimation() {
        if (!running) {
            running = true;
            started = System.nanoTime();
            handler.post(frame);
        }
    }

    void stopAnimation() {
        running = false;
        handler.removeCallbacks(frame);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth(), height = getHeight();
        if (width <= 0 || height <= 0) return;
        long now = System.nanoTime();
        float elapsed = (now - started) / 1_000_000_000f;
        float phase = elapsed * (0.035f + options.speed * 0.017f);
        boolean night = options.lighting == 2 || (options.lighting == 0 && isNight());
        int background = backgroundColor(options.palette, night);
        canvas.drawColor(background);
        drawAtmosphere(canvas, width, height, elapsed, background, night);

        int step = Math.max(15, Math.min(24, (int) (width / 92f)));
        int newColumns = Math.max(50, Math.min(132, (int) (width / step)));
        int newRows = Math.max(30, Math.min(76, (int) (height / step)));
        if (columns != newColumns || rows != newRows) {
            columns = newColumns;
            rows = newRows;
            heights = new float[(columns + 1) * (rows + 1)];
        }
        sampleHeights(phase);

        int lineColor = lineColor(options.palette, night);
        float brightness = (0.42f + options.brightness * 0.145f) * (night ? 0.68f : 1f);
        float cellWidth = width / columns;
        float cellHeight = height / rows;
        int levelCount = 5 + options.density * 2;
        for (int index = 1; index <= levelCount; index++) {
            float level = 0.14f + index * (0.72f / (levelCount + 1));
            Path path = contour(level, cellWidth, cellHeight);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth((1.1f + options.weight * 0.34f) * width / 1920f);
            paint.setColor(withAlpha(lineColor, (int) (220 * brightness)));
            canvas.drawPath(path, paint);
            if (options.palette != 3) {
                paint.setStrokeWidth((2.4f + options.weight * 0.38f) * width / 1920f);
                paint.setColor(withAlpha(lineColor, (int) (26 * brightness)));
                canvas.drawPath(path, paint);
            }
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private void sampleHeights(float phase) {
        int stride = columns + 1;
        for (int y = 0; y <= rows; y++) {
            float fieldY = (y / (float) rows - 0.5f) * 2.4f;
            for (int x = 0; x <= columns; x++) {
                float fieldX = (x / (float) columns - 0.5f) * 4.1f;
                heights[y * stride + x] =
                        ContourFlowField.sample(fieldX, fieldY, phase, options.relief);
            }
        }
    }

    private Path contour(float level, float cellWidth, float cellHeight) {
        Path path = new Path();
        int stride = columns + 1;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                int topLeft = y * stride + x;
                float a = heights[topLeft];
                float b = heights[topLeft + 1];
                float c = heights[topLeft + stride + 1];
                float d = heights[topLeft + stride];
                int count = 0;
                if (crosses(a, b, level)) {
                    float position = ContourFlowField.interpolate(a, b, level);
                    intersectionsX[count] = (x + position) * cellWidth;
                    intersectionsY[count++] = y * cellHeight;
                }
                if (crosses(b, c, level)) {
                    float position = ContourFlowField.interpolate(b, c, level);
                    intersectionsX[count] = (x + 1) * cellWidth;
                    intersectionsY[count++] = (y + position) * cellHeight;
                }
                if (crosses(c, d, level)) {
                    float position = ContourFlowField.interpolate(c, d, level);
                    intersectionsX[count] = (x + 1 - position) * cellWidth;
                    intersectionsY[count++] = (y + 1) * cellHeight;
                }
                if (crosses(d, a, level)) {
                    float position = ContourFlowField.interpolate(d, a, level);
                    intersectionsX[count] = x * cellWidth;
                    intersectionsY[count++] = (y + 1 - position) * cellHeight;
                }
                if (count == 2) {
                    segment(path, 0, 1);
                } else if (count == 4) {
                    if ((a + b + c + d) * 0.25f >= level) {
                        segment(path, 0, 1);
                        segment(path, 2, 3);
                    } else {
                        segment(path, 0, 3);
                        segment(path, 1, 2);
                    }
                }
            }
        }
        return path;
    }

    private static boolean crosses(float first, float second, float level) {
        return (first < level && second >= level) || (second < level && first >= level);
    }

    private void segment(Path path, int first, int second) {
        path.moveTo(intersectionsX[first], intersectionsY[first]);
        path.lineTo(intersectionsX[second], intersectionsY[second]);
    }

    private void drawAtmosphere(
            Canvas canvas,
            float width,
            float height,
            float elapsed,
            int background,
            boolean night) {
        int glow = atmosphereColor(options.palette, night);
        float x = width * (0.48f + (float) Math.sin(elapsed * 0.035f) * 0.14f);
        float y = height * (0.48f + (float) Math.cos(elapsed * 0.027f) * 0.12f);
        paint.setShader(
                new RadialGradient(
                        x,
                        y,
                        Math.max(width, height) * 0.76f,
                        new int[] {withAlpha(glow, 72), withAlpha(glow, 18), background},
                        null,
                        Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height, paint);
        paint.setShader(null);
        float horizon = height * (0.5f + (float) Math.sin(elapsed * 0.02f) * 0.07f);
        paint.setShader(
                new LinearGradient(
                        0,
                        horizon - height * 0.45f,
                        width,
                        horizon + height * 0.35f,
                        new int[] {0x00000000, withAlpha(glow, 12), 0x00000000},
                        null,
                        Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height, paint);
        paint.setShader(null);
    }

    private static int backgroundColor(int palette, boolean night) {
        int[][] colors = {
            {Color.rgb(3, 14, 21), Color.rgb(8, 25, 34)},
            {Color.rgb(21, 27, 32), Color.rgb(31, 38, 42)},
            {Color.rgb(39, 26, 14), Color.rgb(54, 39, 23)},
            {Color.rgb(8, 19, 20), Color.rgb(222, 234, 225)}
        };
        return colors[palette][night ? 0 : 1];
    }

    private static int atmosphereColor(int palette, boolean night) {
        int[][] colors = {
            {Color.rgb(16, 100, 129), Color.rgb(47, 169, 193)},
            {Color.rgb(76, 107, 126), Color.rgb(139, 172, 179)},
            {Color.rgb(150, 77, 28), Color.rgb(220, 149, 61)},
            {Color.rgb(35, 111, 105), Color.rgb(37, 149, 123)}
        };
        return colors[palette][night ? 0 : 1];
    }

    private static int lineColor(int palette, boolean night) {
        int[][] colors = {
            {Color.rgb(42, 184, 213), Color.rgb(102, 220, 224)},
            {Color.rgb(124, 179, 193), Color.rgb(165, 205, 198)},
            {Color.rgb(223, 151, 70), Color.rgb(251, 201, 123)},
            {Color.rgb(76, 158, 143), Color.rgb(15, 110, 93)}
        };
        return colors[palette][night ? 0 : 1];
    }

    private static boolean isNight() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        return hour < 7 || hour >= 19;
    }

    private static int withAlpha(int color, int alpha) {
        return Color.argb(
                Math.max(0, Math.min(255, alpha)),
                Color.red(color),
                Color.green(color),
                Color.blue(color));
    }
}
