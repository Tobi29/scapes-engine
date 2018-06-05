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
import org.tobi29.stdex.combineToLong
import org.tobi29.stdex.splitToBytes

data class DoubleType(
    val comparison: Pair<Double, TestOperator>?,
    val endianType: EndianType
) : MagicMatcher {
    override val startingBytes
        get() = if (comparison != null)
            comparison.first.convert(endianType).toRawBits()
                .splitToBytes { v7, v6, v5, v4, v3, v2, v1, v0 ->
                    byteArrayOf(v7, v6, v5, v4, v3, v2, v1, v0)
                } else null

    override fun isMatch(
        bytes: BytesRO,
        required: Boolean
    ): Pair<Int, (Appendable, MagicFormatter) -> Unit>? =
        if (bytes.size >= 8)
            Double.fromBits(
                combineToLong(
                    bytes[0], bytes[1], bytes[2], bytes[3],
                    bytes[4], bytes[5], bytes[6], bytes[7]
                )
            ).convert(endianType).let { extracted ->
                if (comparison == null || comparison.second.compare(
                        extracted, comparison.first
                    )) 8 to
                        { sb: Appendable, formatter: MagicFormatter ->
                            formatter.format(sb, extracted)
                        } else null
            } else null
}

fun DoubleType(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    endianType: EndianType
): DoubleType = DoubleType(decodeComparisonDecimal(testStr)?.let { (a, b) ->
    a.toDouble() to b
}, endianType)

fun DoubleTypeBE(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): DoubleType = DoubleType(
    typeStr, testStr, andValue, EndianType.BIG
)

fun DoubleTypeLE(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): DoubleType = DoubleType(
    typeStr, testStr, andValue, EndianType.LITTLE
)

fun DoubleTypeME(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): DoubleType = DoubleType(
    typeStr, testStr, andValue, EndianType.MIDDLE
)

fun DoubleTypeNE(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): DoubleType = DoubleType(
    typeStr, testStr, andValue, EndianType.NATIVE
)
