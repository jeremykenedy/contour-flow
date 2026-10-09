# Contributing

Open a focused pull request with a clear description of the behavior changed.
Keep the application offline and do not add tracking, analytics, advertising,
or remote configuration. Preserve the package ID and release signing identity.

Run `./test.sh`, `./build.sh --unsigned`, `bash scripts/check-style.sh`,
`python3 scripts/check-docs.py`, and `bash scripts/test-coverage.sh` before
submitting. Include device model, OS/API, resolution, settings, and observed
behavior when a change affects rendering or TV navigation.
