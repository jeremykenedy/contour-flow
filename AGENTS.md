# Project standards

- Keep the Android application offline. Do not add network permission, ads, analytics, tracking, telemetry, or remote configuration.
- Preserve the package ID and the release signing key for all upgrades.
- Keep the contour scene continuously animated, with no labels, symbols, watermark, or other text in the rendered scene.
- Render at the Android-provided surface size. Do not force 4K without target-device measurements.
- Stop frame scheduling and release drawing resources when the view or DreamService stops.
- Expose supported settings through `ContourFlowSettingsProvider` and keep its schema documented in `docs/SETTINGS_PROVIDER.md`.
- Use Android platform APIs and keep runtime dependencies out of the app unless a requirement needs them.
- Keep signing keys, passwords, device backups, and build outputs out of Git.
- Use Apache License 2.0 and retain notices for any future third-party content.
- Do not add Aikido or Scrutinizer integrations or badges.
- Run `./test.sh`, `./build.sh --unsigned`, `bash scripts/check-style.sh`, `python3 scripts/check-docs.py`, and `bash scripts/test-coverage.sh` before release.
- Record emulator and physical-device evidence separately. Request missing-device reports with model, OS/API, resolution, behavior, and results.
