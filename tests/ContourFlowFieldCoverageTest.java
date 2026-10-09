package com.jeremykenedy.contourflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class ContourFlowFieldCoverageTest {
    @Test
    public void reliefShapesRemainBoundedAndChangeOverTime() {
        for (int relief = 0; relief < ContourFlowOptions.RELIEFS.length; relief++) {
            float first = ContourFlowField.sample(0.1f, -0.4f, 0.2f, relief);
            float later = ContourFlowField.sample(0.1f, -0.4f, 1.7f, relief);
            assertTrue(first >= 0.02f && first <= 0.98f);
            assertTrue(later >= 0.02f && later <= 0.98f);
            assertNotEquals(first, later, 0.0001f);
        }
        float minimum = 1f;
        float maximum = 0f;
        for (int x = -20; x <= 20; x++) {
            for (int y = -12; y <= 12; y++) {
                float value = ContourFlowField.sample(x * 0.17f, y * 0.19f, 2.5f, 0);
                minimum = Math.min(minimum, value);
                maximum = Math.max(maximum, value);
            }
        }
        assertTrue(minimum >= 0.02f);
        assertTrue(maximum <= 0.98f);
        assertNotEquals(minimum, maximum, 0.01f);
    }

    @Test
    public void edgeCrossingsClampAndHandleFlatSamples() {
        assertEquals(0.5f, ContourFlowField.interpolate(0f, 1f, 0.5f), 0.0001f);
        assertEquals(0.25f, ContourFlowField.interpolate(0f, 1f, 0.25f), 0.0001f);
        assertEquals(0f, ContourFlowField.interpolate(0f, 1f, -2f), 0f);
        assertEquals(1f, ContourFlowField.interpolate(0f, 1f, 2f), 0f);
        assertEquals(0.5f, ContourFlowField.interpolate(0.3f, 0.300001f, 0.9f), 0f);
    }
}
