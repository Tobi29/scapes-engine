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
package org.tobi29.scapes.engine.utils.io.tag.json

import org.tobi29.scapes.engine.utils.io.ByteStreamOutputStream
import org.tobi29.scapes.engine.utils.io.WritableByteStream
import org.tobi29.scapes.engine.utils.io.tag.TagStructure
import org.tobi29.scapes.engine.utils.io.tag.TagStructureWriter
import java.io.IOException
import java.io.OutputStream
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import javax.json.Json
import javax.json.stream.JsonGenerator
import javax.json.stream.JsonGeneratorFactory

class TagStructureWriterJSON(streamOut: OutputStream, pretty: Boolean) : TagStructureJSON(), TagStructureWriter {
    private val writer: JsonGenerator

    constructor(stream: WritableByteStream, pretty: Boolean) : this(
            ByteStreamOutputStream(stream), pretty)

    init {
        writer = if (pretty) {
            FACTORY_PRETTY.createGenerator(streamOut, StandardCharsets.UTF_8)
        } else {
            FACTORY.createGenerator(streamOut, StandardCharsets.UTF_8)
        }
    }

    override fun begin(root: TagStructure) {
        writer.writeStartObject()
    }

    override fun end() {
        writer.writeEnd()
        writer.close()
    }

    override fun beginStructure() {
        writer.writeStartObject()
    }

    override fun beginStructure(key: String) {
        writer.writeStartObject(key)
    }

    override fun endStructure() {
        writer.writeEnd()
    }

    override fun structureEmpty() {
        writer.writeStartObject()
        writer.writeEnd()
    }

    override fun structureEmpty(key: String) {
        writer.writeStartObject(key)
        writer.writeEnd()
    }

    override fun beginList(key: String) {
        writer.writeStartArray(key)
    }

    override fun endListWidthTerminate() {
        writer.writeEnd()
        writer.writeEnd()
    }

    override fun endListWithEmpty() {
        writer.writeStartObject()
        writer.writeEnd()
        writer.writeEnd()
    }

    override fun listEmpty(key: String) {
        writer.writeStartArray(key)
        writer.writeEnd()
    }

    override fun writeTag(key: String,
                          tag: Any) {
        if (tag is Boolean) {
            writer.write(key, tag)
        } else if (tag is Byte) {
            writer.write(key, tag.toInt())
        } else if (tag is ByteArray) {
            writer.writeStartArray(key)
            writer.write(8)
            for (value in tag) {
                writer.write(value.toInt())
            }
            writer.writeEnd()
        } else if (tag is Short) {
            writer.write(key, tag.toInt())
        } else if (tag is Int) {
            writer.write(key, tag)
        } else if (tag is Long) {
            writer.write(key, tag)
        } else if (tag is Float) {
            writer.write(key, armor(tag.toDouble()))
        } else if (tag is Double) {
            writer.write(key, armor(tag))
        } else if (tag is Number) {
            // TODO: Use BigDecimal instead?
            writer.write(key, armor(tag.toDouble()))
        } else if (tag is String) {
            writer.write(key, tag)
        } else {
            throw IOException("Invalid type: " + tag.javaClass)
        }
    }

    private fun armor(value: Double): BigDecimal {
        if (value == Double.POSITIVE_INFINITY) {
            return TagStructureJSON.POSITIVE_INFINITY
        } else if (value == Double.NEGATIVE_INFINITY) {
            return TagStructureJSON.POSITIVE_INFINITY
        } else if (value.isNaN()) {
            return TagStructureJSON.NAN
        }
        return BigDecimal(value)
    }

    companion object {
        private val FACTORY: JsonGeneratorFactory
        private val FACTORY_PRETTY: JsonGeneratorFactory

        init {
            val map = ConcurrentHashMap<String, Any>()
            val mapPretty = ConcurrentHashMap<String, Any>()
            mapPretty.put(JsonGenerator.PRETTY_PRINTING, true)
            FACTORY = Json.createGeneratorFactory(map)
            FACTORY_PRETTY = Json.createGeneratorFactory(mapPretty)
        }
    }
}
