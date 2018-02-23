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
import org.tobi29.stdex.copyToString

/**
 * A Pascal-style string where the first byte is interpreted as the an unsigned length. The string is not '\0'
 * terminated.
 *
 * @author graywatson
 */
class PStringType : StringType() {

    /**
     * Extracted value is the extracted string using the first byte as the length.
     */
    override fun extractValueFromBytes(
        offset: Int,
        bytes: ByteArraySliceRO,
        required: Boolean
    ): Any? {
        // we don't need to extract the value if all we are doing is matching
        if (!required) {
            return ""
        }
        if (offset >= bytes.size) {
            return null
        }
        // length is from the first byte of the string
        var len = bytes[offset].toInt() and 0xFF
        val left = bytes.size - offset - 1
        if (len > left) {
            len = left
        }
        val chars = CharArray(len)
        for (i in chars.indices) {
            chars[i] = (bytes[offset + 1 + i].toInt() and 0xFF).toChar()
        }
        /*
		 * NOTE: we need to make a new string because it might be returned if we don't match below.
		 */
        return chars.copyToString()
    }

    override fun isMatch(
        testValue: Any?,
        andValue: Long?,
        unsignedType: Boolean,
        extractedValue: Any?,
        mutableOffset: MagicMatcher.MutableOffset,
        bytes: ByteArraySliceRO
    ): Any? {

        if (mutableOffset.offset >= bytes.size) {
            return null
        }
        // our maximum position is +1 to move past the length byte and then add in the length
        val len = bytes[mutableOffset.offset].toInt() and 0xFF
        var maxPos = 1 + len
        if (maxPos > bytes.size) {
            maxPos = bytes.size
        }

        // we start matching past the length byte so the starting offset is +1
        return findOffsetMatch(
            testValue as StringType.TestInfo,
            mutableOffset.offset + 1,
            mutableOffset,
            bytes,
            null,
            maxPos
        )
    }
}
