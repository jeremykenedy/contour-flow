package com.jeremykenedy.contourflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class ContourFlowOptionsCoverageTest {
    @Test
    public void defaultsAndBoundsAreStable() {
        ContourFlowOptions defaults = ContourFlowOptions.defaults();
        assertEquals(0, defaults.palette);
        assertEquals(0, defaults.relief);
        assertEquals(3, defaults.density);
        assertEquals(3, defaults.speed);
        assertEquals(2, defaults.weight);
        assertEquals(3, defaults.brightness);
        assertEquals(0, defaults.randomMask);
        assertEquals(defaults, defaults.resolve(42));

        ContourFlowOptions bounded = new ContourFlowOptions(-1, 8, -2, 9, 0, 4, 99, 255);
        assertEquals(0, bounded.palette);
        assertEquals(2, bounded.relief);
        assertEquals(1, bounded.density);
        assertEquals(5, bounded.speed);
        assertEquals(1, bounded.weight);
        assertEquals(2, bounded.lighting);
        assertEquals(5, bounded.brightness);
        assertEquals(127, bounded.randomMask);
        assertEquals(1, ContourFlowOptions.clamp(0, 1, 5));
        assertEquals(5, ContourFlowOptions.clamp(8, 1, 5));
        assertEquals(3, ContourFlowOptions.clamp(3, 1, 5));
    }

    @Test
    public void randomSelectionsResolveWithinTheirRanges() {
        ContourFlowOptions all = new ContourFlowOptions(0, 0, 1, 1, 1, 0, 1, 127);
        ContourFlowOptions resolved = all.resolve(8192);
        assertEquals(0, resolved.randomMask);
        assertTrue(resolved.palette >= 0 && resolved.palette < 4);
        assertTrue(resolved.relief >= 0 && resolved.relief < 3);
        assertTrue(resolved.density >= 1 && resolved.density <= 5);
        assertTrue(resolved.speed >= 1 && resolved.speed <= 5);
        assertTrue(resolved.weight >= 1 && resolved.weight <= 5);
        assertTrue(resolved.lighting >= 0 && resolved.lighting < 3);
        assertTrue(resolved.brightness >= 1 && resolved.brightness <= 5);
        assertNotSame(all, resolved);
        assertEquals(resolved.palette, all.resolve(8192).palette);
    }

    @Test
    public void fixedSettingsAreUnaffectedByRandomSelection() {
        ContourFlowOptions fixed = new ContourFlowOptions(3, 2, 4, 2, 5, 1, 3, 4 | 32 | 64);
        ContourFlowOptions resolved = fixed.resolve(91);
        assertEquals(3, resolved.palette);
        assertEquals(2, resolved.relief);
        assertTrue(resolved.density >= 1 && resolved.density <= 5);
        assertTrue(resolved.lighting >= 0 && resolved.lighting < 3);
        assertTrue(resolved.brightness >= 1 && resolved.brightness <= 5);
        assertEquals(0, resolved.randomMask);
    }
}
