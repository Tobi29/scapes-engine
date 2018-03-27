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

import org.tobi29.arrays.BytesRO

/**
 * Classes which are able to match content according to operations and output description.
 *
 * @author graywatson
 */
interface MagicMatcher {

    /**
     * Converts the test-string from the magic line to be the testValue object to be passed into
     * [.isMatch] and [.getStartingBytes].
     */
    fun convertTestString(typeStr: String, testStr: String): Any?

    /**
     * Extract the value from the bytes either for doing the match or rendering it in the format.
     *
     * @param offset
     * Number of bytes into the bytes array that we are extracting from.
     * @param bytes
     * Array of bytes we are extracting from.
     * @param required
     * Whether or not the extracted value is required for later. If it is not then the type may opt to not
     * extract the value and to do the matching directly.
     * @return The object to be passed to [.isMatch] or null
     * if not enough bytes.
     */
    fun extractValueFromBytes(
        offset: Int,
        bytes: BytesRO,
        required: Boolean
    ): Any?

    /**
     * Matches if the bytes match at a certain offset.
     *
     * @return The extracted-value object, or null if no match.
     */
    fun isMatch(
        testValue: Any?,
        andValue: Long?,
        unsignedType: Boolean,
        extractedValue: Any?,
        offset: MutableOffset,
        bytes: BytesRO
    ): Any?

    /**
     * Returns the string version of the extracted value.
     */
    fun renderValue(
        sb: Appendable,
        extractedValue: Any?,
        formatter: MagicFormatter
    )

    /**
     * Return the starting bytes of the pattern or null if none.
     */
    fun getStartingBytes(testValue: Any?): ByteArray? = null

    /**
     * Offset which we can update.
     */
    class MutableOffset(offset: Int) {
        init {
            if (offset < 0)
                throw IllegalArgumentException("Negative offset: $offset")
        }

        var offset: Int = offset
            set(value) {
                if (value < 0)
                    throw IllegalArgumentException("Negative offset: $value")
                field = value
            }

        override fun toString(): String {
            return "$offset"
        }
    }
}
