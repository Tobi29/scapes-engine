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

package org.tobi29.scapes.engine.utils.io.tag.json

import org.tobi29.scapes.engine.utils.io.ReadableByteStream
import org.tobi29.scapes.engine.utils.io.WritableByteStream
import org.tobi29.scapes.engine.utils.io.tag.TagStructure
import org.tobi29.scapes.engine.utils.io.use
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.math.BigDecimal
import javax.json.JsonException

open class TagStructureJSON {
    companion object {
        // Workarounds to store non-finite numbers
        val POSITIVE_INFINITY: BigDecimal = BigDecimal(Double.MAX_VALUE).add(
                BigDecimal("1.0"))
        val NEGATIVE_INFINITY: BigDecimal = POSITIVE_INFINITY.multiply(
                BigDecimal("-1.0"))
        val NAN: BigDecimal = POSITIVE_INFINITY.add(BigDecimal("2.0"))

        @Throws(IOException::class)
        fun write(tagStructure: TagStructure,
                  streamOut: OutputStream,
                  pretty: Boolean = true) {
            tagStructure.write(TagStructureWriterJSON(streamOut, pretty))
        }

        @Throws(IOException::class)
        fun read(streamIn: InputStream): TagStructure {
            return read(TagStructure(), streamIn)
        }

        @Throws(IOException::class)
        fun read(tagStructure: TagStructure,
                 streamIn: InputStream): TagStructure {
            TagStructureReaderJSON(streamIn).use { reader ->
                reader.readStructure(tagStructure)
            }
            return tagStructure
        }

        @Throws(IOException::class)
        fun write(tagStructure: TagStructure,
                  stream: WritableByteStream,
                  pretty: Boolean = true) {
            tagStructure.write(TagStructureWriterJSON(stream, pretty))
        }

        @Throws(IOException::class)
        fun read(stream: ReadableByteStream,
                 tagStructure: TagStructure = TagStructure()): TagStructure {
            try {
                TagStructureReaderJSON(stream).use { reader ->
                    reader.readStructure(tagStructure)
                }
            } catch (e: JsonException) {
                throw IOException(e)
            }
            return tagStructure
        }
    }
}
