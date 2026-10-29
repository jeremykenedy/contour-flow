"""Observable field-error checks; shader compilation and image quality remain native gates."""

import hashlib
import math
import random
import re
import unittest
from types import SimpleNamespace

from contour_field_reference import (
    CORRECTION,
    ROOT,
    H,
    W,
    field,
    interpolate,
    transported,
)


class ContourField(unittest.TestCase):
    def test_original_fragment_fallback_is_unchanged(self):
        self.assertEqual(
            hashlib.sha256((ROOT / "res/raw/contours.glsl").read_bytes()).hexdigest(),
            "c00279dc4a6a3dcfd8a147c99ec27416b1cb572e61a391c44cb6cd35bde9e4e1",
        )

    def test_vertex_field_keeps_the_original_landscape_equations(self):
        original = (ROOT / "res/raw/contours.glsl").read_text()
        vertex = (ROOT / "res/raw/contours_vertex.glsl").read_text()
        # The numerical reference is not a GLSL interpreter. Keep its landscape
        # oracle tied to the exact original field, including the analytic gradient.
        original_field = original[
            original.index("    float t=uPhase;") : original.index("    vec3 normal=")
        ]
        vertex_field = vertex[
            vertex.index("    float t=uPhase;") : vertex.index("    vec2 gradientUV=")
        ]
        self.assertEqual(
            re.sub(r"\s+", "", original_field), re.sub(r"\s+", "", vertex_field)
        )

    def test_production_correction_reconstructs_planar_and_quadratic_fields(self):
        rng = random.Random(491)
        points = ((0.2, 0.1), (0.9, 0.2), (0.3, 0.8))
        for _ in range(100):
            a, b, c, d, e, f = (rng.uniform(-2, 2) for _ in range(6))
            weights = [rng.random() for _ in range(3)]
            weights = [w / sum(weights) for w in weights]
            u, v = (sum(w * p[k] for w, p in zip(weights, points)) for k in (0, 1))

            def value(x, y, coefficients=(a, b, c, d, e, f)):
                a, b, c, d, e, f = coefficients
                return a * x * x + b * x * y + c * y * y + d * x + e * y + f

            samples = [
                (value(x, y), 2 * a * x + b * y + d, b * x + 2 * c * y + e)
                for x, y in points
            ]
            linear = [sum(w * p[k] for w, p in zip(weights, samples)) for k in range(3)]
            weighted_dot = sum(
                w * (s[1] * p[0] + s[2] * p[1])
                for w, s, p in zip(weights, samples, points)
            )
            actual = eval(
                CORRECTION,
                {"__builtins__": {}},
                {
                    "vField": SimpleNamespace(
                        x=linear[0], yz=linear[1:], w=weighted_dot
                    ),
                    "uv": (u, v),
                    "dot": lambda x, y: sum(a * b for a, b in zip(x, y)),
                },
            )
            self.assertAlmostEqual(actual, value(u, v), places=12)

    def test_reference_gradient_matches_height_changes(self):
        rng = random.Random(732)
        for _ in range(30):
            u, v, phase = rng.random(), rng.random(), rng.random() * 2 * math.pi
            for relief in range(3):
                _, gx, gy = field(u, v, phase, relief)
                step = 1e-6
                dx = (
                    field(u + step, v, phase, relief)[0]
                    - field(u - step, v, phase, relief)[0]
                ) / (2 * step)
                dy = (
                    field(u, v + step, phase, relief)[0]
                    - field(u, v - step, phase, relief)[0]
                ) / (2 * step)
                self.assertAlmostEqual(gx, dx, places=7)
                self.assertAlmostEqual(gy, dy, places=7)

    def test_sampled_curved_contours_keep_subpixel_field_displacement(self):
        rng = random.Random(3107)
        largest = 0
        plain_total = corrected_total = 0
        for relief in range(3):
            for _ in range(2500):
                u, v, phase = rng.random(), rng.random(), rng.random() * 2 * math.pi
                height, gx, gy = field(u, v, phase, relief)
                footprint = math.hypot(gx / W, gy / H)
                plain = interpolate(u, v, phase, relief, 256, 144)[0]
                corrected = interpolate(u, v, phase, relief, 256, 144, True)[0]
                self.assertTrue(math.isfinite(corrected))
                error = abs(height - corrected) / max(footprint, 1e-7)
                largest = max(largest, error)
                corrected_total += error
                plain_total += abs(height - plain) / max(footprint, 1e-7)
        self.assertLess(largest, 0.1)
        self.assertLess(corrected_total, plain_total / 30)

    def test_float32_varying_transport_keeps_sampled_displacement_subpixel(self):
        rng = random.Random(3107)
        largest = 0
        for relief in range(3):
            for _ in range(2500):
                u, v, phase = rng.random(), rng.random(), rng.random() * 2 * math.pi
                height, gx, gy = field(u, v, phase, relief)
                actual = transported(u, v, phase, relief)
                footprint = math.hypot(gx / W, gy / H)
                largest = max(largest, abs(height - actual) / max(footprint, 1e-7))
        self.assertLess(largest, 0.1)


if __name__ == "__main__":
    unittest.main()
