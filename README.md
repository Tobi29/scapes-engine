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

### Kotlin/JS
An initial port of the engine to run on Kotlin/JS allows running a decent
subset of the engine in the browser. There are known issues and limitations
as this is still a work in progress. Assets can be loaded through
XMLHttpRequests as well as 2d audio (music can be streamed and even looped, as
long as Chrome fixes looping streams one day).

### iOS
The engine can run on the [Multi OS Engine](https://multi-os-engine.org),
however there is no backend for it that is in a usable state. There might be one
added in the future, but this is rather low priority.

A Kotlin/Native based port seems feasible as well by now, but requires quite a
bit more work.

### Kotlin/Native
Many modules run on Kotlin/Native by now, however things are still very fast
moving and unstable. Currently the last showstopper that needs to be overcome
is `kotlinx.coroutines` support.

## Build
The project uses Gradle to build all modules.

You can run `:install` to install all its module as maven artifacts.

To use anything you can add any module through
[jitpack.io](https://jitpack.io/#Tobi29/ScapesEngine) or set up a composite
build.

# Modules

## STDEx
Random extensions to the Kotlin stdlib to make multiplatform code easier.

### Artifacts
  * stdex
  * stdex-js
  * stdex-jvm

## Logging
Basic logging facade, backing into a platform specific implementaion.

### Artifacts
  * logging
  * logging-js
  * logging-jvm

### Dependencies
  * [SLF4J](http://www.slf4j.org) (JVM only)

## Tag
Tag structure classes as a universal JSON-like memory structure for
dynamic hierarchical data.

### Artifacts
  * tag
  * tag-js
  * tag-jvm

## Uuid
A cross-platform Uuid class.

### Artifacts
  * uuid
  * uuid-js
  * uuid-jvm

## Arrays
Various interfaces for arrays and slices mostly useful for creating
very generic parameter types (whilst avoiding the more complex collections
from Kotlin).

### Artifacts
  * arrays
  * arrays-js
  * arrays-jvm

## Argument Parser
Simple lightweight argument parser both for parsing command line arguments
as well as internal interactive commands (e.g. for remotely controlling a
server or in-game commands).

Allows building metadata once and parsing commands using that on threads
in parallel multiple times.

### Artifacts
  * argumentparser
  * argumentparser-js
  * argumentparser-jvm

## Base64
Simple base 64 encoder and decoder with very flexible input and output
handling.

### Artifacts
  * base64
  * base64-js
  * base64-jvm

## Checksums
Contains CRC32 and SHA-256 implementations. Additional algorithms may come
eventually. The JVM version uses the `MessageDigest` class in order to keep
things lightweight.

### Artifacts
  * checksums
  * checksums-js
  * checksums-jvm

## Utils
Contains various convenience functions and provides a common foundation
for everything else to use.

Depends on various libraries that provide features available in Java 8 to
allow easier Android support.

### Artifacts
  * utils
  * utils-js
  * utils-jvm

## Coroutines
Various coroutine and thread related utilities.

In particular useful to write cross-platform code that takes advantage of
threads on the JVM but relies on coroutines on JS.

### Artifacts
  * coroutines
  * coroutines-js
  * coroutines-jvm

## IO
Base library for IO operations inspired by `java.nio`.

Contains various interfaces and utilities for doing IO work.

A file system api designed as a basic alternative to `java.nio.file` is
also available for Kotlin/JVM (and later Kotlin/Native).

Has a `java.nio.file` backend for the JVM and also a `java.io` based one for
Android or other platforms lacking support for `java.nio.file`.

### Artifacts
  * io
  * io-js
  * io-jvm
  * filesystem
  * filesystem-jvm
  * filesystem-nio-jvm (not recommended on Android)
  * filesystem-io-jvm (only recommended on Android)

## Content Info
Utilities for inspecting file contents, such as guessing the
MIME types (aka Media Types) of some data.

### Artifacts
  * contentinfo
  * contentinfo-js
  * contentinfo-jvm

### Dependencies
  * [Apache Tika](https://tika.apache.org) (JVM only)

## Math
Various math primitives and utils

Contains faster approximations
(e.g. lookup table based trigonometric functions), simple vectors and matrices
useful for game development, as well as a cross platform alternative to
`java.util.Random`.

### Artifacts
  * math
  * math-js
  * math-jvm

## Graphics Utils
Library for image loading, writing and very basic manipulations.

Contains a PNG decoder and encoder function and a simple
Image class to pass around images and do copy paste operations on them.

### Artifacts
  * graphics-utils
  * graphics-utils-js
  * graphics-utils-jvm

### Dependencies
  * [PNGJ](https://github.com/leonbloy/pngj) (JVM only)

## Generation Utils
Various utilities for procedural generation, such as Perlin and OpenSimplex
noise, noise transformations and maze generators

### Artifacts
  * generation-utils
  * generation-utils-js
  * generation-utils-jvm

## Chrono
Basic calendar classes and convertion from time instants to dates and time.

### Artifacts
  * chrono
  * chrono-js
  * chrono-jvm

## Platform Integration
Various utilities to allow integrating with the host platform.

Includes cross-platform functions for retrieving platform info,
environment variables and standard paths.

### Artifacts
  * platformintegration
  * platformintegration-jvm

## Application Framework
Simple entry point framework for as an easy starting point for all kinds
of applications.

Automatically provides infrastructure for command line parsing, exit codes
and program metadata.

### Artifacts
  * application-framework
  * application-framework-jvm

## SWT Utils
Framework for multi-document SWT based gui applications.

### Artifacts
  * swt-utils-jvm

### Dependencies
  * [SWT](https://www.eclipse.org/swt)

## Server Framework
Various utilities for non-blocking protocol implementations and
encryption using SSL/TLS.

### Artifacts
  * server-framework-jvm

## Codec
Audio decoding library that can use mime-types to identify the format
and load an appropriate decoder through an SPI.

### Artifacts
  * codec-jvm
  * codec-mp3-jvm
  * codec-ogg-jvm (Includes both Vorbis and Opus support)
  * codec-wav-jvm
### Dependencies
  * [JOrbis](http://www.jcraft.com/jorbis) (OGG Vorbis and Opus, JVM only)
  * [Concentus](https://github.com/lostromb/concentus) (Code is currently stored
    [here](https://github.com/Tobi29/Concentus)) (OGG Vorbis and Opus, JVM only)
  * [JLayer](http://www.javazoom.net/javalayer/javalayer.html)
    (MP3, JVM only)

## SQL Framework
Simple SQL api to allow basic database access without manually writing
SQL statements.

Allows abstracting over different backends more reliably.
### Dependencies
  * [SQLite JDBC Driver](https://github.com/xerial/sqlite-jdbc)
    (SQLite, JVM only)
  * [SQLJet](https://sqljet.com) (SQLJet, JVM only)
  * [MariaDB](https://mariadb.org) (MariaDB, JVM only)

## Tile Maps
Basic classes for storing tile maps.

A JVM-only parser for [Tiled](https://mapeditor.org) maps can be found in
the LibTiled artifact, which in turn is loosely based on their old Java library.

### Artifacts
  * tilemaps
  * tilemaps-js
  * tilemaps-jvm
  * libtiled-jvm

## Engine
General purpose engine foundation

Straps together a backend and various utilities to allow cross-platform
development of games or more sophisticated engines

### Artifacts
  * engine
  * engine-js
  * gles-webgl2-backend-js (Backend supporting WebGL 2 and WebAudio)
  * engine-jvm
  * glfw-backend-jvm (Backend supporting OpenGL 3.3, OpenGLES 3.0 using LWJGL
    for bindings)

### Dependencies
  * [LWJGL](http://lwjgl.org)

## Profiler
Tracing profiler facade to allow cross platform code to take advantage
of platform specific trace apis (e.g. Android).

### Artifacts
  * profiler
  * profiler-js
  * profiler-jvm

## Test Assertions
Basic test assertions, in particular useful for Spek.

### Artifacts
  * test-assertions-jvm
