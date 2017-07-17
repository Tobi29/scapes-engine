package org.tobi29.scapes.engine.utils.io.tag.json

import org.tobi29.scapes.engine.utils.io.IOException
import org.tobi29.scapes.engine.utils.tag.*
import org.tobi29.scapes.engine.utils.toString

internal fun Appendable.primitive(tag: TagPrimitive) = when (tag) {
    is TagUnit -> append("null")
    is TagBoolean -> append(tag.value.toString())
    is TagInteger -> append(tag.value.toString())
    is TagDecimal -> append(verify(tag.value).toString())
    is TagString -> append('"').append(tag.value.jsonEscape()).append('"')
    is TagByteArray -> tag.value.joinTo(this, separator = ",", prefix = "[",
            postfix = "]")
    else -> throw IOException("Invalid type: ${this::class}")
}

private fun verify(value: Number) = when (value) {
    is Float -> run {
        if (!value.isFinite()) {
            throw IOException("Non-finite number: $value")
        }
        value
    }
    is Double -> run {
        if (!value.isFinite()) {
            throw IOException("Non-finite number: $value")
        }
        value
    }
    else -> value
}

internal fun String.jsonEscape() = StringBuilder(length).apply {
    for (c in this@jsonEscape) {
        when (c) {
            '"' -> append("\\\"")
            '\\' -> append("\\\\")
            '\b' -> append("\\b")
            '\u000c' -> append("\\f")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> if (c.isISOControl()) {
                append("\\u${c.toInt().toString(16, 4)}")
            } else append(c)
        }
    }
}.toString()

internal fun Char.isISOControl() = toInt().isISOControl()

internal fun Int.isISOControl() = this <= 0x9F && (this >= 0x7F || (this.ushr(
        5) == 0))
