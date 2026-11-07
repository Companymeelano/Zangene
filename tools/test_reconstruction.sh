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
  reconstruction/android/src/com/asiyestore/admin/app/{ThemePreference,SimpleNavigation,JewelIconRegistry,ReferenceDraftMigration,ProductImageRegistry}.java \
  reconstruction/android/test/com/asiyestore/admin/app/{RecoveryTest,ReferenceDraftMigrationTest,ProductImageRegistryTest}.java
"$JAVA" -cp "$WORK/jvm" com.asiyestore.admin.app.RecoveryTest reconstruction/android/assets/icons
"$JAVA" -cp "$WORK/jvm" com.asiyestore.admin.app.ReferenceDraftMigrationTest
"$JAVA" -cp "$WORK/jvm" com.asiyestore.admin.app.ProductImageRegistryTest reconstruction/android/res/drawable-nodpi
mkdir -p "$WORK/images"
"$JAVA" -jar "$ECJ" -source 8 -target 8 -encoding UTF-8 -proc:none -d "$WORK/images" \
  $(find reconstruction/android/test/images -name '*.java') \
  reconstruction/android/src/com/asiyestore/admin/app/{ProductImageRegistry,AndroidProductImages}.java
"$JAVA" -cp "$WORK/images" com.asiyestore.admin.app.AndroidProductImagesTest
mkdir -p "$WORK/sqlite"
"$JAVA" -jar "$ECJ" -source 8 -target 8 -encoding UTF-8 -proc:none -d "$WORK/sqlite" \
  $(find reconstruction/android/test/sqlite -name '*.java') \
  reconstruction/android/src/com/asiyestore/admin/app/{ReferenceDraftMigration,AndroidReferenceDraftStore,CatalogSyncGuard,AndroidCatalogSyncStore}.java
"$JAVA" -cp "$WORK/sqlite" com.asiyestore.admin.app.RealSqliteMigrationTest \
  reconstruction/android/test/sqlite/sqlite_bridge.py reconstruction/android/test/sqlite/products_v9.sql
"$JAVA" -cp "$WORK/sqlite" com.asiyestore.admin.app.CatalogSyncGuardSqliteTest \
  reconstruction/android/test/sqlite/sqlite_bridge.py reconstruction/android/test/sqlite/products_v9.sql
python3 -m unittest discover -s tests -p 'test_*.py' -v
