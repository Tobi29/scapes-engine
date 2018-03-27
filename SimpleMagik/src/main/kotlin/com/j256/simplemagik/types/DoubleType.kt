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

import com.j256.simplemagik.endian.EndianType
import org.tobi29.arrays.BytesRO

/**
 * A 64-bit double precision IEEE floating point number in this machine's native byte order.
 *
 * @author graywatson
 */
open class DoubleType(endianType: EndianType) : NumberType(endianType) {

    /**
     * Return the number of bytes in this type.
     */
    override val bytesPerType: Int
        get() = BYTES_PER_DOUBLE

    override fun decodeValueString(valueStr: String): Number {
        return valueStr.toDouble()
    }

    override fun extractValueFromBytes(
        offset: Int,
        bytes: BytesRO,
        required: Boolean
    ): Any? {
        val `val` = endianConverter.convertNumber(offset, bytes, bytesPerType)
        return if (`val` == null) {
            null
        } else {
            longToObject(`val`)
        }
    }

    override fun compare(
        unsignedType: Boolean,
        extractedValue: Number,
        testValue: Number
    ): Int {
        val extractedDouble = extractedValue.toDouble()
        val testDouble = testValue.toDouble()
        return if (extractedDouble > testDouble) {
            1
        } else if (extractedDouble < testDouble) {
            -1
        } else {
            0
        }
    }

    /**
     * Convert a long to the type.
     */
    protected open fun longToObject(value: Long?): Any {
        return Double.fromBits(value!!)
    }

    override fun maskValue(value: Long): Long {
        return value
    }
}

private const val BYTES_PER_DOUBLE = 8
