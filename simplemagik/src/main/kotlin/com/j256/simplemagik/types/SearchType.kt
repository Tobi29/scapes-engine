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
import com.j256.simplemagik.entries.decodeInt
import com.j256.simplemagik.entries.unescapeString
import org.tobi29.arrays.BytesRO

/**
 * From the magic(5) man page: A literal string search starting at the given line offset. The same modifier flags can be
 * used as for string patterns. The modifier flags (if any) must be followed by /number range, that is, the number of
 * positions at which the match will be attempted, starting from the start offset. This is suitable for searching larger
 * binary expressions with variable offsets, using \ escapes for special characters. The offset works as for regex.
 *
 *
 *
 * **NOTE:** in our experience, the /number is _before_ the flags in 99% of the lines so that is how we implemented
 * it.
 *
 *
 * @author graywatson
 */
class SearchType : BaseStringType() {
    override fun convertTestString(typeStr: String, testStr: String): Any {
        val typeSplit = typeStr.indexOf('/')
        if (typeSplit == -1) throw IllegalArgumentException("Search test is missing max offset")
        val maxOffsetSplit =
            if (typeSplit == typeStr.lastIndex) -1
            else typeStr.indexOf('/', typeSplit + 1)
        val maxOffsetStr = typeStr.substring(
            typeSplit + 1,
            if (maxOffsetSplit == -1) typeStr.length else maxOffsetSplit
        )
        val flagsStr =
            if (maxOffsetSplit == -1) null
            else typeStr.substring(maxOffsetSplit + 1)
        var testStr = testStr
        // max-offset is ignored by the string type
        val maxOffset = decodeInt(maxOffsetStr)
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
            caseInsensitiveUpper,
            maxOffset
        )
    }

    override fun isMatch(
        testValue: Any?,
        andValue: Long?,
        unsignedType: Boolean,
        extractedValue: Any?,
        mutableOffset: MagicMatcher.MutableOffset,
        bytes: BytesRO
    ): Any? {
        val info = testValue as TestInfo
        val end =
            (mutableOffset.offset + info.maxOffset).coerceAtMost(bytes.size)
        for (offset in mutableOffset.offset until end) {
            val match = findOffsetMatch(
                info, offset, mutableOffset, bytes, null, bytes.size
            )
            if (match != null) return match
        }
        return null
    }

    private class TestInfo(
        operator: StringOperator,
        pattern: String?,
        compactWhiteSpace: Boolean,
        optionalWhiteSpace: Boolean,
        caseInsensitiveLower: Boolean,
        caseInsensitiveUpper: Boolean,
        val maxOffset: Int
    ) : BaseStringType.TestInfo(
        operator,
        pattern,
        compactWhiteSpace,
        optionalWhiteSpace,
        caseInsensitiveLower,
        caseInsensitiveUpper
    )
}
