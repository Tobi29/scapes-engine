package org.tobi29.scapes.engine.platform

import org.tobi29.scapes.engine.utils.io.filesystem.FilePath
import org.tobi29.scapes.engine.utils.io.filesystem.exists

/**
 * Searches through the given paths, resolves [path] against them and returns
 * the first existing one
 * @param path The path to resolve with
 * @receiver The paths to resolve onto
 * @return An existing path or `null` if nothing was found
 */
fun Iterable<FilePath>.locate(path: FilePath): FilePath? = asSequence()
        .map { it.resolve(path) }.find { exists(it) }

/**
 * Searches through config directories for the given [path] and returns the
 * highest priority one that exists
 * @param path The path to resolve with
 * @return An existing path or `null` if nothing was found
 */
fun locateConfig(path: FilePath): FilePath? = configDirs.locate(path)

/**
 * Resolves the given [path] onto the config home directory
 * @param path The path to resolve with
 * @return The given [path]  resolved onto [configHome]
 */
fun writeConfig(path: FilePath): FilePath = configHome.resolve(path)

/**
 * Searches through data directories for the given [path] and returns the
 * highest priority one that exists
 * @param path The path to resolve with
 * @return An existing path or `null` if nothing was found
 */
fun locateData(path: FilePath): FilePath? = dataDirs.locate(path)

/**
 * Resolves the given [path] onto the data home directory
 * @param path The path to resolve with
 * @return The given [path]  resolved onto [dataHome]
 */
fun writeData(path: FilePath): FilePath = dataHome.resolve(path)
