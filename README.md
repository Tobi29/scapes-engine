# Scapes Engine
General purpose game engine written in Kotlin.

Its codebase was split off [Scapes](https://github.com/Tobi29/Scapes) to make it
reusable for other projects.

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

## File system api
File system api designed as a basic alternative to `java.nio.file`

Has a `java.nio.file` backend for the JVM and also a `java.io` based one for
Android (can be found in the ScapesEngineAndroid repository)

Uses Apache Tika for cross-platform mime-type support
### Dependencies
  * Utils
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
  * File system api
### For SQLite support
  * [SQLite JDBC Driver](https://github.com/xerial/sqlite-jdbc)
### For MariaDB support
  * [MariaDB](https://mariadb.org)

## Graphics utils

### Dependencies
  * [PNGJ](https://github.com/leonbloy/pngj)

## SWT utils
### Dependencies
  * [SWT](https://www.eclipse.org/swt)

## Engine
### Dependencies
  * [Antlr](http://www.antlr.org)
  * OpenGL 3.3
  * OpenAL 1.1
  * A binding for OpenGL, OpenAL, dialogs and font rendering
### Default Backend
  * [LWJGL](http://lwjgl.org)
