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

import org.tobi29.stdex.BIG_ENDIAN
import org.tobi29.stdex.NATIVE_ENDIAN

/**
 * Types of endian supported by the system.
 *
 * @author graywatson
 */
enum class EndianType(
    /**
     * Returns the converter associated with this endian-type.
     */
    val converter: EndianConverter
) {
    /** big endian, also called network byte order (motorola 68k)  */
    BIG(BigEndianConverter()),
    /** little endian (x86)  */
    LITTLE(LittleEndianConverter()),
    /** old PDP11 byte order  */
    MIDDLE(MiddleEndianConverter()),
    /** uses the byte order of the current system  */
    NATIVE(if (NATIVE_ENDIAN == BIG_ENDIAN) BIG.converter else LITTLE.converter)
} // end
