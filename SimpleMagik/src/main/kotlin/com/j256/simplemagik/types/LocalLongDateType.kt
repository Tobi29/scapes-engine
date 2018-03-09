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
import org.tobi29.utils.InstantNanos
import org.tobi29.utils.toInt128

/**
 * An 8-byte value interpreted as a UNIX-style date, but interpreted as local time rather than UTC.
 *
 * @author graywatson
 */
class LocalLongDateType(endianType: EndianType) : LocalDateType(endianType) {

    override val bytesPerType: Int
        get() = BYTES_PER_LOCAL_LONG_DATE

    override fun dateFromExtractedValue(`val`: Long): InstantNanos {
        // TODO: is this in millis or seconds?
        return `val`.toInt128() * 1000000L.toInt128()
    }

    companion object {

        private val BYTES_PER_LOCAL_LONG_DATE = 8
    }
}