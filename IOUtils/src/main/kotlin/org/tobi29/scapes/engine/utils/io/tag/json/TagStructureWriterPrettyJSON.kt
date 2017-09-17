package org.tobi29.scapes.engine.utils.io.tag.json

import org.tobi29.scapes.engine.utils.tag.TagByteArray
import org.tobi29.scapes.engine.utils.tag.TagMap
import org.tobi29.scapes.engine.utils.tag.TagPrimitive
import org.tobi29.scapes.engine.utils.tag.TagStructureWriter

class TagStructureWriterPrettyJSON(private val writer: Appendable) : TagStructureWriter {
    private var first = true
    private var indent = 0

    override fun begin(root: TagMap) {
        writer.append('{')
        indent++
        first = true
    }

    override fun end() {
        indent--
        writer.newLine().append('}')
        first = false
    }

    override fun beginStructure() {
        writer.maybeComma().newLine().append('{')
        indent++
        first = true
    }

    override fun beginStructure(key: String) {
        writer.maybeComma().newLine().append('"').append(key.jsonEscape())
                .append("\": {")
        indent++
        first = true
    }

    override fun endStructure() {
        indent--
        writer.newLine().append('}')
        first = false
    }

    override fun structureEmpty() {
        writer.maybeComma().newLine().append("{}")
    }

    override fun structureEmpty(key: String) {
        writer.maybeComma().newLine().append('"').append(key.jsonEscape())
                .append("\": {}")
    }

    override fun beginList(key: String) {
        writer.maybeComma().newLine().append('"').append(key.jsonEscape())
                .append("\": [")
        indent++
        first = true
    }

    override fun beginList() {
        writer.maybeComma()
        writer.newLine()
        writer.append('[')
        indent++
        first = true
    }

    override fun endList() {
        indent--
        writer.newLine()
        writer.append(']')
        first = false
    }

    override fun listEmpty(key: String) {
        writer.maybeComma()
        writer.newLine()
        writer.append('"').append(key.jsonEscape()).append("\": []")
    }

    override fun listEmpty() {
        writer.maybeComma()
        writer.newLine()
        writer.append("[]")
    }

    override fun writePrimitiveTag(key: String,
                                   tag: TagPrimitive) {
        if (tag is TagByteArray) {
            beginList(key.jsonEscape())
            for (element in tag.value) {
                writer.maybeComma().newLine().append(element.toString())
            }
            endList()
        } else {
            writer.maybeComma().newLine().append('"').append(key.jsonEscape())
                    .append("\": ").primitive(tag)
        }
    }

    override fun writePrimitiveTag(tag: TagPrimitive) {
        if (tag is TagByteArray) {
            beginList()
            for (element in tag.value) {
                writer.maybeComma().newLine().append(element.toString())
            }
            endList()
        } else {
            writer.maybeComma().newLine().primitive(tag)
        }
    }

    private fun Appendable.newLine(): Appendable = apply {
        writer.append('\n')
        repeat(indent) {
            writer.append("    ")
        }
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