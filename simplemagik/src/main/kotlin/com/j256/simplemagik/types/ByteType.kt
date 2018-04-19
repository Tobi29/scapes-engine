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
 * A one-byte value.
 *
 * @author graywatson
 */
class ByteType : BaseLongType(EndianType.NATIVE) {

    override val bytesPerType: Int
        get() = 1

    override fun maskValue(value: Long): Long {
        return value and 0xFFL
    }

    override fun compare(
        unsignedType: Boolean,
        extractedValue: Number,
        testValue: Number
    ): Int {
        if (unsignedType) {
            return staticCompare(extractedValue, testValue)
        }
        val extractedByte = extractedValue.toByte()
        val testByte = testValue.toByte()
        return if (extractedByte > testByte) {
            1
        } else if (extractedByte < testByte) {
            -1
        } else {
            0
        }
    }
} // we don't care about byte order since we only process 1 byte at a time
