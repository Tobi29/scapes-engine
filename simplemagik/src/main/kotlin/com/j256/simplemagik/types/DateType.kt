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
import com.j256.simplemagik.entries.toIntChecked
import org.tobi29.arrays.BytesRO
import org.tobi29.chrono.*
import org.tobi29.stdex.combineToInt
import org.tobi29.stdex.splitToBytes
import org.tobi29.utils.toInt128

data class DateType(
    val comparison: Pair<Int, TestOperator>?,
    val andValue: Int,
    val local: Boolean,
    val endianType: EndianType
) : MagicMatcher {
    override val startingBytes
        get() = if (comparison != null && andValue == -1)
            comparison.first.convert(endianType)
                .splitToBytes { v3, v2, v1, v0 ->
                    byteArrayOf(v3, v2, v1, v0)
                } else null

    override fun isMatch(
        bytes: BytesRO,
        required: Boolean
    ): Pair<Int, (Appendable, MagicFormatter) -> Unit>? =
        if (bytes.size >= 4)
            (combineToInt(
                bytes[0], bytes[1], bytes[2], bytes[3]
            ) and andValue).convert(endianType).let { extracted ->
                if (comparison == null || comparison.second.compare(
                        extracted, comparison.first
                    )) 4 to
                        { sb: Appendable, formatter: MagicFormatter ->
                            val (dateTime, offset) =
                                    timeZone.encodeWithOffset(
                                        extracted.toInt128() * 1000000000L.toInt128()
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

fun DateType(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean,
    local: Boolean,
    endianType: EndianType
): DateType = (andValue?.toIntChecked() ?: -1).let { andValue2 ->
    DateType(decodeComparison(testStr)?.let { (a, b) ->
        a.toIntChecked() to b
    }, andValue2, local, endianType)
}

fun DateTypeUtcBE(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): DateType = DateType(
    typeStr, testStr, andValue, unsignedType, false, EndianType.BIG
)

fun DateTypeUtcLE(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): DateType = DateType(
    typeStr, testStr, andValue, unsignedType, false, EndianType.LITTLE
)

fun DateTypeUtcME(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): DateType = DateType(
    typeStr, testStr, andValue, unsignedType, false, EndianType.MIDDLE
)

fun DateTypeUtcNE(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): DateType = DateType(
    typeStr, testStr, andValue, unsignedType, false, EndianType.NATIVE
)

fun DateTypeLocalBE(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): DateType = DateType(
    typeStr, testStr, andValue, unsignedType, true, EndianType.BIG
)

fun DateTypeLocalLE(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): DateType = DateType(
    typeStr, testStr, andValue, unsignedType, true, EndianType.LITTLE
)

fun DateTypeLocalME(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): DateType = DateType(
    typeStr, testStr, andValue, unsignedType, true, EndianType.MIDDLE
)

fun DateTypeLocalNE(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): DateType = DateType(
    typeStr, testStr, andValue, unsignedType, true, EndianType.NATIVE
)
