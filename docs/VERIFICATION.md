# Verification

Run the required local checks before release:

```bash
./test.sh
bash scripts/test-coverage.sh
python3 scripts/check-docs.py
bash scripts/check-secrets.sh
bash scripts/check-style.sh
./build.sh --unsigned
./build.sh
```

Behavior tests exercise installer rollback, seven-option validation and persistence, randomization, provider schema, elapsed scene-clock lifecycle, flat-gradient width limits, mesh interpolation and index coverage. The host SceneView fixture observes actual start/stop/detach scheduling against test-only Android stubs. These checks do not compile a GPU shader or establish visual quality.

## Native evidence

| Target | Build | Observed result | Remaining limit |
|---|---|---|---|
| API 34 Android TV emulator, native 1920 x 1080 | Candidate 5 | Native GLES preview, seven remote settings, motion and pause/recreation checks | Hardware pacing is separate |
| Amazon AFTDEC012E, API 30, native 1920 x 1080 | Candidate 5 | Three quiet compositor windows: 32.894155, 32.774628 and 32.774952 FPS; default and Coast weight contrasts inspected | Physical Contour normal-idle startup and sustained thermal behavior were not tested |
| Release package | Code 6 | Core runtime equivalence is checked against code 5; packaging gate recorded separately | Do not infer a new renderer-performance result from package equivalence |

Earlier candidates 3 and 4 remained near 20 FPS on this Fire TV and are not the release-performance evidence. Native recordings can lose encoded frames; only the separate quiet presentation windows support the pacing figures above. See [animation development](ANIMATION_REWORK.md).

## Additional device reports

A Fire TV owner, Android TV owner or Google TV owner testing another model should report the model, OS/API, actual surface resolution, settings, motion, normal-idle binding, exit behavior and results through the repository's Issues page. Native 4K output and sustained thermal behavior remain unverified.

## Coverage runner correction

The former Gradle 8.14.3 pin could not run the configured Android Gradle plugin 9.4.1, so it failed before tests. Updating only the development runner to Gradle 9.6.1, with its official checksum, allowed the required gate to execute: 179 owned Java lines/58 branches and 122 installer statements/42 branches passed at 100%. The existing mesh behavior test is included; no renderer logic or artificial coverage-only case was added.
