# Releasing

Keep the package ID, signing key, component, and settings schema stable for compatible updates. For each release:

1. Run tests, coverage, style, documentation, secret scan, and signed and unsigned builds.
2. Inspect the APK package, permissions, DreamService metadata, settings provider, and signing certificate.
3. Install and inspect on the available Android TV or Google TV emulator. Record platform and resolution.
4. Review README, screenshots, device matrix, and Apache notices.
5. Create a SemVer tag and publish the signed APK with its sibling `.sha256` file and release notes.

Do not replace an existing tag or modify a published release artifact. Make fixes in a new patch release. Preserve the signing key and password for future upgrades.
