# Installation

Download `contour-flow.apk` and `contour-flow.apk.sha256` from the [latest release](https://github.com/jeremykenedy/contour-flow/releases/latest). The installer requires Python 3 and ADB.

```bash
adb connect TV_IP:5555
python3 install.py --device TV_IP:5555 --apk ~/Downloads/contour-flow.apk
```

The installer checks the APK checksum, records the current screensaver and enabled state, previews the planned changes, and requires confirmation. Use `--restore` to restore the saved screensaver selection. Use `--uninstall` to restore the selection and remove Contour Flow.

To install without the helper, use Android's package installer. Open Contour Flow to preview the scene and adjust its settings. Select it in the device's screensaver settings to use it as the system dream. Device menu names vary.

Fire TV Toolkit supports catalogued apps through a guided terminal flow. After this repository is registered, its documented commands install, select, and remove Contour Flow. The Fire TV UI app can select an installed DreamService but does not install APKs from GitHub.

## Android users

The installer queries and validates the TV's current Android user before confirmation. APK installation, removal and secure screensaver settings are all scoped to that same captured user. Other Android users are preserved. A failed or ambiguous user query stops before changes.

New state files record both device and Android user. A backup for another user is rejected; use the existing `--state-file` option for a separate backup. Older state files without a user field remain compatible with Android user 0 only. Do not reuse those files while another Android user is active. TV account profiles and Android system users are different concepts.
