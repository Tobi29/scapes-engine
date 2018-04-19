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

package com.j256.simplemagik.entries

import com.j256.simplemagik.endian.EndianType
import com.j256.simplemagik.types.*


private val typeMap = HashMap<String, MagicMatcher>().apply {
    /** Single byte value.  */
    register("byte", ByteType())
    /** 2 byte short integer in native-endian byte order.  */
    register("short", ShortType(EndianType.NATIVE))
    /** 4 byte "long" integer in native-endian byte order. This is C language long register(shudder).  */
    register("long", IntegerType(EndianType.NATIVE))
    /** 8 byte long integer in native-endian byte order.  */
    register("quad", LongType(EndianType.NATIVE))
    /** 4 byte floating point number in native-endian byte order.  */
    register("float", FloatType(EndianType.NATIVE))
    /** 8 byte floating point number in native-endian byte order.  */
    register("double", DoubleType(EndianType.NATIVE))
    /** Special string matching that supports white-space and case handling.  */
    register("string", StringType())
    /** Strings that are encoded with the first byte being the length of the string.  */
    register("pstring", PStringType())
    /** 4 byte value in native=endian byte order, interpreted as a Unix date using UTC time zone.  */
    register("date", UtcDateType(EndianType.NATIVE))
    /** 8 byte value in native-endian byte order, interpreted as a Unix date using UTC time zone.  */
    register("qdate", UtcLongDateType(EndianType.NATIVE))
    /** 4 byte value in native-endian byte order, interpreted as a Unix date using the local time zone.  */
    register("ldate", LocalDateType(EndianType.NATIVE))
    /** 8 byte value in native-endian byte order, interpreted as a Unix date using the local time zone.  */
    register("qldate", LocalLongDateType(EndianType.NATIVE))

    /** 4 byte integer with each byte using lower 7-bits in big-endian byte order.  */
    register("beid3", Id3LengthType(EndianType.BIG))
    /** 2 byte short integer in big-endian byte order.  */
    register("beshort", ShortType(EndianType.BIG))
    /** 4 byte "long" integer in big-endian byte order. This is C language long register(shudder).  */
    register("belong", IntegerType(EndianType.BIG))
    /** 8 byte long integer in big-endian byte order.  */
    register("bequad", LongType(EndianType.BIG))
    /** 4 byte floating point number in big-endian byte order.  */
    register("befloat", FloatType(EndianType.BIG))
    /** 8 byte floating point number in big-endian byte order.  */
    register("bedouble", DoubleType(EndianType.BIG))
    /** 4 byte value in big-endian byte order, interpreted as a Unix date using UTC time zone.  */
    register("bedate", UtcDateType(EndianType.BIG))
    /** 8 byte value in big-endian byte order, interpreted as a Unix date using UTC time zone.  */
    register("beqdate", UtcLongDateType(EndianType.BIG))
    /** 4 byte value big-endian byte order, interpreted as a Unix date using the local time zone.  */
    register("beldate", LocalDateType(EndianType.BIG))
    /** 8 byte value in big-endian byte order, interpreted as a Unix date using the local time zone.  */
    register("beqldate", LocalLongDateType(EndianType.BIG))
    /** String made up of 2-byte characters in big-endian byte order.  */
    register("bestring16", BigEndianString16Type())

    /** 4 byte integer with each byte using lower 7-bits in little-endian byte order.  */
    register("leid3", Id3LengthType(EndianType.LITTLE))
    /** 2 byte short integer in little-endian byte order.  */
    register("leshort", ShortType(EndianType.LITTLE))
    /** 4 byte "long" integer in little-endian byte order. This is C language long register(shudder).  */
    register("lelong", IntegerType(EndianType.LITTLE))
    /** 8 byte long integer in little-endian byte order.  */
    register("lequad", LongType(EndianType.LITTLE))
    /** 4 byte floating point number in little-endian byte order.  */
    register("lefloat", FloatType(EndianType.LITTLE))
    /** 8 byte floating point number in little-endian byte order.  */
    register("ledouble", DoubleType(EndianType.LITTLE))
    /** 4 byte value in little-endian byte order, interpreted as a Unix date using UTC time zone.  */
    register("ledate", UtcDateType(EndianType.LITTLE))
    /** 8 byte value in little-endian byte order, interpreted as a Unix date using UTC time zone.  */
    register("leqdate", UtcLongDateType(EndianType.LITTLE))
    /** 4 byte value little-endian byte order, interpreted as a Unix date using the local time zone.  */
    register("leldate", LocalDateType(EndianType.LITTLE))
    /** 8 byte value in little-endian byte order, interpreted as a Unix date using the local time zone.  */
    register("leqldate", LocalLongDateType(EndianType.LITTLE))
    /** String made up of 2-byte characters in little-endian byte order.  */
    register("lestring16", LittleEndianString16Type())

    // indirect -- special

    register("indirect", IndirectType())
    register("name", NameType())
    register("use", UseType())
    /** Regex line search looking for compiled patterns.  */
    register("regex", RegexType())
    /** String line search looking for sub-strings.  */
    register("search", SearchType())

    /** 4 byte "long" integer in middle-endian byte order. This is C language long register(shudder).  */
    register("melong", IntegerType(EndianType.MIDDLE))
    /** 4 byte value in middle-endian byte order, interpreted as a Unix date using UTC time zone.  */
    register("medate", UtcDateType(EndianType.MIDDLE))
    /** 4 byte value middle-endian byte order, interpreted as a Unix date using the local time zone.  */
    register("meldate", LocalDateType(EndianType.MIDDLE))

    /** Default type that always matches. Used in rule chaining.  */
    register("default", DefaultType())
}

@Suppress("NOTHING_TO_INLINE")
private inline fun MutableMap<String, MagicMatcher>.register(
    name: String,
    matcher: MagicMatcher
) = put(name, matcher)

/**
 * Find the associated matcher to the string.
 */
internal fun matcherfromString(typeString: String): MagicMatcher? =
    typeMap[typeString]
