#!/usr/bin/env bash
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
cd "$HERE"
python3 -m unittest -v test_install.py
mkdir -p build/tests
javac -d build/tests src/com/jeremykenedy/contourflow/ContourFlowOptions.java src/com/jeremykenedy/contourflow/ContourFlowMotion.java tests/ContourFlowOptionsTest.java tests/ContourFlowMotionTest.java
java -cp build/tests com.jeremykenedy.contourflow.ContourFlowOptionsTest
java -cp build/tests com.jeremykenedy.contourflow.ContourFlowMotionTest
python3 -m unittest discover -s tests -p 'test_contour_*.py' -v
mkdir -p build/frame-tests
javac -d build/frame-tests tests/frame-support/android/content/Context.java \
  tests/frame-support/android/opengl/GLSurfaceView.java tests/frame-support/android/view/Choreographer.java \
  tests/frame-support/com/jeremykenedy/contourflow/ContourFlowRenderer.java \
  src/com/jeremykenedy/contourflow/ContourFlowOptions.java src/com/jeremykenedy/contourflow/ContourFlowMotion.java \
  src/com/jeremykenedy/contourflow/ContourFlowSceneView.java tests/ContourFlowFrameTest.java
java -cp build/frame-tests com.jeremykenedy.contourflow.ContourFlowFrameTest

javac -d build/tests src/com/jeremykenedy/contourflow/ContourFlowMesh.java tests/ContourFlowMeshTest.java
java -cp build/tests com.jeremykenedy.contourflow.ContourFlowMeshTest

mkdir -p build/renderer-tests
find tests/renderer-support -name '*.java' > build/renderer-tests/sources.txt
javac -d build/renderer-tests @build/renderer-tests/sources.txt \
  src/com/jeremykenedy/contourflow/ContourFlowOptions.java \
  src/com/jeremykenedy/contourflow/ContourFlowMesh.java \
  src/com/jeremykenedy/contourflow/ContourFlowRenderer.java tests/ContourFlowRendererTest.java
java -cp build/renderer-tests com.jeremykenedy.contourflow.ContourFlowRendererTest
