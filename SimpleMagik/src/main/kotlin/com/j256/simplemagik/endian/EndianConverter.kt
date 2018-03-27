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

import org.tobi29.arrays.BytesRO

/**
 * Class which converts from a particular machine byte representation into values appropriate for Java.
 *
 * @author graywatson
 */
interface EndianConverter {

    /**
     * Convert a number of bytes starting at an offset into a long integer.
     *
     * @return The long or null if not enough bytes.
     */
    fun convertNumber(offset: Int, bytes: BytesRO, size: Int): Long?

    /**
     * Convert a number of bytes starting at an offset into a long integer where the high-bit in each byte is always 0.
     *
     * @return The long or null if not enough bytes.
     */
    fun convertId3(offset: Int, bytes: BytesRO, size: Int): Long?

    /**
     * Translate a number into an array of bytes.
     */
    fun convertToByteArray(value: Long, size: Int): ByteArray?
}
