# Building

Install Android SDK platform 36, build-tools 36.0.0, Java, and standard shell tools. Then run:

```bash
./test.sh
./build.sh --unsigned
./build.sh
```

The unsigned APK is for inspection and cannot update a signed build. The signed build uses a repository-specific key at `~/.android/contour-flow.jks` and its password file at `~/.android/contour-flow.pass`. Both are ignored by Git. Back up both securely. Never replace the signing key for a published package.

The build verifies package identity, launcher and DreamService metadata, settings authority, and absence of Internet permission in the packaged APK.

## Functional coverage

Run `bash scripts/test-coverage.sh` to exercise the owned options, clock, preferences, provider and mesh logic with JUnit/Robolectric, then verify installer branches. The pinned Gradle 9.6.1 runner is compatible with the existing Android Gradle plugin 9.4.1; its official distribution checksum is checked before execution. These are development-only dependencies and are not bundled in the native APK.

The current gate covers 179 Java lines and 58 branches at 100%, including the previously omitted mesh. Installer coverage is 122 statements and 42 branches at 100%. Android Activity presentation, actual GPU driver/shader execution and Dream binding are separate native gates. Host renderer/scheduling stubs test control flow, not physical rendering quality.
