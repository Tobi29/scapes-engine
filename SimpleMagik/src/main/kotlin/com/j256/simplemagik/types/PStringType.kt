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

import com.j256.simplemagik.entries.MagicMatcher
import com.j256.simplemagik.entries.unescapeString
import org.tobi29.arrays.ByteArraySliceRO
import org.tobi29.stdex.combineToInt
import org.tobi29.stdex.combineToShort
import org.tobi29.stdex.copyToString
import kotlin.math.abs

/**
 * A Pascal-style string where the first byte is interpreted as the an unsigned length. The string is not '\0'
 * terminated.
 *
 * @author graywatson
 */
class PStringType : BaseStringType() {
    override fun convertTestString(typeStr: String, testStr: String): Any {
        val typeSplit = typeStr.indexOf('/')
        val flagsStr = if (typeSplit != -1) {
            typeStr.substring(typeSplit + 1)
        } else null
        var testStr = testStr
        var compactWhiteSpace = false
        var optionalWhiteSpace = false
        var caseInsensitiveLower = false
        var caseInsensitiveUpper = false
        var trim = false
        var lengthType = 1
        var lengthIncludesLength = false
        if (flagsStr != null) {
            // look at flags/modifiers
            for (ch in flagsStr) {
                when (ch) {
                    'W' -> compactWhiteSpace = true
                    'w' -> optionalWhiteSpace = true
                    'c' -> caseInsensitiveLower = true
                    'C' -> caseInsensitiveUpper = true
                    'T' -> trim = true
                    'B' -> lengthType = 1
                    'H' -> lengthType = 4
                    'h' -> lengthType = 2
                    'L' -> lengthType = -4
                    'l' -> lengthType = -2
                    'J' -> lengthIncludesLength = true
                    'b', 't' -> {
                        // Should we implement these?
                    }
                    's' -> {
                        // XXX: no idea what these do
                    }
                    else -> throw IllegalArgumentException("Invalid flag: $ch")
                }
            }
        }
        var operator = StringOperator.fromTest(testStr)
        if (operator == null) {
            operator = StringOperator.DEFAULT_OPERATOR
        } else {
            testStr = testStr.substring(1)
        }
        val processedPattern = unescapeString(testStr)
            .let { if (trim) it.trim() else it }
        return TestInfo(
            operator,
            processedPattern,
            compactWhiteSpace,
            optionalWhiteSpace,
            caseInsensitiveLower,
            caseInsensitiveUpper,
            lengthType,
            lengthIncludesLength
        )
    }

    /**
     * Extracted value is the extracted string using the first byte as the length.
     */
    override fun extractValueFromBytes(
        offset: Int,
        bytes: ByteArraySliceRO,
        required: Boolean
    ): Any? {
        // we don't need to extract the value if all we are doing is matching
        if (!required) {
            return ""
        }
        if (offset >= bytes.size) {
            return null
        }
        // length is from the first byte of the string
        var len = bytes[offset].toInt() and 0xFF
        val left = bytes.size - offset - 1
        if (len > left) {
            len = left
        }
        val chars = CharArray(len)
        for (i in chars.indices) {
            chars[i] = (bytes[offset + 1 + i].toInt() and 0xFF).toChar()
        }
        /*
		 * NOTE: we need to make a new string because it might be returned if we don't match below.
		 */
        return chars.copyToString()
    }

    override fun isMatch(
        testValue: Any?,
        andValue: Long?,
        unsignedType: Boolean,
        extractedValue: Any?,
        mutableOffset: MagicMatcher.MutableOffset,
        bytes: ByteArraySliceRO
    ): Any? {
        testValue as TestInfo

        if (mutableOffset.offset >= bytes.size + abs(testValue.lengthType))
            return null

        // our maximum position is +1 to move past the length byte and then add in the length
        val len = when (testValue.lengthType) {
            1 -> bytes[mutableOffset.offset].toInt() and 0xFF
            2 -> combineToShort(
                bytes[mutableOffset.offset + 0],
                bytes[mutableOffset.offset + 1]
            ).toInt() and 0xFFFF
            -2 -> combineToShort(
                bytes[mutableOffset.offset + 1],
                bytes[mutableOffset.offset + 0]
            ).toInt() and 0xFFFF
            4 -> combineToInt(
                bytes[mutableOffset.offset + 0],
                bytes[mutableOffset.offset + 1],
                bytes[mutableOffset.offset + 2],
                bytes[mutableOffset.offset + 3]
            ) and 0x7FFFFFFF
            -4 -> combineToInt(
                bytes[mutableOffset.offset + 3],
                bytes[mutableOffset.offset + 2],
                bytes[mutableOffset.offset + 1],
                bytes[mutableOffset.offset + 0]
            ) and 0x7FFFFFFF
            else -> error("Invalid length type: ${testValue.lengthType}")
        }
        var maxPos =
            (if (testValue.lengthIncludesLength) 0 else abs(testValue.lengthType)) + len
        if (maxPos > bytes.size) {
            maxPos = bytes.size
        }

        // we start matching past the length byte so the starting offset is +1
        return findOffsetMatch(
            testValue,
            mutableOffset.offset + 1,
            mutableOffset,
            bytes,
            null,
            maxPos
        )
    }

    private class TestInfo(
        operator: StringOperator,
        pattern: String?,
        compactWhiteSpace: Boolean,
        optionalWhiteSpace: Boolean,
        caseInsensitiveLower: Boolean,
        caseInsensitiveUpper: Boolean,
        val lengthType: Int,
        val lengthIncludesLength: Boolean
    ) : BaseStringType.TestInfo(
        operator,
        pattern,
        compactWhiteSpace,
        optionalWhiteSpace,
        caseInsensitiveLower,
        caseInsensitiveUpper
    )
}
