package org.tobi29.scapes.engine.utils.io.tag.json

import org.tobi29.scapes.engine.utils.Readable
import org.tobi29.scapes.engine.utils.io.IOException
import org.tobi29.scapes.engine.utils.isISOControl
import org.tobi29.scapes.engine.utils.tag.*
import org.tobi29.scapes.engine.utils.toString

class JSONTokenizer(private val reader: Readable) {
    // Current char being parsed
    private var current: Char = '\u0000'
    private var depth = 0

    fun read(): TagMap {
        current = reader.read()
        skipWhitespace()
        return readObject()
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
        current = reader.read()
        skipWhitespace()
        if (current != '}') {
            while (true) {
                val key = readString()
                current = reader.read()
                skipWhitespace()
                if (current != ':') {
                    unexpected("character after key", current)
                }
                current = reader.read()
                skipWhitespace()
                this[key] = readValue()
                skipWhitespace()
                if (current != ',' && current != '}') {
                    unexpected("character in object", current)
                }
                if (current == '}') {
                    break
                }
                current = reader.read()
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
        current = reader.read()
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
                current = reader.read()
                skipWhitespace()
            }
        }
        depth--
    }

    // Assumes first is part of value, stops one after number end and stores
    // skipped character in skippedChar
    private fun readValue(): Tag = when (current) {
        '"' -> readString().toTag().also { current = reader.read() }
        '{' -> readObject().also { current = reader.read() }
        '[' -> readArray().also { current = reader.read() }
        't' -> {
            val c1 = reader.read()
            if (c1 != 'r') {
                unexpected("character in null", c1)
            }
            val c2 = reader.read()
            if (c2 != 'u') {
                unexpected("character in null", c2)
            }
            val c3 = reader.read()
            if (c3 != 'e') {
                unexpected("character in null", c3)
            }
            current = reader.read()
            true.toTag()
        }
        'f' -> {
            val c1 = reader.read()
            if (c1 != 'a') {
                unexpected("character in null", c1)
            }
            val c2 = reader.read()
            if (c2 != 'l') {
                unexpected("character in null", c2)
            }
            val c3 = reader.read()
            if (c3 != 's') {
                unexpected("character in null", c3)
            }
            val c4 = reader.read()
            if (c4 != 'e') {
                unexpected("character in null", c4)
            }
            current = reader.read()
            false.toTag()
        }
        'n' -> {
            val c1 = reader.read()
            if (c1 != 'u') {
                unexpected("character in null", c1)
            }
            val c2 = reader.read()
            if (c2 != 'l') {
                unexpected("character in null", c2)
            }
            val c3 = reader.read()
            if (c3 != 'l') {
                unexpected("character in null", c3)
            }
            current = reader.read()
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
            val c = reader.read()
            if (c.isISOControl()) {
                unexpected("control character in string", c)
            }
            when (c) {
                '"' -> break@loop
                '\\' -> {
                    val c1 = reader.read()
                    if (c1.isISOControl()) {
                        unexpected("control character in string", c1)
                    } else when (c1) {
                        '"' -> output.append('"')
                        '\\' -> output.append('"')
                        '/' -> output.append('/')
                        'b' -> output.append('\b')
                        'f' -> output.append('\u000c')
                        'n' -> output.append('\n')
                        'r' -> output.append('\r')
                        't' -> output.append('\t')
                        'u' -> {
                            val uc = ((decodeHex(reader.read()) shr 12) or
                                    (decodeHex(reader.read()) shr 8) or
                                    (decodeHex(reader.read()) shr 4) or
                                    (decodeHex(reader.read()) shr 0)).toChar()
                            output.append(uc)
                        }
                        else -> unexpected("escaped character", c1)
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
                reader.read()
            }
            else -> current
        }
        val c2 = when (c1) {
            '0' -> {
                number.append('0')
                reader.read()
            }
            in '1'..'9' -> {
                var next = c1
                do {
                    number.append(next)
                    next = reader.read()
                } while (next in '0'..'9')
                next
            }
            else -> c1
        }
        val c3 = when (c2) {
            '.' -> {
                number.append('.')
                decimal = true
                var next = reader.read()
                if (next !in '0'..'9') {
                    unexpected("digit after fraction", next)
                }
                do {
                    number.append(next)
                    next = reader.read()
                } while (next in '0'..'9')
                next
            }
            else -> c2
        }
        val c4 = when (c3) {
            'e', 'E' -> {
                number.append(c3)
                val sign = reader.read()
                var next = when (sign) {
                    '+', '-' -> {
                        number.append(sign)
                        reader.read()
                    }
                    else -> sign
                }
                if (next !in '0'..'9') {
                    unexpected("character in exponent", next)
                }
                do {
                    number.append(next)
                    next = reader.read()
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
        while (isWhitespace(current)) {
            current = reader.read()
        }
    }

    companion object {
        private fun decodeHex(c: Char): Int {
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

        private fun isWhitespace(c: Char) = when (c) {
            ' ', '\t', '\n', '\r' -> true
            else -> false
        }

        private fun unexpected(message: String,
                               c: Char): Nothing =
                if (c.isISOControl())
                    throw IOException(
                            "Unexpected $message #${c.toInt().toString(16)}")
                else
                    throw IOException(
                            "Unexpected $message $c (#${c.toInt().toString(
                                    16)})")
    }
}
