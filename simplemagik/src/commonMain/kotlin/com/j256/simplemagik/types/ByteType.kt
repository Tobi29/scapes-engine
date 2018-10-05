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
import com.j256.simplemagik.entries.toByteChecked
import org.tobi29.arrays.BytesRO
import org.tobi29.io.HeapViewByteBE
import org.tobi29.io.IOException
import org.tobi29.io.MemoryViewReadableStream
import org.tobi29.io.WritableByteStream
import org.tobi29.stdex.maskAt
import org.tobi29.stdex.setAt
import kotlin.experimental.and
import kotlin.experimental.or

data class ByteType(
    val comparison: Pair<Byte, TestOperator>?,
    val andValue: Byte,
    val unsignedType: Boolean
) : MagicMatcher {
    override fun canStartWithByte(value: Byte): Boolean =
        comparison?.second?.compare(
            value and andValue,
            comparison.first, unsignedType
        ) ?: true

    override fun isMatch(
        bytes: BytesRO,
        required: Boolean
    ): Pair<Int, (Appendable, MagicFormatter) -> Unit>? =
        if (bytes.size >= 1)
            (bytes[0] and andValue).let { extracted ->
                if (comparison == null || comparison.second.compare(
                        extracted, comparison.first, unsignedType
                    )) 1 to
                        { sb: Appendable, formatter: MagicFormatter ->
                            formatter.format(
                                sb, extracted.toShortSigned(unsignedType)
                            )
                        } else null
            } else null
}

@Suppress("UNUSED_PARAMETER")
fun ByteType(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): ByteType = (andValue?.toByteChecked() ?: -1).let { andValue2 ->
    ByteType(decodeComparison(testStr)?.let { (a, b) ->
        a.toByteChecked() to b
    }, andValue2, unsignedType)
}

internal fun Byte.toShortSigned(unsignedType: Boolean): Short =
    toShort().let { if (unsignedType) it and 0xFF else it }

internal fun Short.toIntSigned(unsignedType: Boolean): Int =
    toInt().let { if (unsignedType) it and 0xFFFF else it }

internal fun Int.toLongSigned(unsignedType: Boolean): Long =
    toLong().let { if (unsignedType) it and 0xFFFFFFFF else it }

internal fun ByteType.write(stream: WritableByteStream) {
    stream.put(
        0.toByte()
            .setAt(0, comparison != null)
            .setAt(1, unsignedType)
            .let {
                if (comparison == null) it
                else it or (comparison.second.id shl 2).toByte()
            }
    )
    stream.put(andValue)
    if (comparison != null) {
        stream.put(comparison.first)
    }
}

internal fun readByteType(stream: MemoryViewReadableStream<HeapViewByteBE>): ByteType {
    val flags = stream.get()
    val comparisonHas = flags.maskAt(0)
    val unsignedType = flags.maskAt(1)
    val andValue = stream.get()
    val comparison = if (comparisonHas) {
        val first = stream.get()
        val second = TestOperator.of((flags.toInt() ushr 2) and 7)
                ?: throw IOException("Invalid test operator")
        first to second
    } else null
    return ByteType(
        comparison,
        andValue,
        unsignedType
    )
}
