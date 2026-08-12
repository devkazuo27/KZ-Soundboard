#!/usr/bin/env bash
# Prepara lib/ con las dependencias necesarias para compilar.
#
# Este repositorio NO incluye binarios de terceros: las librerias de 2014 tienen licencias
# distintas entre si (GPL, LGPL, BSD, Apache y la de Apple) y redistribuirlas aqui mezcladas
# traeria obligaciones que no vienen a cuento. En su lugar se sacan de donde ya estaban:
#
#   - las 8 librerias originales, del propio JAR 0.5 que publico Expenosa en SourceForge
#   - FlatLaf (el tema visual, unica dependencia nueva), de Maven Central
#
# Uso:
#   tools/fetch-deps.sh                      descarga el JAR 0.5 de SourceForge
#   tools/fetch-deps.sh /ruta/al/original.jar   usa una copia local que ya tengas
set -euo pipefail
cd "$(dirname "$0")/.."

ORIGINAL_URL="https://sourceforge.net/projects/expsoundboard/files/Releases/All%20Platforms%20%28.jar%29/EXP%20Soundboard_05.jar/download"
FLATLAF_URL="https://repo1.maven.org/maven2/com/formdev/flatlaf/3.6/flatlaf-3.6.jar"
LIBS="AppleJavaExtensions.jar JNativeHook.jar gson-2.2.4.jar jave-1.0.2.jar jl1.0.1.jar miglayout15-swing.jar mp3spi1.9.5.jar tritonus_share-0.3.6.jar"

mkdir -p lib
original="${1:-}"

if [ -z "$original" ]; then
    original="$(mktemp -d)/EXP Soundboard_05.jar"
    echo ">> Descargando el JAR original 0.5 de SourceForge..."
    curl -sSL --fail -o "$original" "$ORIGINAL_URL"
fi

if [ ! -f "$original" ]; then
    echo "No encuentro el JAR original: $original" >&2
    exit 1
fi

echo ">> Extrayendo las librerias originales de $(basename "$original")..."
for lib in $LIBS; do
    unzip -oq "$original" "$lib" -d lib
    echo "   $lib"
done

echo ">> Descargando FlatLaf 3.6..."
curl -sSL --fail -o lib/flatlaf-3.6.jar "$FLATLAF_URL"

echo ">> lib/ listo:"
ls -1 lib
