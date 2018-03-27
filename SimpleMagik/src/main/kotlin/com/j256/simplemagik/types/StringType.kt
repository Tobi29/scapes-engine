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
import com.j256.simplemagik.entries.unescapeString
import org.tobi29.arrays.BytesRO
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
open class StringType : BaseStringType() {
    override fun getStartingBytes(testValue: Any?): ByteArray? {
        return if (testValue == null) {
            null
        } else {
            (testValue as TestInfo).startingBytes
        }
    }

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
            caseInsensitiveUpper
        )
    }
}

abstract class BaseStringType : MagicMatcher {
    override fun extractValueFromBytes(
        offset: Int,
        bytes: BytesRO,
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
        bytes: BytesRO
    ): Any? {
        return findOffsetMatch(
            testValue,
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

    protected fun findOffsetMatch(
        info: Any?,
        startOffset: Int,
        mutableOffset: MagicMatcher.MutableOffset,
        bytes: BytesRO?,
        chars: CharArray?,
        maxPos: Int
    ): String? {
        info as TestInfo
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

            if (info.caseInsensitiveLower) {
                if (info.operator.doTest(
                        targetCh.toLowerCase(),
                        magicCh,
                        lastChar
                    )) {
                    // matches
                    continue
                }
            }

            if (info.caseInsensitiveUpper) {
                if (info.operator.doTest(
                        targetCh.toUpperCase(),
                        magicCh,
                        lastChar
                    )) {
                    // matches
                    continue
                }
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

    private fun charFromByte(bytes: BytesRO, index: Int): Char {
        return (bytes[index].toInt() and 0xFF).toChar()
    }

    protected open class TestInfo(
        val operator: StringOperator,
        val pattern: String?,
        val compactWhiteSpace: Boolean,
        val optionalWhiteSpace: Boolean,
        val caseInsensitiveLower: Boolean,
        val caseInsensitiveUpper: Boolean
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

