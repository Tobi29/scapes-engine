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
import org.tobi29.arrays.ByteArraySliceRO
import org.tobi29.arrays.readAsByteArray
import org.tobi29.stdex.copyToArray
import org.tobi29.stdex.utf8ToArray
import org.tobi29.stdex.utf8ToString

/**
 * From the magic(5) man page: A regular expression match in extended POSIX regular expression syntax (like egrep).
 * Regular expressions can take exponential time to process, and their performance is hard to predict, so their use is
 * discouraged. When used in production environments, their performance should be carefully checked. The type
 * specification can be optionally followed by /[c][s]. The 'c' flag makes the match case insensitive, while the 's'
 * flag update the offset to the start offset of the match, rather than the end. The regular expression is tested
 * against line N + 1 onwards, where N is the given offset. Line endings are assumed to be in the machine's native
 * format. ^ and $ match the beginning and end of individual lines, respectively, not beginning and end of file.
 *
 * @author graywatson
 */
class RegexType : MagicMatcher {

    override fun convertTestString(typeStr: String, testStr: String): Any {
        val matcher = TYPE_PATTERN.matchEntire(typeStr)
        val patternInfo = PatternInfo()
        val options = HashSet<RegexOption>()
        if (matcher != null) {
            val flagsStr = matcher.groups[1]
            if (flagsStr != null && flagsStr.value.length > 1) {
                for (ch in flagsStr.value.copyToArray()) {
                    if (ch == 'c') {
                        options.add(RegexOption.IGNORE_CASE)
                    } else if (ch == 's') {
                        patternInfo.updateOffsetStart = true
                    }
                }
            }
        }
        val regexStr = "(.*)(${unescapeString(testStr)}).*"
        patternInfo.pattern = regexStr.toRegex(options)
        return patternInfo
    }

    override fun extractValueFromBytes(
        offset: Int,
        bytes: ByteArraySliceRO,
        required: Boolean
    ): Any {
        return EMPTY
    }

    override fun isMatch(
        testValue: Any?,
        andValue: Long?,
        unsignedType: Boolean,
        extractedValue: Any?,
        mutableOffset: MagicMatcher.MutableOffset,
        bytes: ByteArraySliceRO
    ): Any? {
        // TODO: This seems wrong
        var i = 0
        repeat(mutableOffset.offset) {
            // if eof then no match
            if (i >= bytes.size) return null
            while (bytes[i] != '\n'.toByte()) {
                i++
                // if eof then no match
                if (i >= bytes.size) return null
            }
            i++
        }
        var e = i
        while (bytes[e] != '\n'.toByte()) {
            e++
            // if eof then no match
            if (i >= bytes.size) return null
        }
        val patternInfo = testValue as PatternInfo
        val matcher =
            patternInfo.pattern!!.matchEntire(
                bytes.readAsByteArray().utf8ToString(i, e - i)
            )
        if (matcher != null) {
            // TODO: need to time this
            val skip = matcher.groupValues[1]
            val group = matcher.groupValues[2]
            // TODO: optimize
            mutableOffset.offset = i + skip.utf8ToArray().size +
                    group.utf8ToArray().size
            return group
        } else {
            return null
        }
    }

    override fun renderValue(
        sb: Appendable,
        extractedValue: Any?,
        formatter: MagicFormatter
    ) {
        formatter.format(sb, extractedValue)
    }

    private class PatternInfo {
        internal var updateOffsetStart: Boolean = false
        internal var pattern: Regex? = null
    }
}

private val TYPE_PATTERN = "[^/]+(/[cs]*)?".toRegex()
private const val EMPTY = ""
