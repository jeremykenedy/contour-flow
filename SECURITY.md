# Security policy

Report security issues through GitHub's private
[security advisory form](https://github.com/jeremykenedy/contour-flow/security/advisories/new).
Do not include personal device identifiers or private keys in public issues.

The app does not request network access. The exported settings provider accepts
only the documented local settings operations. The ADB installer validates
device serials, verifies APK checksums, backs up device screensaver settings,
and restores them before uninstalling the app.
