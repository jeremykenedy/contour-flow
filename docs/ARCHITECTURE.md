# Architecture

Contour Flow is a native Android app with no runtime libraries. `ContourFlowDreamService` creates the animated `ContourFlowSceneView` when Android starts the screensaver. The view samples a low-resolution scalar relief field, traces its contour crossings, and draws the resulting paths with Android Canvas at the size supplied by the system.

The relief field shifts slowly over time. Frame callbacks run only while the preview or dream is visible and stop when the activity pauses or the dream stops. Density controls contour levels and speed controls field movement. No text or symbols are drawn in the screensaver scene.

`ContourFlowOptions` validates choices and resolves random values once per session. `ContourFlowPreferences` stores user choices locally. `ContourFlowSettingsProvider` publishes the documented schema for host apps. The settings activity supports D-pad navigation.

The DreamService component is `com.jeremykenedy.contourflow/.ContourFlowDreamService`. The manifest includes the DreamService bind permission, action, metadata and preview resource. The app does not request Internet access.
