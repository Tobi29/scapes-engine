/*
 * Copyright 2012-2018 Tobi29
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

package org.tobi29.io

interface ReadSource {
    fun toUri(): Uri? = null

    val name: String? get() = null

    // TODO: @Throws(IOException::class)
    fun channel(): ReadableByteChannel

    // TODO: @Throws(IOException::class)
    suspend fun <R> readAsync(reader: suspend (ReadableByteStream) -> R): R = channel().use {
        reader(BufferedReadChannelStream(it))
    }

    // TODO: @Throws(IOException::class)
    suspend fun data(): ByteViewRO = readAsync { it.asByteView() }
}

interface ReadSourceLocal : ReadSource {
    // TODO: @Throws(IOException::class)
    fun <R> readNow(reader: (ReadableByteStream) -> R) = channel().use {
        reader(BufferedReadChannelStream(it))
    }

    override suspend fun data() = dataNow()

    // TODO: @Throws(IOException::class)
    fun dataNow(): ByteViewRO = readNow { it.asByteView() }
}
