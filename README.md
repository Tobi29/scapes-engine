# Scapes Engine
General purpose game engine written in Kotlin and various cross-platform
libraries to allow as much code sharing as possible.

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
actively tested. There are known issues when running on Dalvik in particular
with Vorbis decoding, hence targeting it is discouraged.
Also as Java performance varies a lot between older Android releases to now,
the engine probably will not run smoothly on older devices.

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
The engine has been successfully compiled and run using Kotlin/Native by
implementing the backend modules used for the jvm as well, with a reasonable
amount of modules working.
Once the transition to the new multiplatform is complete first code pushes
should be possible.

## Build
The project uses Gradle to build all modules.

You can run `:publishToMavenLocal` to install all its module as maven artifacts.

To use anything you can add any module through
[jitpack.io](https://jitpack.io/#Tobi29/ScapesEngine) or set up a composite
build.

## Artifacts
All platform artifacts follow a naming scheme:

| Platform      | Suffix      |
|:--------------|:------------|
| Common        | -metadata   |
| JVM           | -jvm        |
| JVM (LWJGL 3) | -jvm-lwjgl3 |
| JS            | -js         |

The `-jvm-lwjgl3` suffix is used for JVM artifacts that rely on LWJGL 3 and
hence cannot be used on Android currently.

# Modules (Tier 0)
Only contains `stdex` as it mostly acts as a compatibility layer.

## STDEx
Random extensions to the Kotlin stdlib to make multiplatform code easier.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| stdex                   |    ✓    |    ✓    |

# Modules (Tier 1)
Modules providing fundamental data types to be used and shared in other modules

## Arrays
Various interfaces for arrays and slices mostly useful for creating
very generic parameter types (whilst avoiding the more complex collections
from Kotlin).

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| arrays                  |    ✓    |    ✓    |

## Math
Various math primitives and utils

Contains faster approximations
(e.g. lookup table based trigonometric functions), simple vectors and matrices
useful for game development, as well as a cross platform alternative to
`java.util.Random`.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| math                    |    ✓    |    ✓    |

## Profiler
Tracing profiler facade to allow cross platform code to take advantage
of platform specific trace apis (e.g. Android).

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| profiler                |    ✓    |    ✓    |

## Tag
Tag structure classes as a universal JSON-like memory structure for
dynamic hierarchical data.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| tag                     |    ✓    |    ✓    |

## Utils
Contains various convenience functions and provides a common foundation
for everything else to use.

Depends on various libraries that provide features available in Java 8 to
allow easier Android support.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| utils                   |    ✓    |    ✓    |

## Uuid
A cross-platform Uuid class.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| uuid                    |    ✓    |    ✓    |

# Modules (Tier 2)

## Algorithms
Generic implementations of various algorithms, in particular for AI,
such as moving ships to collect loot.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| algorithms              |    ✓    |    ✓    |

## Argument Parser
Simple lightweight argument parser both for parsing command line arguments
as well as internal interactive commands (e.g. for remotely controlling a
server or in-game commands).

Allows building metadata once and parsing commands using that on threads
in parallel multiple times.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| argumentparser          |    ✓    |    ✓    |

## Base64
Simple base 64 encoder and decoder with very flexible input and output
handling.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| base64                  |    ✓    |    ✓    |

## Checksums
Contains CRC32, Adler32 and SHA-256 implementations.
Additional algorithms may come eventually.
The JVM version uses the `MessageDigest` class wherever appropriate in order to
keep things lightweight.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| checksums               |    ✓    |    ✓    |

## Chrono
Basic calendar classes and convertion from time instants to dates and time.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| chrono                  |    ✓    |    ✓    |

## Compression Deflate
Deflate and Inflate filters.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| compression-deflate     |    ✓    |    ✓    |

## Content Info
Utilities for inspecting file contents, such as guessing the
MIME types (aka Media Types) of some data.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| contentinfo             |    ✓    |    ✓    |

## Coroutines
Various coroutine and thread related utilities.

In particular useful to write cross-platform code that takes advantage of
threads on the JVM but relies on coroutines on JS.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| coroutines              |    ✓    |    ✓    |

## Graphics Utils
Library for image loading, writing and very basic manipulations.

Contains a PNG decoder and encoder function and a simple
Image class to pass around images and do copy paste operations on them.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| graphics-utils          |    ✓    |    ✓    |

## IO
Base library for IO operations inspired by `java.nio`.

Contains various interfaces and utilities for doing IO work.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| io                      |    ✓    |    ✓    |

## Logging
Basic logging facade, backing into a platform specific implementaion.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| logging                 |    ✓    |    ✓    |

### Dependencies
  * [SLF4J](http://www.slf4j.org) (JVM only)

## Tag Binary
Compact binary format for tag structure with compression support.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| tag-binary              |    ✓    |    ✓    |

## Tag Bundle
File tree stored in a tag structure

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| tag-bundle              |    ✓    |    ✓    |

## Tag JSON
JSON parser and writer using tag structures.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| tag-json                |    ✓    |    ✓    |

## Tile Maps
Basic classes for storing tile maps.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| tilemaps                |    ✓    |    ✓    |

# Modules (Tier 3)

## Application Framework
Simple entry point framework for as an easy starting point for all kinds
of applications.

Automatically provides infrastructure for command line parsing, exit codes
and program metadata.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| application-framework   |    ✓    |    ✓    |

## Filesystem
A file system api designed as a basic alternative to `java.nio.file`.

Has a `java.nio.file` backend for the JVM and also a `java.io` based one for
Android or other platforms lacking support for `java.nio.file`.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| filesystem              |    ✓    |         |
| filesystem-nio          |    ✓    |         |
| filesystem-io           |    ✓    |         |

## Platform Integration
Various utilities to allow integrating with the host platform.

Includes cross-platform functions for retrieving platform info,
environment variables and standard paths.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| platformintegration     |    ✓    |    ✓    |

# Modules (Tier 4)

## Codec
Audio decoding library that can use mime-types to identify the format
and load an appropriate decoder through an SPI.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| codec                   |    ✓    |         |
| codec-mp3               |    ✓    |         |
| codec-ogg               |    ✓    |         |
| codec-wav               |    ✓    |         |

### Dependencies
  * [JOrbis](http://www.jcraft.com/jorbis) (OGG Vorbis and Opus, JVM only)
  * [Concentus](https://github.com/lostromb/concentus) (Code is currently stored
    [here](https://github.com/Tobi29/Concentus)) (OGG Vorbis and Opus, JVM only)
  * [JLayer](http://www.javazoom.net/javalayer/javalayer.html)
    (MP3, JVM only)

## Generation Utils
Various utilities for procedural generation, such as Perlin and OpenSimplex
noise, noise transformations and maze generators

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| generation-utils        |    ✓    |    ✓    |

## JBox2D
Kotlin port of [JBox2D](https://github.com/jbox2d/jbox2d).

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| jbox2d                  |    ✓    |    ✓    |

## LibTiled
A JVM-only parser for [Tiled](https://mapeditor.org), which in turn is loosely
based on their old Java library.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| libtiled                |    ✓    |         |

## Server Framework
Various utilities for non-blocking protocol implementations and
encryption using SSL/TLS.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| server-framework        |    ✓    |    ✓    |

## Shader Compiler
Intermediate representation for shaders, can generate GLSL shaders on runtime.

There also is a JVM-only frontend for compiling the custom shader language.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| shader-compiler         |    ✓    |    ✓    |
| shader-clike            |    ✓    |         |

# Modules (Engine)

## Engine
General purpose engine foundation

Straps together a backend and various utilities to allow cross-platform
development of games or more sophisticated engines

On Kotlin/JVM at least either `gl-backend-jvm-lwjgl3` or
`gles-backend-jvm-lwjgl3` should be used together with `glfw-backend-jvm-lwjgl3`
in order to start the engine.

On Kotlin/JS `gles-backend-js` provides everything needed for starting
the engine in an HTML 5 canvas.

### Artifacts
| Component               |   JVM   | JVM (LWJGL 3) |   JS    |
|:------------------------|:-------:|:-------------:|:-------:|
| engine                  |    ✓    |               |    ✓    |
| gl-backend              |         |       ✓       |    ✓    |
| gles-backend            |         |       ✓       |    ✓    |
| glfw-backend            |         |       ✓       |         |
| lwjgl3-backend          |         |       ✓       |         |

### Dependencies
  * [LWJGL](http://lwjgl.org)
  
## Tile Maps Renderer
2D tile renderer.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| tilemaps-renderer       |    ✓    |    ✓    |

# Modules (Misc)

## MBeans CPU Reader
CPU usage reader backend using mbeans on some JVMs.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| mbeans-cpu-reader       |    ✓    |         |

## SWT Utils
Framework for multi-document SWT based gui applications.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| swt-utils               |    ✓    |         |

### Dependencies
  * [SWT](https://www.eclipse.org/swt)

# Modules (Testing)
## Test Assertions
Basic test assertions, in particular useful for Spek.

### Artifacts
| Component               |   JVM   |   JS    |
|:------------------------|:-------:|:-------:|
| test-assertions         |    ✓    |    ✓    |
