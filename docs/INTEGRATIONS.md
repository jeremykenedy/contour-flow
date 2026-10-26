# Integrations

## Fire TV Toolkit

Install Toolkit from its [current release and setup guide](https://github.com/jeremykenedy/fire-tv-toolkit), then run:

```bash
firetv-screensavers --install=contour-flow --yes
screensaver --set=contour-flow
```

Remove the app with `firetv-screensavers --uninstall=contour-flow --force --yes`. Device safeguards cover only settings supported by the connected TV. Installing an update preserves the app's stored choices when the same package and signer are used.

## Fire TV UI

The reviewed catalog entry can install Contour Flow, select its DreamService, open its exported full-screen PreviewActivity, and edit the versioned settings provider. Host catalog updates are published separately from this app; check the host's current release for its pinned app version.

## Stable contract

- Package: `com.jeremykenedy.contourflow`
- Dream: `.ContourFlowDreamService`
- Preview: `.PreviewActivity`
- Settings: `.SettingsActivity`
- Authority: `com.jeremykenedy.contourflow.settings`

The seven setting keys and provider behavior are documented in [Settings provider](SETTINGS_PROVIDER.md). No new host permissions, runtime libraries or network services are required by this release.
