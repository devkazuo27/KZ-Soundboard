#!/usr/bin/env bash
# Populates lib/ with the dependencies needed to compile.
#
# This repository ships NO third-party binaries: the 2014 libraries carry different licences
# (GPL, LGPL, BSD, Apache and Apple's), and redistributing them bundled here would drag in
# obligations for no good reason. They are fetched from where they already live instead:
#
#   - the 8 original libraries, from the very 0.5 JAR Expenosa published on SourceForge
#   - FlatLaf (the theme, the only new dependency), from Maven Central
#
# Usage:
#   tools/fetch-deps.sh                      downloads the 0.5 JAR from SourceForge
#   tools/fetch-deps.sh /path/to/original.jar   uses a local copy you already have
set -euo pipefail
cd "$(dirname "$0")/.."

ORIGINAL_URL="https://sourceforge.net/projects/expsoundboard/files/Releases/All%20Platforms%20%28.jar%29/EXP%20Soundboard_05.jar/download"
FLATLAF_URL="https://repo1.maven.org/maven2/com/formdev/flatlaf/3.6/flatlaf-3.6.jar"
LIBS="AppleJavaExtensions.jar JNativeHook.jar gson-2.2.4.jar jave-1.0.2.jar jl1.0.1.jar miglayout15-swing.jar mp3spi1.9.5.jar tritonus_share-0.3.6.jar"

mkdir -p lib
original="${1:-}"

if [ -z "$original" ]; then
    original="$(mktemp -d)/EXP Soundboard_05.jar"
    echo ">> Downloading the original 0.5 JAR from SourceForge..."
    curl -sSL --fail -o "$original" "$ORIGINAL_URL"
fi

if [ ! -f "$original" ]; then
    echo "Cannot find the original JAR: $original" >&2
    exit 1
fi

echo ">> Extracting the original libraries from $(basename "$original")..."
for lib in $LIBS; do
    unzip -oq "$original" "$lib" -d lib
    echo "   $lib"
done

echo ">> Downloading FlatLaf 3.6..."
curl -sSL --fail -o lib/flatlaf-3.6.jar "$FLATLAF_URL"

echo ">> lib/ ready:"
ls -1 lib
