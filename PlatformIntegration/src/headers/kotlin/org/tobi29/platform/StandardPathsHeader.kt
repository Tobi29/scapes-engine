/*
 * Copyright 2012-2017 Tobi29
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

/**
 * Home directory of the user, might not be accessible
 */
expect val homeDir: FilePath

/**
 * Writable directory for storing config files, may be same as [dataHome]
 *
 * **Note:** This might be a system-wide directory, so you should *always*
 * resolve [appIDForConfig] with this
 */
expect val configHome: FilePath

/**
 * List of directories that might contain config files, ordered by highest
 * priority first, with first directory being always [configHome]
 *
 * **Note:** This might be a system-wide directory, so you should *always*
 * resolve [appIDForConfig] with this
 *
 * **Note:** It is discouraged to write to any of these and instead use
 * [configHome] for that
 */
expect val configDirs: List<FilePath>

/**
 * Writable directory for storing data files, may be same as [configHome]
 *
 * **Note:** This might be a system-wide directory, so you should *always*
 * resolve [appIDForData] with this
 */
expect val dataHome: FilePath

/**
 * List of directories that might contain data files, ordered by highest
 * priority first, with first directory being always [dataDirs]
 *
 * **Note:** This might be a system-wide directory, so you should *always*
 * resolve [appIDForData] with this
 *
 * **Note:** It is discouraged to write to any of these and instead use
 * [dataHome] for that
 */
expect val dataDirs: List<FilePath>

/**
 * Writable directory for storing cache files, may be located inside [dataHome]
 * with a name like `Cache`
 *
 * **Note:** This might be a system-wide directory, so you should *always*
 * resolve [appIDForCache] with this
 */
expect val cacheHome: FilePath

/**
 * Returns a path to be resolved against [configHome] or one of [configDirs]
 * to retrieve an application specific directory
 * @param id Reverse domain id of the running application
 * @param name Name of the running application
 * @return A relative path, possibly empty
 */
expect fun appIDForConfig(id: String,
                          name: String): FilePath

/**
 * Returns a path to be resolved against [dataHome] or one of [dataDirs]
 * to retrieve an application specific directory
 * @param id Reverse domain id of the running application
 * @param name Name of the running application
 * @return A relative path, possibly empty
 */
expect fun appIDForData(id: String,
                        name: String): FilePath

/**
 * Returns a path to be resolved against [cacheHome]
 * to retrieve an application specific directory
 * @param id Reverse domain id of the running application
 * @param name Name of the running application
 * @return A relative path, possibly empty
 */
expect fun appIDForCache(id: String,
                         name: String): FilePath
