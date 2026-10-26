#!/usr/bin/env bash
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
cd "$HERE"
python3 -m unittest -v test_install.py
mkdir -p build/tests
javac -d build/tests src/com/jeremykenedy/contourflow/ContourFlowOptions.java src/com/jeremykenedy/contourflow/ContourFlowMotion.java tests/ContourFlowOptionsTest.java tests/ContourFlowMotionTest.java
java -cp build/tests com.jeremykenedy.contourflow.ContourFlowOptionsTest
java -cp build/tests com.jeremykenedy.contourflow.ContourFlowMotionTest
