# Third parties

## The original work

**EXP Soundboard** © **Expenosa**, 2014 — <https://sourceforge.net/projects/expsoundboard/>
Released under [Creative Commons Attribution-ShareAlike 3.0 Unported](LICENSE).

This repository is a derivative work: the sources were decompiled from the 0.5 JAR the author
published and then modified. The changes are listed in the README and in full in
`CHANGES.diff`. The logo (`resources/exp/gui/EXP logo.png`) and the `loader.mp3` clip are part
of the original work and are redistributed under the same licence.

Neither this repository nor its authors are associated with Expenosa, and this version is not
official. If the original author would like something removed or changed, opening an issue is
enough.

## Libraries

**They are not redistributed in this repository.** `tools/fetch-deps.sh` pulls them out of the
original 0.5 JAR (which bundles them) and from Maven Central. Each keeps its own licence:

| Library | Version | Licence |
|---|---|---|
| [JNativeHook](https://github.com/kwhat/jnativehook) | 1.1 | GPL v3 / LGPL v3 |
| [JAVE](http://www.sauronsoftware.it/projects/jave/) (bundles ffmpeg binaries) | 1.0.2 | GPL |
| [JLayer](http://www.javazoom.net/javalayer/javalayer.html) | 1.0.1 | LGPL |
| [MP3SPI](http://www.javazoom.net/mp3spi/mp3spi.html) | 1.9.5 | LGPL |
| [Tritonus share](http://www.tritonus.org/) | 0.3.6 | LGPL |
| [MigLayout](https://www.miglayout.com/) | 1.5 (swing) | BSD |
| [Gson](https://github.com/google/gson) | 2.2.4 | Apache 2.0 |
| AppleJavaExtensions | — | Apple sample code licence |
| [FlatLaf](https://github.com/JFormDesigner/FlatLaf) | 3.6 | Apache 2.0 |

FlatLaf is the only dependency this rework adds; everything else already shipped inside the
original JAR. `guava-18.0.jar`, which the original included, was dropped because nothing used
it.

## Sources for the GPL/LGPL components

The JAR attached to the [releases](../../releases) bundles these libraries, unmodified, exactly
as they came in the original 0.5 JAR. Their licences require stating where to obtain their
source code:

| Component | Source code |
|---|---|
| JNativeHook 1.1 (GPL v3 / LGPL v3) | <https://github.com/kwhat/jnativehook> |
| JAVE 1.0.2 (GPL) | <http://www.sauronsoftware.it/projects/jave/> |
| JLayer 1.0.1 (LGPL) | <http://www.javazoom.net/javalayer/sources.html> |
| MP3SPI 1.9.5 (LGPL) | <http://www.javazoom.net/mp3spi/mp3spi.html> |
| Tritonus share 0.3.6 (LGPL) | <http://www.tritonus.org/> |
| ffmpeg (GPL) | A 2009 binary embedded in JAVE 1.0.2, redistributed exactly as it came in it. JAVE does not document the exact version — see <http://www.sauronsoftware.it/projects/jave/> and <https://ffmpeg.org/download.html> |

None of these components was modified by this rework. If you build the JAR yourself with
`build.sh` and redistribute it, the same obligations apply to you.

## The executable's Java runtime

The Windows `.zip` includes a Java runtime trimmed with `jlink`, generated from
[Eclipse Temurin](https://adoptium.net/) (OpenJDK), under **GPL v2 with Classpath Exception**,
which is precisely what allows shipping it alongside an application. Source code (built with
Temurin 25.0.3): <https://github.com/adoptium/jdk25u> and <https://openjdk.org/>.

## Tooling

The sources were recovered with [CFR](https://www.benf.org/other/cfr/) (MIT), which is not
redistributed here.
