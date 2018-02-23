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

package com.j256.simplemagik.endian

import org.tobi29.arrays.ByteArraySliceRO

/**
 * A four-byte value in middle-endian (god help us) PDP-11 byte order.
 *
 * @author graywatson
 */
class MiddleEndianConverter internal constructor() // only EndiaType should construct this
    : EndianConverter {

    override fun convertNumber(
        offset: Int,
        bytes: ByteArraySliceRO,
        size: Int
    ): Long? {
        return convertNumber(offset, bytes, size, 8, 0xFF)
    }

    override fun convertId3(offset: Int, bytes: ByteArraySliceRO, size: Int): Long? {
        return convertNumber(offset, bytes, size, 7, 0x7F)
    }

    override fun convertToByteArray(value: Long, size: Int): ByteArray? {
        return if (size == 4) {
            // BADC again
            byteArrayOf(
                (value shr 16 and 0XFF).toByte(),
                (value shr 24 and 0XFF).toByte(),
                (value shr 0 and 0XFF).toByte(),
                (value shr 8 and 0XFF).toByte()
            )
        } else {
            null
        }
    }

    private fun convertNumber(
        offset: Int,
        bytes: ByteArraySliceRO,
        size: Int,
        shift: Int,
        mask: Int
    ): Long? {
        if (size != 4) {
            throw UnsupportedOperationException("Middle-endian only supports 4-byte integers")
        }
        if (offset < 0 || offset + size > bytes.size) {
            return null
        }
        var value: Long = 0
        // BADC
        // TODO: This seems wrong
        value = value shl shift or (bytes[offset + 1].toInt() and mask).toLong()
        value = value shl shift or (bytes[offset + 0].toInt() and mask).toLong()
        value = value shl shift or (bytes[offset + 3].toInt() and mask).toLong()
        value = value shl shift or (bytes[offset + 2].toInt() and mask).toLong()
        return value
    }
}
