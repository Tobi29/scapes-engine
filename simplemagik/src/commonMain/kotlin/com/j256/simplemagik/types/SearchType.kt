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

import com.j256.simplemagik.entries.*
import org.tobi29.arrays.BytesRO
import org.tobi29.io.HeapViewByteBE
import org.tobi29.io.IOException
import org.tobi29.io.MemoryViewReadableStream
import org.tobi29.io.WritableByteStream
import org.tobi29.stdex.maskAt
import org.tobi29.stdex.setAt
import kotlin.experimental.or

data class SearchType(
    val comparison: StringComparison,
    val maxOffset: Int
) : MagicMatcher {
    override fun isMatch(
        bytes: BytesRO,
        required: Boolean
    ): Pair<Int, (Appendable, MagicFormatter) -> Unit>? {
        val end = maxOffset.coerceAtMost(bytes.size)
        for (offset in 0 until end) {
            val slice = bytes.slice(offset)
            findOffsetMatchUtf8(
                comparison.operator,
                comparison.pattern,
                comparison.compactWhiteSpace,
                comparison.optionalWhiteSpace,
                comparison.caseInsensitiveLower,
                comparison.caseInsensitiveUpper,
                slice
            )?.let { offset ->
                return offset to { sb, formatter ->
                    formatter.formatUtf8(sb, slice)
                }
            }
        }
        return null
    }
}

fun SearchType(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): SearchType {
    val (_, maxOffset, flagsStr) = splitType(typeStr)
    val comparison = parseStringTestStrFlags(flagsStr, testStr)
            ?: throw IllegalArgumentException("No comparison for search")
    return SearchType(comparison, maxOffset ?: 8 * 1024)
}

internal fun splitType(typeStr: String): Triple<String, Int?, String?> {
    val split = typeStr.split('/', limit = 3)
    var maxOffset: Int? = null
    var flags: String? = null
    if (split.size >= 2) {
        try {
            maxOffset = decodeInt(split[1])
        } catch (e: NumberFormatException) {
            flags = split[1]
        }
    }
    if (split.size >= 3) {
        try {
            maxOffset = decodeInt(split[2])
        } catch (e: NumberFormatException) {
            flags = split[2]
        }

    }
    return Triple(split[0], maxOffset, flags)
}

internal fun SearchType.write(stream: WritableByteStream) {
    stream.put(
        0.toByte()
            // Keep pattern for better compression
            .let {
                it.setAt(1, comparison.compactWhiteSpace)
                    .setAt(2, comparison.optionalWhiteSpace)
                    .setAt(3, comparison.caseInsensitiveLower)
                    .setAt(4, comparison.caseInsensitiveUpper) or
                        (comparison.operator.id shl 5).toByte()
            }
    )
    stream.putCompactString(comparison.pattern)
    stream.putCompactInt(maxOffset)
}

internal fun readSearchType(stream: MemoryViewReadableStream<HeapViewByteBE>): SearchType {
    val flags = stream.get()
    val comparison = run {
        val pattern = stream.getCompactString()
        val operator = StringOperator.of((flags.toInt() ushr 2) and 7)
                ?: throw IOException("Invalid string operator")
        val compactWhiteSpace = flags.maskAt(1)
        val optionalWhiteSpace = flags.maskAt(2)
        val caseInsensitiveLower = flags.maskAt(3)
        val caseInsensitiveUpper = flags.maskAt(4)
        StringComparison(
            pattern,
            operator,
            compactWhiteSpace,
            optionalWhiteSpace,
            caseInsensitiveLower,
            caseInsensitiveUpper
        )
    }
    val maxOffset = stream.getCompactInt()
    return SearchType(
        comparison,
        maxOffset
    )
}
