# Troubleshooting

## Contour Flow does not appear in the screensaver list

Confirm that the APK installed successfully and that the device supports Android DreamService screensavers. Open the device's display or screensaver settings and refresh the available services. Menu names vary by manufacturer.

## The preview opens to a blank surface

Wait for the surface to start, then back out and reopen the preview. Check `adb logcat` for process or graphics errors. Include the device model and Android API in a bug report.

## The installer reports a checksum mismatch

Download both release assets again and keep them in the same directory. Do not install an APK whose checksum does not match its accompanying `.sha256` file.

## Settings do not change the active scene

Some devices recreate the dream only after it is restarted. Stop the current preview or screensaver and start it again so saved options and per-session random values are loaded.
