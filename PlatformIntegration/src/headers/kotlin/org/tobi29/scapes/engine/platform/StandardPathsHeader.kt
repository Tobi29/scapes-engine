package org.tobi29.scapes.engine.platform

import org.tobi29.scapes.engine.utils.io.filesystem.FilePath

/**
 * Home directory of the user, might not be accessible
 */
header val homeDir: FilePath

/**
 * Writable directory for storing config files, may be same as [dataHome]
 *
 * **Note:** This might be a system-wide directory, so you should *always*
 * resolve [appIDForConfig] with this
 */
header val configHome: FilePath

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
header val configDirs: List<FilePath>

/**
 * Writable directory for storing data files, may be same as [configHome]
 *
 * **Note:** This might be a system-wide directory, so you should *always*
 * resolve [appIDForData] with this
 */
header val dataHome: FilePath

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
header val dataDirs: List<FilePath>

/**
 * Writable directory for storing cache files, may be located inside [dataHome]
 * with a name like `Cache`
 *
 * **Note:** This might be a system-wide directory, so you should *always*
 * resolve [appIDForCache] with this
 */
header val cacheHome: FilePath

/**
 * Returns a path to be resolved against [configHome] or one of [configDirs]
 * to retrieve an application specific directory
 * @param id Reverse domain id of the running application
 * @param name Name of the running application
 * @return A relative path, possibly empty
 */
header fun appIDForConfig(id: String,
                          name: String): FilePath

/**
 * Returns a path to be resolved against [dataHome] or one of [dataDirs]
 * to retrieve an application specific directory
 * @param id Reverse domain id of the running application
 * @param name Name of the running application
 * @return A relative path, possibly empty
 */
header fun appIDForData(id: String,
                        name: String): FilePath

/**
 * Returns a path to be resolved against [cacheHome]
 * to retrieve an application specific directory
 * @param id Reverse domain id of the running application
 * @param name Name of the running application
 * @return A relative path, possibly empty
 */
header fun appIDForCache(id: String,
                         name: String): FilePath
