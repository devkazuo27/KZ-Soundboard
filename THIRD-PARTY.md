# Terceros

## La obra original

**EXP Soundboard** © **Expenosa**, 2014 — <https://sourceforge.net/projects/expsoundboard/>
Publicado bajo [Creative Commons Attribution-ShareAlike 3.0 Unported](LICENSE).

Este repositorio es un trabajo derivado: los fuentes se descompilaron del JAR 0.5 publicado
por el autor y se modificaron. Los cambios están listados en el README y al detalle en
`CHANGES.diff`. El logotipo (`resources/exp/gui/EXP logo.png`) y el clip `loader.mp3` son
parte de la obra original y se redistribuyen bajo la misma licencia.

Ni este repositorio ni sus autores están asociados con Expenosa, y esta versión no es
oficial. Si el autor original quiere que se retire o se cambie algo, basta con abrir un
issue.

## Librerías

**No se redistribuyen en este repositorio.** `tools/fetch-deps.sh` las obtiene del JAR 0.5
original (que las trae dentro) y de Maven Central. Cada una conserva su licencia:

| Librería | Versión | Licencia |
|---|---|---|
| [JNativeHook](https://github.com/kwhat/jnativehook) | 1.1 | GPL v3 / LGPL v3 |
| [JAVE](http://www.sauronsoftware.it/projects/jave/) (incluye binarios de ffmpeg) | 1.0.2 | GPL |
| [JLayer](http://www.javazoom.net/javalayer/javalayer.html) | 1.0.1 | LGPL |
| [MP3SPI](http://www.javazoom.net/mp3spi/mp3spi.html) | 1.9.5 | LGPL |
| [Tritonus share](http://www.tritonus.org/) | 0.3.6 | LGPL |
| [MigLayout](https://www.miglayout.com/) | 1.5 (swing) | BSD |
| [Gson](https://github.com/google/gson) | 2.2.4 | Apache 2.0 |
| AppleJavaExtensions | — | Licencia de ejemplo de Apple |
| [FlatLaf](https://github.com/JFormDesigner/FlatLaf) | 3.6 | Apache 2.0 |

FlatLaf es la única dependencia añadida por este rework; el resto venía ya en el JAR
original. `guava-18.0.jar`, que el original incluía, se eliminó porque no lo usaba nada.

## Fuentes de los componentes GPL/LGPL

El JAR que se adjunta en las [releases](../../releases) lleva dentro estas librerías, sin
modificar, tal cual venían en el JAR 0.5 original. Sus licencias obligan a indicar dónde
conseguir su código fuente:

| Componente | Código fuente |
|---|---|
| JNativeHook 1.1 (GPL v3 / LGPL v3) | <https://github.com/kwhat/jnativehook> |
| JAVE 1.0.2 (GPL) | <http://www.sauronsoftware.it/projects/jave/> |
| JLayer 1.0.1 (LGPL) | <http://www.javazoom.net/javalayer/sources.html> |
| MP3SPI 1.9.5 (LGPL) | <http://www.javazoom.net/mp3spi/mp3spi.html> |
| Tritonus share 0.3.6 (LGPL) | <http://www.tritonus.org/> |
| ffmpeg (GPL) | Binario de 2009 incrustado en JAVE 1.0.2; se distribuye tal cual venía en él. La versión exacta no está documentada por JAVE — ver <http://www.sauronsoftware.it/projects/jave/> y <https://ffmpeg.org/download.html> |

Ninguno de estos componentes ha sido modificado en este rework. Si compilas tú el JAR con
`build.sh` y lo redistribuyes, estas mismas obligaciones te aplican a ti.

## Runtime de Java del ejecutable

El `.zip` de Windows incluye un runtime de Java recortado con `jlink`, generado a partir de
[Eclipse Temurin](https://adoptium.net/) (OpenJDK), bajo **GPL v2 con Classpath Exception**,
que permite precisamente distribuirlo junto a una aplicación. Código fuente:
(construido con Temurin 25.0.3) <https://github.com/adoptium/jdk25u> y <https://openjdk.org/>.

## Herramientas

Los fuentes se recuperaron con [CFR](https://www.benf.org/other/cfr/) (MIT), que no se
redistribuye aquí.
