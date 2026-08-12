#!/usr/bin/env bash
# Compiles and packages EXP Soundboard into a runnable JAR.
#
# Why a flat JAR: the original used Eclipse's jar-in-jar loader, which since Java 9 can no
# longer load the nested libraries (NoClassDefFoundError). Here the dependencies are unpacked
# into the final JAR, which is the layout every Java version understands.
set -euo pipefail
cd "$(dirname "$0")"

MAIN_CLASS="exp.gui.SoundboardFrame"
OUT_JAR="dist/EXP Soundboard_051.jar"
RELEASE=17           # bytecode compatible with Java 17 and later

rm -rf classes staging
mkdir -p classes staging dist

CP=$(ls lib/*.jar | tr '\n' ';')

echo ">> Compiling..."
javac --release "$RELEASE" -encoding UTF-8 -Xlint:-options -nowarn \
      -cp "$CP" -d classes $(find src -name '*.java')

echo ">> Unpacking dependencies..."
for jar in lib/*.jar; do
    # Extracted straight into staging (-o overwrites). Copying afterwards with "cp" caused
    # trouble on Windows with jave's two ffmpeg binaries (6 MB + 8 MB).
    # Digital signatures are excluded: MigLayout ships signed, and a signature over a
    # repackaged JAR invalidates it (SecurityException at startup).
    unzip -oq "$jar" -d staging \
          -x 'META-INF/MANIFEST.MF' 'META-INF/*.SF' 'META-INF/*.RSA' 'META-INF/*.DSA' \
             'META-INF/INDEX.LIST' 'META-INF/services/*'
    # META-INF/services entries (the audio SPI from mp3spi/tritonus) must be MERGED, not
    # overwritten: if one JAR clobbers the previous one's, Java loses the MP3 reader.
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

echo ">> Adding classes and resources..."
cp -r classes/. staging/
cp -r resources/. staging/
# The original JAR carried a Windows Thumbs.db inside. Out it goes.
find staging -name 'Thumbs.db' -delete

cat > staging/manifest.txt <<EOF
Main-Class: $MAIN_CLASS
Implementation-Title: EXP Soundboard
Implementation-Version: 0.5.1
Enable-Native-Access: ALL-UNNAMED
EOF

echo ">> Packaging..."
( cd staging && jar --create --file "../$OUT_JAR" --manifest manifest.txt \
      $(ls -A | grep -v '^manifest.txt$') )
rm -f staging/manifest.txt

echo ">> Done: $(pwd)/$OUT_JAR"
ls -la "$OUT_JAR"
