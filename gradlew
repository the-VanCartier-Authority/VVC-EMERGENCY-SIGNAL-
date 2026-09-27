#!/usr/bin/env sh
set -eu
GRADLE_VERSION="8.5"
CACHE_DIR="${GRADLE_USER_HOME:-$HOME/.gradle}/vvc-wrapper/${GRADLE_VERSION}"
GRADLE_BIN="$CACHE_DIR/gradle-${GRADLE_VERSION}/bin/gradle"
DIST_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
if [ ! -x "$GRADLE_BIN" ]; then
  mkdir -p "$CACHE_DIR"
  TMP_ZIP="$CACHE_DIR/gradle.zip"
  [ -f "$TMP_ZIP" ] || curl -fsSL "$DIST_URL" -o "$TMP_ZIP"
  rm -rf "$CACHE_DIR/gradle-${GRADLE_VERSION}"
  command -v unzip >/dev/null 2>&1 || { echo "unzip is required" >&2; exit 1; }
  unzip -q "$TMP_ZIP" -d "$CACHE_DIR"
fi
exec "$GRADLE_BIN" "$@"
