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
| API 34 Google TV emulator, native 1920 x 1080 | Code 6 | Real normal-idle Dream after 301.454 seconds, visible animated pixels, 20-second recording, native Preview/Settings/Credits and ordinary Back exit | Emulator recording is not hardware pacing or a FireOS natural-idle result |

Earlier candidates 3 and 4 remained near 20 FPS on this Fire TV and are not the release-performance evidence. Native recordings can lose encoded frames; only the separate quiet presentation windows support the pacing figures above. See [animation development](ANIMATION_REWORK.md).

## Additional device reports

A Fire TV owner, Android TV owner or Google TV owner testing another model should report the model, OS/API, actual surface resolution, settings, motion, normal-idle binding, exit behavior and results through the repository's Issues page. Native 4K output and sustained thermal behavior remain unverified.

## Coverage runner correction

The former Gradle 8.14.3 pin could not run the configured Android Gradle plugin 9.4.1, so it failed before tests. Updating only the development runner to Gradle 9.6.1, with its official checksum, allowed the required gate to execute: 179 owned Java lines/58 branches and 122 installer statements/42 branches passed at 100%. The existing mesh behavior test is included; no renderer logic or artificial coverage-only case was added.

## Final code 6 packaging gate

The same-signer upgrade to 1.1.0/code 6 was installed explicitly for current Android user 0. All seven saved options and per-user presence flags were preserved. Actual Dream records showed the expected component, user 0, `isPreview=false` and `mBound=true` after 301.454 seconds of configured idle with no manual Dream command. The actual BLAST surface was 1920 x 1080, using the 256 x 144 mesh path and reported fragment precision 23. Settled native pixels and a second frame twenty seconds later showed animation. Ordinary Back removed the Dream surface and returned to a Dream-null state.

The retained recording spans 19.684156 seconds and contains 969 encoded frames. Its SHA-256 is `ab6f4ec0c6c05a04edab01e9b7919614d11a25aff76485af2441c25484917f6c`. Encoded frame counts are not native hardware presentation measurements. The separate Sandstone/Coast Preview and native Settings/Credits were inspected; the full legal reader body matched bundled files.

Final strict restoration passed with no errors: OS19, all 34 package UID identities, both tested savers' options and per-user presence, external files including 32 icons and two pictures, permission booleans/flags and configured app-op modes, accessibility and guard state. Both upgraded savers were stopped without a PID; Google Home was Awake with no Dream. UI72 remained stopped and unexecuted. Its 14 typed private settings preservation references the preceding strict nonexecution proof, not a new private export. No app-op grants, Home changes or accessibility changes were needed for the gate; temporary normal-idle configuration was restored exactly.

The standalone installer separately captures and validates the current Android user before mutation. Installation, removal and secure selection backup/readback/rollback/restoration target that captured user. A mismatched backup user fails closed; legacy userless backups apply only to user 0. The existing 19 installer tests passed. These mocked tests do not establish behavior on every vendor profile implementation.

## Release artifact

| Fact | Value |
|---|---|
| Version | 1.1.0, code 6 |
| APK bytes | 66,572 |
| APK SHA-256 | `6b4302ee3220c25df5b40c3e7ff6d72d6c859b4403203f0577a01a9b7cc2ebf6` |
| Signer certificate SHA-256 | `4b9b57d5eb49024959ea6eb2f87e2cc41a1a1db128ab8b58070b3ddfbefc9d27` |

Code 6 normal-idle FireOS behavior, native 4K, sustained thermals, broad hardware compatibility and owner visual acceptance remain unverified. The current [gallery](ARTWORK.md) and machine-readable [provenance](../art/screenshots/provenance.json) distinguish code 6 emulator evidence from code 5 physical evidence.
