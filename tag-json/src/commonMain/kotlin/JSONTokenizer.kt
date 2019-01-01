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
import org.tobi29.stdex.toString

inline fun readJSON(
    crossinline input: () -> Char
): TagMap = object {
    // Current char being parsed
    private var current: Char = '\u0000'
    private var depth = 0

    fun read(): TagMap {
        readNextChar()
        skipWhitespace()
        return readObject()
    }

    private fun readChar() = input()

    private fun readNextChar() {
        current = readChar()
    }

    // Assumes { is current, makes } current
    private fun readObject() = TagMap {
        if (depth > 250) {
            throw IOException("Too deeply recursive")
        }
        depth++
        if (current != '{') {
            unexpected("object beginning", current)
        }
        readNextChar()
        skipWhitespace()
        if (current != '}') {
            while (true) {
                val key = readString()
                readNextChar()
                skipWhitespace()
                if (current != ':') {
                    unexpected("character after key", current)
                }
                readNextChar()
                skipWhitespace()
                this[key] = readValue()
                skipWhitespace()
                if (current != ',' && current != '}') {
                    unexpected("character in object", current)
                }
                if (current == '}') {
                    break
                }
                readNextChar()
                skipWhitespace()
            }
        }
        depth--
    }

    // Assumes [ is current, makes ] current
    private fun readArray() = TagList {
        if (depth > 250) {
            throw IOException("Too deeply recursive")
        }
        depth++
        if (current != '[') {
            unexpected("array beginning", current)
        }
        readNextChar()
        skipWhitespace()
        if (current != ']') {
            while (true) {
                add(readValue())
                skipWhitespace()
                if (current != ',' && current != ']') {
                    unexpected("character in array", current)
                }
                if (current == ']') {
                    break
                }
                readNextChar()
                skipWhitespace()
            }
        }
        depth--
    }

    // Assumes first is part of value, stops one after number end and stores
    // skipped character in skippedChar
    private fun readValue(): Tag = when (current) {
        '"' -> readString().toTag().also { readNextChar() }
        '{' -> readObject().also { readNextChar() }
        '[' -> readArray().also { readNextChar() }
        't' -> {
            val c1 = readChar()
            if (c1 != 'r') {
                unexpected("character in null", c1)
            }
            val c2 = readChar()
            if (c2 != 'u') {
                unexpected("character in null", c2)
            }
            val c3 = readChar()
            if (c3 != 'e') {
                unexpected("character in null", c3)
            }
            readNextChar()
            true.toTag()
        }
        'f' -> {
            val c1 = readChar()
            if (c1 != 'a') {
                unexpected("character in null", c1)
            }
            val c2 = readChar()
            if (c2 != 'l') {
                unexpected("character in null", c2)
            }
            val c3 = readChar()
            if (c3 != 's') {
                unexpected("character in null", c3)
            }
            val c4 = readChar()
            if (c4 != 'e') {
                unexpected("character in null", c4)
            }
            readNextChar()
            false.toTag()
        }
        'n' -> {
            val c1 = readChar()
            if (c1 != 'u') {
                unexpected("character in null", c1)
            }
            val c2 = readChar()
            if (c2 != 'l') {
                unexpected("character in null", c2)
            }
            val c3 = readChar()
            if (c3 != 'l') {
                unexpected("character in null", c3)
            }
            readNextChar()
            TagUnit
        }
        else -> readNumber()
    }

    // Assumes " is current, makes " current
    private fun readString(): String {
        if (current != '"') {
            unexpected("string beginning", current)
        }
        val output = StringBuilder()
        loop@ while (true) {
            val c = readChar()
            if (c.isISOControl()) {
                unexpected("control character in string", c)
            }
            when (c) {
                '"' -> break@loop
                '\\' -> {
                    val c1 = readChar()
                    if (c1.isISOControl()) {
                        unexpected(
                            "control character in string",
                            c1
                        )
                    } else when (c1) {
                        '"' -> output.append('"')
                        '\\' -> output.append('\\')
                        '/' -> output.append('/')
                        'b' -> output.append('\b')
                        'f' -> output.append('\u000c')
                        'n' -> output.append('\n')
                        'r' -> output.append('\r')
                        't' -> output.append('\t')
                        'u' -> {
                            val uc =
                                ((decodeHex(readChar()) shr 12) or
                                        (decodeHex(readChar()) shr 8) or
                                        (decodeHex(readChar()) shr 4) or
                                        (decodeHex(readChar()) shr 0)).toChar()
                            output.append(uc)
                        }
                        else -> unexpected(
                            "escaped character",
                            c1
                        )
                    }
                }
                else -> output.append(c)
            }
        }
        return output.toString()
    }

    // Assumes first character of number is current, makes character after
    // number current
    private fun readNumber(): TagNumber {
        val number = StringBuilder()
        var decimal = false
        val c1 = when (current) {
            '-' -> {
                number.append('-')
                readChar()
            }
            else -> current
        }
        val c2 = when (c1) {
            '0' -> {
                number.append('0')
                readChar()
            }
            in '1'..'9' -> {
                var next = c1
                do {
                    number.append(next)
                    next = readChar()
                } while (next in '0'..'9')
                next
            }
            else -> c1
        }
        val c3 = when (c2) {
            '.' -> {
                number.append('.')
                decimal = true
                var next = readChar()
                if (next !in '0'..'9') {
                    unexpected("digit after fraction", next)
                }
                do {
                    number.append(next)
                    next = readChar()
                } while (next in '0'..'9')
                next
            }
            else -> c2
        }
        val c4 = when (c3) {
            'e', 'E' -> {
                number.append(c3)
                val sign = readChar()
                var next = when (sign) {
                    '+', '-' -> {
                        number.append(sign)
                        readChar()
                    }
                    else -> sign
                }
                if (next !in '0'..'9') {
                    unexpected("character in exponent", next)
                }
                do {
                    number.append(next)
                    next = readChar()
                } while (next in '0'..'9')
                next
            }
            else -> c3
        }
        current = c4
        val str = number.toString()
        // TODO: Support big numbers
        return if (decimal) {
            (str.toDoubleOrNull()
                    ?: throw IOException("Invalid number: $str")).toTag()
        } else {
            (str.toLongOrNull()
                    ?: throw IOException("Invalid number: $str")).toTag()
        }
    }

    // Makes current the next non-whitespace character
    private fun skipWhitespace() {
        while (isWhitespace(current)) readNextChar()
    }
}.read()

@PublishedApi
internal fun decodeHex(c: Char): Int {
    val n = c.toInt()
    if (n >= '0'.toInt() && n <= '9'.toInt()) {
        return n - '0'.toInt()
    } else if (n >= 'A'.toInt() && n <= 'F'.toInt()) {
        return n - 'A'.toInt() + 10
    } else if (n >= 'a'.toInt() && n <= 'f'.toInt()) {
        return n - 'a'.toInt() + 10
    }
    unexpected("hex digit", c)
}


@PublishedApi
internal fun isWhitespace(c: Char) = when (c) {
    ' ', '\t', '\n', '\r' -> true
    else -> false
}

@PublishedApi
internal fun unexpected(message: String, c: Char): Nothing =
    if (c.isISOControl())
        throw IOException("Unexpected $message #${c.toInt().toString(16)}")
    else
        throw IOException("Unexpected $message $c (#${c.toInt().toString(16)})")
