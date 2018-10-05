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
import org.tobi29.stdex.combineToShort
import org.tobi29.stdex.maskAt
import org.tobi29.stdex.setAt
import org.tobi29.stdex.utf8ToString
import kotlin.experimental.or

data class StringType(
    val comparison: StringComparison?
) : MagicMatcher {
    override fun isMatch(
        bytes: BytesRO,
        required: Boolean
    ): Pair<Int, (Appendable, MagicFormatter) -> Unit>? =
        (if (comparison == null) 0
        else findOffsetMatchUtf8(
            comparison.operator,
            comparison.pattern,
            comparison.compactWhiteSpace,
            comparison.optionalWhiteSpace,
            comparison.caseInsensitiveLower,
            comparison.caseInsensitiveUpper,
            bytes
        ))?.let { offset ->
            offset to { sb: Appendable, formatter: MagicFormatter ->
                formatter.formatUtf8(sb, bytes)
            }
        }

    override val startingBytes: ByteArray?
        get() = if (comparison == null || comparison.operator != StringOperator.EQUALS) null
        else comparison.pattern
}

fun StringType(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): StringType =
    StringType(parseStringTestStr(typeStr, testStr))

internal fun parseStringTestStr(
    typeStr: String,
    testStr: String?,
    unknownFlag: (Char) -> Boolean = { false }
): StringComparison? {
    val typeSplit = typeStr.indexOf('/')
    val flagsStr = if (typeSplit != -1) {
        typeStr.substring(typeSplit + 1)
    } else null
    return parseStringTestStrFlags(flagsStr, testStr, unknownFlag)
}

internal fun parseStringTestStrFlags(
    flagsStr: String?,
    testStr: String?,
    unknownFlag: (Char) -> Boolean = { false }
): StringComparison? {
    var compactWhiteSpace = false
    var optionalWhiteSpace = false
    var caseInsensitiveLower = false
    var caseInsensitiveUpper = false
    var trim = false
    if (flagsStr != null) {
        // look at flags/modifiers
        for (ch in flagsStr) {
            when (ch) {
                'W' -> compactWhiteSpace = true
                'w' -> optionalWhiteSpace = true
                'c' -> caseInsensitiveLower = true
                'C' -> caseInsensitiveUpper = true
                'T' -> trim = true
                'b', 't' -> {
                    // Should we implement these?
                }
                's' -> {
                    // XXX: no idea what these do
                }
                else -> if (!unknownFlag(ch))
                    throw IllegalArgumentException("Invalid flag: $ch")
            }
        }
    }
    if (testStr == null) return null
    var operator = StringOperator.fromTest(testStr)
    var testStr = testStr
    if (operator == null) {
        operator = StringOperator.DEFAULT_OPERATOR
    } else {
        testStr = testStr.substring(1)
    }
    val processedPattern = unescapeString(testStr)
        .let { if (trim) it.trim() else it }
    return StringComparison(
        processedPattern,
        operator,
        compactWhiteSpace,
        optionalWhiteSpace,
        caseInsensitiveLower,
        caseInsensitiveUpper
    )
}

data class StringComparison(
    val pattern: ByteArray,
    val operator: StringOperator,
    val compactWhiteSpace: Boolean,
    val optionalWhiteSpace: Boolean,
    val caseInsensitiveLower: Boolean,
    val caseInsensitiveUpper: Boolean
) {
    override fun toString(): String =
        "StringComparison(pattern=${pattern.utf8ToString()}, " +
                "operator=$operator, compactWhiteSpace=$compactWhiteSpace, " +
                "optionalWhiteSpace=$optionalWhiteSpace, " +
                "caseInsensitiveLower=$caseInsensitiveLower, " +
                "caseInsensitiveUpper=$caseInsensitiveUpper)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StringComparison) return false

        if (!pattern.contentEquals(other.pattern)) return false
        if (operator != other.operator) return false
        if (compactWhiteSpace != other.compactWhiteSpace) return false
        if (optionalWhiteSpace != other.optionalWhiteSpace) return false
        if (caseInsensitiveLower != other.caseInsensitiveLower) return false
        if (caseInsensitiveUpper != other.caseInsensitiveUpper) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pattern.contentHashCode()
        result = 31 * result + operator.hashCode()
        result = 31 * result + compactWhiteSpace.hashCode()
        result = 31 * result + optionalWhiteSpace.hashCode()
        result = 31 * result + caseInsensitiveLower.hashCode()
        result = 31 * result + caseInsensitiveUpper.hashCode()
        return result
    }
}

data class StringComparison16(
    val pattern: String,
    val operator: StringOperator,
    val compactWhiteSpace: Boolean,
    val optionalWhiteSpace: Boolean,
    val caseInsensitiveLower: Boolean,
    val caseInsensitiveUpper: Boolean
)

internal fun StringComparison.toUtf16(): StringComparison16 =
    StringComparison16(
        pattern.utf8ToString(), operator, compactWhiteSpace, optionalWhiteSpace,
        caseInsensitiveLower, caseInsensitiveUpper
    )

internal fun findOffsetMatchUtf8(
    operator: StringOperator,
    pattern: ByteArray,
    compactWhiteSpace: Boolean,
    optionalWhiteSpace: Boolean,
    caseInsensitiveLower: Boolean,
    caseInsensitiveUpper: Boolean,
    bytes: BytesRO
): Int? = findOffsetMatch(
    operator,
    { pattern[it].toChar() }, pattern.size,
    compactWhiteSpace,
    optionalWhiteSpace,
    caseInsensitiveLower,
    caseInsensitiveUpper,
    { bytes[it].toChar() },
    bytes.size
)

internal fun findOffsetMatchUtf16BE(
    operator: StringOperator,
    pattern: String,
    compactWhiteSpace: Boolean,
    optionalWhiteSpace: Boolean,
    caseInsensitiveLower: Boolean,
    caseInsensitiveUpper: Boolean,
    bytes: BytesRO
): Int? = findOffsetMatch(
    operator,
    { pattern[it] }, pattern.length,
    compactWhiteSpace,
    optionalWhiteSpace,
    caseInsensitiveLower,
    caseInsensitiveUpper,
    { (it shl 1).let { combineToShort(bytes[it], bytes[it + 1]) }.toChar() },
    bytes.size shr 1
)?.let { it shl 1 }

internal fun findOffsetMatchUtf16LE(
    operator: StringOperator,
    pattern: String,
    compactWhiteSpace: Boolean,
    optionalWhiteSpace: Boolean,
    caseInsensitiveLower: Boolean,
    caseInsensitiveUpper: Boolean,
    bytes: BytesRO
): Int? = findOffsetMatch(
    operator,
    { pattern[it] }, pattern.length,
    compactWhiteSpace,
    optionalWhiteSpace,
    caseInsensitiveLower,
    caseInsensitiveUpper,
    { (it shl 1).let { combineToShort(bytes[it + 1], bytes[it]) }.toChar() },
    bytes.size shr 1
)?.let { it shl 1 }

internal inline fun findOffsetMatch(
    operator: StringOperator,
    pattern: (Int) -> Char,
    patternSize: Int,
    compactWhiteSpace: Boolean,
    optionalWhiteSpace: Boolean,
    caseInsensitiveLower: Boolean,
    caseInsensitiveUpper: Boolean,
    input: (Int) -> Char,
    size: Int
): Int? {
    var targetPos = 0
    var lastMagicCompactWhitespace = false
    for (magicPos in 0 until patternSize) {
        val magicCh = pattern(magicPos)
        val lastChar = magicPos == patternSize - 1
        // did we reach the end?
        if (targetPos >= size) {
            return null
        }
        var targetCh = input(targetPos++)

        // if it matches, we can continue
        if (operator.doTest(targetCh, magicCh, lastChar)) {
            if (compactWhiteSpace) {
                lastMagicCompactWhitespace = magicCh.isWhitespace()
            }
            continue
        }

        // if it doesn't match, maybe the target is a whitespace
        if ((lastMagicCompactWhitespace || optionalWhiteSpace) && targetCh.isWhitespace()) {
            do {
                if (targetPos >= size) {
                    break
                }
                targetCh = input(targetPos++)
                targetPos++
            } while (targetCh.isWhitespace())
            // now that we get to the first non-whitespace, it must match
            if (operator.doTest(targetCh, magicCh, lastChar)) {
                if (compactWhiteSpace) {
                    lastMagicCompactWhitespace = magicCh.isWhitespace()
                }
                continue
            }
            // if it doesn't match, check the case insensitive
        }

        if (caseInsensitiveLower) {
            if (operator.doTest(targetCh.toLowerCase(), magicCh, lastChar)) {
                // matches
                continue
            }
        }

        if (caseInsensitiveUpper) {
            if (operator.doTest(targetCh.toUpperCase(), magicCh, lastChar)) {
                // matches
                continue
            }
        }

        return null
    }
    return targetPos
}

internal fun StringType.write(stream: WritableByteStream) {
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
        stream.putCompactByteArray(comparison.pattern)
    }
}

internal fun readStringType(stream: MemoryViewReadableStream<HeapViewByteBE>): StringType {
    val flags = stream.get()
    val comparisonHas = flags.maskAt(0)
    val comparison = if (comparisonHas) {
        val pattern = stream.getCompactByteArray()
        val operator = StringOperator.of((flags.toInt() ushr 5) and 3)
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
    return StringType(comparison)
}
