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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.utils

private const val encodeTable = "$alphabetLatinUppercase$alphabetLatinLowercase$digitsArabic+/"
private val decodeTable = ByteArray(128) { -1 }.apply {
    for ((i, c) in encodeTable.withIndex()) {
        this[c.toInt()] = i.toByte()
    }
}

private inline fun Appendable.appendTriplet(b0: Byte,
                                            b1: Byte,
                                            b2: Byte) {
    val i0 = b0.toInt()
    val i1 = b1.toInt()
    val i2 = b2.toInt()
    append(encodeTable[(i0 ushr 2) and 0b00111111])
    append(encodeTable[((i0 shl 4) and 0b00110000) or ((i1 ushr 4) and 0b00001111)])
    append(encodeTable[((i1 shl 2) and 0b00111100) or ((i2 ushr 6) and 0b00000011)])
    append(encodeTable[(i2 ushr 0) and 0b00111111])
}

private inline fun Appendable.appendPadding1(b0: Byte,
                                             b1: Byte) {
    val i0 = b0.toInt()
    val i1 = b1.toInt()
    append(encodeTable[(i0 ushr 2) and 0b00111111])
    append(encodeTable[((i0 shl 4) and 0b00110000) or ((i1 ushr 4) and 0b00001111)])
    append(encodeTable[(i1 shl 2) and 0b00111100])
    append('=')
}

private inline fun Appendable.appendPadding2(b0: Byte) {
    val i0 = b0.toInt()
    append(encodeTable[(i0 ushr 2) and 0b00111111])
    append(encodeTable[(i0 shl 4) and 0b00110000])
    append("==")
}

fun Appendable.appendBase64(array: ByteArray,
                            offset: Int = 0,
                            length: Int = array.size - offset) {
    if (offset < 0 || length < 0 || offset + length > array.size)
        throw IndexOutOfBoundsException("Offset or length are out of bounds")

    val padding = length % 3
    val lastTriplet = length - padding
    var i = 0
    while (i < lastTriplet) {
        appendTriplet(array[i++], array[i++], array[i++])
    }
    when (padding) {
        2 -> appendPadding1(array[i++], array[i])
        1 -> appendPadding2(array[i])
    }
}

/**
 * Converts a byte array to a base64 encoded string
 * @receiver Array to convert
 * @return String containing the data
 */
fun ByteArray.toBase64(): String = StringBuilder((size + 2) / 3 * 4)
        .apply { appendBase64(this@toBase64) }.toString()

private inline fun decodeChar(c: Char): Byte {
    val i = c.toInt()
    if (i and 0x7F.inv() == 0) {
        decodeTable[i].let { if (it >= 0) return it }
    }
    throw IllegalArgumentException("Invalid base64 character: $c")
}

private inline fun <R> CharSequence.decodeTriplet(c0: Char,
                                                  c1: Char,
                                                  c2: Char,
                                                  c3: Char,
                                                  output: (Byte, Byte, Byte) -> R): R {
    val d0 = decodeChar(c0).toInt()
    val d1 = decodeChar(c1).toInt()
    val d2 = decodeChar(c2).toInt()
    val d3 = decodeChar(c3).toInt()
    return output(((d0 shl 2) or (d1 ushr 4)).toByte(),
            ((d1 shl 4) or (d2 ushr 2)).toByte(),
            ((d2 shl 6) or (d3 ushr 0)).toByte())
}

private inline fun <R> CharSequence.decodePadding1(c0: Char,
                                                   c1: Char,
                                                   c2: Char,
                                                   c3: Char,
                                                   output: (Byte, Byte) -> R): R {
    val d0 = decodeChar(c0).toInt()
    val d1 = decodeChar(c1).toInt()
    val d2 = decodeChar(c2).toInt()
    if (c3 != '=') throw IllegalArgumentException("Invalid padding: $c3")
    return output(((d0 shl 2) or (d1 ushr 4)).toByte(),
            ((d1 shl 4) or (d2 ushr 2)).toByte())
}

private inline fun <R> CharSequence.decodePadding2(c0: Char,
                                                   c1: Char,
                                                   c2: Char,
                                                   c3: Char,
                                                   output: (Byte) -> R): R {
    val d0 = decodeChar(c0).toInt()
    val d1 = decodeChar(c1).toInt()
    if (c2 != '=') throw IllegalArgumentException("Invalid padding: $c2")
    if (c3 != '=') throw IllegalArgumentException("Invalid padding: $c3")
    return output(((d0 shl 2) or (d1 ushr 4)).toByte())
}

/**
 * Returns the data length of the given base64 encoded string
 * @receiver String to inspect
 * @return Length of the data encoded or `null` if an invalid base64 string was given
 */
fun CharSequence.lengthBase64(): Int? {
    val length = length
    if (length % 4 != 0) return null
    return paddingBase64()?.let { length / 4 * 3 - it }
}

/**
 * Returns the padding length of the given base64 encoded string
 * @receiver String to inspect
 * @return Padding of encoding or `null` if an invalid base64 string was given
 */
fun CharSequence.paddingBase64(): Int? {
    val length = length
    if (length < 2) return 0
    val p1 = this[length - 1] == '='
    val p2 = this[length - 2] == '='
    return if (!p1 && !p2) 0
    else if (p1 && !p2) 1
    else if (p1 && p2) 2
    else null
}

/**
 * Converts a base64 encoded string to a byte array
 * @receiver String to convert
 * @throws IllegalArgumentException When an invalid base64 string was given
 * @return Array containing decoded bytes
 */
fun CharSequence.fromBase64(): ByteArray =
        ByteArray(lengthBase64() ?: throw IllegalArgumentException(
                "Unable to determine output length")).also {
            fromBase64(it, 0, it.size)
        }

/**
 * Converts a base64 encoded string to a byte array
 * @param array Array to write to
 * @param offset First index in array to write to
 * @param length Exact amount of byte to write
 * @receiver String to convert
 * @throws IllegalArgumentException When an invalid base64 string was given
 * @return Amount of byte written to the array
 * @see [lengthBase64]
 */
fun CharSequence.fromBase64(array: ByteArray,
                              offset: Int = 0,
                              length: Int = lengthBase64() ?: 0): Int {
    if (offset < 0 || length < 0 || offset + length > array.size)
        throw IndexOutOfBoundsException("Offset or length are out of bounds")
    if (this.length % 4 != 0)
        throw IllegalArgumentException("Invalid input length")
    val padding = paddingBase64() ?: throw IllegalArgumentException(
            "Unable to determine padding")
    val triplets = this.length / 4 * 3
    if (length != triplets - padding)
        throw IllegalArgumentException("Invalid length")
    val lastBatch = if (padding == 0) triplets else triplets - 3

    var i = 0
    var o = 0
    while (o < lastBatch) {
        decodeTriplet(this[i++], this[i++], this[i++],
                this[i++]) { b0, b1, b2 ->
            array[o++] = b0
            array[o++] = b1
            array[o++] = b2
        }
    }
    when (padding) {
        1 -> decodePadding1(this[i++], this[i++], this[i++],
                this[i]) { b0, b1 ->
            array[o++] = b0
            array[o++] = b1
        }
        2 -> decodePadding2(this[i++], this[i++], this[i++],
                this[i]) { b0 ->
            array[o++] = b0
        }
    }

    return o
}
