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
import com.j256.simplemagik.types.write
import org.tobi29.io.HeapViewByteBE
import org.tobi29.io.IOException
import org.tobi29.io.MemoryViewReadableStream
import org.tobi29.io.WritableByteStream

typealias MatcherConstructor = (String, String?, Long?, Boolean) -> MagicMatcher

private val typeMap =
    HashMap<String, MatcherConstructor>().apply {
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
private inline fun MutableMap<String, MatcherConstructor>.register(
    name: String,
    noinline matcher: MatcherConstructor
) = put(name, matcher)

/**
 * Find the associated matcher to the string.
 */
internal fun matcherfromString(typeString: String): MatcherConstructor? =
    typeMap[typeString]

internal fun MagicMatcher.write(stream: WritableByteStream) {
    when (this) {
        is BigEndianString16Type -> stream.put(0).also { write(stream) }
        is ByteType -> stream.put(1).also { write(stream) }
        is DateType -> stream.put(2).also { write(stream) }
        is DefaultType -> stream.put(3).also { write(stream) }
        is DoubleType -> stream.put(4).also { write(stream) }
        is FloatType -> stream.put(5).also { write(stream) }
        is Id3LengthType -> stream.put(6).also { write(stream) }
        is IndirectType -> stream.put(7).also { write(stream) }
        is IntType -> stream.put(8).also { write(stream) }
        is LittleEndianString16Type -> stream.put(9).also { write(stream) }
        is LongDateType -> stream.put(10).also { write(stream) }
        is LongType -> stream.put(11).also { write(stream) }
        is NameType -> stream.put(12).also { write(stream) }
        is PStringType -> stream.put(13).also { write(stream) }
        is RegexType -> stream.put(14).also { write(stream) }
        is SearchType -> stream.put(15).also { write(stream) }
        is ShortType -> stream.put(16).also { write(stream) }
        is StringType -> stream.put(17).also { write(stream) }
        is UnknownType -> stream.put(18).also { write(stream) }
        is UseType -> stream.put(19).also { write(stream) }
    }
}

internal fun readMagicMatcher(stream: MemoryViewReadableStream<HeapViewByteBE>): MagicMatcher =
    when (stream.get()) {
        0.toByte() -> readBigEndianString16Type(stream)
        1.toByte() -> readByteType(stream)
        2.toByte() -> readDateType(stream)
        3.toByte() -> readDefaultType(stream)
        4.toByte() -> readDoubleType(stream)
        5.toByte() -> readFloatType(stream)
        6.toByte() -> readId3LengthType(stream)
        7.toByte() -> readIndirectType(stream)
        8.toByte() -> readIntType(stream)
        9.toByte() -> readLittleEndianString16Type(stream)
        10.toByte() -> readLongDateType(stream)
        11.toByte() -> readLongType(stream)
        12.toByte() -> readNameType(stream)
        13.toByte() -> readPStringType(stream)
        14.toByte() -> readRegexType(stream)
        15.toByte() -> readSearchType(stream)
        16.toByte() -> readShortType(stream)
        17.toByte() -> readStringType(stream)
        18.toByte() -> readUnknownType(stream)
        19.toByte() -> readUseType(stream)
        else -> throw IOException("Invalid magic matcher id")
    }
