# Continuous relief experiment

The verified starting primary commit is `020d0db162771f48c27ee38d680c8d5aaa67cdbb`.
The original frame handler and Activity/Dream callbacks were active in source.
The apparent stillness has a concrete pacing cause: default phase advances at
0.086 per second and field terms attenuate that again by factors of 0.08 to
0.24 with small displacement amplitudes. This is a source diagnosis, not a
fresh physical-TV reproduction.

This prototype replaces coarse Canvas marching-square paths with one native
GLES 2 fullscreen pass. A continuous analytic field produces smooth contour
bands over shaded relief. Its hills advect and evolve visibly by design;
actual moving quality must still be checked natively. Line antialiasing uses
the analytic height gradient and surface pixel size, without a required
shader extension, a low-resolution intermediate texture or an image backdrop.
No third-party visual assets, dependencies or runtime network are added.

All seven existing settings and random flags retain their keys, types,
choices and defaults. Density controls contour spacing, weight controls
pixel line width, speed controls the evolving field, relief retains distinct
ridges/basins/coast, and palette/brightness/day-night affect the scene. The
clock now preserves paused progress, removes callbacks offscreen and bounds
scheduling stalls. Periodic shader harmonics allow a precise wrapped phase
without a long-running discontinuity.

The package and upgrade signer are unchanged. The existing PreviewActivity
is exported so hosts can reach its actual full-screen scene directly. The
prototype is version 1.1.0/code3; it has not been published or integrated.

Local checks cover existing options/installer behavior, phase/speed controls,
pause/resume, duplicate starts, scheduling gaps and phase wrapping. Both APK
builds and source/style checks pass. Java compilation does not compile the
GPU shader; actual driver shader compilation, native image quality, motion,
D-pad controls, lifecycle and target-device pacing remain open. Existing
coverage wiring follows the replacement motion clock; a full coverage/release
sweep is deferred until the visual prototype warrants release work.

## Native candidate 2 and width correction 3

Candidate 2 compiled and rendered on the API34 Google TV AVD at an actual
1920x1080 SurfaceView. Native D-pad palette/landform/speed, process recreation,
invalid-value preservation and Settings/Home surface removal passed. Three
native looks and movies were captured; full moving visual acceptance remains
open. The new unique install was stopped and all prior33 apps, UI14 keys,
OS19 keys, provider values, external bytes, appops and accessibility state were
restored exactly.

The dense Sandstone/Coast look exposed a real defect: an absolute .00018
height-footprint floor inflated line width on shallow terrain. Candidate 3
keeps the field, time, colors and seven controls unchanged. The contour width
now uses actual pixel distance and fades bands whose gradient is below usable
precision, including a conservative mediump fallback. Scalar tests evaluate
the production shader band function for stable pixel half-width across slopes,
finite/invisible flat terrain and effective line-weight changes. This is a CPU
math reference, not a shader compiler or native-render test. Candidate 3 still
needs a new native grant and actual flat-Coast review. Candidate 2 stays frozen.

## Physical candidate 3 and scheduling experiment 4

The original API30 Fire TV rendered the candidate 3 default at an actual
1920x1080 buffer, but three quiet presentation rings measured about19.63–19.69
FPS with median50ms intervals. The test stopped before the planned Contour
idle-Dream gate. All60 prior APKs except the intended Contour upgrade, UI51,
OS19 plus full OS settings, providers, external bytes, appops, actual two
accessibility bindings and guard were preserved; Aerial/Main/Awake restored.

Candidate 4 isolates scheduling only. Its shader, renderer, art, options and
native-size behavior are byte-identical to candidate 3. A Choreographer callback
submits the supplied vsync timestamp and requests a frame while visible; it
removes the callback on stop/detach. The existing elapsed scene clock still
retains paused progress and does not catch up offscreen. It does not introduce
a frame-rate claim: the physical comparison must establish whether scheduling
or another rendering cost caused the slow presentation.

A host-only boundary fixture compiles the actual SceneView against minimal
Android scheduler/GL-view stubs and observes duplicate start, visible-vsync
requests, late callbacks after stop, elapsed resume behavior and detach cleanup.
Those test-only classes are excluded from Android/Robolectric test compilation;
they neither create a GL context nor establish device performance. Candidate 4
is not published and has no native evidence yet.


## Physical scheduling result and field candidate 5

Candidate 4 remained near 19.63 FPS on the original API 30 Fire TV. Scheduling alone did not resolve the observed presentation limit. Candidate 5 moved smooth field and analytic-gradient evaluation to a dense 256 by 144 vertex mesh when fragment precision is at least 23 bits. A correction term retains subpixel contour accuracy between vertices; lower-precision drivers retain fragment evaluation. Full native surface size, line antialiasing, seven choices and scene design remain intact.

The actual 1920 x 1080 candidate 5 surface measured 32.894155, 32.774628 and 32.774952 FPS in three separate quiet compositor windows. Median frame intervals were about 33.32 ms. These are observed presentation results on one Fire TV, not universal performance or sustained thermal claims. Default and Sandstone/Coast weight contrasts were inspected. The requested 20-second recording encoded only 17.988833 seconds and 117 frames, so its encoded frame rate is not used as renderer pacing evidence.

Code 6 is release packaging of candidate 5. The renderer, field mesh, motion, options, provider, Dream and Preview classes are preserved. Only the settings credits entry, bundled notices, native-derived preview resource and version metadata change. Code 6 still requires its own packaging gate before publication; source equivalence does not replace that check.
