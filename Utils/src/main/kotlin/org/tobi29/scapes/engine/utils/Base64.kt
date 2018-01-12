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

package org.tobi29.scapes.engine.utils

fun Appendable.appendBase64(array: ByteArray,
                            offset: Int = 0,
                            length: Int = array.size - offset) {
    if (offset < 0 || length < 0 || offset + length > array.size)
        throw IndexOutOfBoundsException("Offset or length are out of bounds")

    var i = offset
    val end = offset + length
    encodeBase64({
        if (i >= end) BASE64_DATA_END
        else array[i++].toInt()
    }, { append(it) })
}

/**
 * Converts a byte array to a base64 encoded string
 * @receiver Array to convert
 * @return String containing the data
 */
fun ByteArray.toBase64(): String = StringBuilder((size + 2) / 3 * 4)
        .apply { appendBase64(this@toBase64) }.toString()

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
    val p1 = this[length - 1] == 61.toChar() /* '=' */
    val p2 = this[length - 2] == 61.toChar() /* '=' */
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

    var i = offset
    val end = this.length
    var o = offset
    decodeBase64({
        if (i >= end) -1
        else this[i++].toInt()
    }, { array[o++] = it })
    return o
}

inline fun Readable.fromBase64(output: (Byte) -> Unit): Base64Result =
        decodeBase64({ readTry() }, output)

const val BASE64_DATA_END = Int.MIN_VALUE

/**
 * Encodes data in base64
 *
 * Reads bytes from [input] ([BASE64_DATA_END] indicates EOS, otherwise
 * casting the original [Byte] to an [Int] is preferred), encodes and writes to
 * [output]
 *
 * **Note:** This is a low-level utility, consider higher-level alternatives
 * when applicable
 *
 * **Warning:** [input] and [output] will be copied multiple times so it is
 * highly recommended to keep it as small as possible
 * @param input Input byte supplier
 * @param output Output character consumer
 * @param padding Padding character to use or `null` for no padding
 */
inline fun encodeBase64(input: () -> Int,
                        output: (Char) -> Unit,
                        padding: Nothing?) {
    while (true) {
        val b0 = input()
        val b1 = input()
        val b2 = input()
        base64EncodeBatch(b0, b1, b2, {
            return
        }, { c0, c1 ->
            output(c0)
            output(c1)
            return
        }, { c0, c1, c2 ->
            output(c0)
            output(c1)
            output(c2)
            return
        }, { c0, c1, c2, c3 ->
            output(c0)
            output(c1)
            output(c2)
            output(c3)
        })
    }
}

/**
 * Encodes data in base64
 *
 * Reads bytes from [input] ([BASE64_DATA_END] indicates EOS, otherwise
 * casting the original [Byte] to an [Int] is preferred), encodes and writes to
 * [output]
 *
 * **Note:** This is a low-level utility, consider higher-level alternatives
 * when applicable
 *
 * **Warning:** [input] and [output] will be copied multiple times so it is
 * highly recommended to keep it as small as possible
 * @param input Input byte supplier
 * @param output Output character consumer
 * @param padding Padding character to use or `null` for no padding
 */
inline fun encodeBase64(input: () -> Int,
                        output: (Char) -> Unit) =
        encodeBase64(input, output, 61.toChar() /* '=' */)

/**
 * Encodes data in base64
 *
 * Reads bytes from [input] ([BASE64_DATA_END] indicates EOS, otherwise
 * casting the original [Byte] to an [Int] is preferred), encodes and writes to
 * [output]
 *
 * **Note:** This is a low-level utility, consider higher-level alternatives
 * when applicable
 *
 * **Warning:** [input] and [output] will be copied multiple times so it is
 * highly recommended to keep it as small as possible
 * @param input Input byte supplier
 * @param output Output character consumer
 * @param padding Padding character to use or `null` for no padding
 */
inline fun encodeBase64(input: () -> Int,
                        output: (Char) -> Unit,
                        padding: Char) {
    while (true) {
        val b0 = input()
        val b1 = input()
        val b2 = input()
        base64EncodeBatch(b0, b1, b2, {
            return
        }, { c0, c1 ->
            output(c0)
            output(c1)
            repeat(2) { output(padding) }
            return
        }, { c0, c1, c2 ->
            output(c0)
            output(c1)
            output(c2)
            output(padding)
            return
        }, { c0, c1, c2, c3 ->
            output(c0)
            output(c1)
            output(c2)
            output(c3)
        })
    }
}

/**
 * Decodes base64 encoded data
 *
 * Reads characters from [input] (-1 indicates EOS, otherwise value is expected
 * to be a [Char] cast to an [Int]), decodes and writes to [output]
 *
 * **Note:** This only throws with fatal errors, in particular padding is not
 * enforced, however one can check this using the returned value
 *
 * **Note:** This is a low-level utility, consider higher-level alternatives
 * when applicable
 *
 * **Warning:** [input] and [output] will be copied multiple times so it is
 * highly recommended to keep it as small as possible
 * @param input Input character supplier
 * @param output Output byte consumer
 * @throws IllegalArgumentException When an invalid character was encountered
 * @return Reason for data end
 */
inline fun decodeBase64(
        input: () -> Int,
        output: (Byte) -> Unit
): Base64Result {
    while (true) {
        val c0 = input()
        val c1 = input()
        val c2 = input()
        val c3 = input()
        base64DecodeBatch(c0, c1, c2, c3, { b0, b1, b2 ->
            output(b0)
            output(b1)
            output(b2)
        }, { b0, padded ->
            output(b0)
            return if (padded) Base64Result.PADDED else Base64Result.NOT_PADDED
        }, { b0, b1, padded ->
            output(b0)
            output(b1)
            return if (padded) Base64Result.PADDED else Base64Result.NOT_PADDED
        }, {
            return Base64Result.ALIGNED
        })
    }
}

enum class Base64Result {
    ALIGNED,
    PADDED,
    NOT_PADDED
}

@PublishedApi
internal const val ENCODE_TABLE = "$alphabetLatinUppercase$alphabetLatinLowercase$digitsArabic+/"

@PublishedApi
internal val base64DecodeTable = ByteArray(128) { -1 }.apply {
    for ((i, c) in ENCODE_TABLE.withIndex()) {
        this[c.toInt()] = i.toByte()
    }
}

@PublishedApi
internal inline fun <R> base64EncodeBatch(
        b0: Int,
        b1: Int,
        b2: Int,
        end: () -> R,
        outputPadding2: (Char, Char) -> R,
        outputPadding1: (Char, Char, Char) -> R,
        outputFull: (Char, Char, Char, Char) -> R
): R = if (b0 == BASE64_DATA_END) {
    end()
} else if (b1 == BASE64_DATA_END) {
    base64EncodePadding2(b0.toByte(), outputPadding2)
} else if (b2 == BASE64_DATA_END) {
    base64EncodePadding1(b0.toByte(), b1.toByte(), outputPadding1)
} else {
    base64EncodeFull(b0.toByte(), b1.toByte(), b2.toByte(), outputFull)
}

@PublishedApi
internal inline fun <R> base64DecodeBatch(
        c0: Int,
        c1: Int,
        c2: Int,
        c3: Int,
        outputFull: (Byte, Byte, Byte) -> R,
        outputPadding2: (Byte, Boolean) -> R,
        outputPadding1: (Byte, Byte, Boolean) -> R,
        end: () -> R
): R = if (c0 < 0 || c1 < 0) {
    end()
} else if (c3 < 0 || c3.toChar() == 61.toChar() /* '=' */) {
    if (c2 < 0 || c2.toChar() == 61.toChar() /* '=' */) {
        base64DecodePadding2(c0.toChar(), c1.toChar()) { b0 ->
            outputPadding2(b0, c2 >= 0)
        }
    } else {
        base64DecodePadding1(c0.toChar(), c1.toChar(), c2.toChar()) { b0, b1 ->
            outputPadding1(b0, b1, c3 >= 0)
        }
    }
} else {
    base64DecodeFull(c0.toChar(), c1.toChar(), c2.toChar(), c3.toChar(),
            outputFull)
}

@PublishedApi
internal inline fun <R> base64EncodeFull(
        b0: Byte,
        b1: Byte,
        b2: Byte,
        output: (Char, Char, Char, Char) -> R
): R {
    val i0 = b0.toInt()
    val i1 = b1.toInt()
    val i2 = b2.toInt()
    return output(ENCODE_TABLE[(i0 ushr 2) and 0b00111111],
            ENCODE_TABLE[((i0 shl 4) and 0b00110000) or ((i1 ushr 4) and 0b00001111)],
            ENCODE_TABLE[((i1 shl 2) and 0b00111100) or ((i2 ushr 6) and 0b00000011)],
            ENCODE_TABLE[(i2 ushr 0) and 0b00111111])

}

@PublishedApi
internal inline fun <R> base64EncodePadding1(
        b0: Byte,
        b1: Byte,
        output: (Char, Char, Char) -> R
): R {
    val i0 = b0.toInt()
    val i1 = b1.toInt()
    return output(ENCODE_TABLE[(i0 ushr 2) and 0b00111111],
            ENCODE_TABLE[((i0 shl 4) and 0b00110000) or ((i1 ushr 4) and 0b00001111)],
            ENCODE_TABLE[(i1 shl 2) and 0b00111100])
}

@PublishedApi
internal inline fun <R> base64EncodePadding2(
        b0: Byte,
        output: (Char, Char) -> R
): R {
    val i0 = b0.toInt()
    return output(ENCODE_TABLE[(i0 ushr 2) and 0b00111111],
            ENCODE_TABLE[(i0 shl 4) and 0b00110000])
}

@PublishedApi
internal fun base64DecodeChar(c: Char): Byte {
    val i = c.toInt()
    if (i and 0x7F.inv() == 0) {
        base64DecodeTable[i].let { if (it >= 0) return it }
    }
    throw IllegalArgumentException("Invalid base64 character: $c")
}

@PublishedApi
internal inline fun <R> base64DecodeFull(
        c0: Char,
        c1: Char,
        c2: Char,
        c3: Char,
        output: (Byte, Byte, Byte) -> R
): R {
    val d0 = base64DecodeChar(c0).toInt()
    val d1 = base64DecodeChar(c1).toInt()
    val d2 = base64DecodeChar(c2).toInt()
    val d3 = base64DecodeChar(c3).toInt()
    return output(((d0 shl 2) or (d1 ushr 4)).toByte(),
            ((d1 shl 4) or (d2 ushr 2)).toByte(),
            ((d2 shl 6) or (d3 ushr 0)).toByte())
}

@PublishedApi
internal inline fun <R> base64DecodePadding1(
        c0: Char,
        c1: Char,
        c2: Char,
        output: (Byte, Byte) -> R
): R {
    val d0 = base64DecodeChar(c0).toInt()
    val d1 = base64DecodeChar(c1).toInt()
    val d2 = base64DecodeChar(c2).toInt()
    return output(((d0 shl 2) or (d1 ushr 4)).toByte(),
            ((d1 shl 4) or (d2 ushr 2)).toByte())
}

@PublishedApi
internal inline fun <R> base64DecodePadding2(
        c0: Char,
        c1: Char,
        output: (Byte) -> R
): R {
    val d0 = base64DecodeChar(c0).toInt()
    val d1 = base64DecodeChar(c1).toInt()
    return output(((d0 shl 2) or (d1 ushr 4)).toByte())
}
