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

package org.tobi29.stdex

expect fun Int.toString(radix: Int): String

expect fun Long.toString(radix: Int): String

fun Int.toStringCaseSensitive(radix: Int): String {
    checkRadix(radix)

    if (this == 0) return "0"
    val str = CharArray(33)
    val sign = this > 0
    var value = if (sign) -this else this
    var i = str.size
    while (value < 0) {
        str[--i] = (-(value % radix)).toDigit()
        value /= radix
    }
    if (!sign) str[--i] = '-'
    return String(str, i, str.size - i)
}

fun Long.toStringCaseSensitive(radix: Int): String {
    checkRadix(radix)

    if (this == 0L) return "0"
    val str = CharArray(65)
    val sign = this > 0L
    var value = if (sign) -this else this
    var i = str.size
    while (value < 0L) {
        str[--i] = (-(value % radix)).toInt().toDigit()
        value /= radix
    }
    if (!sign) str[--i] = '-'
    return String(str, i, str.size - i)
}

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun String.toIntCaseSensitive(radix: Int): Int =
    toIntCaseSensitiveOrNull(radix)
            ?: throw NumberFormatException("Invalid number: $this")

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun String.toLongCaseSensitive(radix: Int): Long =
    toLongCaseSensitiveOrNull(radix)
            ?: throw NumberFormatException("Invalid number: $this")

// Taken from Kotlin stdlib, copyright above
fun String.toIntCaseSensitiveOrNull(radix: Int): Int? {
    checkRadix(radix)

    val length = this.length
    if (length == 0) return null

    val start: Int
    val isNegative: Boolean
    val limit: Int

    val firstChar = this[0]
    if (firstChar < '0') {  // Possible leading sign
        if (length == 1) return null  // non-digit (possible sign) only, no digits after

        start = 1

        when (firstChar) {
            '-' -> {
                isNegative = true
                limit = Int.MIN_VALUE
            }
            '+' -> {
                isNegative = false
                limit = -Int.MAX_VALUE
            }
            else -> return null
        }
    } else {
        start = 0
        isNegative = false
        limit = -Int.MAX_VALUE
    }


    val limitBeforeMul = limit / radix
    var result = 0
    for (i in start..(length - 1)) {
        val digit = digitCaseSensitiveOf(this[i], radix)

        if (digit < 0) return null
        if (result < limitBeforeMul) return null

        result *= radix

        if (result < limit + digit) return null

        result -= digit
    }

    return if (isNegative) result else -result
}

// Taken from Kotlin stdlib, copyright above
fun String.toLongCaseSensitiveOrNull(radix: Int): Long? {
    checkRadix(radix)

    val length = this.length
    if (length == 0) return null

    val start: Int
    val isNegative: Boolean
    val limit: Long

    val firstChar = this[0]
    if (firstChar < '0') {  // Possible leading sign
        if (length == 1) return null  // non-digit (possible sign) only, no digits after

        start = 1

        when (firstChar) {
            '-' -> {
                isNegative = true
                limit = Long.MIN_VALUE
            }
            '+' -> {
                isNegative = false
                limit = -Long.MAX_VALUE
            }
            else -> return null
        }
    } else {
        start = 0
        isNegative = false
        limit = -Long.MAX_VALUE
    }


    val limitBeforeMul = limit / radix
    var result = 0L
    for (i in start..(length - 1)) {
        val digit = digitCaseSensitiveOf(this[i], radix)

        if (digit < 0) return null
        if (result < limitBeforeMul) return null

        result *= radix

        if (result < limit + digit) return null

        result -= digit
    }

    return if (isNegative) result else -result
}

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Boolean.primitiveHashCode(): Int = if (this) 1231 else 1237

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Byte.primitiveHashCode(): Int = this.toInt()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Short.primitiveHashCode(): Int = this.toInt()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Char.primitiveHashCode(): Int = this.toInt()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.primitiveHashCode(): Int = this

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Float.primitiveHashCode(): Int = toBits()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Long.primitiveHashCode(): Int = (this xor this.ushr(32)).toInt()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Double.primitiveHashCode(): Int = toBits().primitiveHashCode()

private fun checkRadix(radix: Int) {
    if (radix !in 1..62)
        throw IllegalArgumentException("Invalid radix: $radix")
}

private fun digitCaseSensitiveOf(char: Char, radix: Int) = when (char) {
    in '0'..'9' -> (char - '0').let { if (it < radix) it else -1 }
    in 'a'..'z' -> (char - 'a' + 10).let { if (it < radix) it else -1 }
    in 'A'..'Z' -> (char - 'A' + 36).let { if (it < radix) it else -1 }
    else -> -1
}

private fun Int.toDigit(): Char = when (this) {
    in 0..9 -> '0' + this
    in 10..35 -> 'a' + (this - 10)
    in 36..61 -> 'A' + (this - 36)
    else -> throw IllegalArgumentException("Invalid digit: $this")
}
