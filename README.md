# Scapes Engine
General purpose game engine written in Kotlin.

Its codebase was split off [Scapes](https://github.com/Tobi29/Scapes) to make it
reusable for other projects.

## Platforms

### Linux, MacOSX, Windows
Uses LWJGL3 for access to GLFW (for graphics and input) and OpenAL-soft (for
audio).

Either OpenGL 3.3 or OpenGL ES 3.0 need to be supported by the graphics
driver (support for ES is not automatically enabled currently).

Can be embedded into any program as long as giving up the main thread to the
rendering loop is acceptable (one also could make a customized backend to allow
embedding into an existing loop on the main thread).

### Android
Android support is contained in the
[ScapesEngineAndroid](https://github.com/Tobi29/ScapesEngineAndroid) repository.
For technical information visit that repository.

Requires OpenGL ES 3.0, should run in an emulator with it enabled, however not
tested due to rather interesting errors when trying to run it on Mesa Radeonsi.
Should theoretically support API Level 18 and higher, however only 25 is
actively tested. Also as Java performance varies a lot between older Android
releases to now, the engine probably will not run smoothly on older devices.

Embedding into existing applications can be achieved using the available classes
in the backend module.

### iOS
The engine can run on the [Multi OS Engine](https://multi-os-engine.org),
however there is no backend for it that is in a usable state. There might be one
added in the future, but this is rather low priority.

### Kotlin/JS
Kotlin/JS is not supported at the moment, but various modules have been
partially ported, waiting for more stable multiplatform support in Kotlin for
public upload.

Current efforts are targeted at browser support, Node.js not being planned (Use
JVM for server code).

### Kotlin/Native
Kotlin/Native is not supported at the moment, using some of the Kotlin/JS code
and monkey-patching the incomplete stdlib already works fairly well, however
waiting for multiplatform support for public upload.

Due to the early stage of development of Kotlin/Native, no scope has been
decided on for future support.

## Build
The project uses Gradle to build all modules.

You can run `:install` to install all its module as maven artifacts.

To use anything you can add any module through
[jitpack.io](https://jitpack.io/#Tobi29/ScapesEngine) or set up a composite
build.

# Modules

## Utils
Base library used by all other modules

Contains various convenience functions and provides a common foundation
for everything else to use

Depends on various libraries that provide features available in Java 8 to
allow easier Android support
### Dependencies
  * [Kotlin](https://kotlinlang.org)
  * [SLF4J](http://www.slf4j.org)
  * [ThreeTen](http://www.threeten.org)
  * [Base64](https://github.com/karlroberts/base64)
  * [JUnit 5](http://junit.org/junit5) (testing only)
  * [Spek](http://spekframework.org) (testing only)
### For JSON support
  * [JSON Processing](https://jsonp.java.net)

## Math utils
Various math primitives and utils

Contains concise wrappers for `java.lang.Math` and faster approximations
(e.g. lookup table based trigonometric functions), simple vectors and matrices
useful for game development, as well as wrappers around `java.util.Random`.
### Dependencies
  * Utils

## IO utils
Base library for IO operations using `java.nio`

Contains various interfaces and utilities for doing IO work

Uses Apache Tika for cross-platform mime-type support
### Dependencies
  * Utils
  * [Apache Tika](https://tika.apache.org)

## File system api
File system api designed as a basic alternative to `java.nio.file`

Has a `java.nio.file` backend for the JVM and also a `java.io` based one for
Android or other platforms lacking support for `java.nio.file`
### Dependencies
  * IO utils

## Audio codecs
Audio decoding library that can use mime-types to identify the format
and load an appropriate decoder through an SPI
### Dependencies
  * File system api
### For OGG Vorbis and Opus support
  * [JOrbis](http://www.jcraft.com/jorbis)
  * [Concentus](https://github.com/lostromb/concentus) (Code is currently
    included in this repo as there is no up-to-data build it seems)
### For MP3 support
  * [JLayer](http://www.javazoom.net/javalayer/javalayer.html)

## SQL api
Simple SQL api to allow basic database access without manually writing
SQL statements

Allows abstracting over different backends more reliably
### Dependencies
  * Utils
### For SQLite support
  * [SQLite JDBC Driver](https://github.com/xerial/sqlite-jdbc)
### For pure Java SQLIte support
  * [SQLJet](https://sqljet.com)
### For MariaDB support
  * [MariaDB](https://mariadb.org)

## Graphics utils
Library for image loading, writing and very basic manipulations

Contains a PNG decoder and encoder function implemented by PNGJ and a simple
Image class to pass around images and do copy paste operations on them
### Dependencies
  * IO utils
  * [PNGJ](https://github.com/leonbloy/pngj)

## SWT utils
Base library for making GUI applications using SWT

Contains various utilities and a simple framework for an application
### Dependencies
  * File system api
  * [SWT](https://www.eclipse.org/swt)

## Engine
General purpose engine foundation

Straps together a backend and various utilities to allow cross-platform
development of games or more sophisticated engines
### Dependencies
  * [Antlr](http://www.antlr.org)
  * OpenGL 3.3
  * OpenAL 1.1
  * A binding for OpenGL, OpenAL, dialogs and font rendering
### Default Backend
  * [LWJGL](http://lwjgl.org)
