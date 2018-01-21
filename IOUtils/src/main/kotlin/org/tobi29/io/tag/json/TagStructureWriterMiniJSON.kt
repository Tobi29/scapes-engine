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

package org.tobi29.io.tag.json

import org.tobi29.io.tag.TagMap
import org.tobi29.io.tag.TagPrimitive
import org.tobi29.io.tag.TagStructureWriter

class TagStructureWriterMiniJSON(private val writer: Appendable) : TagStructureWriter {
    private var first = true

    override fun begin(root: TagMap) {
        writer.append('{')
        first = true
    }

    override fun end() {
        writer.append('}')
        first = false
    }

    override fun beginStructure() {
        writer.maybeComma().append('{')
        first = true
    }

    override fun beginStructure(key: String) {
        writer.maybeComma().append('"').append(key.jsonEscape()).append("\":{")
        first = true
    }

    override fun endStructure() {
        writer.append('}')
        first = false
    }

    override fun structureEmpty() {
        writer.maybeComma().append("{}")
    }

    override fun structureEmpty(key: String) {
        writer.maybeComma().append('"').append(key.jsonEscape()).append("\":{}")
    }

    override fun beginList(key: String) {
        writer.maybeComma().append('"').append(key.jsonEscape()).append("\":[")
        first = true
    }

    override fun beginList() {
        writer.maybeComma().append('[')
        first = true
    }

    override fun endListWithTerminate() {
        writer.append("}]")
        first = false
    }

    override fun endListWithEmpty() {
        writer.maybeComma().append("{}]")
        first = false
    }

    override fun endList() {
        writer.append(']')
        first = false
    }

    override fun listEmpty(key: String) {
        writer.maybeComma().append('"').append(key).append("\":[]")
    }

    override fun listEmpty() {
        writer.maybeComma().append("[]")
    }

    override fun writePrimitiveTag(key: String,
                                   tag: TagPrimitive) {
        writer.maybeComma().append('"').append(key.jsonEscape()).append("\":")
                .primitive(tag)
    }

    override fun writePrimitiveTag(tag: TagPrimitive) {
        writer.maybeComma().primitive(tag)
    }

    private fun Appendable.maybeComma(): Appendable {
        if (first) {
            first = false
        } else {
            append(',')
        }
        return this
    }
}