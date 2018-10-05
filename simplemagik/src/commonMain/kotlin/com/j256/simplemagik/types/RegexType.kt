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
import org.tobi29.arrays.readAsByteArray
import org.tobi29.io.HeapViewByteBE
import org.tobi29.io.MemoryViewReadableStream
import org.tobi29.io.WritableByteStream
import org.tobi29.stdex.maskAt
import org.tobi29.stdex.setAt
import org.tobi29.stdex.utf8ToArray
import org.tobi29.stdex.utf8ToString

data class RegexType(
    val pattern: Regex,
    val maxOffset: Int,
    val updateOffsetStart: Boolean,
    val lines: Boolean
) : MagicMatcher {
    override fun isMatch(
        bytes: BytesRO,
        required: Boolean
    ): Pair<Int, (Appendable, MagicFormatter) -> Unit>? {
        var end = 0
        while (bytes[end] != '\n'.toByte() && end < maxOffset) end++
        if (end > 0) end--

        val str = bytes.slice(0, end).readAsByteArray().utf8ToString()
        val matcher = pattern.find(str)
        if (matcher != null) {
            // TODO: optimize
            val skipped =
                str.substring(0, matcher.range.first).utf8ToArray().size
            return (if (updateOffsetStart) skipped
            else skipped + matcher.value.utf8ToArray().size) to { sb, formatter ->
                formatter.format(sb, matcher.value)
            }
        } else {
            return null
        }
    }
}

fun RegexType(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): RegexType {
    if (testStr == null) throw IllegalArgumentException("Regex without pattern")
    val (_, maxOffset, flagsStr) = splitType(typeStr)
    val options = HashSet<RegexOption>()
    var updateOffsetStart = false
    var lines = false
    if (flagsStr != null) {
        for (ch in flagsStr) {
            when (ch) {
                'c' -> options.add(RegexOption.IGNORE_CASE)
                's' -> updateOffsetStart = true
                'l' -> lines = true
            }
        }
    }
    val regex = unescapeString(testStr).utf8ToString()
    if (lines) lines = false // TODO
    return RegexType(
        regex.toRegex(options),
        maxOffset ?: 8 * 1024,
        updateOffsetStart,
        lines
    )
}

internal fun RegexType.write(stream: WritableByteStream) {
    stream.put(
        0.toByte()
            .setAt(0, updateOffsetStart)
            .setAt(1, lines)
            .setAt(2, RegexOption.IGNORE_CASE in pattern.options)
    )
    stream.putCompactString(pattern.pattern)
    stream.putCompactInt(maxOffset)
}

internal fun readRegexType(stream: MemoryViewReadableStream<HeapViewByteBE>): RegexType {
    val flags = stream.get()
    val options = HashSet<RegexOption>()
    if (flags.maskAt(2)) options.add(RegexOption.IGNORE_CASE)
    val pattern = stream.getCompactString().toRegex(options)
    val maxOffset = stream.getCompactInt()
    val updateOffsetStart = flags.maskAt(0)
    val lines = flags.maskAt(1)
    return RegexType(
        pattern,
        maxOffset,
        updateOffsetStart,
        lines
    )
}
