/*
 * Copyright 2012-2016 Tobi29
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

package org.tobi29.scapes.engine.utils.io.filesystem

import mu.KLogging
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.tobi29.scapes.engine.utils.io.*
import org.tobi29.scapes.engine.utils.toHexadecimal
import java.io.IOException

class FileCache(private val root: FilePath, private val time: Duration = Duration.ofDays(
        16)) {

    init {
        createDirectories(root)
    }

    @Synchronized
    fun store(resource: ReadSource,
              type: String): Location {
        return resource.read { stream -> store(stream, type) }
    }

    @Synchronized
    fun store(stream: ReadableByteStream,
              type: String): Location {
        val write = createTempFile("CacheWrite", ".jar")
        return tempChannel(write) { channel ->
            val digest = Algorithm.SHA256.digest()
            val streamOut = BufferedWriteChannelStream(channel)
            process(stream, { buffer ->
                digest.update(buffer)
                buffer.rewind()
                streamOut.put(buffer)
            })
            streamOut.flush()
            channel.position(0)
            val checksum = digest.digest()
            val parent = root.resolve(type)
            createDirectories(parent)
            val name = checksum.toHexadecimal()
            val streamIn = BufferedReadChannelStream(channel)
            write(parent.resolve(name)) { output ->
                process(streamIn, { output.put(it) })
            }
            Location(type, checksum)
        }
    }

    @Synchronized
    fun retrieve(location: Location): FilePath? {
        val name = location.array.toHexadecimal()
        val file = file(location.type, name)
        if (exists(file)) {
            setLastModifiedTime(file, Instant.now())
            return file
        }
        return null
    }

    @Synchronized
    fun delete(location: Location) {
        val name = location.array.toHexadecimal()
        val file = file(location.type, name)
        deleteIfExists(file)
    }

    @Synchronized
    fun delete(type: String) {
        deleteDir(root.resolve(type))
    }

    @Synchronized
    fun check() {
        val currentTime = Instant.now().minus(time)
        for (invalid in listRecursive(root,
                ::isRegularFile,
                ::isNotHidden, { file ->
            try {
                return@listRecursive getLastModifiedTime(
                        file).isBefore(
                        currentTime)
            } catch (e: IOException) {
                return@listRecursive false
            }
        })) {
            deleteIfExists(invalid)
            logger.debug { "Deleted old cache entry: $invalid" }
        }
    }

    private fun file(type: String,
                     name: String): FilePath {
        return root.resolve(type).resolve(name)
    }

    class Location(val type: String, val array: ByteArray)

    companion object : KLogging()
}