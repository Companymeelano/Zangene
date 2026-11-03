#!/usr/bin/env bash
# JVM-only tests. JSON-java is fetched into /tmp; no device or live provider access.
set -euo pipefail
ROOT=$(cd "$(dirname "$0")/../.." && pwd)
TOOLS=${TOOLS:-/tmp/android-tools}
JAVA=${JAVA:-/tmp/atv/lib/python3.11/site-packages/jdk4py/java-runtime/bin/java}
ECJ=$(find "$TOOLS/node_modules/@vscjava/java-language-server/server/plugins" -name 'org.eclipse.jdt.core.compiler.batch_*.jar' | head -1)
OUT=/tmp/asiye-studio-regression
mkdir -p "$OUT/transport" "$OUT/pipeline"
cd "$ROOT"
"$JAVA" -jar "$ECJ" -source 8 -target 8 -encoding UTF-8 -proc:none -nowarn -d "$OUT/transport" \
 tests/studio_jvm/transport_stub/com/asiyestore/admin/app/DirectAiStudio.java \
 android/app/src/main/java/com/asiyestore/admin/app/{AiHttpTransport,AiProviderOrder,AiProviderError,CredentialCipher}.java \
 tests/studio_jvm/com/asiyestore/admin/app/{TransportRegression,ProviderErrorRegression,CredentialRegression}.java
"$JAVA" -jar "$ECJ" -source 8 -target 8 -encoding UTF-8 -proc:none -nowarn -d "$OUT/transport" \
 tests/studio_jvm/pipeline_stub/android/graphics/Bitmap.java \
 android/app/src/main/java/com/asiyestore/admin/app/AiProductStudio.java tests/studio_jvm/com/asiyestore/admin/app/StudioRegression.java
for t in TransportRegression ProviderErrorRegression CredentialRegression StudioRegression; do "$JAVA" -cp "$OUT/transport" com.asiyestore.admin.app."$t"; done
if [ ! -d /tmp/JSON-java-20240303 ]; then
 curl -fsSL https://codeload.github.com/stleary/JSON-java/zip/refs/tags/20240303 -o /tmp/jsonjava-20240303.zip
 unzip -q /tmp/jsonjava-20240303.zip -d /tmp
fi
"$JAVA" -jar "$ECJ" -source 8 -target 8 -encoding UTF-8 -proc:none -nowarn -d "$OUT/pipeline" \
 $(find /tmp/JSON-java-20240303/src/main/java/org/json -name '*.java') \
 $(find tests/studio_jvm/pipeline_stub -name '*.java') \
 android/app/src/main/java/com/asiyestore/admin/app/{DirectAiStudio,AiProviderOrder,AiProviderError}.java \
 tests/studio_jvm/com/asiyestore/admin/app/PipelineRegression.java
"$JAVA" -cp "$OUT/pipeline" com.asiyestore.admin.app.PipelineRegression
