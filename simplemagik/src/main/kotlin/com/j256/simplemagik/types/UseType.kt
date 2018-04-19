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
import org.tobi29.arrays.BytesRO

// TODO: Implement

class UseType : MagicMatcher {
    override fun convertTestString(typeStr: String, testStr: String): Any? {
        return null
    }

    override fun extractValueFromBytes(
        offset: Int,
        bytes: BytesRO,
        required: Boolean
    ): Any? {
        return null
    }

    override fun isMatch(
        testValue: Any?,
        andValue: Long?,
        unsignedType: Boolean,
        extractedValue: Any?,
        offset: MagicMatcher.MutableOffset,
        bytes: BytesRO
    ): Any? {
        return null
    }

    override fun renderValue(
        sb: Appendable,
        extractedValue: Any?,
        formatter: MagicFormatter
    ) {
        formatter.format(sb, extractedValue)
    }
}
