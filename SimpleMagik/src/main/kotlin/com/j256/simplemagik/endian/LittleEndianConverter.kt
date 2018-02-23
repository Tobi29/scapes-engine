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
 * Converts values in "little" endian-ness where the high-order bytes come _after_ the low-order (DCBA). x86 processors.
 *
 * @author graywatson
 */
class LittleEndianConverter internal constructor() : EndianConverter {
    override fun convertNumber(
        offset: Int,
        bytes: ByteArraySliceRO,
        size: Int
    ): Long? {
        return convertNumber(offset, bytes, size, 8, 0xFF)
    }

    override fun convertId3(
        offset: Int,
        bytes: ByteArraySliceRO,
        size: Int
    ): Long? {
        return convertNumber(offset, bytes, size, 7, 0x7F)
    }

    override fun convertToByteArray(value: Long, size: Int): ByteArray {
        var current = value
        val result = ByteArray(size)
        for (i in 0 until size) {
            result[i] = current.toByte()
            current = current shr 8
        }
        return result
    }

    private fun convertNumber(
        offset: Int,
        bytes: ByteArraySliceRO,
        size: Int,
        shift: Int,
        mask: Long
    ): Long? {
        if (offset < 0 || offset + size > bytes.size) {
            return null
        }
        var value = 0L
        for (i in offset + (size - 1) downTo offset) {
            value = (value shl shift) or (bytes[i].toLong() and mask)
        }
        return value
    }
}
