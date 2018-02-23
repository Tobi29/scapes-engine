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

import com.j256.simplemagik.endian.EndianConverter
import com.j256.simplemagik.endian.EndianType
import com.j256.simplemagik.entries.MagicFormatter
import com.j256.simplemagik.entries.MagicMatcher
import org.tobi29.arrays.ByteArraySliceRO

/**
 * Base class for our numbers so we can do generic operations on them.
 *
 * @author graywatson
 */
abstract class NumberType(endianType: EndianType) : MagicMatcher {

    protected val endianConverter: EndianConverter

    /**
     * Return the number of bytes in this type.
     */
    abstract val bytesPerType: Int

    init {
        this.endianConverter = endianType.converter
    }

    /**
     * Decode the test string value.
     */
    abstract fun decodeValueString(valueStr: String): Number

    /**
     * Return -1 if extractedValue is < testValue, 1 if it is >, 0 if it is equals.
     */
    abstract fun compare(
        unsignedType: Boolean,
        extractedValue: Number,
        testValue: Number
    ): Int

    /**
     * Return the value with the appropriate bytes masked off corresponding to the bytes in the type.
     */
    abstract fun maskValue(value: Long): Long

    override fun convertTestString(typeStr: String, testStr: String): Any {
        return NumberComparison(this, testStr)
    }

    override fun extractValueFromBytes(
        offset: Int,
        bytes: ByteArraySliceRO,
        required: Boolean
    ): Any? {
        return endianConverter.convertNumber(offset, bytes, bytesPerType)
    }

    override fun isMatch(
        testValue: Any?,
        andValue: Long?,
        unsignedType: Boolean,
        extractedValue: Any?,
        mutableOffset: MagicMatcher.MutableOffset,
        bytes: ByteArraySliceRO
    ): Any? {
        if ((testValue as NumberComparison).isMatch(
                andValue,
                unsignedType,
                extractedValue as Number
            )) {
            mutableOffset.offset += bytesPerType
            return extractedValue
        } else {
            return null
        }
    }

    override fun renderValue(
        sb: Appendable,
        extractedValue: Any?,
        formatter: MagicFormatter
    ) {
        formatter.format(sb, extractedValue)
    }
}
