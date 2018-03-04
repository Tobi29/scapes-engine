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

package com.j256.simplemagik.types

import com.j256.simplemagik.entries.MagicMatcher
import org.tobi29.arrays.ByteArraySliceRO

/**
 * A two-byte unicode (UCS16) string in big-endian byte order.
 *
 * @author graywatson
 */
open class BigEndianString16Type : StringType() {

    override fun extractValueFromBytes(
        offset: Int,
        bytes: ByteArraySliceRO,
        required: Boolean
    ): Any {
        var len: Int
        // find the 2 (I guess) '\0' chars, we do the -1 to make sure we don't have odd number of bytes
        len = offset
        while (len < bytes.size - 1) {
            if (bytes[len].toInt() == 0 && bytes[len + 1].toInt() == 0) {
                break
            }
            len += 2
        }
        val chars = CharArray(len / 2)
        for (i in chars.indices) {
            chars[i] =
                    bytesToChar(bytes[i * 2].toInt(), bytes[i * 2 + 1].toInt())
        }
        return chars
    }

    override fun isMatch(
        testValue: Any?,
        andValue: Long?,
        unsignedType: Boolean,
        extractedValue: Any?,
        mutableOffset: MagicMatcher.MutableOffset,
        bytes: ByteArraySliceRO
    ): Any? {
        // we do the match on the extracted chars
        val chars = extractedValue as CharArray
        return super.findOffsetMatch(
            testValue as TestInfo,
            mutableOffset.offset,
            mutableOffset,
            null,
            chars,
            chars.size
        )
    }

    /**
     * Convert 2 bytes into a character.
     */
    protected open fun bytesToChar(firstByte: Int, secondByte: Int): Char {
        return ((firstByte shl 8) + secondByte).toChar()
    }
}
