package com.jeremykenedy.contourflow;

final class ContourFlowField {
    private ContourFlowField() {}

    static float sample(float x, float y, float phase, int relief) {
        float movingX = (float) Math.sin(phase * 0.24f) * 0.1f;
        float movingY = (float) Math.cos(phase * 0.19f) * 0.09f;
        float warpedX = x + (float) Math.sin(y * 1.7f + phase * 0.12f) * 0.16f;
        float warpedY = y + (float) Math.sin(x * 1.3f - phase * 0.1f) * 0.12f;
        float north = hill(warpedX + 1.22f + movingX, warpedY + 0.1f, 2.5f);
        float central = hill(warpedX - 0.25f, warpedY - 0.42f + movingY, 3.4f) * 0.8f;
        float east = hill(warpedX - 1.18f, warpedY + 0.68f, 2.1f) * 0.72f;
        float southwest = hill(warpedX + 1.02f, warpedY - 0.76f, 2.8f) * 0.62f;
        float bowl = hill(warpedX - 0.25f, warpedY + 0.22f, 2.3f);
        float waves =
                (float) Math.sin(warpedX * 2.2f + (float) Math.cos(warpedY * 1.8f) * 0.5f)
                        * (float) Math.cos(warpedY * 2.5f - phase * 0.08f);
        float detail = (float) Math.sin((warpedX - warpedY) * 4.7f + phase * 0.15f) * 0.035f;
        float value;
        switch (relief) {
            case 1:
                value =
                        0.52f
                                - north * 0.27f
                                - central * 0.2f
                                + east * 0.15f
                                - bowl * 0.12f
                                + waves * 0.06f
                                + detail;
                break;
            case 2:
                value =
                        0.5f
                                - warpedY * 0.17f
                                + north * 0.18f
                                - central * 0.11f
                                + southwest * 0.14f
                                + waves * 0.045f
                                + detail * 0.7f;
                break;
            default:
                value =
                        0.48f
                                + north * 0.3f
                                + central * 0.24f
                                + east * 0.23f
                                + southwest * 0.19f
                                - bowl * 0.12f
                                + waves * 0.055f
                                + detail;
                break;
        }
        return Math.max(0.02f, Math.min(0.98f, value));
    }

    static float interpolate(float first, float second, float level) {
        float difference = second - first;
        if (Math.abs(difference) < 0.00001f) return 0.5f;
        return Math.max(0f, Math.min(1f, (level - first) / difference));
    }

    private static float hill(float x, float y, float width) {
        return (float) Math.exp(-(x * x + y * y) * width);
    }
}
