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

import com.j256.simplemagik.entries.MagicFormatter
import com.j256.simplemagik.entries.MagicMatcher
import org.tobi29.arrays.BytesRO
import org.tobi29.stdex.combineToInt
import org.tobi29.stdex.combineToShort

data class PStringType(
    val comparison: StringComparison?,
    val lengthType: Int,
    val lengthIncludesLength: Boolean
) : MagicMatcher {
    override fun isMatch(
        bytes: BytesRO,
        required: Boolean
    ): Pair<Int, (Appendable, MagicFormatter) -> Unit>? =
        if (comparison != null) {
            if (bytes.size <= 0) null
            else extractSize(bytes)?.let { size ->
                val slice = bytes.slice(1, size)
                findOffsetMatchUtf8(
                    comparison.operator,
                    comparison.pattern,
                    comparison.compactWhiteSpace,
                    comparison.optionalWhiteSpace,
                    comparison.caseInsensitiveLower,
                    comparison.caseInsensitiveUpper,
                    slice
                )?.let { offset ->
                    offset to { sb: Appendable, formatter: MagicFormatter ->
                        formatter.formatUtf8(sb, slice)
                    }
                }
            }
        } else 0 to { sb: Appendable, formatter: MagicFormatter ->
            formatter.formatUtf8(sb, bytes)
        }

    private fun extractSize(bytes: BytesRO): Int? {
        if (bytes.size < lengthType) return null
        return when (lengthType) {
            1 -> bytes[0].toInt() and 0xFF
            2 -> combineToShort(
                bytes[0], bytes[1]
            ).toInt() and 0xFFFF
            -2 -> combineToShort(
                bytes[1], bytes[0]
            ).toInt() and 0xFFFF
            4 -> combineToInt(
                bytes[0], bytes[1], bytes[2], bytes[3]
            ) and 0x7FFFFFFF
            -4 -> combineToInt(
                bytes[0], bytes[1], bytes[2], bytes[3]
            ) and 0x7FFFFFFF
            else -> error("Invalid lengthType: $lengthType")
        }.let {
            if (lengthIncludesLength) it - lengthType else it
        }
    }
}

fun PStringType(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): PStringType {
    var lengthType = 1
    var lengthIncludesLength = false
    val comparison = parseStringTestStr(typeStr, testStr) {
        when (it) {
            'B' -> lengthType = 1
            'H' -> lengthType = 4
            'h' -> lengthType = 2
            'L' -> lengthType = -4
            'l' -> lengthType = -2
            'J' -> lengthIncludesLength = true
            else -> return@parseStringTestStr false
        }
        true
    }
    return PStringType(comparison, lengthType, lengthIncludesLength)
}
