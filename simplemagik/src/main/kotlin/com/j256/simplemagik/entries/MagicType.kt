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

import com.j256.simplemagik.types.*


private val typeMap =
    HashMap<String, (String, String?, Long?, Boolean) -> MagicMatcher>().apply {
        /** Single byte value.  */
        register("byte", ::ByteType)
        /** 2 byte short integer in native-endian byte order.  */
        register("short", ::ShortTypeNE)
        /** 4 byte "long" integer in native-endian byte order. This is C language long register(shudder).  */
        register("long", ::IntTypeNE)
        /** 8 byte long integer in native-endian byte order.  */
        register("quad", ::LongTypeNE)
        /** 4 byte floating point number in native-endian byte order.  */
        register("float", ::FloatTypeNE)
        /** 8 byte floating point number in native-endian byte order.  */
        register("double", ::DoubleTypeNE)
        /** Special string matching that supports white-space and case handling.  */
        register("string", ::StringType)
        /** Strings that are encoded with the first byte being the length of the string.  */
        register("pstring", ::PStringType)
        /** 4 byte value in native=endian byte order, interpreted as a Unix date using UTC time zone.  */
        register("date", ::DateTypeUtcNE)
        /** 8 byte value in native-endian byte order, interpreted as a Unix date using UTC time zone.  */
        register("qdate", ::LongDateTypeUtcNE)
        /** 4 byte value in native-endian byte order, interpreted as a Unix date using the local time zone.  */
        register("ldate", ::DateTypeLocalNE)
        /** 8 byte value in native-endian byte order, interpreted as a Unix date using the local time zone.  */
        register("qldate", ::LongDateTypeLocalNE)

        /** 4 byte integer with each byte using lower 7-bits in big-endian byte order.  */
        register("beid3", ::Id3LengthTypeBE)
        /** 2 byte short integer in big-endian byte order.  */
        register("beshort", ::ShortTypeBE)
        /** 4 byte "long" integer in big-endian byte order. This is C language long register(shudder).  */
        register("belong", ::IntTypeBE)
        /** 8 byte long integer in big-endian byte order.  */
        register("bequad", ::LongTypeBE)
        /** 4 byte floating point number in big-endian byte order.  */
        register("befloat", ::FloatTypeBE)
        /** 8 byte floating point number in big-endian byte order.  */
        register("bedouble", ::DoubleTypeBE)
        /** 4 byte value in big-endian byte order, interpreted as a Unix date using UTC time zone.  */
        register("bedate", ::DateTypeUtcBE)
        /** 8 byte value in big-endian byte order, interpreted as a Unix date using UTC time zone.  */
        register("beqdate", ::LongDateTypeUtcBE)
        /** 4 byte value big-endian byte order, interpreted as a Unix date using the local time zone.  */
        register("beldate", ::DateTypeLocalBE)
        /** 8 byte value in big-endian byte order, interpreted as a Unix date using the local time zone.  */
        register("beqldate", ::LongDateTypeLocalBE)
        /** String made up of 2-byte characters in big-endian byte order.  */
        register("bestring16", ::BigEndianString16Type)

        /** 4 byte integer with each byte using lower 7-bits in little-endian byte order.  */
        register("leid3", ::Id3LengthTypeLE)
        /** 2 byte short integer in little-endian byte order.  */
        register("leshort", ::ShortTypeLE)
        /** 4 byte "long" integer in little-endian byte order. This is C language long register(shudder).  */
        register("lelong", ::IntTypeLE)
        /** 8 byte long integer in little-endian byte order.  */
        register("lequad", ::LongTypeLE)
        /** 4 byte floating point number in little-endian byte order.  */
        register("lefloat", ::FloatTypeLE)
        /** 8 byte floating point number in little-endian byte order.  */
        register("ledouble", ::DoubleTypeLE)
        /** 4 byte value in little-endian byte order, interpreted as a Unix date using UTC time zone.  */
        register("ledate", ::DateTypeUtcLE)
        /** 8 byte value in little-endian byte order, interpreted as a Unix date using UTC time zone.  */
        register("leqdate", ::LongDateTypeUtcLE)
        /** 4 byte value little-endian byte order, interpreted as a Unix date using the local time zone.  */
        register("leldate", ::DateTypeLocalLE)
        /** 8 byte value in little-endian byte order, interpreted as a Unix date using the local time zone.  */
        register("leqldate", ::LongDateTypeLocalLE)
        /** String made up of 2-byte characters in little-endian byte order.  */
        register("lestring16", ::LittleEndianString16Type)

        /** 4 byte integer with each byte using lower 7-bits in middle-endian byte order.  */
        register("meid3", ::Id3LengthTypeME)
        /** 2 byte short integer in middle-endian byte order.  */
        register("meshort", ::ShortTypeME)
        /** 4 byte "long" integer in middle-endian byte order. This is C language long register(shudder).  */
        register("melong", ::IntTypeME)
        /** 8 byte long integer in middle-endian byte order.  */
        register("mequad", ::LongTypeME)
        /** 4 byte floating point number in middle-endian byte order.  */
        register("mefloat", ::FloatTypeME)
        /** 8 byte floating point number in middle-endian byte order.  */
        register("medouble", ::DoubleTypeME)
        /** 4 byte value in middle-endian byte order, interpreted as a Unix date using UTC time zone.  */
        register("medate", ::DateTypeUtcME)
        /** 8 byte value in middle-endian byte order, interpreted as a Unix date using UTC time zone.  */
        register("meqdate", ::LongDateTypeUtcME)
        /** 4 byte value middle-endian byte order, interpreted as a Unix date using the local time zone.  */
        register("meldate", ::DateTypeLocalME)
        /** 8 byte value in middle-endian byte order, interpreted as a Unix date using the local time zone.  */
        register("meqldate", ::LongDateTypeLocalME)
        /** String made up of 2-byte characters in middle-endian byte order.  */

        // indirect -- special

        register("indirect", ::IndirectType)
        register("name", ::NameType)
        register("use", ::UseType)
        /** Regex line search looking for compiled patterns.  */
        register("regex", ::RegexType)
        /** String line search looking for sub-strings.  */
        register("search", ::SearchType)

        /** Default type that always matches. Used in rule chaining.  */
        register("default", ::DefaultType)
    }

@Suppress("NOTHING_TO_INLINE")
private inline fun MutableMap<String, (String, String?, Long?, Boolean) -> MagicMatcher>.register(
    name: String,
    noinline matcher: (String, String?, Long?, Boolean) -> MagicMatcher
) = put(name, matcher)

/**
 * Find the associated matcher to the string.
 */
internal fun matcherfromString(typeString: String): ((String, String?, Long?, Boolean) -> MagicMatcher)? =
    typeMap[typeString]
