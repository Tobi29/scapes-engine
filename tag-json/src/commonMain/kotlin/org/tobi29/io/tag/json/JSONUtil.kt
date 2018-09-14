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

import org.tobi29.io.IOException
import org.tobi29.io.tag.*
import org.tobi29.stdex.isISOControl
import org.tobi29.utils.toString

internal fun Appendable.primitive(tag: TagPrimitive) = when (tag) {
    is TagUnit -> append("null")
    is TagBoolean -> append(tag.value.toString())
    is TagInteger -> append(tag.value.toString())
    is TagDecimal -> append(verify(tag.value).toString())
    is TagString -> append('"').append(tag.value.jsonEscape()).append('"')
    is TagByteArray -> tag.value.joinTo(this, separator = ",", prefix = "[",
            postfix = "]")
    else -> throw IOException("Invalid type: $this")
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
