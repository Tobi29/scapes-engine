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
import com.j256.simplemagik.entries.toShortChecked
import org.tobi29.arrays.BytesRO
import org.tobi29.stdex.combineToShort
import org.tobi29.stdex.splitToBytes
import kotlin.experimental.and

data class ShortType(
    val comparison: Pair<Short, TestOperator>?,
    val andValue: Short,
    val unsignedType: Boolean,
    val endianType: EndianType
) : MagicMatcher {
    override val startingBytes
        get() = if (comparison != null && andValue == (-1).toShort())
            comparison.first.convert(endianType)
                .splitToBytes { v1, v0 ->
                    byteArrayOf(v1, v0)
                } else null

    override fun isMatch(
        bytes: BytesRO,
        required: Boolean
    ): Pair<Int, (Appendable, MagicFormatter) -> Unit>? =
        if (bytes.size >= 2)
            (combineToShort(
                bytes[0], bytes[1]
            ) and andValue).convert(endianType).let { extracted ->
                if (comparison == null || comparison.second.compare(
                        extracted, comparison.first, unsignedType
                    )) 2 to
                        { sb: Appendable, formatter: MagicFormatter ->
                            formatter.format(
                                sb, extracted.toIntSigned(unsignedType)
                            )
                        } else null
            } else null
}

fun ShortType(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean,
    endianType: EndianType
): ShortType = (andValue?.toShortChecked() ?: -1).let { andValue2 ->
    ShortType(decodeComparison(testStr)?.let { (a, b) ->
        a.toShortChecked() to b
    }, andValue2, unsignedType, endianType)
}

fun ShortTypeBE(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): ShortType = ShortType(
    typeStr, testStr, andValue, unsignedType, EndianType.BIG
)

fun ShortTypeLE(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): ShortType = ShortType(
    typeStr, testStr, andValue, unsignedType, EndianType.LITTLE
)

fun ShortTypeME(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): ShortType = ShortType(
    typeStr, testStr, andValue, unsignedType, EndianType.MIDDLE
)

fun ShortTypeNE(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): ShortType = ShortType(
    typeStr, testStr, andValue, unsignedType, EndianType.NATIVE
)
