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

package org.tobi29.scapes.engine.utils.io.tag

import org.apache.tika.Tika
import org.tobi29.scapes.engine.utils.IOException
import org.tobi29.scapes.engine.utils.io.*

class TagBundleResource(private val data: ByteBuffer? = null) : ReadSource {
    override fun <R> read(reader: (ReadableByteStream) -> R): R {
        val stream = data?.asReadOnlyBuffer()?.let(::ByteBufferStream)
                ?: throw IOException("Entry does not exist")
        return reader(stream)
    }

    override fun exists() = data != null

    override fun readIO() = data?.asReadOnlyBuffer()?.let(::ByteBufferStream)
            ?.let(::ByteStreamInputStream)
            ?: throw IOException("Entry does not exist")

    override fun channel() = data?.asReadOnlyBuffer()?.let(::ByteBufferChannel)
            ?: throw IOException("Entry does not exist")

    override fun mimeType(): String {
        readIO().use { streamIn -> return Tika().detect(streamIn) }
    }

    override fun hashCode() = data?.hashCode() ?: 0

    override fun equals(other: Any?): Boolean {
        if (other !is TagBundleResource) {
            return false
        }
        return data == other.data
    }
}
