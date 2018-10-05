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
import com.j256.simplemagik.entries.getCompactLong
import com.j256.simplemagik.entries.putCompactLong
import org.tobi29.arrays.BytesRO
import org.tobi29.chrono.*
import org.tobi29.io.HeapViewByteBE
import org.tobi29.io.IOException
import org.tobi29.io.MemoryViewReadableStream
import org.tobi29.io.WritableByteStream
import org.tobi29.stdex.combineToLong
import org.tobi29.stdex.maskAt
import org.tobi29.stdex.setAt
import org.tobi29.stdex.splitToBytes
import org.tobi29.utils.toInt128
import kotlin.experimental.or

data class LongDateType(
    val comparison: Pair<Long, TestOperator>?,
    val andValue: Long,
    val local: Boolean,
    val endianType: EndianType
) : MagicMatcher {
    override fun canStartWithByte(value: Byte): Boolean =
        comparison?.second?.isBitwise != true
                || comparison.second.compare(
            value, (comparison.first.convert(endianType) and andValue)
                .splitToBytes { b, _, _, _, _, _, _, _ -> b }
        )

    override fun isMatch(
        bytes: BytesRO,
        required: Boolean
    ): Pair<Int, (Appendable, MagicFormatter) -> Unit>? =
        if (bytes.size >= 8)
            (combineToLong(
                bytes[0], bytes[1], bytes[2], bytes[3],
                bytes[4], bytes[5], bytes[6], bytes[7]
            ).convert(endianType) and andValue).let { extracted ->
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

@Suppress("UNUSED_PARAMETER")
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

internal fun LongDateType.write(stream: WritableByteStream) {
    stream.put(
        0.toByte()
            .setAt(0, comparison != null)
            .setAt(1, local)
            .let {
                if (comparison == null) it
                else it or (comparison.second.id shl 2).toByte()
            } or (endianType.id shl 5).toByte()
    )
    stream.putCompactLong(andValue)
    if (comparison != null) {
        stream.putCompactLong(comparison.first)
    }
}

internal fun readLongDateType(stream: MemoryViewReadableStream<HeapViewByteBE>): LongDateType {
    val flags = stream.get()
    val comparisonHas = flags.maskAt(0)
    val local = flags.maskAt(1)
    val andValue = stream.getCompactLong()
    val comparison = if (comparisonHas) {
        val first = stream.getCompactLong()
        val second = TestOperator.of((flags.toInt() ushr 2) and 7)
                ?: throw IOException("Invalid test operator")
        first to second
    } else null
    val endianType = EndianType.of((flags.toInt() ushr 5) and 3)
            ?: throw IOException("Invalid endian type")
    return LongDateType(
        comparison,
        andValue,
        local,
        endianType
    )
}
