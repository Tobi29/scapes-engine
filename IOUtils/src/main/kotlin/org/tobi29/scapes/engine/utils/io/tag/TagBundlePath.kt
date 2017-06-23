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

data class TagBundlePath(private val bundle: TagBundle,
                         private val path: String) : Path {
    private val data by lazy { bundle.resolve(path) }

    override fun get(path: String): Path {
        UnixPathEnvironment.run {
            return TagBundlePath(bundle,
                    this@TagBundlePath.path.resolve(path))
        }
    }

    override fun parent(): Path? {
        UnixPathEnvironment.run {
            return path.resolve("..").normalize().let {
                TagBundlePath(bundle, it)
            }
        }
    }

    override fun <R> read(reader: (ReadableByteStream) -> R): R {
        val stream = data?.asReadOnlyBuffer()?.let(::ByteBufferStream)
                ?: throw IOException("Entry does not exist")
        return reader(stream)
    }

    override fun exists() = data != null

    override fun channel() = data?.asReadOnlyBuffer()?.let(::ByteBufferChannel)
            ?: throw IOException("Entry does not exist")
}
