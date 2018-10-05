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

import org.tobi29.arrays.sliceOver
import org.tobi29.io.*
import org.tobi29.stdex.utf8ToArray
import org.tobi29.stdex.utf8ToString
import kotlin.experimental.and

internal fun unescapeString(pattern: String): ByteArray =
    unescapeString(pattern.utf8ToArray())

internal fun unescapeString(pattern: ByteArray): ByteArray {
    val index = pattern.indexOf('\\'.toByte())
    if (index < 0) {
        return pattern
    }

    val sb = MemoryViewStreamDefault()
    var pos = 0
    while (pos < pattern.size) {
        var ch = pattern[pos]
        if (ch != '\\'.toByte()) {
            sb.put(ch)
            pos++
            continue
        }
        if (pos + 1 >= pattern.size) {
            // we'll end the pattern with a '\\' char
            sb.put(ch)
            break
        }
        ch = pattern[++pos]
        when (ch) {
            'a'.toByte() -> sb.put(0x07)
            'b'.toByte() -> sb.put('\b'.toByte())
            'f'.toByte() -> sb.put(0x0C)
            'n'.toByte() -> sb.put('\n'.toByte())
            'r'.toByte() -> sb.put('\r'.toByte())
            't'.toByte() -> sb.put('\t'.toByte())
            '\\'.toByte(), '\''.toByte(), '"'.toByte(), '?'.toByte(),
            ' '.toByte() -> sb.put(ch)
            '0'.toByte(), '1'.toByte(), '2'.toByte(), '3'.toByte(),
            '4'.toByte(), '5'.toByte(), '6'.toByte(), '7'.toByte() -> {
                // \o or \oo or \ooo ... where o is an octal digit
                var octal = digit(ch, 8)
                var i = 1
                while (i <= 2 && pos + 1 < pattern.size) {
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
            'x'.toByte() -> {
                // \xD9
                pattern.decodeHexChar(pos) { char, len ->
                    sb.put(char)
                    pos += len
                }
            }
            else -> sb.put(ch)
        }
        pos++
    }
    sb.flip()
    return sb.asByteArray()
}

private inline fun <R> ByteArray.decodeHexChar(
    pos: Int,
    output: (Byte, Int) -> R
): R {
    if (pos + 2 < size) {
        utf8ToString(pos + 1, 2).toShortOrNull(16)?.let { hex ->
            if (hex >= 0) return output(hex.toByte(), 2)
        }
    }
    if (pos + 1 < size) {
        utf8ToString(pos + 1, 1).toShortOrNull(16)?.let { hex ->
            if (hex >= 0) return output(hex.toByte(), 1)
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

private fun digit(char: Byte, radix: Int) = when (char) {
    in '0'.toByte()..'9'.toByte() ->
        (char - '0'.toByte()).let { if (it < radix) it else -1 }
    in 'a'.toByte()..'z'.toByte() ->
        (char - 'a'.toByte() + 10).let { if (it < radix) it else -1 }
    in 'A'.toByte()..'Z'.toByte() ->
        (char - 'a'.toByte() + 10).let { if (it < radix) it else -1 }
    else -> -1
}

internal fun WritableByteStream.putCompactShort(value: Short) = when (value) {
    in 0..0xFE -> {
        put(value.toByte())
    }
    else -> {
        put(0xFF.toByte())
        putShort(value)
    }
}

internal fun WritableByteStream.putCompactInt(value: Int) = when (value) {
    in 0..0xFD -> {
        put(value.toByte())
    }
    in 0xFE..0xFFFF -> {
        put(0xFE.toByte())
        putShort(value.toShort())
    }
    else -> {
        put(0xFF.toByte())
        putInt(value)
    }
}

internal fun WritableByteStream.putCompactLong(value: Long) = when (value) {
    in 0L..0xFCL -> {
        put(value.toByte())
    }
    in 0xFDL..0xFFFFL -> {
        put(0xFD.toByte())
        putShort(value.toShort())
    }
    in 0x10000L..0xFFFFFFFFL -> {
        put(0xFE.toByte())
        putInt(value.toInt())
    }
    else -> {
        put(0xFF.toByte())
        putLong(value)
    }
}

internal fun WritableByteStream.putCompactByteArray(value: ByteArray) {
    putCompactInt(value.size)
    put(value.sliceOver())
}

internal fun WritableByteStream.putCompactString(value: String) {
    putCompactByteArray(value.utf8ToArray())
}

internal fun MemoryViewReadableStream<HeapViewByteBE>.getCompactShort(): Short {
    val prefix = get()
    return when (prefix) {
        0xFF.toByte() -> getShort()
        else -> prefix.toShort() and 0xFF
    }
}

internal fun MemoryViewReadableStream<HeapViewByteBE>.getCompactInt(): Int {
    val prefix = get()
    return when (prefix) {
        0xFE.toByte() -> getShort().toInt() and 0xFFFF
        0xFF.toByte() -> getInt()
        else -> prefix.toInt() and 0xFF
    }
}

internal fun MemoryViewReadableStream<HeapViewByteBE>.getCompactLong(): Long {
    val prefix = get()
    return when (prefix) {
        0xFD.toByte() -> getShort().toLong() and 0xFFFFL
        0xFE.toByte() -> getInt().toLong() and 0xFFFFFFFFL
        0xFF.toByte() -> getLong()
        else -> prefix.toLong() and 0xFFL
    }
}

internal fun MemoryViewReadableStream<HeapViewByteBE>.getCompactByteArray(): ByteArray {
    val size = getCompactInt()
    val position = position
    skip(size)
    val buffer = buffer()
    val start = position + buffer.offset
    return buffer.array.sliceArray(start until start + size)
}

internal fun MemoryViewReadableStream<HeapViewByteBE>.getCompactString(): String {
    val size = getCompactInt()
    val position = position
    skip(size)
    val buffer = buffer()
    val start = position + buffer.offset
    return buffer.array.utf8ToString(start, size)
}

internal fun ByteArray.trim(): ByteArray {
    var start = 0
    while (start < size && this[start].toChar().isWhitespace()) start++
    var end = size - 1
    while (end > start && this[end].toChar().isWhitespace()) end--
    return sliceArray(start..end)
}
