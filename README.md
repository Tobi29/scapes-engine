# Scapes Engine
General purpose game engine written in Java.

Its codebase was split off [Scapes](https://github.com/Tobi29/Scapes) to make it
reusable for other projects.

## Build
The engine is not meant to be built directly, but should be included into your
project by either using a local copy or adding this repository as a submodule.

## Setup
First and foremost, you have to use Gradle for your project in order to use the
default build scripts.
After adding the `ScapesEngine` directory into your project (e.g.
`CoolGame/ScapesEngine`), you need to add
`apply from: "ScapesEngine/include.gradle"` to your `settings.gradle`.

For an example setup for including a backend and optional modules (e.g. codecs),
see [Scapes](https://github.com/Tobi29/Scapes)

## Dependencies
### General
  * Java 8
  * [SLF4J](http://www.slf4j.org)
  * [PNGJ](https://github.com/leonbloy/pngj)
  * [JSON Processing](https://jsonp.java.net)
  * [Apache Tika](https://tika.apache.org)
  * [JUnit](http://junit.org)
  * [Gradle](https://gradle.org)
  * OpenGL 3.2
  * OpenAL 1.1
  * A binding for OpenGL, OpenAL, dialogs and font rendering

### Default Backend
  * [LWJGL](http://lwjgl.org)
  * [SWT](https://www.eclipse.org/swt)

### Codecs
  * [JOrbis](http://www.jcraft.com/jorbis/)
  * [JLayer](http://www.javazoom.net/javalayer/javalayer.html)

### SQL Databases
  * [SQLite4Java](https://bitbucket.org/almworks/sqlite4java)
  * [MariaDB](https://mariadb.org)
