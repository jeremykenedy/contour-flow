package com.jeremykenedy.contourflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public final class ContourFlowSettingsCoverageTest {
    private ContourFlowPreferences preferences;
    private ContourFlowSettingsProvider provider;

    @Before
    public void setUp() {
        RuntimeEnvironment.getApplication()
                .getSharedPreferences(ContourFlowPreferences.FILE, 0)
                .edit()
                .clear()
                .commit();
        preferences = new ContourFlowPreferences(RuntimeEnvironment.getApplication());
        provider =
                Robolectric.buildContentProvider(ContourFlowSettingsProvider.class).create().get();
    }

    @Test
    public void preferencesValidatePersistAndRandomizeSupportedFields() {
        ContourFlowOptions defaults = preferences.read();
        assertEquals(0, defaults.palette);
        preferences.set("palette", "Glacier");
        preferences.set("relief", "Coast");
        preferences.set("density", "5");
        preferences.set("speed", "2");
        preferences.set("weight", "9");
        preferences.set("lighting", "Night");
        preferences.set("brightness", "1");
        ContourFlowOptions saved = preferences.read();
        assertEquals(3, saved.palette);
        assertEquals(2, saved.relief);
        assertEquals(5, saved.density);
        assertEquals(2, saved.speed);
        assertEquals(5, saved.weight);
        assertEquals(2, saved.lighting);
        assertEquals(1, saved.brightness);
        assertEquals(0, saved.randomMask);

        preferences.set("palette", "random");
        preferences.set("relief", "random");
        preferences.set("density", "random");
        preferences.set("speed", "random");
        preferences.set("weight", "random");
        preferences.set("lighting", "random");
        preferences.set("brightness", "random");
        assertEquals(127, preferences.read().randomMask);
        preferences.set("density", "4");
        assertEquals(123, preferences.read().randomMask);
        expectInvalid(() -> preferences.set("unknown", "1"));
        expectInvalid(() -> preferences.set("palette", "Forest"));
        expectInvalid(() -> preferences.set("density", "many"));
    }

    @Test
    public void providerPublishesSchemaAndSettingsMutations() {
        assertTrue(provider.onCreate());
        Uri uri = Uri.parse("content://" + ContourFlowSettingsProvider.AUTHORITY);
        assertNull(provider.query(uri, null, null, null, null));
        assertEquals("application/json", provider.getType(uri));
        String schema = json("get_schema", null, null);
        assertTrue(schema.contains("\"key\":\"relief\""));
        assertTrue(schema.contains("\"key\":\"brightness\""));
        assertTrue(json("get_settings", null, null).contains("\"palette\":\"Abyss\""));

        assertTrue(json("set_random_all", null, null).contains("\"brightness\":\"Random\""));
        assertTrue(json("set_random_none", null, null).contains("\"palette\":\"Abyss\""));
        Bundle extras = new Bundle();
        extras.putString("value", "Glacier");
        assertTrue(json("set_setting", "palette", extras).contains("\"palette\":\"Glacier\""));
        expectInvalid(() -> provider.call("set_setting", "palette", null));
        expectInvalid(() -> provider.call("set_setting", "palette", new Bundle()));
        expectInvalid(() -> provider.call("set_setting", "unknown", extras));
        expectInvalid(() -> provider.call("unsupported", null, null));
    }

    @Test
    public void providerRejectsUnsupportedMutationMethods() {
        Uri uri = Uri.parse("content://" + ContourFlowSettingsProvider.AUTHORITY);
        try {
            provider.insert(uri, new ContentValues());
            fail("insert must be rejected");
        } catch (UnsupportedOperationException expected) {
            assertNotNull(expected.getMessage());
        }
        try {
            provider.update(uri, new ContentValues(), null, null);
            fail("update must be rejected");
        } catch (UnsupportedOperationException expected) {
            assertNotNull(expected.getMessage());
        }
        try {
            provider.delete(uri, null, null);
            fail("delete must be rejected");
        } catch (UnsupportedOperationException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    private String json(String method, String arg, Bundle extras) {
        return provider.call(method, arg, extras).getString("json");
    }

    private static void expectInvalid(Runnable operation) {
        try {
            operation.run();
            fail("Expected invalid input to be rejected");
        } catch (IllegalArgumentException expected) {
            assertNotNull(expected.getMessage());
        }
    }
}
