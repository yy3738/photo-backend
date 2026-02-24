#!/bin/sh
# Maven Wrapper script for Unix/macOS/Git Bash
# Downloads Maven if not present and runs it

set -e

MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(cd "$(dirname "$0")" && pwd)}"
MAVEN_WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"

if [ -f "$MAVEN_WRAPPER_PROPERTIES" ]; then
  MAVEN_DIST_URL=$(grep 'distributionUrl' "$MAVEN_WRAPPER_PROPERTIES" | cut -d'=' -f2-)
fi

MAVEN_HOME="$HOME/.m2/wrapper/dists/apache-maven-3.9.6"

if [ ! -f "$MAVEN_HOME/bin/mvn" ]; then
  echo "Downloading Maven 3.9.6..."
  mkdir -p "$MAVEN_HOME"
  TMPFILE=$(mktemp /tmp/maven-XXXXXX.zip)
  if command -v curl > /dev/null 2>&1; then
    curl -fsSL -o "$TMPFILE" "$MAVEN_DIST_URL"
  elif command -v wget > /dev/null 2>&1; then
    wget -q -O "$TMPFILE" "$MAVEN_DIST_URL"
  else
    echo "Error: curl or wget required to download Maven" >&2
    exit 1
  fi
  unzip -q -o "$TMPFILE" -d "$MAVEN_HOME/.."
  # The zip extracts to apache-maven-3.9.6/ directory
  if [ -d "$MAVEN_HOME/../apache-maven-3.9.6" ] && [ "$MAVEN_HOME" != "$MAVEN_HOME/../apache-maven-3.9.6" ]; then
    cp -rf "$MAVEN_HOME/../apache-maven-3.9.6/"* "$MAVEN_HOME/" 2>/dev/null || true
  fi
  rm -f "$TMPFILE"
  chmod +x "$MAVEN_HOME/bin/mvn"
  echo "Maven 3.9.6 installed to $MAVEN_HOME"
fi

export MAVEN_HOME
exec "$MAVEN_HOME/bin/mvn" "$@"
