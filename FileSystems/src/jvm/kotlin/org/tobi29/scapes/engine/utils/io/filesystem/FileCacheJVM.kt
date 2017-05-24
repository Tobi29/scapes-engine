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

package org.tobi29.scapes.engine.utils.io.filesystem

import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.tobi29.scapes.engine.utils.Algorithm
import org.tobi29.scapes.engine.utils.io.IOException
import org.tobi29.scapes.engine.utils.io.*
import org.tobi29.scapes.engine.utils.logging.KLogging
import org.tobi29.scapes.engine.utils.toHexadecimal

object FileCache : KLogging() {
    fun store(root: FilePath,
              resource: ReadSource): Location {
        return resource.read { stream -> store(root, stream) }
    }

    fun store(root: FilePath,
              stream: ReadableByteStream): Location {
        val write = createTempFile("CacheWrite", ".tmp")
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
            createDirectories(root)
            val name = checksum.toHexadecimal()
            val streamIn = BufferedReadChannelStream(channel)
            val destination = root.resolve(name)
            if (exists(destination)) {
                setLastModifiedTime(destination, Instant.now())
            } else {
                write(destination) { output ->
                    process(streamIn, { output.put(it) })
                }
            }
            Location(checksum)
        }
    }

    fun retrieve(root: FilePath,
                 location: Location): FilePath? {
        val file = root.resolve(location.array.toHexadecimal())
        if (exists(file)) {
            try {
                setLastModifiedTime(file, Instant.now())
            } catch (e: IOException) {
            }
            return file
        }
        return null
    }

    fun delete(root: FilePath,
               location: Location) {
        val file = root.resolve(location.array.toHexadecimal())
        deleteIfExists(file)
    }

    fun check(root: FilePath,
              time: Duration = Duration.ofDays(16)) {
        val currentTime = Instant.now().minus(time)
        list(root) {
            filter(::isRegularFile).filter(::isNotHidden).filter { file ->
                try {
                    getLastModifiedTime(file).isBefore(currentTime)
                } catch (e: IOException) {
                    false
                }
            }.forEach { file ->
                deleteIfExists(file)
                logger.debug { "Deleted old cache entry: $file" }
            }
        }
    }

    class Location(val array: ByteArray)
}