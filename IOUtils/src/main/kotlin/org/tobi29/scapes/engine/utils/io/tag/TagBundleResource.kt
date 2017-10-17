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

import org.tobi29.scapes.engine.utils.io.*

data class TagBundleResource(private val data: ByteViewRO? = null) : ReadSource {
    override fun <R> read(reader: (ReadableByteStream) -> R): R {
        val stream = data?.viewBE?.let(::MemoryViewReadableStream)
                ?: throw IOException("Entry does not exist")
        return reader(stream)
    }

    override fun exists() = data != null

    override fun channel() = data?.viewBE?.let(::MemoryViewReadableStream)
            ?.let(::ReadableByteStreamChannel)
            ?: throw IOException("Entry does not exist")

    override suspend fun data(): ByteViewRO =
            data ?: throw IOException("Entry does not exist")
}
