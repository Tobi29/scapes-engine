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
import com.j256.simplemagik.entries.decodeInt
import com.j256.simplemagik.entries.unescapeString
import org.tobi29.arrays.ByteArraySliceRO
import org.tobi29.stdex.copyToArray
import org.tobi29.stdex.copyToString

/**
 * From the magic(5) man page: A string of bytes. The string type specification can be optionally followed by /[Bbc]*.
 * The ``B'' flag compacts whitespace in the target, which must contain at least one whitespace character. If the magic
 * has n consecutive blanks, the target needs at least n consecutive blanks to match. The ``b'' flag treats every blank
 * in the target as an optional blank. Finally the ``c'' flag, specifies case insensitive matching: lower-case
 * characters in the magic match both lower and upper case characters in the target, whereas upper case characters in
 * the magic only match upper-case characters in the target.
 *
 * @author graywatson
 */
open class StringType : MagicMatcher {

    override fun convertTestString(typeStr: String, testStr: String): Any {
        var testStr = testStr
        val matcher = TYPE_PATTERN.matchEntire(typeStr)
        if (matcher == null) {
            // may not be able to get here
            return TestInfo(
                StringOperator.DEFAULT_OPERATOR,
                testStr,
                false,
                false,
                false,
                0
            )
        }
        // max-offset is ignored by the string type
        var maxOffset = 0
        val lengthStr = matcher.groups[1]
        if (lengthStr != null && lengthStr.value.length > 1) {
            try {
                // skip the '/'
                maxOffset = decodeInt(lengthStr.value.substring(1))
            } catch (e: NumberFormatException) {
                // may not be able to get here
                throw IllegalArgumentException("Invalid format for search length: " + testStr)
            }

        }
        var compactWhiteSpace = false
        var optionalWhiteSpace = false
        var caseInsensitive = false
        val flagsStr = matcher.groups[2]
        if (flagsStr != null) {
            // look at flags/modifiers
            for (ch in flagsStr.value.copyToArray()) {
                when (ch) {
                    'B' -> compactWhiteSpace = true
                    'b' -> optionalWhiteSpace = true
                    'c' -> caseInsensitive = true
                    't', 'w', 'W' -> {
                    }
                } // XXX: no idea what these do
            }
        }
        var operator = StringOperator.fromTest(testStr)
        if (operator == null) {
            operator = StringOperator.DEFAULT_OPERATOR
        } else {
            testStr = testStr.substring(1)
        }
        val processedPattern =
            unescapeString(testStr)
        return TestInfo(
            operator,
            processedPattern,
            compactWhiteSpace,
            optionalWhiteSpace,
            caseInsensitive,
            maxOffset
        )
    }

    override fun extractValueFromBytes(
        offset: Int,
        bytes: ByteArraySliceRO,
        required: Boolean
    ): Any? {
        return ""
    }

    override fun isMatch(
        testValue: Any?,
        andValue: Long?,
        unsignedType: Boolean,
        extractedValue: Any?,
        mutableOffset: MagicMatcher.MutableOffset,
        bytes: ByteArraySliceRO
    ): Any? {
        return findOffsetMatch(
            testValue as TestInfo,
            mutableOffset.offset,
            mutableOffset,
            bytes,
            null,
            bytes.size
        )
    }

    override fun renderValue(
        sb: Appendable,
        extractedValue: Any?,
        formatter: MagicFormatter
    ) {
        formatter.format(sb, extractedValue)
    }

    override fun getStartingBytes(testValue: Any?): ByteArray? {
        return if (testValue == null) {
            null
        } else {
            (testValue as TestInfo).startingBytes
        }
    }

    /**
     * Find offset match either in an array of bytes or chars, which ever is not null.
     */
    protected fun findOffsetMatch(
        info: TestInfo,
        startOffset: Int,
        mutableOffset: MagicMatcher.MutableOffset,
        bytes: ByteArraySliceRO?,
        chars: CharArray?,
        maxPos: Int
    ): String? {
        var chars = chars

        var targetPos = startOffset
        var lastMagicCompactWhitespace = false
        for (magicPos in 0 until info.pattern!!.length) {
            val magicCh = info.pattern[magicPos]
            val lastChar = magicPos == info.pattern.length - 1
            // did we reach the end?
            if (targetPos >= maxPos) {
                return null
            }
            var targetCh: Char
            if (bytes == null) {
                targetCh = chars!![targetPos]
            } else {
                targetCh = charFromByte(bytes, targetPos)
            }
            targetPos++

            // if it matches, we can continue
            if (info.operator.doTest(targetCh, magicCh, lastChar)) {
                if (info.compactWhiteSpace) {
                    lastMagicCompactWhitespace = magicCh.isWhitespace()
                }
                continue
            }

            // if it doesn't match, maybe the target is a whitespace
            if ((lastMagicCompactWhitespace || info.optionalWhiteSpace) && targetCh.isWhitespace()) {
                do {
                    if (targetPos >= maxPos) {
                        break
                    }
                    if (bytes == null) {
                        targetCh = chars!![targetPos]
                    } else {
                        targetCh = charFromByte(bytes, targetPos)
                    }
                    targetPos++
                } while (targetCh.isWhitespace())
                // now that we get to the first non-whitespace, it must match
                if (info.operator.doTest(targetCh, magicCh, lastChar)) {
                    if (info.compactWhiteSpace) {
                        lastMagicCompactWhitespace = magicCh.isWhitespace()
                    }
                    continue
                }
                // if it doesn't match, check the case insensitive
            }

            // maybe it doesn't match because of case insensitive handling and magic-char is lowercase
            // TODO: avoid lower case hack
            if (info.caseInsensitive && magicCh == magicCh.toLowerCase()) {
                if (info.operator.doTest(
                        targetCh.toLowerCase(),
                        magicCh,
                        lastChar
                    )) {
                    // matches
                    continue
                }
                // upper-case characters must match
            }

            return null
        }

        if (bytes == null) {
            chars = chars!!.copyOfRange(startOffset, targetPos)
        } else {
            chars = CharArray(targetPos - startOffset)
            for (i in chars.indices) {
                chars[i] = charFromByte(bytes, startOffset + i)
            }
        }
        mutableOffset.offset = targetPos
        return chars.copyToString()
    }

    private fun charFromByte(bytes: ByteArraySliceRO, index: Int): Char {
        return (bytes[index].toInt() and 0xFF).toChar()
    }

    /**
     * Internal holder for test information about strings.
     */
    protected class TestInfo(
        internal val operator: StringOperator,
        internal val pattern: String?,
        internal val compactWhiteSpace: Boolean,
        internal val optionalWhiteSpace: Boolean,
        internal val caseInsensitive: Boolean, // ignored by the string type
        internal val maxOffset: Int
    ) {

        /**
         * Get the bytes that start the pattern from an optimization standpoint.
         */
        val startingBytes: ByteArray?
            get() = if (pattern == null || pattern.length < 4) {
                null
            } else {
                byteArrayOf(
                    pattern[0].toByte(),
                    pattern[1].toByte(),
                    pattern[2].toByte(),
                    pattern[3].toByte()
                )
            }

        override fun toString(): String {
            // TODO: Is this fine?
            return pattern ?: ""
        }
    }
}

private val TYPE_PATTERN = "[^/]+(/\\d+)?(/[BbcwWt]*)?".toRegex()
