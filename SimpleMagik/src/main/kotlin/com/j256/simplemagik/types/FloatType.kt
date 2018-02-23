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

/**
 * A 32-bit single precision IEEE floating point number in this machine's native byte order.
 *
 * @author graywatson
 */
class FloatType(endianType: EndianType) : DoubleType(endianType) {

    override val bytesPerType: Int
        get() = BYTES_PER_FLOAT

    override fun decodeValueString(valueStr: String): Number {
        return valueStr.toFloat()
    }

    override fun compare(
        unsignedType: Boolean,
        extractedValue: Number,
        testValue: Number
    ): Int {
        val extractedFloat = extractedValue.toFloat()
        val testFloat = testValue.toFloat()
        return if (extractedFloat > testFloat) {
            1
        } else if (extractedFloat < testFloat) {
            -1
        } else {
            0
        }
    }

    override fun longToObject(value: Long?): Any {
        return Float.fromBits(value!!.toInt())
    }

    companion object {

        private val BYTES_PER_FLOAT = 4
    }
}
