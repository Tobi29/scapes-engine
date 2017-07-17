package org.tobi29.scapes.engine.utils.io.tag.json

import org.tobi29.scapes.engine.utils.tag.TagMap
import org.tobi29.scapes.engine.utils.tag.TagPrimitive
import org.tobi29.scapes.engine.utils.tag.TagStructureWriter

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