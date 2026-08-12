# EXP Soundboard 0.5.1 — rework

Versión modificada de **EXP Soundboard 0.5**, la soundboard de escritorio en Java que
[Expenosa](https://sourceforge.net/projects/expsoundboard/) publicó en diciembre de 2014 y
que lleva sin actualizarse desde entonces.

El original **ya no arranca en Java 9 ni posterior**. Este rework lo devuelve a la vida en
Java moderno, corrige una docena de errores del motor de audio y moderniza la interfaz.
**No añade funciones nuevas**: mismas ventanas, mismos ajustes, mismo formato `.json`, mismas
preferencias. Un soundboard guardado con el 0.5 se abre aquí sin tocar nada.

> **Atribución.** Este es un trabajo derivado, no oficial. La aplicación original, su nombre
> y su logotipo son de **Expenosa** (© 2014), publicados bajo
> [CC BY-SA 3.0](https://creativecommons.org/licenses/by-sa/3.0/). Este repositorio se
> distribuye bajo la misma licencia. Los cambios respecto al original están listados abajo y
> al detalle en [`CHANGES.diff`](CHANGES.diff).
>
> **Los fuentes de este repositorio están descompilados**, no son los originales de
> Expenosa, que nunca se publicaron. Se obtuvieron del JAR 0.5 con
> [CFR](https://www.benf.org/other/cfr/) y a partir de ahí se corrigieron. Los nombres de
> variables locales y el estilo de algunos bucles son, por tanto, cosa del descompilador.

---

## Descargar

En [releases](../../releases) hay dos formas de usarlo:

- **`EXP-Soundboard-0.5.1-windows-x64.zip`** — para Windows, con un Java recortado dentro.
  Descomprime y ejecuta `EXP Soundboard.exe`. **No necesita tener Java instalado.**
- **`EXP-Soundboard-0.5.1.jar`** — para cualquier sistema con Java 17 o superior:
  `java -jar "EXP-Soundboard-0.5.1.jar"`

## Compilar

Requiere JDK 17 o superior. Las dependencias no están en el repositorio (ver
[`tools/fetch-deps.sh`](tools/fetch-deps.sh) para el porqué):

```bash
tools/fetch-deps.sh          # trae las librerías (o pásale tu copia del JAR 0.5)
bash build.sh                # compila y empaqueta -> dist/
java -jar "dist/EXP Soundboard_051.jar"

tools/build-exe.sh           # opcional: ejecutable de Windows -> build-exe/
```

El ejecutable se genera con `jpackage` (JDK 14+) como *app-image*: una carpeta con el `.exe`
y un runtime de Java recortado. No se genera instalador `.msi` porque eso necesita tener
instalado [WiX Toolset](https://wixtoolset.org/).

## Qué se arregló

### Arrancar en Java moderno

El JAR original usaba el *jar-in-jar loader* de Eclipse, que carga las librerías anidadas con
un protocolo `rsrc:` propio que dejó de funcionar con los cambios de carga de clases de
Java 9:

```
Exception in thread "main" java.lang.NoClassDefFoundError: org/jnativehook/keyboard/NativeKeyListener
	at org.eclipse.jdt.internal.jarinjarloader.JarRsrcLoader.main(JarRsrcLoader.java:56)
```

`build.sh` genera en su lugar un JAR plano. Dos detalles importantes al aplanar: hay que
quitar las firmas digitales (MigLayout viene firmado y su firma invalida un JAR
reempaquetado) y hay que **fusionar** los `META-INF/services`, porque si un JAR pisa los del
anterior, Java pierde el lector de MP3 y sólo reproduce WAV.

### Errores del motor de audio

| Fichero | Error | Efecto |
|---|---|---|
| `AudioManager` | Mixer sin comprobar y línea nula tras `LineUnavailableException` | `NullPointerException` al reproducir sin dispositivo elegido o con la línea ocupada |
| `AudioManager` | `MASTER_GAIN` no existe en todas las líneas, y un diálogo modal por pulsación | `IllegalArgumentException` en algunos dispositivos; decenas de ventanas de error al mantener una hotkey |
| `Utils` | `PLAYALL`: flag estático **no volátil** que cada clip nuevo devolvía a `true` | "Stop All" no paraba nada si otro clip arrancaba a la vez. Sustituido por un contador de generación atómico |
| `Utils` | `stopFilePlaying` con cast directo y sin comprobar `null` | `NullPointerException` / `ClassCastException` al desactivar el solapamiento |
| `Utils` | `micInjector.start()` sobre un `Thread` ya usado | `IllegalThreadStateException`: desactivar y reactivar el Mic Injector lo dejaba muerto hasta reiniciar |
| `MicInjector` | `read()` devuelve 0 sin bloquear si la línea se para | Bucle a **100 % de CPU** de un núcleo |
| `MicInjector` | Líneas nunca cerradas al parar; mixers y líneas nulas sin validar | Micrófono y cable virtual ocupados indefinidamente; `NullPointerException` en cadena |
| `SoundboardFrame` | `com.apple.eawt.Application`, eliminado del JDK en Java 9 | En macOS con Java moderno la aplicación no abría |
| `UpdateChecker` | Petición HTTP sin *timeouts* | Un hilo colgado para siempre en cada arranque |
| `Utils` | `startMp3Decoder` pasaba el stream del JAR sin buffer | `IOException: mark/reset not supported`: el precalentado del decodificador MP3 fallaba siempre |

También fuera: `guava-18.0.jar` (2,2 MB) que **no se usaba en ninguna parte**, un `Thumbs.db`
de 70 KB dentro del JAR y el paquete muerto `exp.cache`. Y los componentes Swing ahora se
construyen en el hilo de eventos (EDT), como es debido.

### Interfaz

- **Tema claro y oscuro** con [FlatLaf](https://www.formdev.com/flatlaf/), en *Option →
  Appearance*: seguir al sistema, claro u oscuro. Se recuerda y cambia en caliente. El
  original aplicaba el Look and Feel del sistema mediante un bucle que buscaba Nimbus y
  acababa aplicando otra cosa — un error que explica su aspecto de Windows XP.
- **Colores y fuentes fijos fuera.** `Color.WHITE`, `CYAN`, `RED`, `DARK_GRAY`, separadores
  negros y `Tahoma` a tamaño fijo hacían imposible cualquier tema oscuro. Todo pasa ahora por
  `exp.gui.Ui`, que los deriva del tema activo.
- **Ventana principal reorganizada**: el MigLayout de 14 columnas con anchos fijos en píxeles
  (que no escalaban con el DPI) es ahora una columna con secciones, márgenes reales y tamaños
  derivados de la fuente. La tabla tiene filas alternas, proporciones sensatas y ya no
  arranca con una fila vacía fantasma. Recuerda el tamaño de la ventana.

## Verificación

```bash
java -Djava.awt.headless=true -cp "test-classes;classes;resources;lib/*" exp.soundboard.SmokeTest
java -cp "test-classes;classes;resources;lib/*" exp.soundboard.HotkeyTest
java -cp "test-classes;classes;resources;lib/*" exp.gui.ThemeSwitchTest
```

- `SmokeTest` — reproducir sin dispositivo no revienta; "Stop All" corta de verdad; y un clip
  nuevo **no** revive a los que se acaban de parar.
- `HotkeyTest` — el hook nativo de teclado (JNativeHook, binario de 2013) sigue recibiendo
  pulsaciones en Windows 11.
- `ThemeSwitchTest` — el cambio de tema en caliente no lanza excepciones.
- `exp.gui.DesignShots <carpeta> [light|dark]` renderiza cada ventana a PNG sin mostrarla, y
  `exp.gui.DesignPreview [light|dark]` abre la principal con clips de ejemplo.

## Lo que sigue pendiente

Ver [issues](../../issues). En resumen: latencia de reproducción (se abre una línea de audio
nueva por clip), rutas absolutas en los `.json` que se rompen al mover la carpeta de sonidos,
dependencias de 2014 (JNativeHook 1.1, gson 2.2.4, `jave-1.0.2` con dos binarios de ffmpeg de
2009 que son el grueso del JAR) y el comprobador de actualizaciones, que raspa el HTML de
SourceForge buscando un formato que ya no existe.

## Licencia

[CC BY-SA 3.0](LICENSE), la misma del original. Las librerías de terceros mantienen cada una
la suya: ver [`THIRD-PARTY.md`](THIRD-PARTY.md).
