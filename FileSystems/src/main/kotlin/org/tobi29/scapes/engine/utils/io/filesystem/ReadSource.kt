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

import org.tobi29.scapes.engine.utils.io.ReadableByteStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.channels.ReadableByteChannel

interface ReadSource {
    fun exists(): Boolean

    @Throws(IOException::class)
    fun readIO(): InputStream

    @Throws(IOException::class)
    fun <R> read(reader: (ReadableByteStream) -> R): R

    @Throws(IOException::class)
    fun channel(): ReadableByteChannel

    @Throws(IOException::class)
    fun reader(): BufferedReader {
        return BufferedReader(InputStreamReader(readIO()))
    }

    @Throws(IOException::class)
    fun mimeType(): String
}