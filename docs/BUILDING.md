# Building

Install Android SDK platform 36, build-tools 36.0.0, Java, and standard shell tools. Then run:

```bash
./test.sh
./build.sh --unsigned
./build.sh
```

The unsigned APK is for inspection and cannot update a signed build. The signed build uses a repository-specific key at `~/.android/contour-flow.jks` and its password file at `~/.android/contour-flow.pass`. Both are ignored by Git. Back up both securely. Never replace the signing key for a published package.

The build verifies package identity, launcher and DreamService metadata, settings authority, and absence of Internet permission in the packaged APK.
