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
import com.j256.simplemagik.entries.*
import org.tobi29.arrays.BytesRO
import org.tobi29.io.HeapViewByteBE
import org.tobi29.io.IOException
import org.tobi29.io.MemoryViewReadableStream
import org.tobi29.io.WritableByteStream
import org.tobi29.stdex.combineToInt
import org.tobi29.stdex.maskAt
import org.tobi29.stdex.setAt
import kotlin.experimental.or

data class Id3LengthType(
    val comparison: Pair<Int, TestOperator>?,
    val andValue: Int,
    val unsignedType: Boolean,
    val endianType: EndianType
) : MagicMatcher {
    override fun isMatch(
        bytes: BytesRO,
        required: Boolean
    ): Pair<Int, (Appendable, MagicFormatter) -> Unit>? =
        if (bytes.size >= 4) {
            (combineToInt(
                bytes[0], bytes[1], bytes[2], bytes[3]
            ) and andValue).convert(endianType).parseId3().let { extracted ->
                if (comparison == null || comparison.second.compare(
                        extracted, comparison.first, unsignedType
                    )) 4 to
                        { sb: Appendable, formatter: MagicFormatter ->
                            formatter.format(
                                sb, extracted.toLongSigned(unsignedType)
                            )
                        } else null
            }
        } else null
}

fun Id3LengthType(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean,
    endianType: EndianType
): Id3LengthType = (andValue?.toIntChecked() ?: -1).let { andValue2 ->
    Id3LengthType(decodeComparison(testStr)?.let { (a, b) ->
        a.toIntChecked() to b
    }, andValue2, unsignedType, endianType)
}

fun Id3LengthTypeBE(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): Id3LengthType = Id3LengthType(
    typeStr, testStr, andValue, unsignedType, EndianType.BIG
)

fun Id3LengthTypeLE(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): Id3LengthType = Id3LengthType(
    typeStr, testStr, andValue, unsignedType, EndianType.LITTLE
)

fun Id3LengthTypeME(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): Id3LengthType = Id3LengthType(
    typeStr, testStr, andValue, unsignedType, EndianType.MIDDLE
)

fun Id3LengthTypeNE(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): Id3LengthType = Id3LengthType(
    typeStr, testStr, andValue, unsignedType, EndianType.NATIVE
)

internal fun Int.parseId3(): Int =
    ((this shr 24) and 0x7F shl 21) or
            ((this shr 16) and 0x7F shl 14) or
            ((this shr 8) and 0x7F shl 7) or
            ((this shr 0) and 0x7F shl 0)

internal fun Id3LengthType.write(stream: WritableByteStream) {
    stream.put(
        0.toByte()
            .setAt(0, comparison != null)
            .setAt(1, unsignedType)
            .let {
                if (comparison == null) it
                else it or (comparison.second.id shl 2).toByte()
            } or (endianType.id shl 5).toByte()
    )
    stream.putCompactInt(andValue)
    if (comparison != null) {
        stream.putCompactInt(comparison.first)
    }
}

internal fun readId3LengthType(stream: MemoryViewReadableStream<HeapViewByteBE>): Id3LengthType {
    val flags = stream.get()
    val comparisonHas = flags.maskAt(0)
    val unsignedType = flags.maskAt(1)
    val andValue = stream.getCompactInt()
    val comparison = if (comparisonHas) {
        val first = stream.getCompactInt()
        val second = TestOperator.of((flags.toInt() ushr 2) and 7)
                ?: throw IOException("Invalid test operator")
        first to second
    } else null
    val endianType = EndianType.of((flags.toInt() ushr 5) and 3)
            ?: throw IOException("Invalid endian type")
    return Id3LengthType(
        comparison,
        andValue,
        unsignedType,
        endianType
    )
}
