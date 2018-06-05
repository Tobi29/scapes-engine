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

import org.tobi29.stdex.*

/**
 * Types of endian supported by the system.
 *
 * @author graywatson
 */
enum class EndianType(
    resolved: EndianType? = null
) {
    /** big endian, also called network byte order (motorola 68k)  */
    BIG,
    /** little endian (x86)  */
    LITTLE,
    /** old PDP11 byte order  */
    MIDDLE,
    /** uses the byte order of the current system  */
    NATIVE(if (NATIVE_ENDIAN == BIG_ENDIAN) BIG else LITTLE);

    val resolved: EndianType = resolved ?: this
} // end

fun Short.convert(endianType: EndianType): Short = when (endianType.resolved) {
    EndianType.BIG -> this
    EndianType.LITTLE -> splitToBytes { v1, v0 ->
        combineToShort(v0, v1)
    }
    EndianType.MIDDLE -> splitToBytes { v1, v0 ->
        combineToShort(v0, v1)
    }
    else -> error("Impossible")
}

fun Int.convert(endianType: EndianType): Int = when (endianType.resolved) {
    EndianType.BIG -> this
    EndianType.LITTLE -> splitToBytes { v3, v2, v1, v0 ->
        combineToInt(v0, v1, v2, v3)
    }
    EndianType.MIDDLE -> splitToBytes { v3, v2, v1, v0 ->
        combineToInt(v2, v3, v0, v1)
    }
    else -> error("Impossible")
}

fun Long.convert(endianType: EndianType): Long = when (endianType.resolved) {
    EndianType.BIG -> this
    EndianType.LITTLE -> splitToBytes { v7, v6, v5, v4, v3, v2, v1, v0 ->
        combineToLong(v0, v1, v2, v3, v4, v5, v6, v7)
    }
    EndianType.MIDDLE -> splitToBytes { v7, v6, v5, v4, v3, v2, v1, v0 ->
        combineToLong(v6, v7, v4, v5, v2, v3, v0, v1)
    }
    else -> error("Impossible")
}

fun Float.convert(endianType: EndianType): Float =
    Float.fromBits(toRawBits().convert(endianType))

fun Double.convert(endianType: EndianType): Double =
    Double.fromBits(toRawBits().convert(endianType))
