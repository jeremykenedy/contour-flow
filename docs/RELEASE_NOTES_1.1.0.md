# Contour Flow 1.1.0

Continuously evolving topographic relief replaces the original nearly still Canvas scene. All seven settings, existing saved values, package identity and upgrade signer remain compatible.

## Upgrade guide

Update from 1.0.0 using the same signed package through the standalone installer or a current host catalog. Existing palette, relief, density, speed, weight, lighting, brightness and random choices remain stored. No account, migration, new permission or runtime dependency is needed. Open the full-screen Preview, check motion and settings, then retain your selected system screensaver and timers. Keep your prior signed APK for recovery; Android may reject a downgrade, so do not uninstall if you need to preserve app data.

## Breaking changes

None to the package, minimum Android API 23, seven-option settings contract or offline behavior. The scene's rendering and motion are intentionally different from 1.0.0.

## Major changes

- Continuous native GLES relief with clear evolving ridges, basins and coasts.
- Gradient-corrected mesh field evaluation where driver precision permits, full-fragment fallback otherwise, and full supplied surface size.
- Analytic line antialiasing and stable thin lines on shallow terrain.

## Minor changes and fixes

- Visible-vsync scheduling and paused elapsed-clock retention.
- Native-derived system preview and offline Credits and licenses entry.
- Current code 6 native Dream, Coast, Settings and Credits gallery with raw capture provenance.
- Standalone installation and removal explicitly target the captured current Android user; selection backups and restoration reject a different user.

## Verification and limits

See [verification](VERIFICATION.md) for source checks, native build provenance and measured device results. Code 5 measured about 32.8 FPS at an actual 1920 x 1080 surface on one API 30 Fire TV. Code 6 preserves that core runtime and adds packaging; its final API 34 packaging gate passed with a real normal-idle Dream after 301.454 seconds, visible animation at an actual 1920 x 1080 surface, native Settings/Credits and exact final state restoration. Physical code 6 FireOS normal-idle behavior remains unverified. Native 4K, sustained thermal durability and broad owner visual acceptance remain unverified. Do not treat emulator or encoded-video frame rates as hardware pacing evidence.

The app remains offline with no analytics, tracking, ads or runtime network permission. Notices and the complete Apache license are bundled.

[Compare changes](https://github.com/jeremykenedy/contour-flow/compare/v1.0.0...v1.1.0)

## Signed package

The 1.1.0/code 6 APK is 66,572 bytes with SHA-256 `6b4302ee3220c25df5b40c3e7ff6d72d6c859b4403203f0577a01a9b7cc2ebf6`. Its upgrade certificate SHA-256 is `4b9b57d5eb49024959ea6eb2f87e2cc41a1a1db128ab8b58070b3ddfbefc9d27`. See [verification](VERIFICATION.md) for completed coverage, native evidence and remaining limits.
