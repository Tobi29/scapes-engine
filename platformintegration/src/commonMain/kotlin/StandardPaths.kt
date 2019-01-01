/*
 * Copyright 2012-2019 Tobi29
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tobi29.platform

import org.tobi29.io.filesystem.FilePath
import org.tobi29.io.filesystem.exists
import org.tobi29.utils.Identified
import org.tobi29.utils.Named

/**
 * Home directory of the user, might not be accessible
 */
expect val homeDir: FilePath

/**
 * Directory for storing configuration files
 * @note Might have to be created
 * @note Might be same as [dataHome]
 */
expect fun <M> M.configHome(): FilePath where M : Identified, M : Named

/**
 * Directories for retrieving configuration files
 * @note Might be same as [dataDirectories]
 */
expect fun <M> M.configDirectories(): List<FilePath> where M : Identified, M : Named

/**
 * Directory for storing data files
 * @note Might have to be created
 * @note Might be same as [configHome]
 */
expect fun <M> M.dataHome(): FilePath where M : Identified, M : Named

/**
 * Directories for retrieving data files
 * @note Might be same as [configDirectories]
 */
expect fun <M> M.dataDirectories(): List<FilePath> where M : Identified, M : Named

/**
 * Directory for storing cache files
 * @note Might have to be created
 * @note Might be located at `[dataHome]/Cache`
 */
expect fun <M> M.cacheHome(): FilePath where M : Identified, M : Named

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
fun <M> M.locateConfig(path: FilePath): FilePath? where M : Identified, M : Named =
    configDirectories().locate(path)

/**
 * Resolves the given [path] onto the config home directory
 * @param path The path to resolve with
 * @return The given [path]  resolved onto [configHome]
 */
fun <M> M.writeConfig(path: FilePath): FilePath where M : Identified, M : Named =
    configHome().resolve(path)

/**
 * Searches through data directories for the given [path] and returns the
 * highest priority one that exists
 * @param path The path to resolve with
 * @return An existing path or `null` if nothing was found
 */
fun <M> M.locateData(path: FilePath): FilePath? where M : Identified, M : Named =
    dataDirectories().locate(path)

/**
 * Resolves the given [path] onto the data home directory
 * @param path The path to resolve with
 * @return The given [path]  resolved onto [dataHome]
 */
fun <M> M.writeData(path: FilePath): FilePath where M : Identified, M : Named =
    dataHome().resolve(path)
