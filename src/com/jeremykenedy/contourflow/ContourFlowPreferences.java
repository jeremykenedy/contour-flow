package com.jeremykenedy.contourflow;

import android.content.Context;
import android.content.SharedPreferences;

final class ContourFlowPreferences {
    static final String FILE = "contour_flow_preferences";
    private final SharedPreferences values;

    ContourFlowPreferences(Context context) {
        values = context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    ContourFlowOptions read() {
        ContourFlowOptions defaults = ContourFlowOptions.defaults();
        return new ContourFlowOptions(
                values.getInt("palette", defaults.palette),
                values.getInt("relief", defaults.relief),
                values.getInt("density", defaults.density),
                values.getInt("speed", defaults.speed),
                values.getInt("weight", defaults.weight),
                values.getInt("lighting", defaults.lighting),
                values.getInt("brightness", defaults.brightness),
                values.getInt("random_mask", 0));
    }

    void write(ContourFlowOptions options) {
        values.edit()
                .putInt("palette", options.palette)
                .putInt("relief", options.relief)
                .putInt("density", options.density)
                .putInt("speed", options.speed)
                .putInt("weight", options.weight)
                .putInt("lighting", options.lighting)
                .putInt("brightness", options.brightness)
                .putInt("random_mask", options.randomMask)
                .apply();
    }

    void set(String key, String value) {
        ContourFlowOptions old = read();
        int palette = old.palette,
                relief = old.relief,
                density = old.density,
                speed = old.speed,
                weight = old.weight,
                lighting = old.lighting,
                brightness = old.brightness,
                mask = old.randomMask;
        switch (key) {
            case "palette":
                palette = choice(value, ContourFlowOptions.PALETTES, palette);
                mask = random(mask, 1, value);
                break;
            case "relief":
                relief = choice(value, ContourFlowOptions.RELIEFS, relief);
                mask = random(mask, 2, value);
                break;
            case "density":
                density = number(value, 1, 5, density);
                mask = random(mask, 4, value);
                break;
            case "speed":
                speed = number(value, 1, 5, speed);
                mask = random(mask, 8, value);
                break;
            case "weight":
                weight = number(value, 1, 5, weight);
                mask = random(mask, 16, value);
                break;
            case "lighting":
                lighting = choice(value, ContourFlowOptions.LIGHTING, lighting);
                mask = random(mask, 32, value);
                break;
            case "brightness":
                brightness = number(value, 1, 5, brightness);
                mask = random(mask, 64, value);
                break;
            default:
                throw new IllegalArgumentException("Unsupported setting: " + key);
        }
        write(
                new ContourFlowOptions(
                        palette, relief, density, speed, weight, lighting, brightness, mask));
    }

    private static int choice(String value, String[] choices, int fallback) {
        if ("random".equalsIgnoreCase(value)) return fallback;
        for (int i = 0; i < choices.length; i++) if (choices[i].equalsIgnoreCase(value)) return i;
        throw new IllegalArgumentException("Unsupported choice: " + value);
    }

    private static int number(String value, int low, int high, int fallback) {
        if ("random".equalsIgnoreCase(value)) return fallback;
        try {
            return ContourFlowOptions.clamp(Integer.parseInt(value), low, high);
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("Expected a number", error);
        }
    }

    private static int random(int mask, int bit, String value) {
        return "random".equalsIgnoreCase(value) ? mask | bit : mask & ~bit;
    }
}
