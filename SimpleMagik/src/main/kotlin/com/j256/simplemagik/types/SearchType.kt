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
 * From the magic(5) man page: A literal string search starting at the given line offset. The same modifier flags can be
 * used as for string patterns. The modifier flags (if any) must be followed by /number range, that is, the number of
 * positions at which the match will be attempted, starting from the start offset. This is suitable for searching larger
 * binary expressions with variable offsets, using \ escapes for special characters. The offset works as for regex.
 *
 *
 *
 * **NOTE:** in our experience, the /number is _before_ the flags in 99% of the lines so that is how we implemented
 * it.
 *
 *
 * @author graywatson
 */
class SearchType : StringType() {

    override fun isMatch(
        testValue: Any?,
        andValue: Long?,
        unsignedType: Boolean,
        extractedValue: Any?,
        mutableOffset: MagicMatcher.MutableOffset,
        bytes: ByteArraySliceRO
    ): Any? {
        val info = testValue as StringType.TestInfo
        var maxOffset = info.maxOffset
        if (info.optionalWhiteSpace) {
            // we have to look at all of the bytes unfortunately
            maxOffset = bytes.size
        }
        // if offset is 1 then we need to pre-read 1 char
        var end = mutableOffset.offset + maxOffset + info.pattern!!.length
        if (end > bytes.size) {
            end = bytes.size
        }
        for (offset in mutableOffset.offset until end) {
            val match = findOffsetMatch(
                info,
                offset,
                mutableOffset,
                bytes,
                null,
                bytes.size
            )
            if (match != null) {
                return match
            }
        }
        return null
    }
}
