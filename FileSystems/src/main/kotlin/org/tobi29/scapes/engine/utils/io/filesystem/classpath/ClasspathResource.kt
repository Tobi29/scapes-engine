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

package org.tobi29.scapes.engine.utils.io.filesystem.classpath

import org.apache.tika.Tika
import org.tobi29.scapes.engine.utils.io.BufferedReadChannelStream
import org.tobi29.scapes.engine.utils.io.ReadableByteStream
import org.tobi29.scapes.engine.utils.io.filesystem.ReadSource
import java.io.InputStream
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.security.AccessController
import java.security.PrivilegedAction

class ClasspathResource(private val classLoader: ClassLoader,
                        private val path: String) : ReadSource {
    override fun <R> read(reader: (ReadableByteStream) -> R): R {
        readIO().use { streamIn ->
            return reader(BufferedReadChannelStream(
                    Channels.newChannel(streamIn)))
        }
    }

    override fun exists(): Boolean {
        return AccessController.doPrivileged(
                PrivilegedAction { classLoader.getResource(path) }) != null
    }

    override fun readIO(): InputStream {
        return AccessController.doPrivileged(
                PrivilegedAction { classLoader.getResourceAsStream(path) })
    }

    override fun channel(): ReadableByteChannel {
        return Channels.newChannel(readIO())
    }

    override fun mimeType(): String {
        readIO().use { streamIn -> return Tika().detect(streamIn, path) }
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + path.hashCode()
        result = prime * result + classLoader.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ClasspathResource) {
            return false
        }
        return path == other.path && classLoader == other.classLoader
    }
}
