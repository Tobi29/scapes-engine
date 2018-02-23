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
import org.tobi29.arrays.ByteArraySliceRO
import org.tobi29.arrays.sliceOver
import kotlin.experimental.and

/**
 * A four-byte integer value where the high bit of each byte is ignored.
 *
 * @author graywatson
 */
class Id3LengthType(endianType: EndianType) : IntegerType(endianType) {

    override fun extractValueFromBytes(
        offset: Int,
        bytes: ByteArraySliceRO,
        required: Boolean
    ): Any? {
        // because we only use the lower 7-bits of each byte, we need to copy into a local byte array
        val bytesPerType = bytesPerType
        val sevenBitBytes = ByteArray(bytesPerType)
        for (i in 0 until bytesPerType) {
            sevenBitBytes[i] = bytes[offset + i] and 0x7F
        }
        // because we've copied into a local array, we use the 0 offset
        return endianConverter.convertNumber(
            0, sevenBitBytes.sliceOver(), bytesPerType
        )
    }
}
