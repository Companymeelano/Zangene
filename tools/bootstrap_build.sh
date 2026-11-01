#!/usr/bin/env bash
# Re-provisions the whole Android build chain inside a fresh sandbox.
# Idempotent: everything is skipped when already present.
set -e

TOOLS=${TOOLS:-/tmp/android-tools}
VENV=${VENV:-/tmp/atv}
REPO=$(cd "$(dirname "$0")/.." && pwd)

mkdir -p "$TOOLS"

# 1. JDK
if [ ! -x "$VENV/bin/python" ]; then
  python3 -m venv "$VENV"
  "$VENV/bin/pip" install --quiet jdk4py
fi
JAVA="$VENV/lib/python3.11/site-packages/jdk4py/java-runtime/bin/java"

# 2. Platform + dex + signing jars
for f in android.jar d8.jar apksigner.jar; do
  if [ ! -s "$TOOLS/$f" ]; then
    gh api "repos/HashShin/H2APK/contents/tools/$f" -H "Accept: application/vnd.github.raw" > "$TOOLS/$f"
  fi
done

# 3. aapt2 + ECJ from npm
cd "$TOOLS"
if [ ! -d node_modules/aaptjs3 ]; then
  npm init -y > /dev/null 2>&1 || true
  npm install --silent aaptjs3
fi
if [ ! -d node_modules/@vscjava/java-language-server ]; then
  npm install --silent @vscjava/java-language-server
fi
chmod +x "$TOOLS"/node_modules/aaptjs3/bin/x64/linux/aapt2

# 4. Release keystore (versioned on purpose so update continuity never breaks again)
KS="$REPO/android/keystore/asiye-release.jks"
if [ ! -s "$KS" ]; then
  "$VENV/lib/python3.11/site-packages/jdk4py/java-runtime/bin/keytool" -genkeypair -v \
    -keystore "$KS" -storetype PKCS12 \
    -alias asiye_signature -keyalg RSA -keysize 2048 -validity 10950 \
    -dname "CN=ASIYE STORE Signature Edition, OU=Zangeneh Management, O=ASIYE STORE, L=Tehran, ST=Tehran, C=IR" \
    -storepass "Asiye2026!" -keypass "Asiye2026!" > /dev/null 2>&1
fi

echo "JAVA=$JAVA"
echo "ECJ=$(ls "$TOOLS"/node_modules/@vscjava/java-language-server/server/plugins/org.eclipse.jdt.core.compiler.batch_*.jar | head -1)"
echo "AAPT2=$TOOLS/node_modules/aaptjs3/bin/x64/linux/aapt2"
echo "KEYSTORE=$KS"
