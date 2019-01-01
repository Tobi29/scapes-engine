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

/**
 * 32-bit codepoint
 *
 * Values in range 0x0..0xFFFF match those of [Char]
 */
typealias Codepoint = Int

/**
 * String containing all lowercase latin letters
 */
const val alphabetLatinLowercase = "abcdefghijklmnopqrstuvwxyz"

/**
 * String containing all uppercase latin letters
 */
const val alphabetLatinUppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

/**
 * String containing all arabic digits
 */
const val digitsArabic = "0123456789"

/**
 * Converts a single (non-surrogate) character to a codepoint
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Char.toCP(): Codepoint = toInt()

/**
 * Converts a codepoint to a string containing one or two characters
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
fun Codepoint.toCPString(): String =
    if (isBmpCodepoint()) "${toChar()}"
    else "${highSurrogate()}${lowSurrogate()}"

/**
 * Checks if the given character is an ISO control character
 * @receiver The character to check
 * @return `true` if the given character is an ISO control character
 */
expect fun Char.isISOControl(): Boolean

/**
 * Checks if the given codepoint is an ISO control character
 * @receiver The codepoint to check
 * @return `true` if the given codepoint is an ISO control character
 */
expect fun Codepoint.isISOControl(): Boolean

/**
 * Checks if the given codepoint can be represented in a [Char]
 *
 * **Note:** Whenever casting an [Int] (especially if declared using
 * [Codepoint]) consider checking with this method first
 * @receiver The codepoint to check
 * @return `tru` if the given codepoint can be represented in a [Char]
 */
expect fun Codepoint.isBmpCodepoint(): Boolean

/**
 * Retrieve the high surrogate character from the given codepoint
 *
 * **Note:** This may return any [Char] if the given codepoint is not surrogate
 * @receiver The codepoint to read
 * @return The high surrogate character from the given codepoint
 */
expect fun Codepoint.highSurrogate(): Char

/**
 * Retrieve the low surrogate character from the given codepoint
 *
 * **Note:** This may return any [Char] if the given codepoint is not surrogate
 * @receiver The codepoint to read
 * @return The low surrogate character from the given codepoint
 */
expect fun Codepoint.lowSurrogate(): Char

/**
 * Combine the given surrogate characters into a codepoint
 *
 * **Note:** This may return any [Char] if the given characters are not a
 * surrogate pair
 * @param high High surrogate
 * @param low Low surrogate
 * @return Codepoint for the given surrogate characters
 */
expect fun surrogateCodepoint(high: Char, low: Char): Codepoint

/**
 * Creates an [Iterator] over all codepoints in a [CharSequence]
 *
 * **Note:** Invalid characters are silently skipped or might produce mangled
 * codepoints
 */
fun CharSequence.codepoints(): Iterator<Int> = object : Iterator<Int> {
    var i = 0
    var c = 0
    var q = false

    override fun hasNext(): Boolean {
        if (q) return true
        return progressCodepoint(i) { n, cn ->
            i = n
            c = cn
            true
        } == true
    }

    override fun next(): Int {
        if (!hasNext()) throw NoSuchElementException(
            "Iterator has no more elements"
        )
        q = false
        return c
    }
}

/**
 * Iterates over all codepoints in a [CharSequence]
 *
 * **Note:** Invalid characters are silently skipped or might produce mangled
 * codepoints
 */
inline fun CharSequence.forEachCodepoint(block: (Codepoint) -> Unit) {
    var i = 0
    while (progressCodepoint(i) { n, c ->
            i = n
            block(c)
            true
        } == true);
}

/**
 * Progresses over a single codepoint in a [CharSequence]
 *
 * **Note:** Invalid characters are silently skipped or might produce mangled
 * codepoints
 *
 * **Note:** This is a low-level utility, consider higher-level alternatives
 * when applicable
 * @param i The first character for the codepoint
 * @param output Called with next character index and codepoint
 * @return `null` if the input ended or otherwise return value of [output]
 * @see CharSequence.codepoints
 * @see CharSequence.forEachCodepoint
 */
inline fun <R> CharSequence.progressCodepoint(
    i: Int,
    output: (Int, Codepoint) -> R
): R? {
    if (i >= length) return null
    val c0 = this[i]
    val n: Int
    val c = if (c0.isSurrogate()) {
        if (i >= length) return null
        val c1 = this[i + 1]
        n = i + 2
        surrogateCodepoint(c0, c1)
    } else {
        n = i + 1
        c0.toCP()
    }
    return output(n, c)
}
