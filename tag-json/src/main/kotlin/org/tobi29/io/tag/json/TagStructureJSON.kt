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

package org.tobi29.io.tag.json

import org.tobi29.io.EndOfStreamException
import org.tobi29.io.tag.TagMap
import org.tobi29.io.tag.writeContents
import org.tobi29.stdex.Readable

fun readJSON(stream: Readable): TagMap = readJSON { stream.read() }

fun readJSON(
    str: String,
    offset: Int = 0,
    size: Int = str.length - offset
): TagMap {
    val end = offset + size
    var i = offset
    return readJSON {
        if (i >= end) throw EndOfStreamException()
        str[i++]
    }
}

fun TagMap.writeJSON(stream: Appendable, pretty: Boolean = true) {
    if (pretty) {
        TagStructureWriterPrettyJSON(stream).let { writer ->
            writer.begin(this)
            writeContents(writer)
            writer.end()
        }
    } else {
        TagStructureWriterMiniJSON(stream).let { writer ->
            writer.begin(this)
            writeContents(writer)
            writer.end()
        }
    }
}
