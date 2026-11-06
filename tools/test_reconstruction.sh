#!/usr/bin/env bash
# Test the isolated recovery components, not an Android app or production APK.
set -euo pipefail
ROOT=$(cd "$(dirname "$0")/.." && pwd)
cd "$ROOT"
: "${JAVA:?Set JAVA to a Java runtime}"
: "${ECJ:?Set ECJ to the Eclipse Java compiler jar}"
: "${ANDROID_JAR:?Set ANDROID_JAR to an Android API jar}"
WORK=$(mktemp -d)
trap 'rm -rf "$WORK"' EXIT
mkdir -p "$WORK/android" "$WORK/jvm"
"$JAVA" -jar "$ECJ" -source 8 -target 8 -encoding UTF-8 -proc:none \
  -bootclasspath "$ANDROID_JAR" -d "$WORK/android" $(find reconstruction/android/src -name '*.java')
"$JAVA" -jar "$ECJ" -source 8 -target 8 -encoding UTF-8 -proc:none -d "$WORK/jvm" \
  reconstruction/android/src/com/asiyestore/admin/app/{ThemePreference,SimpleNavigation,JewelIconRegistry}.java \
  reconstruction/android/test/com/asiyestore/admin/app/RecoveryTest.java
"$JAVA" -cp "$WORK/jvm" com.asiyestore.admin.app.RecoveryTest reconstruction/android/assets/icons
python3 -m unittest discover -s tests -p 'test_*.py' -v
