# EXP Soundboard 0.5.1 — rework

A modified version of **EXP Soundboard 0.5**, the Java desktop soundboard that
[Expenosa](https://sourceforge.net/projects/expsoundboard/) released in December 2014 and has
not touched since.

The original **no longer starts on Java 9 or later**. This rework brings it back to life on a
modern Java, fixes a dozen bugs in the audio engine and modernises the interface. It **adds no
new features**: same windows, same settings, same `.json` format, same preferences. A
soundboard saved with 0.5 opens here untouched.

> **Attribution.** This is a derivative work and is not official. The original application, its
> name and its logo belong to **Expenosa** (© 2014), released under
> [CC BY-SA 3.0](https://creativecommons.org/licenses/by-sa/3.0/). This repository is
> distributed under the same licence. The changes against the original are listed below and in
> full in [`CHANGES.diff`](CHANGES.diff).
>
> **The sources in this repository are decompiled**; they are not Expenosa's originals, which
> were never published. They were recovered from the 0.5 JAR with
> [CFR](https://www.benf.org/other/cfr/) and fixed up from there. Local variable names and the
> shape of some loops are therefore the decompiler's doing, not anyone's style.

---

## Download

[Releases](../../releases) offers two ways to run it:

- **`EXP-Soundboard-0.5.1-windows-x64.zip`** — for Windows, with a trimmed Java inside.
  Unzip and run `EXP Soundboard.exe`. **No Java installation required.**
- **`EXP-Soundboard-0.5.1.jar`** — for any system with Java 17 or later:
  `java -jar "EXP-Soundboard-0.5.1.jar"`

## Building

Needs JDK 17 or later. Dependencies are not in the repository (see
[`tools/fetch-deps.sh`](tools/fetch-deps.sh) for why):

```bash
tools/fetch-deps.sh          # fetches the libraries (or pass it your copy of the 0.5 JAR)
bash build.sh                # compiles and packages -> dist/
java -jar "dist/EXP Soundboard_051.jar"

tools/build-exe.sh           # optional: Windows executable -> build-exe/
```

The executable is produced by `jpackage` (JDK 14+) as an *app image*: a folder with the `.exe`
and a trimmed Java runtime. No `.msi` installer is generated, because that requires
[WiX Toolset](https://wixtoolset.org/) to be installed.

## What was fixed

### Starting on a modern Java

The original JAR used Eclipse's *jar-in-jar loader*, which loads nested libraries through its
own `rsrc:` protocol — something that stopped working with the class loading changes in Java 9:

```
Exception in thread "main" java.lang.NoClassDefFoundError: org/jnativehook/keyboard/NativeKeyListener
	at org.eclipse.jdt.internal.jarinjarloader.JarRsrcLoader.main(JarRsrcLoader.java:56)
```

`build.sh` produces a flat JAR instead. Two things matter when flattening: the digital
signatures have to go (MigLayout ships signed, and its signature invalidates a repackaged JAR),
and the `META-INF/services` entries have to be **merged**, because if one JAR clobbers the
previous one's, Java loses the MP3 reader and only plays WAV.

### Audio engine bugs

| File | Bug | Effect |
|---|---|---|
| `AudioManager` | Mixer never checked, and a null line after `LineUnavailableException` | `NullPointerException` when playing with no device selected or with the line busy |
| `AudioManager` | `MASTER_GAIN` is not supported by every line, plus one modal dialog per key press | `IllegalArgumentException` on some devices; dozens of error windows when holding a hotkey |
| `Utils` | `PLAYALL`: a **non-volatile** static flag that every new clip set back to `true` | "Stop All" stopped nothing if another clip started at the same time. Replaced with an atomic generation counter |
| `Utils` | `stopFilePlaying` cast blindly and never checked for `null` | `NullPointerException` / `ClassCastException` when clip overlapping was turned off |
| `Utils` | `micInjector.start()` on an already-used `Thread` | `IllegalThreadStateException`: turning the Mic Injector off and back on left it dead until restart |
| `MicInjector` | `read()` returns 0 without blocking once the line stops | Loop burning **100 % of a core** |
| `MicInjector` | Lines never closed on stop; mixers and lines never validated | Microphone and virtual cable claimed indefinitely; cascading `NullPointerException` |
| `SoundboardFrame` | `com.apple.eawt.Application`, removed from the JDK in Java 9 | On macOS with a modern Java the application would not open |
| `UpdateChecker` | HTTP request with no timeouts | A thread hanging forever on every startup |
| `Utils` | `startMp3Decoder` passed the JAR stream unbuffered | `IOException: mark/reset not supported`: warming up the MP3 decoder always failed |

Also gone: `guava-18.0.jar` (2.2 MB) which **nothing used**, a 70 KB `Thumbs.db` inside the JAR,
and the dead `exp.cache` package. And Swing components are now built on the event dispatch
thread, as they should be.

### Interface

- **Light and dark themes** via [FlatLaf](https://www.formdev.com/flatlaf/), under *Option →
  Appearance*: match system, light, or dark. The choice is remembered and applies immediately.
  The original applied the system look and feel through a loop that looked for Nimbus and ended
  up applying something else — a bug, and the reason it looked like Windows XP.
- **Hard-coded colours and fonts gone.** `Color.WHITE`, `CYAN`, `RED`, `DARK_GRAY`, black
  separators and fixed-size `Tahoma` made any dark theme impossible. It all goes through
  `exp.gui.Ui` now, which derives them from the active theme.
- **Main window reorganised**: the 14-column MigLayout with fixed pixel widths (which did not
  scale with display DPI) is now a single column with sections, real margins and font-derived
  sizes. The table has banded rows, sensible proportions, and no longer starts with a phantom
  empty row. Window size is remembered.

## Verifying

```bash
java -Djava.awt.headless=true -cp "test-classes;classes;resources;lib/*" exp.soundboard.SmokeTest
java -cp "test-classes;classes;resources;lib/*" exp.soundboard.HotkeyTest
java -cp "test-classes;classes;resources;lib/*" exp.gui.ThemeSwitchTest
```

- `SmokeTest` — playing with no device does not blow up; "Stop All" really cuts off; and a new
  clip does **not** revive the ones just stopped.
- `HotkeyTest` — the native keyboard hook (JNativeHook, a 2013 binary) still receives key
  presses on Windows 11.
- `ThemeSwitchTest` — switching theme on the fly throws nothing.
- `exp.gui.DesignShots <dir> [light|dark]` renders each window to a PNG without showing it, and
  `exp.gui.DesignPreview [light|dark]` opens the main one with sample clips.

## Still open

See [issues](../../issues). In short: playback latency (a fresh audio line is opened per clip),
absolute paths in the `.json` files that break when the sounds folder moves, 2014 dependencies
(JNativeHook 1.1, gson 2.2.4, `jave-1.0.2` with two 2009 ffmpeg binaries that make up the bulk
of the JAR), and the update checker, which scrapes SourceForge HTML for a format that no longer
exists.

## Licence

[CC BY-SA 3.0](LICENSE), the same as the original. Third-party libraries each keep their own:
see [`THIRD-PARTY.md`](THIRD-PARTY.md).
