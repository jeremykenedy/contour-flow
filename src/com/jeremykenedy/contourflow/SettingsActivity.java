package com.jeremykenedy.contourflow;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class SettingsActivity extends Activity {
    private static final String[] KEYS = {
        "palette", "relief", "density", "speed", "weight", "lighting", "brightness"
    };
    private static final String[] LABELS = {
        "Color palette",
        "Landform",
        "Contour density",
        "Drift speed",
        "Line weight",
        "Lighting",
        "Brightness"
    };
    private static final int[] BITS = {1, 2, 4, 8, 16, 32, 64};
    private static final String[][] VALUES = {
        ContourFlowOptions.PALETTES,
        ContourFlowOptions.RELIEFS,
        {"1", "2", "3", "4", "5"},
        {"1", "2", "3", "4", "5"},
        {"1", "2", "3", "4", "5"},
        ContourFlowOptions.LIGHTING,
        {"1", "2", "3", "4", "5"}
    };
    private ContourFlowPreferences preferences;
    private LinearLayout rows;
    private int focusedRow = -1;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        preferences = new ContourFlowPreferences(this);
        ScrollView scroll = new ScrollView(this);
        rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        rows.setPadding(36, 24, 36, 24);
        TextView title = new TextView(this);
        title.setText("Contour Flow settings");
        title.setTextSize(28);
        title.setPadding(0, 0, 0, 14);
        rows.addView(title);
        Button randomAll = button("Randomize supported settings");
        randomAll.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ContourFlowOptions old = preferences.read();
                        preferences.write(
                                new ContourFlowOptions(
                                        old.palette,
                                        old.relief,
                                        old.density,
                                        old.speed,
                                        old.weight,
                                        old.lighting,
                                        old.brightness,
                                        127));
                        renderRows();
                        rows.getChildAt(1).requestFocus();
                    }
                });
        rows.addView(randomAll);
        Button fixedAll = button("Use fixed settings");
        fixedAll.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ContourFlowOptions old = preferences.read();
                        preferences.write(
                                new ContourFlowOptions(
                                        old.palette,
                                        old.relief,
                                        old.density,
                                        old.speed,
                                        old.weight,
                                        old.lighting,
                                        old.brightness,
                                        0));
                        renderRows();
                        rows.getChildAt(2).requestFocus();
                    }
                });
        rows.addView(fixedAll);
        scroll.addView(rows);
        setContentView(scroll);
        renderRows();
    }

    private void renderRows() {
        while (rows.getChildCount() > 3) rows.removeViewAt(3);
        ContourFlowOptions options = preferences.read();
        String[] active = {
            ContourFlowOptions.PALETTES[options.palette],
            ContourFlowOptions.RELIEFS[options.relief],
            Integer.toString(options.density),
            Integer.toString(options.speed),
            Integer.toString(options.weight),
            ContourFlowOptions.LIGHTING[options.lighting],
            Integer.toString(options.brightness)
        };
        Button[] buttons = new Button[KEYS.length];
        for (int i = 0; i < KEYS.length; i++) {
            buttons[i] = addRow(i, active[i], (options.randomMask & BITS[i]) != 0);
        }
        buttons[focusedRow >= 0 && focusedRow < buttons.length ? focusedRow : 0].requestFocus();
    }

    private Button addRow(int index, String active, boolean random) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView label = new TextView(this);
        label.setText(LABELS[index]);
        label.setTextSize(20);
        row.addView(label, new LinearLayout.LayoutParams(0, -2, 1f));
        Button value = button(random ? "Random" : displayValue(index, active));
        value.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        focusedRow = index;
                        preferences.set(KEYS[index], nextValue(index, active, random));
                        renderRows();
                    }
                });
        row.addView(value, new LinearLayout.LayoutParams(320, -2));
        rows.addView(row, new LinearLayout.LayoutParams(-1, -2));
        return value;
    }

    private String nextValue(int index, String active, boolean random) {
        if (random) return VALUES[index][0];
        for (int i = 0; i < VALUES[index].length; i++) {
            if (VALUES[index][i].equalsIgnoreCase(active)) {
                return i + 1 == VALUES[index].length ? "random" : VALUES[index][i + 1];
            }
        }
        return VALUES[index][0];
    }

    private String displayValue(int index, String value) {
        if (index == 2)
            return new String[] {"Sparse", "Open", "Balanced", "Dense", "Very dense"}
                    [Integer.parseInt(value) - 1];
        if (index == 3)
            return new String[] {"Very slow", "Slow", "Balanced", "Quick", "Fast"}
                    [Integer.parseInt(value) - 1];
        if (index == 4)
            return new String[] {"Fine", "Light", "Balanced", "Bold", "Heavy"}
                    [Integer.parseInt(value) - 1];
        if (index == 6)
            return new String[] {"Dim", "Low", "Balanced", "Bright", "Very bright"}
                    [Integer.parseInt(value) - 1];
        return value;
    }

    private Button button(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(18);
        button.setFocusable(true);
        return button;
    }
}
