#!/usr/bin/env bash
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
SDK="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
BUILD_TOOLS="$SDK/build-tools/36.0.0"
ANDROID_JAR="$SDK/platforms/android-36/android.jar"
OUT="$HERE/build"
KEYSTORE="$HOME/.android/contour-flow.jks"
KEYPASS="$HOME/.android/contour-flow.pass"
SIGNING=1
if [[ "${1:-}" == "--unsigned" ]]; then SIGNING=0; fi

[[ -f "$ANDROID_JAR" && -x "$BUILD_TOOLS/aapt2" ]] || { echo "Install Android SDK platform 36 and build-tools 36.0.0." >&2; exit 1; }
for tool in javac keytool openssl zip shasum; do command -v "$tool" >/dev/null || { echo "Missing build tool: $tool" >&2; exit 1; }; done
mkdir -p "$OUT"
if [[ "$SIGNING" == 1 ]]; then
  mkdir -p "$HOME/.android"
  if [[ ! -f "$KEYSTORE" ]]; then
    [[ ! -e "$KEYPASS" ]] || { echo "Signing key missing but password exists. Restore the key." >&2; exit 1; }
    (umask 077 && openssl rand -hex 24 > "$KEYPASS")
    keytool -genkeypair -keystore "$KEYSTORE" -storepass:file "$KEYPASS" -alias contour-flow \
      -keyalg RSA -keysize 2048 -validity 10950 -dname "CN=Jeremy Kenedy"
    chmod 600 "$KEYSTORE" "$KEYPASS"
  fi
  [[ -s "$KEYPASS" ]] || { echo "Restore the signing-key password file." >&2; exit 1; }
fi
rm -rf "$OUT/classes" "$OUT/dex" "$OUT/generated"
mkdir -p "$OUT/classes" "$OUT/dex"
"$BUILD_TOOLS/aapt2" compile --dir "$HERE/res" -o "$OUT/res.zip"
"$BUILD_TOOLS/aapt2" link -o "$OUT/unsigned.apk" -I "$ANDROID_JAR" --manifest "$HERE/AndroidManifest.xml" \
  --min-sdk-version 23 --target-sdk-version 35 --version-code 1 --version-name 1.0.0 --java "$OUT/generated" "$OUT/res.zip"
find "$HERE/src" "$OUT/generated" -name '*.java' > "$OUT/sources.txt"
javac -nowarn -Xlint:-options -source 8 -target 8 -bootclasspath "$ANDROID_JAR" -d "$OUT/classes" @"$OUT/sources.txt"
find "$OUT/classes" -name '*.class' > "$OUT/classes.txt"
"$BUILD_TOOLS/d8" --release --lib "$ANDROID_JAR" --min-api 23 --output "$OUT/dex" @"$OUT/classes.txt"
(cd "$OUT/dex" && zip -q -j "$OUT/unsigned.apk" classes.dex)
"$BUILD_TOOLS/zipalign" -f 4 "$OUT/unsigned.apk" "$OUT/aligned.apk"
if [[ "$SIGNING" == 1 ]]; then
  APK="$OUT/contour-flow.apk"
  "$BUILD_TOOLS/apksigner" sign --ks "$KEYSTORE" --ks-pass "file:$KEYPASS" --ks-key-alias contour-flow --out "$APK" "$OUT/aligned.apk"
  "$BUILD_TOOLS/apksigner" verify "$APK"
else
  APK="$OUT/contour-flow-unsigned.apk"
  cp "$OUT/aligned.apk" "$APK"
fi
(cd "$OUT" && shasum -a 256 "$(basename "$APK")" > "$(basename "$APK").sha256")
"$BUILD_TOOLS/aapt2" dump badging "$APK" | grep -F "package: name='com.jeremykenedy.contourflow' versionCode='1' versionName='1.0.0'" >/dev/null
BADGING="$("$BUILD_TOOLS/aapt2" dump badging "$APK")"
MANIFEST="$("$BUILD_TOOLS/aapt2" dump xmltree "$APK" --file AndroidManifest.xml)"
PERMISSIONS="$("$BUILD_TOOLS/aapt2" dump permissions "$APK")"
grep -F "launchable-activity: name='com.jeremykenedy.contourflow.ContourFlowActivity'" <<<"$BADGING" >/dev/null
grep -F "android.permission.BIND_DREAM_SERVICE" <<<"$MANIFEST" >/dev/null
grep -F "android.service.dreams.DreamService" <<<"$MANIFEST" >/dev/null
grep -F '".ContourFlowDreamService"' <<<"$MANIFEST" >/dev/null
grep -F "com.jeremykenedy.contourflow.settings" <<<"$MANIFEST" >/dev/null
if grep -qi 'android.permission.INTERNET' <<<"$PERMISSIONS"; then echo "Runtime network permission is prohibited." >&2; exit 1; fi
echo "Built $APK"
cat "$APK.sha256"
