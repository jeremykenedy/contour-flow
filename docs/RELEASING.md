# Releasing

Keep the package ID, signing key, component, and settings schema stable for compatible updates. For each release:

1. Run tests, coverage, style, documentation, secret scan, and signed and unsigned builds.
2. Inspect the APK package, permissions, DreamService metadata, settings provider, and signing certificate.
3. Install and inspect on the available Android TV or Google TV emulator. Record platform and resolution.
4. Review README, screenshots, device matrix, and Apache notices.
5. Merge through normal required CI, fetch and check out the verified remote primary tip, rebuild and compare the tested payload before tagging that exact commit. Publish only from primary with a matching SemVer tag, the signed APK, its sibling `.sha256` file and release notes.
6. Download the public files anonymously and verify their bytes, package, version and original signing certificate. Update and read back About, website and repository topics.

Do not replace an existing tag or modify a published release artifact. Make fixes in a new patch release. Preserve the signing key and password for future upgrades.
