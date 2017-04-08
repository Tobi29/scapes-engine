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

import org.tobi29.scapes.engine.utils.io.ByteStreamOutputStream
import org.tobi29.scapes.engine.utils.io.WritableByteStream
import org.tobi29.scapes.engine.utils.tag.*
import java.io.IOException
import java.io.OutputStream
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import javax.json.Json
import javax.json.stream.JsonGenerator
import javax.json.stream.JsonGeneratorFactory

class TagStructureWriterJSON(streamOut: OutputStream,
                             pretty: Boolean) : TagStructureWriter {
    private val writer: JsonGenerator

    constructor(stream: WritableByteStream,
                pretty: Boolean) : this(
            ByteStreamOutputStream(stream), pretty)

    init {
        writer = if (pretty) {
            FACTORY_PRETTY.createGenerator(streamOut, StandardCharsets.UTF_8)
        } else {
            FACTORY.createGenerator(streamOut, StandardCharsets.UTF_8)
        }
    }

    override fun begin(root: TagMap) {
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

    override fun beginList() {
        writer.writeStartArray()
    }

    override fun beginListStructure() {
        writer.writeStartObject()
    }

    override fun endListWithTerminate() {
        writer.writeEnd()
        writer.writeEnd()
    }

    override fun endListWithEmpty() {
        writer.writeStartObject()
        writer.writeEnd()
        writer.writeEnd()
    }

    override fun endList() {
        writer.writeEnd()
    }

    override fun listEmpty(key: String) {
        writer.writeStartArray(key)
        writer.writeEnd()
    }

    override fun listEmpty() {
        writer.writeStartArray()
        writer.writeEnd()
    }

    override fun writePrimitiveTag(key: String,
                                   tag: TagPrimitive) {
        when (tag) {
            is TagUnit -> writer.writeNull(key)
            is TagBoolean -> writer.write(key, tag.value)
            is TagInteger -> writer.write(key, BigInteger(tag.value.toString()))
            is TagDecimal -> writer.write(key, BigDecimal(tag.value.toString()))
            is TagString -> writer.write(key, tag.value)
            is TagByteArray -> {
                writer.writeStartArray(key)
                for (value in tag.value) {
                    writer.write(value.toInt())
                }
                writer.writeEnd()
            }
            else -> throw IOException("Invalid type: ${tag::class}")
        }
    }

    override fun writePrimitiveTag(tag: TagPrimitive) {
        when (tag) {
            is TagUnit -> writer.writeNull()
            is TagBoolean -> writer.write(tag.value)
            is TagInteger -> writer.write(BigInteger(tag.value.toString()))
            is TagDecimal -> writer.write(BigDecimal(tag.value.toString()))
            is TagString -> writer.write(tag.value)
            is TagByteArray -> {
                writer.writeStartArray()
                for (value in tag.value) {
                    writer.write(value.toInt())
                }
                writer.writeEnd()
            }
            else -> throw IOException("Invalid type: ${tag::class}")
        }
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
