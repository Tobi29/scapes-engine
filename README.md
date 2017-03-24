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

Requires OpenGL ES 3.0, making it impossible to run the engine in an emulator.
Should theoretically support API Level 18 and higher, however only 25 is
actively tested. Also as Java performance varies a lot between older Android
releases to now, the engine probably will not run smoothly on older devices.

Embedding into existing applications might be possible, but is not supported
at the moment and requires a custom backend.

### iOS
The engine can run on the [Multi OS Engine](https://multi-os-engine.org),
however there is no backend for it that is in a usable state. There might be one
added in the future, but this is rather low priority.

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
  * [Kotlin Logging](https://github.com/MicroUtils/kotlin-logging)
  * [SLF4J](http://www.slf4j.org)
  * [ThreeTen](http://www.threeten.org)
  * [Base64](https://github.com/karlroberts/base64)
  * [JUnit 5](http://junit.org/junit5) (testing only)
  * [Spek](http://spekframework.org) (testing only)
### For JSON support
  * [JSON Processing](https://jsonp.java.net)

## IO utils
Base library for IO operations using `java.nio`

Contains various interfaces and utilities for doing IO work
### Dependencies
  * Utils

## File system api
File system api designed as a basic alternative to `java.nio.file`

Has a `java.nio.file` backend for the JVM and also a `java.io` based one for
Android or other platforms lacking support for `java.nio.file`

Uses Apache Tika for cross-platform mime-type support
### Dependencies
  * IO utils
  * [Apache Tika](https://tika.apache.org)

## Audio codecs
Audio decoding library that can use mime-types to identify the format
and load an appropriate decoder through an SPI
### Dependencies
  * File system api
### For OGG Vorbis support
  * [JOrbis](http://www.jcraft.com/jorbis)
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
