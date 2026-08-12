#!/usr/bin/env bash
# Builds the Windows executable from the JAR, using jpackage (JDK 14+).
#
# Produces an "app image": a folder holding KZ Soundboard.exe and a trimmed Java runtime, so
# it works on a Windows box with no Java installed and without needing the .jar.
# It does not produce an .msi/.exe installer, because that requires WiX Toolset to be present.
#
# Usage: tools/build-exe.sh      (run build.sh first if the JAR is missing)
set -euo pipefail
cd "$(dirname "$0")/.."

JAR="dist/KZ Soundboard.jar"
NAME="KZ Soundboard"
VERSION="1.0"

[ -f "$JAR" ] || { echo "Missing $JAR: run build.sh first" >&2; exit 1; }

rm -rf build-exe
mkdir -p build-exe/input
cp "$JAR" build-exe/input/

# Icon: the original logo is a 256x256 PNG, wrapped here in an .ico container (Windows has
# supported icons with a PNG payload since Vista).
python - <<'PY'
import struct, pathlib
png = pathlib.Path("resources/exp/gui/kz-logo.png").read_bytes()
ico = struct.pack("<HHH", 0, 1, 1)                       # ICONDIR: reserved, type=icon, 1 image
ico += struct.pack("<BBBBHHII", 0, 0, 0, 0, 1, 32,       # 0x0 means 256x256; 1 plane, 32 bits
                   len(png), 22)                          # payload size and offset
pathlib.Path("build-exe/app.ico").write_bytes(ico + png)
PY

echo ">> Building the executable with jpackage..."
jpackage \
    --type app-image \
    --name "$NAME" \
    --app-version "$VERSION" \
    --input build-exe/input \
    --main-jar "$(basename "$JAR")" \
    --main-class exp.gui.SoundboardFrame \
    --icon build-exe/app.ico \
    --dest build-exe \
    --vendor "KZ - based on EXP Soundboard by Expenosa" \
    --copyright "Original (c) Expenosa 2014, CC BY-SA 3.0" \
    --description "KZ Soundboard $VERSION" \
    --java-options "--enable-native-access=ALL-UNNAMED" \
    --add-modules java.desktop,java.prefs,java.logging,java.naming,jdk.unsupported

echo ">> Done: build-exe/$NAME/$NAME.exe"
du -sh "build-exe/$NAME"

# The ZIP published in the releases. Git Bash on Windows ships without zip(1); jar(1) comes
# with the same JDK that jpackage already needs, and writes an ordinary deflated archive.
ZIP="KZ-Soundboard-$VERSION-windows-x64.zip"
echo ">> Packing $ZIP..."
rm -f "build-exe/$ZIP"
if command -v zip >/dev/null 2>&1; then
    (cd build-exe && zip -qr9 "$ZIP" "$NAME")
else
    (cd build-exe && jar --create --no-manifest --file "$ZIP" "$NAME")
fi
echo ">> Done: build-exe/$ZIP ($(du -h "build-exe/$ZIP" | cut -f1))"
