package com.jeremykenedy.contourflow;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

public final class ContourFlowSettingsProvider extends ContentProvider {
    static final String AUTHORITY = "com.jeremykenedy.contourflow.settings";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(
            Uri uri, String[] projection, String selection, String[] args, String order) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return "application/json";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Use set_setting.");
    }

    @Override
    public int delete(Uri uri, String selection, String[] args) {
        throw new UnsupportedOperationException("Settings cannot be deleted.");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] args) {
        throw new UnsupportedOperationException("Use set_setting.");
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        if ("get_schema".equals(method)) return result(schema());
        ContourFlowPreferences preferences = new ContourFlowPreferences(getContext());
        if ("get_settings".equals(method)) return result(settings(preferences.read()));
        if ("set_random_all".equals(method) || "set_random_none".equals(method)) {
            ContourFlowOptions old = preferences.read();
            int mask = "set_random_all".equals(method) ? 127 : 0;
            preferences.write(
                    new ContourFlowOptions(
                            old.palette,
                            old.relief,
                            old.density,
                            old.speed,
                            old.weight,
                            old.lighting,
                            old.brightness,
                            mask));
            return result(settings(preferences.read()));
        }
        if ("set_setting".equals(method)) {
            if (extras == null || !extras.containsKey("value"))
                throw new IllegalArgumentException("Missing setting value.");
            preferences.set(arg, extras.getString("value"));
            return result(settings(preferences.read()));
        }
        throw new IllegalArgumentException("Unsupported settings method: " + method);
    }

    private Bundle result(String json) {
        Bundle result = new Bundle();
        result.putString("json", json);
        return result;
    }

    static String schema() {
        return "{\"schemaVersion\":1,\"provider\":\""
                + AUTHORITY
                + "\",\"fields\":["
                + choice("palette", ContourFlowOptions.PALETTES[0], ContourFlowOptions.PALETTES)
                + ","
                + choice("relief", ContourFlowOptions.RELIEFS[0], ContourFlowOptions.RELIEFS)
                + ","
                + number("density", 3, 1, 5)
                + ","
                + number("speed", 3, 1, 5)
                + ","
                + number("weight", 2, 1, 5)
                + ","
                + choice("lighting", "Auto", ContourFlowOptions.LIGHTING)
                + ","
                + number("brightness", 3, 1, 5)
                + "]}";
    }

    private static String choice(String key, String value, String[] choices) {
        StringBuilder json =
                new StringBuilder("{\"key\":\"")
                        .append(key)
                        .append("\",\"type\":\"choice\",\"default\":\"")
                        .append(value)
                        .append("\",\"random\":true,\"choices\":[");
        for (int i = 0; i < choices.length; i++) {
            if (i > 0) json.append(',');
            json.append('"').append(choices[i]).append('"');
        }
        return json.append("]}").toString();
    }

    private static String number(String key, int value, int minimum, int maximum) {
        return "{\"key\":\""
                + key
                + "\",\"type\":\"integer\",\"default\":"
                + value
                + ",\"random\":true,\"minimum\":"
                + minimum
                + ",\"maximum\":"
                + maximum
                + "}";
    }

    static String settings(ContourFlowOptions options) {
        return "{\"schemaVersion\":1,\"palette\":\""
                + value(options, 1, ContourFlowOptions.PALETTES[options.palette])
                + "\",\"relief\":\""
                + value(options, 2, ContourFlowOptions.RELIEFS[options.relief])
                + "\",\"density\":\""
                + value(options, 4, Integer.toString(options.density))
                + "\",\"speed\":\""
                + value(options, 8, Integer.toString(options.speed))
                + "\",\"weight\":\""
                + value(options, 16, Integer.toString(options.weight))
                + "\",\"lighting\":\""
                + value(options, 32, ContourFlowOptions.LIGHTING[options.lighting])
                + "\",\"brightness\":\""
                + value(options, 64, Integer.toString(options.brightness))
                + "\"}";
    }

    private static String value(ContourFlowOptions options, int bit, String value) {
        return (options.randomMask & bit) == 0 ? value : "Random";
    }
}
