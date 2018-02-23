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

import com.j256.simplemagik.entries.MagicFormatter
import com.j256.simplemagik.entries.MagicMatcher
import org.tobi29.arrays.ByteArraySliceRO

class IndirectType : MagicMatcher {

    override fun convertTestString(typeStr: String, testStr: String): Any {
        // null is an error so we just return junk
        return EMPTY
    }

    override fun extractValueFromBytes(
        offset: Int,
        bytes: ByteArraySliceRO,
        required: Boolean
    ): Any {
        return EMPTY
    }

    override fun isMatch(
        testValue: Any?,
        andValue: Long?,
        unsignedType: Boolean,
        extractedValue: Any?,
        offset: MagicMatcher.MutableOffset,
        bytes: ByteArraySliceRO
    ): Any {
        // always matches
        return EMPTY
    }

    override fun renderValue(
        sb: Appendable,
        extractedValue: Any?,
        formatter: MagicFormatter
    ) {
        formatter.format(sb, extractedValue)
    }

    override fun getStartingBytes(testValue: Any?): ByteArray? {
        return null
    }
}

private const val EMPTY = ""
