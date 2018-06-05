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
import org.tobi29.stdex.combineToInt
import org.tobi29.stdex.splitToBytes

data class IntType(
    val comparison: Pair<Int, TestOperator>?,
    val andValue: Int,
    val unsignedType: Boolean,
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
                        extracted, comparison.first, unsignedType
                    )) 4 to
                        { sb: Appendable, formatter: MagicFormatter ->
                            formatter.format(
                                sb, extracted.toLongSigned(unsignedType)
                            )
                        } else null
            } else null
}

fun IntType(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean,
    endianType: EndianType
): IntType = (andValue?.toIntChecked() ?: -1).let { andValue2 ->
    IntType(decodeComparison(testStr)?.let { (a, b) ->
        a.toIntChecked() to b
    }, andValue2, unsignedType, endianType)
}

fun IntTypeBE(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): IntType = IntType(
    typeStr, testStr, andValue, unsignedType, EndianType.BIG
)

fun IntTypeLE(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): IntType = IntType(
    typeStr, testStr, andValue, unsignedType, EndianType.LITTLE
)

fun IntTypeME(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): IntType = IntType(
    typeStr, testStr, andValue, unsignedType, EndianType.MIDDLE
)

fun IntTypeNE(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): IntType = IntType(
    typeStr, testStr, andValue, unsignedType, EndianType.NATIVE
)
