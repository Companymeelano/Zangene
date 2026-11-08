#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
OUT=$(mktemp -d)
trap 'rm -rf "$OUT"' EXIT HUP INT TERM
JAVAC=${JAVA_HOME:+"$JAVA_HOME/bin/"}javac
JAVA=${JAVA_HOME:+"$JAVA_HOME/bin/"}java
"$JAVAC" -encoding UTF-8 -d "$OUT" \
  "$ROOT/derakhshan-android/app/src/main/java/ir/derakhshan/wholesale/ContactLinks.java" \
  "$ROOT/tests/ContactLinksTest.java"
"$JAVA" -cp "$OUT" ir.derakhshan.wholesale.ContactLinksTest
