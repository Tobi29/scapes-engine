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

import org.tobi29.scapes.engine.utils.io.ByteStreamInputStream
import org.tobi29.scapes.engine.utils.io.ReadableByteStream
import org.tobi29.scapes.engine.utils.tag.*
import java.io.IOException
import java.io.InputStream
import javax.json.Json
import javax.json.stream.JsonParser

class TagStructureReaderJSON(streamIn: InputStream) : AutoCloseable {
    private val reader: JsonParser

    constructor(stream: ReadableByteStream) : this(
            ByteStreamInputStream(stream))

    init {
        reader = Json.createParser(streamIn)
        val event = reader.next()
        if (event != JsonParser.Event.START_OBJECT) {
            throw IOException("No root object start")
        }
    }

    override fun close() {
        reader.close()
    }

    fun readMap(map: MutableMap<String, Tag>) {
        while (true) {
            val keyEvent = reader.next()
            when (keyEvent) {
                JsonParser.Event.END_OBJECT -> {
                    return
                }
                JsonParser.Event.KEY_NAME -> {
                    // Continue
                }
                else -> throw IOException("Expected key, but found: $keyEvent")
            }
            val key = reader.string
            val event = reader.next()
            when (event) {
                JsonParser.Event.START_OBJECT -> {
                    map[key] = TagMap { readMap(this) }
                }
                JsonParser.Event.START_ARRAY -> {
                    map[key] = TagList { readList(this) }
                }
                JsonParser.Event.VALUE_NUMBER -> {
                    if (reader.isIntegralNumber) {
                        map[key] = reader.bigDecimal.toBigIntegerExact()
                    } else {
                        map[key] = reader.bigDecimal
                    }
                }
                JsonParser.Event.VALUE_STRING -> {
                    map[key] = reader.string
                }
                JsonParser.Event.VALUE_NULL -> {
                    map[key] = Unit
                }
                JsonParser.Event.VALUE_FALSE -> {
                    map[key] = false
                }
                JsonParser.Event.VALUE_TRUE -> {
                    map[key] = true
                }
                else -> throw IOException("Unexpected event: $event")
            }
        }
    }

    fun readList(list: MutableList<Tag>) {
        while (true) {
            val event = reader.next()
            when (event) {
                JsonParser.Event.START_OBJECT -> {
                    list.add(TagMap { readMap(this) })
                }
                JsonParser.Event.START_ARRAY -> {
                    list.add(TagList { readList(this) })
                }
                JsonParser.Event.VALUE_NUMBER -> {
                    if (reader.isIntegralNumber) {
                        list.add(reader.bigDecimal.toBigIntegerExact().toTag())
                    } else {
                        list.add(reader.bigDecimal.toTag())
                    }
                }
                JsonParser.Event.VALUE_STRING -> {
                    list.add(reader.string.toTag())
                }
                JsonParser.Event.VALUE_NULL -> {
                    list.add(Unit.toTag())
                }
                JsonParser.Event.VALUE_FALSE -> {
                    list.add(false.toTag())
                }
                JsonParser.Event.VALUE_TRUE -> {
                    list.add(true.toTag())
                }
                JsonParser.Event.END_ARRAY -> return
                else -> throw IOException("Unexpected event: $event")
            }
        }
    }
}
