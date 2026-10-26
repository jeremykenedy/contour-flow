package com.jeremykenedy.contourflow;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/** The complete project notices are available without a network connection. */
public final class CreditsActivity extends Activity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll = new ScrollView(this);
        TextView text = new TextView(this);
        text.setTextSize(18);
        text.setPadding(36, 24, 36, 24);
        try {
            text.setText(
                    "Contour Flow credits\n\n"
                            + read("NOTICE.txt")
                            + "\n\nThe scene and captured preview are original project artwork. "
                            + "No third-party scene media or runtime libraries are included.\n\n"
                            + read("LICENSE.txt"));
        } catch (IOException unavailable) {
            text.setText(
                    "The bundled notices could not be read. The project is licensed under Apache"
                            + " License 2.0; its complete LICENSE and NOTICE accompany the release"
                            + " source.");
        }
        scroll.setFocusable(true);
        scroll.addView(text);
        setContentView(scroll);
        scroll.requestFocus();
    }

    private String read(String name) throws IOException {
        StringBuilder text = new StringBuilder();
        try (InputStream input = getAssets().open(name);
                Reader reader = new InputStreamReader(input, "UTF-8")) {
            char[] buffer = new char[2048];
            int count;
            while ((count = reader.read(buffer)) != -1) {
                if (text.length() + count > 65536) throw new IOException("Notice is too large.");
                text.append(buffer, 0, count);
            }
        }
        return text.toString();
    }
}
