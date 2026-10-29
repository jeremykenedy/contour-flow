"""CPU evaluation of the shader's pure scalar band math, not a GPU render test."""

import math
import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = (ROOT / "res/raw/contours.glsl").read_text()
BODY = re.search(r"float contourBand\([^)]*\)\s*\{([^{}]+)\}", SOURCE).group(1)
SLOPES = re.findall(r"const float MIN_SLOPE=([0-9.]+);", SOURCE)
FLAT = re.findall(r"const float FLAT_SLOPE=([0-9.]+);", SOURCE)


def smoothstep(first, last, value):
    t = max(0.0, min(1.0, (value - first) / (last - first)))
    return t * t * (3.0 - 2.0 * t)


def band(distance, footprint, inner, outer, precision=0):
    # Execute only scalar assignments/return from the actual production function.
    # This deliberately does not interpret the scene, shader types or driver behavior.
    values = {
        "distance": distance,
        "footprint": footprint,
        "innerPixels": inner,
        "outerPixels": outer,
        "MIN_SLOPE": float(SLOPES[precision]),
        "FLAT_SLOPE": float(FLAT[precision]),
        "max": max,
        "smoothstep": smoothstep,
    }
    for statement in BODY.split(";"):
        statement = statement.strip()
        if not statement:
            continue
        if statement.startswith("return "):
            return eval(statement[7:], {"__builtins__": {}}, values)
        assignment = re.fullmatch(r"float (\w+)=(.+)", statement)
        if not assignment:
            raise AssertionError("Unsupported scalar shader expression")
        values[assignment[1]] = eval(assignment[2], {"__builtins__": {}}, values)
    raise AssertionError("Shader function has no return")


class ContourWidth(unittest.TestCase):
    def test_half_width_is_in_pixels_even_on_shallow_coast_slopes(self):
        for slope in (0.000004, 0.00001, 0.0001, 0.001):
            for weight in (1.07, 1.49, 2.33, 2.75):
                half_width = weight + 0.4
                self.assertAlmostEqual(
                    band(half_width * slope, slope, weight, weight + 0.8),
                    0.5,
                    places=6,
                )
                self.assertEqual(
                    band((weight + 1) * slope, slope, weight, weight + 0.8),
                    0,
                )

    def test_flat_gradients_are_finite_and_invisible_at_both_precision_levels(self):
        for precision in (0, 1):
            for slope in (0, 1e-12, float(SLOPES[precision])):
                for distance in (0, 0.0001, 0.5):
                    alpha = band(distance, slope, 1.49, 2.29, precision)
                    self.assertTrue(math.isfinite(alpha))
                    self.assertEqual(alpha, 0)
            middle = (float(SLOPES[precision]) + float(FLAT[precision])) / 2
            self.assertGreater(band(0, middle, 1.49, 2.29, precision), 0)
            self.assertLess(band(0, middle, 1.49, 2.29, precision), 1)

    def test_line_weight_changes_width_without_reintroducing_a_flat_filled_band(self):
        slope = 0.00001
        distance = 2 * slope
        self.assertEqual(band(distance, slope, 1.07, 1.87), 0)
        self.assertGreater(band(distance, slope, 2.33, 3.13), 0.99)
        for weight in (1.07, 1.49, 2.33, 2.75):
            self.assertEqual(band(0, 0, weight, weight + 0.8), 0)


if __name__ == "__main__":
    unittest.main()
