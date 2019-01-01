/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.base64

import org.tobi29.stdex.*

fun Appendable.appendBase64(
    array: ByteArray,
    offset: Int = 0,
    length: Int = array.size - offset
) {
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
    ByteArray(
        lengthBase64() ?: throw IllegalArgumentException(
            "Unable to determine output length"
        )
    ).also {
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
fun CharSequence.fromBase64(
    array: ByteArray,
    offset: Int = 0,
    length: Int = lengthBase64() ?: 0
): Int {
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

@Constant
inline val BASE64_DATA_END
    get() = Int.MIN_VALUE

/**
 * Base64 digit, either 0..63 as a value or 64 for padding symbol
 */
typealias Base64Digit = Int

/**
 * Encodes data in base64 without padding
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
 * @param encodingTable String containing 64 characters and the padding symbol
 */
@Suppress("UNUSED_PARAMETER")
inline fun encodeBase64NoPadding(
    input: () -> Int,
    output: (Char) -> Unit,
    encodingTable: String = ENCODE_TABLE
) = encodeBase64Digit(input, { if (it != 64) output(encodingTable[it]) })

/**
 * Encodes data in base64 with the default digit encoding
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
 * @param encodingTable String containing 64 characters and the padding symbol
 */
inline fun encodeBase64(
    input: () -> Int,
    output: (Char) -> Unit,
    encodingTable: String = ENCODE_TABLE
) = encodeBase64Digit(input, { output(encodingTable[it]) })

/**
 * Encodes data in base64 using 0..63 as values and 64 as padding
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
 */
inline fun encodeBase64Digit(
    input: () -> Int,
    output: (Base64Digit) -> Unit
) {
    while (true) {
        val b0 = input()
        val b1 = input()
        val b2 = input()
        base64EncodeBatch(b0, b1, b2, {
            return
        }, { c0, c1 ->
            output(c0)
            output(c1)
            repeat(2) { output(64) }
            return
        }, { c0, c1, c2 ->
            output(c0)
            output(c1)
            output(c2)
            output(64)
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
 * Decodes base64 encoded data with the default digit encoding
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
 * @param decodingTable Array to convert ASCII codes to digits or `-1`
 * @throws IllegalArgumentException When an invalid character was encountered
 * @return Reason for data end
 * @see base64DecodingTableASCII
 */
inline fun decodeBase64(
    input: () -> Int,
    output: (Byte) -> Unit,
    decodingTable: ByteArray = base64DecodeTable
): Base64Result = decodeBase64Digit({
    input().let { digit ->
        if (digit >= 0) base64DecodeChar(decodingTable, digit.toChar())
        else -1
    }
}, output)

/**
 * Decodes base64 encoded data using 0..63 as values and 64 as padding
 *
 * Reads characters from [input] (-1 indicates EOS, otherwise value is expected
 * to conform [Base64Digit]), decodes and writes to [output]
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
inline fun decodeBase64Digit(
    input: () -> Base64Digit,
    output: (Byte) -> Unit
): Base64Result {
    while (true) {
        val d0 = input()
        val d1 = input()
        val d2 = input()
        val d3 = input()
        base64DecodeBatch(d0, d1, d2, d3, { b0, b1, b2 ->
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

/**
 * Decoding result describing what kind of padding was encountered
 */
enum class Base64Result {
    /**
     * No padding was needed
     */
    ALIGNED,
    /**
     * Correct padding was found
     */
    PADDED,
    /**
     * No padding was found but would have been possible
     */
    NOT_PADDED
}

/**
 * Creates a decoding table from the given [encodingTable]
 *
 * **Note:** Only ASCII characters are allowed in [encodingTable]
 */
fun base64DecodingTableASCII(encodingTable: String): ByteArray =
    ByteArray(128) { -1 }.apply {
        for ((i, c) in encodingTable.withIndex()) {
            require(c.toInt() in 0 until 128) {
                "Invalid character in encoding table: '$c'"
            }
            this[c.toInt()] = i.toByte()
        }
    }

@Constant
@PublishedApi
internal inline val ENCODE_TABLE
    get() = "$alphabetLatinUppercase$alphabetLatinLowercase$digitsArabic+/="

@PublishedApi
internal val base64DecodeTable = base64DecodingTableASCII(ENCODE_TABLE)

@PublishedApi
internal inline fun <R> base64EncodeBatch(
    b0: Int,
    b1: Int,
    b2: Int,
    end: () -> R,
    outputPadding2: (Base64Digit, Base64Digit) -> R,
    outputPadding1: (Base64Digit, Int, Base64Digit) -> R,
    outputFull: (Base64Digit, Base64Digit, Base64Digit, Base64Digit) -> R
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
    d0: Base64Digit,
    d1: Base64Digit,
    d2: Base64Digit,
    d3: Base64Digit,
    outputFull: (Byte, Byte, Byte) -> R,
    outputPadding2: (Byte, Boolean) -> R,
    outputPadding1: (Byte, Byte, Boolean) -> R,
    end: () -> R
): R = if (d0 < 0 || d1 < 0) {
    end()
} else if (d3 < 0 || d3 == 64) {
    if (d2 < 0 || d2 == 64) {
        base64DecodePadding2(d0, d1) { b0 ->
            outputPadding2(b0, d2 >= 0)
        }
    } else {
        base64DecodePadding1(d0, d1, d2) { b0, b1 ->
            outputPadding1(b0, b1, d3 >= 0)
        }
    }
} else {
    base64DecodeFull(d0, d1, d2, d3, outputFull)
}

@PublishedApi
internal inline fun <R> base64EncodeFull(
    b0: Byte,
    b1: Byte,
    b2: Byte,
    output: (Base64Digit, Base64Digit, Base64Digit, Base64Digit) -> R
): R {
    val i0 = b0.toInt()
    val i1 = b1.toInt()
    val i2 = b2.toInt()
    return output(
        (i0 ushr 2) and 0b00111111,
        ((i0 shl 4) and 0b00110000) or ((i1 ushr 4) and 0b00001111),
        ((i1 shl 2) and 0b00111100) or ((i2 ushr 6) and 0b00000011),
        (i2 ushr 0) and 0b00111111
    )

}

@PublishedApi
internal inline fun <R> base64EncodePadding1(
    b0: Byte,
    b1: Byte,
    output: (Base64Digit, Base64Digit, Base64Digit) -> R
): R {
    val i0 = b0.toInt()
    val i1 = b1.toInt()
    return output(
        (i0 ushr 2) and 0b00111111,
        ((i0 shl 4) and 0b00110000) or ((i1 ushr 4) and 0b00001111),
        (i1 shl 2) and 0b00111100
    )
}

@PublishedApi
internal inline fun <R> base64EncodePadding2(
    b0: Byte,
    output: (Base64Digit, Base64Digit) -> R
): R {
    val i0 = b0.toInt()
    return output(
        (i0 ushr 2) and 0b00111111,
        (i0 shl 4) and 0b00110000
    )
}

@PublishedApi
internal fun base64DecodeChar(
    decodingTable: ByteArray,
    c: Char
): Base64Digit {
    val i = c.toInt()
    if (i and 0x7F.inv() == 0) {
        decodingTable[i].let { if (it >= 0) return it.toInt() }
    }
    throw IllegalArgumentException("Invalid base64 character: $c")
}

@PublishedApi
internal inline fun <R> base64DecodeFull(
    d0: Base64Digit,
    d1: Base64Digit,
    d2: Base64Digit,
    d3: Base64Digit,
    output: (Byte, Byte, Byte) -> R
): R = output(
    ((d0 shl 2) or (d1 ushr 4)).toByte(),
    ((d1 shl 4) or (d2 ushr 2)).toByte(),
    ((d2 shl 6) or (d3 ushr 0)).toByte()
)

@PublishedApi
internal inline fun <R> base64DecodePadding1(
    d0: Base64Digit,
    d1: Base64Digit,
    d2: Base64Digit,
    output: (Byte, Byte) -> R
): R = output(
    ((d0 shl 2) or (d1 ushr 4)).toByte(),
    ((d1 shl 4) or (d2 ushr 2)).toByte()
)

@PublishedApi
internal inline fun <R> base64DecodePadding2(
    d0: Base64Digit,
    d1: Base64Digit,
    output: (Byte) -> R
): R = output(
    ((d0 shl 2) or (d1 ushr 4)).toByte()
)
