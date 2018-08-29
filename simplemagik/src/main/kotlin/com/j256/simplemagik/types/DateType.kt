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
import org.tobi29.chrono.*
import org.tobi29.io.HeapViewByteBE
import org.tobi29.io.IOException
import org.tobi29.io.MemoryViewReadableStream
import org.tobi29.io.WritableByteStream
import org.tobi29.stdex.combineToInt
import org.tobi29.stdex.maskAt
import org.tobi29.stdex.setAt
import org.tobi29.stdex.splitToBytes
import org.tobi29.utils.toInt128
import kotlin.experimental.or

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

internal fun DateType.write(stream: WritableByteStream) {
    stream.put(
        0.toByte()
            .setAt(0, comparison != null)
            .setAt(1, local)
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

internal fun readDateType(stream: MemoryViewReadableStream<HeapViewByteBE>): DateType {
    val flags = stream.get()
    val comparisonHas = flags.maskAt(0)
    val local = flags.maskAt(1)
    val andValue = stream.getCompactInt()
    val comparison = if (comparisonHas) {
        val first = stream.getCompactInt()
        val second = TestOperator.of((flags.toInt() ushr 2) and 7)
                ?: throw IOException("Invalid test operator")
        first to second
    } else null
    val endianType = EndianType.of((flags.toInt() ushr 5) and 3)
            ?: throw IOException("Invalid endian type")
    return DateType(
        comparison,
        andValue,
        local,
        endianType
    )
}
