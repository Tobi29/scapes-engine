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
import com.j256.simplemagik.entries.getCompactString
import com.j256.simplemagik.entries.putCompactString
import org.tobi29.arrays.BytesRO
import org.tobi29.io.HeapViewByteBE
import org.tobi29.io.IOException
import org.tobi29.io.MemoryViewReadableStream
import org.tobi29.io.WritableByteStream
import org.tobi29.stdex.maskAt
import org.tobi29.stdex.setAt
import kotlin.experimental.or

data class LittleEndianString16Type(
    val comparison: StringComparison?
) : MagicMatcher {
    override fun isMatch(
        bytes: BytesRO,
        required: Boolean
    ): Pair<Int, (Appendable, MagicFormatter) -> Unit>? =
        (if (comparison == null) 0
        else findOffsetMatchUtf16LE(
            comparison.operator,
            comparison.pattern,
            comparison.compactWhiteSpace,
            comparison.optionalWhiteSpace,
            comparison.caseInsensitiveLower,
            comparison.caseInsensitiveUpper,
            bytes
        ))?.let { offset ->
            offset to { sb: Appendable, formatter: MagicFormatter ->
                formatter.formatUtf16LE(sb, bytes)
            }
        }
}

fun LittleEndianString16Type(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): LittleEndianString16Type =
    LittleEndianString16Type(parseStringTestStr(typeStr, testStr))

internal fun LittleEndianString16Type.write(stream: WritableByteStream) {
    stream.put(
        0.toByte()
            .setAt(0, comparison != null)
            .let {
                if (comparison == null) it
                else it.setAt(1, comparison.compactWhiteSpace)
                    .setAt(2, comparison.optionalWhiteSpace)
                    .setAt(3, comparison.caseInsensitiveLower)
                    .setAt(4, comparison.caseInsensitiveUpper) or
                        (comparison.operator.id shl 5).toByte()
            }
    )
    if (comparison != null) {
        stream.putCompactString(comparison.pattern)
    }
}

internal fun readLittleEndianString16Type(stream: MemoryViewReadableStream<HeapViewByteBE>): LittleEndianString16Type {
    val flags = stream.get()
    val comparisonHas = flags.maskAt(0)
    val comparison = if (comparisonHas) {
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
    } else null
    return LittleEndianString16Type(
        comparison
    )
}
