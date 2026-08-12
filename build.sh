#!/usr/bin/env bash
# Compila y empaqueta EXP Soundboard en un JAR ejecutable plano.
#
# Por que un JAR plano: el original usaba el "jar-in-jar loader" de Eclipse, que
# desde Java 9 ya no consigue cargar las librerias anidadas (NoClassDefFoundError).
# Aqui las dependencias se descomprimen dentro del JAR final, que es el formato
# que entienden todas las versiones de Java.
set -euo pipefail
cd "$(dirname "$0")"

MAIN_CLASS="exp.gui.SoundboardFrame"
OUT_JAR="dist/EXP Soundboard_051.jar"
RELEASE=17           # bytecode compatible con Java 17 en adelante

rm -rf classes staging
mkdir -p classes staging dist

CP=$(ls lib/*.jar | tr '\n' ';')

echo ">> Compilando..."
javac --release "$RELEASE" -encoding UTF-8 -Xlint:-options -nowarn \
      -cp "$CP" -d classes $(find src -name '*.java')

echo ">> Desempaquetando dependencias..."
for jar in lib/*.jar; do
    # Se extrae directamente sobre staging (-o sobrescribe). Copiar despues con "cp" daba
    # problemas con los dos binarios de ffmpeg de jave (6 MB + 8 MB) en Windows.
    # Se excluyen las firmas digitales: MigLayout viene firmado y una firma sobre un JAR
    # reempaquetado lo invalida (SecurityException al arrancar).
    unzip -oq "$jar" -d staging \
          -x 'META-INF/MANIFEST.MF' 'META-INF/*.SF' 'META-INF/*.RSA' 'META-INF/*.DSA' \
             'META-INF/INDEX.LIST' 'META-INF/services/*'
    # Los META-INF/services (SPI de audio de mp3spi/tritonus) hay que FUSIONARLOS, no
    # sobrescribirlos: si un JAR pisa los del anterior, Java pierde el lector de MP3.
    tmp=$(mktemp -d)
    if unzip -oq "$jar" 'META-INF/services/*' -d "$tmp" 2>/dev/null; then
        mkdir -p staging/META-INF/services
        for svc in "$tmp"/META-INF/services/*; do
            [ -f "$svc" ] || continue
            cat "$svc" >> "staging/META-INF/services/$(basename "$svc")"
            echo >> "staging/META-INF/services/$(basename "$svc")"
        done
    fi
    rm -rf "$tmp"
done

echo ">> Anadiendo clases y recursos..."
cp -r classes/. staging/
cp -r resources/. staging/
# El JAR original llevaba dentro un Thumbs.db de Windows. Fuera.
find staging -name 'Thumbs.db' -delete

cat > staging/manifest.txt <<EOF
Main-Class: $MAIN_CLASS
Implementation-Title: EXP Soundboard
Implementation-Version: 0.5.1
Enable-Native-Access: ALL-UNNAMED
EOF

echo ">> Empaquetando..."
( cd staging && jar --create --file "../$OUT_JAR" --manifest manifest.txt \
      $(ls -A | grep -v '^manifest.txt$') )
rm -f staging/manifest.txt

echo ">> Listo: $(pwd)/$OUT_JAR"
ls -la "$OUT_JAR"
