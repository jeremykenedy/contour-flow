"""Double precision reference for the unchanged frozen landscape, not a GPU emulator."""

import math
import re
import struct
from pathlib import Path
from types import SimpleNamespace

ROOT = Path(__file__).resolve().parents[1]
W, H = 1920, 1080
CORRECTION = re.search(
    r"float value=(.+);", (ROOT / "res/raw/contours_mesh.glsl").read_text()
).group(1)


def field(u, v, t, relief):
    px = (u - 0.5) * 4.1 + 0.32 * math.sin(t)
    py = (v - 0.5) * (4.1 * H / W) + 0.24 * math.cos(t)
    qx = px + 0.23 * math.sin(py * 1.6 + t)
    qy = py + 0.18 * math.sin(px * 1.3 - t)

    def hill(x, y, f):
        h = math.exp(-(x * x + y * y) * f)
        return h, -2 * f * x * h, -2 * f * y * h

    hills = [
        hill(qx + 1.05 + 0.16 * math.sin(t), qy + 0.18, 2.1),
        hill(qx - 0.22, qy - 0.34 - 0.18 * math.cos(t), 2.8),
        hill(qx - 1.25, qy + 0.62, 2.3),
        hill(qx + 0.88, qy - 0.78, 2.5),
        hill(qx - 0.23, qy + 0.24, 2.0),
    ]
    if relief == 0:
        h, gx, gy = 0.39, 0.0, 0.0
        factors = (0.27, 0.24, 0.24, 0.20, -0.10)
    elif relief == 1:
        h, gx, gy = 0.62, 0.0, 0.0
        factors = (-0.25, -0.22, 0.15, 0.0, -0.19)
    else:
        h, gx, gy = 0.51 - qy * 0.15, 0.0, -0.15
        factors = (0.22, -0.13, 0.0, 0.18, 0.0)
    for weight, value in zip(factors, hills):
        h += weight * value[0]
        gx += weight * value[1]
        gy += weight * value[2]
    a = qx * 2.4 + 0.35 * math.cos(qy * 1.9) + t
    b = qy * 2.6 - t
    h += math.sin(a) * math.cos(b) * 0.065
    gx += 2.4 * math.cos(a) * math.cos(b) * 0.065
    gy += (
        -0.665 * math.sin(qy * 1.9) * math.cos(a) * math.cos(b)
        - 2.6 * math.sin(a) * math.sin(b)
    ) * 0.065
    return (
        h,
        (gx + gy * 0.234 * math.cos(px * 1.3 - t)) * 4.1,
        (gy + gx * 0.368 * math.cos(py * 1.6 + t)) * (4.1 * H / W),
    )


def interpolate(u, v, t, relief, nx, ny, corrected=False):
    x = u * nx
    y = v * ny
    i = min(nx - 1, int(x))
    j = min(ny - 1, int(y))
    fx = x - i
    fy = y - j
    if fx + fy <= 1:
        weights = (1 - fx - fy, fx, fy)
        points = ((i, j), (i + 1, j), (i, j + 1))
    else:
        weights = (fx + fy - 1, 1 - fx, 1 - fy)
        points = ((i + 1, j + 1), (i, j + 1), (i + 1, j))
    f = [field(a / nx, b / ny, t, relief) for a, b in points]
    value = tuple(sum(w * p[k] for w, p in zip(weights, f)) for k in range(3))
    if not corrected:
        return value
    dot = sum(
        w * (p[1] * a / nx + p[2] * b / ny) for w, p, (a, b) in zip(weights, f, points)
    )
    data = SimpleNamespace(x=value[0], yz=value[1:], w=dot)
    corrected_value = eval(
        CORRECTION,
        {"__builtins__": {}},
        {
            "vField": data,
            "uv": (u, v),
            "dot": lambda a, b: sum(x * y for x, y in zip(a, b)),
        },
    )
    return corrected_value, value[1], value[2]


def single(value):
    return struct.unpack("f", struct.pack("f", value))[0]


def transported(u, v, t, relief):
    """Float32 varying transport model; this does not emulate shader instructions."""
    nx, ny = 256, 144
    x, y = u * nx, v * ny
    i, j = min(nx - 1, int(x)), min(ny - 1, int(y))
    fx, fy = x - i, y - j
    if fx + fy <= 1:
        weights, points = (1 - fx - fy, fx, fy), ((i, j), (i + 1, j), (i, j + 1))
    else:
        weights, points = (
            (fx + fy - 1, 1 - fx, 1 - fy),
            ((i + 1, j + 1), (i, j + 1), (i + 1, j)),
        )
    samples = []
    for a, b in points:
        xx, yy = single(a / nx), single(b / ny)
        height, gx, gy = map(single, field(xx, yy, single(t), relief))
        product = single(single(gx * xx) + single(gy * yy))
        samples.append((height, gx, gy, product))
    value = [single(sum(w * p[k] for w, p in zip(weights, samples))) for k in range(4)]
    data = SimpleNamespace(x=value[0], yz=value[1:3], w=value[3])
    actual = eval(
        CORRECTION,
        {"__builtins__": {}},
        {
            "vField": data,
            "uv": (single(u), single(v)),
            "dot": lambda a, b: single(single(a[0] * b[0]) + single(a[1] * b[1])),
        },
    )
    return single(actual)
