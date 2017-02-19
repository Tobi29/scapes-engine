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
import org.tobi29.scapes.engine.utils.io.tag.TagMap
import org.tobi29.scapes.engine.utils.io.tag.write
import org.tobi29.scapes.engine.utils.io.use
import java.io.IOException
import javax.json.JsonException

fun readJSON(stream: ReadableByteStream): TagMap {
    try {
        TagStructureReaderJSON(stream).use { reader ->
            return TagMap { reader.readMap(this) }
        }
    } catch (e: JsonException) {
        throw IOException(e)
    }
}

fun TagMap.writeJSON(stream: WritableByteStream,
                     pretty: Boolean = true) {
    try {
        TagStructureWriterJSON(stream, pretty).let { writer ->
            writer.begin(this)
            write(writer)
            writer.end()
        }
    } catch (e: JsonException) {
        throw IOException(e)
    }
}
