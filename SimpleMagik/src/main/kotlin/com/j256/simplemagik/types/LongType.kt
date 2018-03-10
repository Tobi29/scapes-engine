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
 * An eight-byte value constituted "quad" when the magic file spec was written.
 *
 * @author graywatson
 */
class LongType(endianType: EndianType) : BaseLongType(endianType) {

    /**
     * Return the number of bytes in this type.
     */
    override val bytesPerType: Int
        get() = BYTES_PER_LONG

    override fun maskValue(value: Long): Long {
        return value
    }

    override fun compare(
        unsignedType: Boolean,
        extractedValue: Number,
        testValue: Number
    ): Int = staticCompare(extractedValue, testValue)
}

private const val BYTES_PER_LONG = 8

internal fun staticCompare(extractedValue: Number, testValue: Number): Int {
    val extractedLong = extractedValue.toLong()
    val testLong = testValue.toLong()
    return if (extractedLong > testLong) {
        1
    } else if (extractedLong < testLong) {
        -1
    } else {
        0
    }
}
