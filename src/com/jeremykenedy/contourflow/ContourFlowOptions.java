package com.jeremykenedy.contourflow;

import java.util.Random;

final class ContourFlowOptions {
    static final String[] PALETTES = {"Abyss", "Slate", "Sandstone", "Glacier"};
    static final String[] RELIEFS = {"Ridges", "Basins", "Coast"};
    static final String[] LIGHTING = {"Auto", "Day", "Night"};
    final int palette, relief, density, speed, weight, lighting, brightness, randomMask;

    ContourFlowOptions(
            int palette,
            int relief,
            int density,
            int speed,
            int weight,
            int lighting,
            int brightness,
            int randomMask) {
        this.palette = clamp(palette, 0, PALETTES.length - 1);
        this.relief = clamp(relief, 0, RELIEFS.length - 1);
        this.density = clamp(density, 1, 5);
        this.speed = clamp(speed, 1, 5);
        this.weight = clamp(weight, 1, 5);
        this.lighting = clamp(lighting, 0, LIGHTING.length - 1);
        this.brightness = clamp(brightness, 1, 5);
        this.randomMask = randomMask & 127;
    }

    static ContourFlowOptions defaults() {
        return new ContourFlowOptions(0, 0, 3, 3, 2, 0, 3, 0);
    }

    ContourFlowOptions resolve(long seed) {
        if (randomMask == 0) return this;
        Random random = new Random(seed);
        return new ContourFlowOptions(
                pick(random, 1, palette, PALETTES.length),
                pick(random, 2, relief, RELIEFS.length),
                pick(random, 4, density - 1, 5) + 1,
                pick(random, 8, speed - 1, 5) + 1,
                pick(random, 16, weight - 1, 5) + 1,
                pick(random, 32, lighting, LIGHTING.length),
                pick(random, 64, brightness - 1, 5) + 1,
                0);
    }

    private int pick(Random random, int bit, int current, int count) {
        return (randomMask & bit) == 0 ? current : random.nextInt(count);
    }

    static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
