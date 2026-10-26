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
