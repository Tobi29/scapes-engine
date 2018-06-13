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
import com.j256.simplemagik.endian.convert
import com.j256.simplemagik.entries.MagicFormatter
import com.j256.simplemagik.entries.MagicMatcher
import org.tobi29.arrays.BytesRO
import org.tobi29.chrono.*
import org.tobi29.stdex.combineToLong
import org.tobi29.stdex.splitToBytes
import org.tobi29.utils.toInt128

data class LongDateType(
    val comparison: Pair<Long, TestOperator>?,
    val andValue: Long,
    val local: Boolean,
    val endianType: EndianType
) : MagicMatcher {
    override val startingBytes
        get() = if (comparison != null && andValue == -1L)
            comparison.first.convert(endianType)
                .splitToBytes { v7, v6, v5, v4, v3, v2, v1, v0 ->
                    byteArrayOf(v7, v6, v5, v4, v3, v2, v1, v0)
                } else null

    override fun isMatch(
        bytes: BytesRO,
        required: Boolean
    ): Pair<Int, (Appendable, MagicFormatter) -> Unit>? =
        if (bytes.size >= 8)
            (combineToLong(
                bytes[0], bytes[1], bytes[2], bytes[3],
                bytes[4], bytes[5], bytes[6], bytes[7]
            ) and andValue).convert(endianType).let { extracted ->
                if (comparison == null || comparison.second.compare(
                        extracted, comparison.first
                    )) 8 to
                        { sb: Appendable, formatter: MagicFormatter ->
                            // TODO: is this in millis or seconds?
                            val (dateTime, offset) =
                                    timeZone.encodeWithOffset(
                                        extracted.toInt128() * 1000000L.toInt128()
                                    )
                            val (date, time) = dateTime
                            formatter.format(
                                sb, "${isoDate(date)} ${
                                isoTime(time)} ${plainOffset(offset)}"
                            )
                        } else null
            } else null

    private val timeZone: TimeZone
        get() = if (local) timeZoneLocal else timeZoneUtc
}

fun LongDateType(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean,
    local: Boolean,
    endianType: EndianType
): LongDateType = (andValue ?: -1L).let { andValue2 ->
    LongDateType(decodeComparison(testStr), andValue2, local, endianType)
}

fun LongDateTypeUtcBE(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): LongDateType = LongDateType(
    typeStr, testStr, andValue, unsignedType, false, EndianType.BIG
)

fun LongDateTypeUtcLE(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): LongDateType = LongDateType(
    typeStr, testStr, andValue, unsignedType, false, EndianType.LITTLE
)

fun LongDateTypeUtcME(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): LongDateType = LongDateType(
    typeStr, testStr, andValue, unsignedType, false, EndianType.MIDDLE
)

fun LongDateTypeUtcNE(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): LongDateType = LongDateType(
    typeStr, testStr, andValue, unsignedType, false, EndianType.NATIVE
)

fun LongDateTypeLocalBE(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): LongDateType = LongDateType(
    typeStr, testStr, andValue, unsignedType, true, EndianType.BIG
)

fun LongDateTypeLocalLE(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): LongDateType = LongDateType(
    typeStr, testStr, andValue, unsignedType, true, EndianType.LITTLE
)

fun LongDateTypeLocalME(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): LongDateType = LongDateType(
    typeStr, testStr, andValue, unsignedType, true, EndianType.MIDDLE
)

fun LongDateTypeLocalNE(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): LongDateType = LongDateType(
    typeStr, testStr, andValue, unsignedType, true, EndianType.NATIVE
)