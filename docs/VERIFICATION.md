# Verification

Local checks:

```bash
./test.sh
bash scripts/test-coverage.sh
python3 scripts/check-docs.py
bash scripts/check-secrets.sh
PATH="$(pwd)/build/coverage/python/bin:$PATH" bash scripts/check-style.sh
./build.sh --unsigned
./build.sh
```

The tests cover installer behavior, settings validation and persistence, randomization, the provider schema, and relief-field bounds, shape variation, and contour interpolation. Coverage measures project-owned options, field math, preference, provider, and installer logic. The Android Canvas renderer and framework lifecycle are verified on an emulator and are excluded from JVM logic coverage.

| Device | OS/API | Resolution | Result |
|---|---|---|---|
| Google TV emulator `aql-googletv34` | Android 14, API 34 | 1920 x 1080 | Signed APK installed. Full-screen preview opened by D-pad Select after launch focus was verified. Palette changed through settings by D-pad. Glacier and Day scene capture inspected after 45 seconds; scene pixels changed between captures. DreamService selection/idle activation was not tested. |
| Fire TV hardware | Untested | Not recorded | Looking for a Fire TV owner to test selection, idle startup, remote settings, and exit. Please report model, Fire OS/API, resolution, behavior, and results through Issues. |
| Android TV hardware | Untested | Not recorded | Looking for an Android TV owner to test selection, idle startup, remote settings, and exit. Please report model, OS/API, resolution, behavior, and results through Issues. |
| Google TV hardware | Untested | Not recorded | Looking for a Google TV owner to test selection, idle startup, remote settings, and exit. Please report model, OS/API, resolution, behavior, and results through Issues. |
| Native 4K and sustained thermals | Untested | Not recorded | Requires a physical 4K television. |
