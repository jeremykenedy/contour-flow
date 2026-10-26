# Architecture

Contour Flow is an offline Android DreamService with no runtime libraries. The DreamService and exported PreviewActivity share the same GLES 2 SceneView. Choreographer schedules frames only while visible. Its elapsed clock preserves paused progress and removes callbacks on stop and detach.

The relief field evolves continuously. On fragment drivers with at least 23 precision bits, a 256 by 144 vertex grid evaluates smooth height and analytic gradients, then interpolates those values for full-surface contour shading. Other drivers use the original fragment evaluation path. Both paths use the Android-provided surface size, analytic line antialiasing and a flat-gradient fade. There is no reduced-resolution render target or photographic backdrop.

Options, preferences and the versioned settings provider retain all seven keys, choices, random flags and defaults. The package and signer remain compatible with the original release. Settings has an offline Credits and licenses screen. It reads bundled project notices and Apache license text; it does not alter the scene or contact a server.

Code 6 packages the verified code 5 scene with a native-derived preview resource and credits. Settings changes for that entry are reported separately from the unchanged scene classes. Exact build comparison and native package checks are recorded before publication.
