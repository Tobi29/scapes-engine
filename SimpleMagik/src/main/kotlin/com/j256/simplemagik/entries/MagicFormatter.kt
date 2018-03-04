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

package com.j256.simplemagik.entries

/**
 * Formatter that handles the C %0.2f type formats appropriately. I would have used the [java.util.Formatter] but
 * you can't pre-parse those for some stupid reason. Also, I needed this to be compatible with the printf(3) C formats.
 *
 * @author graywatson
 */
class MagicFormatter
/**
 * This takes a format string, breaks it up into prefix, %-thang, and suffix.
 */
    (formatString: String) {

    private var prefix: String?
    private var percentExpression: PercentExpression?
    private var suffix: String?

    init {
        val matcher = FORMAT_PATTERN.matchEntire(formatString)
        if (matcher == null) {
            // may never get here
            prefix = formatString
            percentExpression = null
            suffix = null
        } else {

            val prefixMatch = matcher.groups[1]
            val percentMatch = matcher.groups[2]
            val suffixMatch = matcher.groups[3]

            if (percentMatch != null && percentMatch.value == "%%") {
                // we go recursive trying to find the first true % pattern
                // TODO: This looks wrong
                val formatter =
                    MagicFormatter(suffixMatch!!.value)
                val sb = StringBuilder()
                if (prefixMatch != null) {
                    sb.append(prefixMatch)
                }
                sb.append('%')
                formatter.prefix?.let { sb.append(it) }
                prefix = sb.toString()
                percentExpression = formatter.percentExpression
                suffix = formatter.suffix
            } else {

                if (prefixMatch == null || prefixMatch.value.isEmpty()) {
                    prefix = null
                } else {
                    prefix = prefixMatch.value
                }
                if (percentMatch == null || percentMatch.value.isEmpty()) {
                    percentExpression = null
                } else {
                    percentExpression =
                            PercentExpression(
                                percentMatch.value
                            )
                }
                if (suffixMatch == null || suffixMatch.value.length == 0) {
                    suffix = null
                } else {
                    suffix = suffixMatch.value.replace("%%", "%")
                }
            }
        }
    }

    /**
     * Formats the extracted value assigned and returns the associated string
     */
    fun format(sb: Appendable, value: Any?) {
        prefix?.let { sb.append(it) }
        if (value != null) percentExpression?.append(value, sb)
        suffix?.let { sb.append(it) }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        prefix?.let { sb.append(it) }
        percentExpression?.let { sb.append(it) }
        suffix?.let { sb.append(it) }
        return sb.toString()
    }
}

internal const val FINAL_PATTERN_CHARS = "%bcdeEfFgGiosuxX"
internal const val PATTERN_MODIFIERS = "lqh"

// NOTE: the backspace is taken care of by checking the format string prefix above
private val FORMAT_PATTERN =
    "([^%]*)(%[-+0-9# .$PATTERN_MODIFIERS]*[$FINAL_PATTERN_CHARS])?(.*)".toRegex()
