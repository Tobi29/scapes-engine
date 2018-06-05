/*
 * Copyright 2017, Gray Watson
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.j256.simplemagik.entries

internal fun unescapeString(pattern: String): String {
    val index = pattern.indexOf('\\')
    if (index < 0) {
        return pattern
    }

    val sb = StringBuilder()
    var pos = 0
    while (pos < pattern.length) {
        var ch = pattern[pos]
        if (ch != '\\') {
            sb.append(ch)
            pos++
            continue
        }
        if (pos + 1 >= pattern.length) {
            // we'll end the pattern with a '\\' char
            sb.append(ch)
            break
        }
        ch = pattern[++pos]
        when (ch) {
            'a' -> sb.append(0x07.toChar())
            'b' -> sb.append('\b')
            'f' -> sb.append(0x0C.toChar())
            'n' -> sb.append('\n')
            'r' -> sb.append('\r')
            't' -> sb.append('\t')
            '\\', '\'', '"', '?', ' ' -> sb.append(ch)
            '0', '1', '2', '3', '4', '5', '6', '7' -> {
                // \o or \oo or \ooo ... where o is an octal digit
                var octal = digit(ch, 8)
                var i = 1
                while (i <= 2 && pos + 1 < pattern.length) {
                    ch = pattern[pos + 1]
                    val digit = digit(ch, 8)
                    if (digit >= 0) {
                        octal = octal * 8 + digit
                        pos++
                    } else {
                        break
                    }
                    i++
                }
                sb.append((octal and 0xff).toChar())
            }
            'x' -> {
                // \xD9
                pattern.decodeHexChar(pos) { char, len ->
                    sb.append(char)
                    pos += len
                }
            }
            else -> sb.append(ch)
        }
        pos++
    }
    return sb.toString()
}

private inline fun <R> String.decodeHexChar(
    pos: Int,
    output: (Char, Int) -> R
): R {
    if (pos + 2 < length) {
        substring(pos + 1, pos + 3).toShortOrNull(16)?.let { hex ->
            if (hex >= 0) return output(hex.toChar(), 2)
        }
    }
    if (pos + 1 < length) {
        substring(pos + 1, pos + 1).toShortOrNull(16)?.let { hex ->
            if (hex >= 0) return output(hex.toChar(), 1)
        }
    }
    return output(this[pos], 0)
}

internal fun Long.toByteChecked(): Byte {
    if (this < Byte.MIN_VALUE || this > 0xFFL)
        throw NumberFormatException("Number out of range: $this")
    return toByte()
}

internal fun Long.toShortChecked(): Short {
    if (this < Short.MIN_VALUE || this > 0xFFFFL)
        throw NumberFormatException("Number out of range: $this")
    return toShort()
}

internal fun Long.toIntChecked(): Int {
    if (this < Int.MIN_VALUE || this > 0xFFFFFFFFL)
        throw NumberFormatException("Number out of range: $this")
    return toInt()
}

internal fun decodeByte(str: String): Byte =
    decodeLong(str).toByteChecked()

internal fun decodeShort(str: String): Short =
    decodeLong(str).toShortChecked()

internal fun decodeInt(str: String): Int =
    decodeLong(str).toIntChecked()

internal fun decodeLong(str: String): Long {
    if (str == "0") return 0L
    return when {
        str.startsWith("0x") || str.startsWith("0X") -> {
            val nstr = str.substring(2).toLowerCase()
            when {
                nstr.length <= 8 -> nstr.toLong(16)
                nstr.length <= 16 ->
                    (nstr.substring(0, nstr.length - 8).toLong(16) shl 32) or
                            nstr.substring(nstr.length - 8).toLong(16)
                else -> throw NumberFormatException("Number too long")
            }
        }
        str.startsWith("0") -> {
            val nstr = str.substring(1)
            when {
                nstr.length <= 12 -> nstr.toLong(8)
                nstr.length <= 23 ->
                    (nstr.substring(0, nstr.length - 12).toLong(8) shl 36) or
                            nstr.substring(nstr.length - 12).toLong(8)
                else -> throw NumberFormatException("Number too long")
            }
        }
        else -> str.toLong()
    }
}

private fun digit(char: Char, radix: Int) = when (char) {
    in '0'..'9' -> (char - '0').let { if (it < radix) it else -1 }
    in 'a'..'z' -> (char - 'a' + 10).let { if (it < radix) it else -1 }
    in 'A'..'Z' -> (char - 'a' + 10).let { if (it < radix) it else -1 }
    else -> -1
}
