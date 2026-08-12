#!/usr/bin/env bash
# Genera el ejecutable de Windows a partir del JAR, con jpackage (JDK 14+).
#
# Produce una "app-image": una carpeta con EXP Soundboard.exe y un Java recortado dentro,
# de modo que funciona en un Windows sin Java instalado y sin necesitar el .jar.
# No genera instalador .msi/.exe porque eso requiere tener WiX Toolset instalado.
#
# Uso: tools/build-exe.sh      (ejecuta antes build.sh si no existe el JAR)
set -euo pipefail
cd "$(dirname "$0")/.."

JAR="dist/EXP Soundboard_051.jar"
NAME="EXP Soundboard"
VERSION="0.5.1"

[ -f "$JAR" ] || { echo "Falta $JAR: ejecuta primero build.sh" >&2; exit 1; }

rm -rf build-exe
mkdir -p build-exe/input
cp "$JAR" build-exe/input/

# Icono: el logo original es un PNG de 256x256, que se envuelve en un contenedor .ico
# (Windows admite iconos con carga PNG desde Vista).
python - <<'PY'
import struct, pathlib
png = pathlib.Path("resources/exp/gui/EXP logo.png").read_bytes()
ico = struct.pack("<HHH", 0, 1, 1)                       # ICONDIR: reservado, tipo=icono, 1 imagen
ico += struct.pack("<BBBBHHII", 0, 0, 0, 0, 1, 32,       # 0x0 = 256x256; 1 plano, 32 bits
                   len(png), 22)                          # tamano y desplazamiento de los datos
pathlib.Path("build-exe/app.ico").write_bytes(ico + png)
PY

echo ">> Generando el ejecutable con jpackage..."
jpackage \
    --type app-image \
    --name "$NAME" \
    --app-version "$VERSION" \
    --input build-exe/input \
    --main-jar "$(basename "$JAR")" \
    --main-class exp.gui.SoundboardFrame \
    --icon build-exe/app.ico \
    --dest build-exe \
    --vendor "Expenosa (obra original) - rework" \
    --copyright "Original (c) Expenosa 2014, CC BY-SA 3.0" \
    --description "EXP Soundboard $VERSION (rework)" \
    --java-options "--enable-native-access=ALL-UNNAMED" \
    --add-modules java.desktop,java.prefs,java.logging,java.naming,jdk.unsupported

echo ">> Listo: build-exe/$NAME/$NAME.exe"
du -sh "build-exe/$NAME"
