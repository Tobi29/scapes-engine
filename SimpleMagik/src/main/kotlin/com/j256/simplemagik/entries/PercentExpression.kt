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

import org.tobi29.stdex.toString
import org.tobi29.utils.toStringDecimal
import org.tobi29.utils.toStringExponential

/**
 * Representation of our percent expression used by the [MagicFormatter] class.
 *
 * @author graywatson
 */
class PercentExpression internal constructor(private val expression: String) {
    private val justValue: Boolean
    private val alternativeForm: Boolean
    private val zeroPrefix: Boolean
    private val plusPrefix: Boolean
    private val spacePrefix: Boolean
    private val leftAdjust: Boolean
    private val totalWidth: Int
    private val truncateWidth: Int
    private val patternChar: Char
    private val decimalPrecision: Int
    private val exponentialPrecision: Int
    /*private val decimalFormat: DecimalFormat?
    /** if we need to choose the shorter of two formats  */
    private val altDecimalFormat: DecimalFormat?*/

    init {
        val matcher = FORMAT_PATTERN.matchEntire(expression)
        if (matcher == null || matcher.groups[6] == null
            || matcher.groupValues[6].length != 1) {
            // may never get here but let's be careful
            this.justValue = true
            this.alternativeForm = false
            this.patternChar = 0.toChar()
            this.zeroPrefix = false
            this.plusPrefix = false
            this.spacePrefix = false
            this.leftAdjust = false
            this.totalWidth = -1
            this.truncateWidth = -1
            this.decimalPrecision = -2
            this.exponentialPrecision = -2
        } else {
            this.justValue = false

            val flags = matcher.groupValues[1]
            this.alternativeForm =
                    readFlag(
                        flags,
                        '#'
                    )
            this.zeroPrefix =
                    readFlag(
                        flags,
                        '0'
                    )
            this.plusPrefix =
                    readFlag(
                        flags,
                        '+'
                    )
            if (this.plusPrefix) {
                // + overrides space
                this.spacePrefix = false
            } else {
                this.spacePrefix =
                        readFlag(
                            flags,
                            ' '
                        )
            }
            this.leftAdjust =
                    readFlag(
                        flags,
                        '-'
                    )
            this.totalWidth =
                    readPrecision(
                        matcher.groupValues[2],
                        -1
                    )
            val dotPrecision =
                readPrecision(
                    matcher.groupValues[4],
                    -1
                )
            // 5 is ignored
            this.patternChar = matcher.groupValues[6][0]
            when (this.patternChar) {
                'e', 'E' -> {
                    this.decimalPrecision = -2
                    this.exponentialPrecision = dotPrecision
                }
                'f', 'F' -> {
                    this.decimalPrecision = dotPrecision
                    this.exponentialPrecision = -2
                }
                'g', 'G' -> {
                    // will take the shorter of the two
                    this.decimalPrecision = dotPrecision
                    this.exponentialPrecision = dotPrecision
                }
                else -> {
                    this.decimalPrecision = -2
                    this.exponentialPrecision = -2
                }
            }
            if (patternChar == 's' || patternChar == 'b') {
                this.truncateWidth = dotPrecision
            } else {
                this.truncateWidth = -1
            }
        }
    }

    fun append(extractedValue: Any, sb: Appendable) {
        if (justValue) {
            // may never get here
            sb.append(extractedValue.toString())
            return
        }

        // %bcdeEfFgGiosuxX
        when (patternChar) {
            'b', 's' -> {
                // same as s but interpret character escapes in backslash notation
                var strValue = extractedValue.toString()
                if (truncateWidth >= 0 && strValue.length > truncateWidth) {
                    strValue = strValue.substring(0, truncateWidth)
                }
                appendValue(sb, null, null, strValue, false)
                return
            }
            'c' -> {
                // character
                val strValue: String
                if (extractedValue is Char) {
                    strValue = extractedValue.toString()
                } else if (extractedValue is Number) {
                    strValue = extractedValue.toShort().toChar().toString()
                } else if (extractedValue is String) {
                    if (extractedValue.length == 0) {
                        strValue = ""
                    } else {
                        strValue = extractedValue.substring(0, 1)
                    }
                } else {
                    strValue = "?"
                }
                appendValue(sb, null, null, strValue, false)
                return
            }
            'd', 'i', 'u' -> if (extractedValue is Number) {
                var value = extractedValue.toLong()
                var sign: String? = null
                if (value >= 0) {
                    if (plusPrefix) {
                        sign = "+"
                    } else if (spacePrefix) {
                        sign = " "
                    }
                } else {
                    sign = "-"
                    value = -value
                }
                val strValue = value.toString()
                appendValue(sb, sign, null, strValue, true)
                return
            }
            'e', 'E', 'f', 'F', 'g', 'G' -> if (extractedValue is Number) {
                var value = extractedValue.toDouble()
                if (value.isFinite()) {
                    sb.append("inf")
                    return
                } else if (value.isNaN()) {
                    sb.append("nan")
                    return
                }
                var sign: String? = null
                if (value >= 0) {
                    if (plusPrefix) {
                        sign = "+"
                    } else if (spacePrefix) {
                        sign = " "
                    }
                } else {
                    // XXX: is this right? setting the value to negative and a sign? need to test this.
                    sign = "-"
                    value = -value
                }
                val decimalStr = if (decimalPrecision >= -1)
                    if (decimalPrecision == -1) value.toStringDecimal()
                    else value.toStringDecimal(decimalPrecision)
                else null
                val exponentialStr = if (exponentialPrecision >= -1)
                    if (exponentialPrecision == -1) value.toStringExponential()
                    else value.toStringExponential(exponentialPrecision)
                else null
                val strValue = if (decimalStr != null && exponentialStr != null)
                    if (decimalStr.length <= exponentialStr.length)
                        decimalStr else exponentialStr
                else decimalStr ?: exponentialStr
                ?: throw IllegalStateException("No formatting for value")
                appendValue(sb, sign, null, strValue, true)
                return
            }
        // case 'i' : same as d above
            'o' ->
                // octal
                if (extractedValue is Number) {
                    var value = extractedValue.toLong()
                    var sign: String? = null
                    if (value < 0) {
                        sign = "-"
                        value = -value
                    }
                    var prefix: String? = null
                    if (alternativeForm) {
                        prefix = "0"
                    }
                    val strValue = value.toString(8)
                    appendValue(sb, sign, prefix, strValue, true)
                    return
                }
        // case 's' : same as b above
        // case 'u' : same as d above
            'x' -> if (extractedValue is Number) {
                appendHex(sb, false, extractedValue)
                return
            }
            'X' -> if (extractedValue is Number) {
                appendHex(sb, true, extractedValue)
                return
            }
            else -> {
            }
        }

        // oh well, just dump it out
        sb.append(extractedValue.toString())
    }

    override fun toString(): String {
        return expression
    }

    private fun appendHex(
        sb: Appendable,
        upper: Boolean,
        extractedValue: Any
    ) {
        var value = (extractedValue as Number).toLong()
        var sign: String? = null
        if (value < 0) {
            sign = "-"
            value = -value
        }
        var prefix: String? = null
        if (alternativeForm) {
            if (upper) {
                prefix = "0X"
            } else {
                prefix = "0x"
            }
        }
        var strValue = value.toString(16)
        if (upper) {
            strValue = strValue.toUpperCase()
        }
        appendValue(sb, sign, prefix, strValue, true)
    }

    private fun appendValue(
        sb: Appendable,
        sign: String?,
        prefix: String?,
        value: String,
        isNumber: Boolean
    ) {
        var sign = sign
        var prefix = prefix
        var len = 0
        if (sign != null) {
            len += sign.length
        }
        if (prefix != null) {
            len += prefix.length
        }
        len += value.length
        var diff = totalWidth - len
        if (diff < 0) {
            diff = 0
        }
        if (!leftAdjust) {
            if (isNumber && zeroPrefix) {
                if (sign != null) {
                    sb.append(sign)
                    sign = null
                }
                if (prefix != null) {
                    // may never get here
                    sb.append(prefix)
                    prefix = null
                }
                appendChars(
                    sb,
                    ZERO_CHARS, diff
                )
            } else {
                appendChars(
                    sb,
                    SPACE_CHARS, diff
                )
            }
        }
        if (sign != null) {
            sb.append(sign)
        }
        if (prefix != null) {
            sb.append(prefix)
        }
        sb.append(value)
        if (leftAdjust) {
            // always space if left-adjust
            appendChars(
                sb,
                SPACE_CHARS, diff
            )
        }
    }

    private fun appendChars(sb: Appendable, indentChars: String, diff: Int) {
        var diff = diff
        while (true) {
            if (diff > indentChars.length) {
                sb.append(indentChars)
                diff -= indentChars.length
            } else {
                sb.append(indentChars, 0, diff)
                break
            }
        }
    }

    /*/**
     * -d.ddd+-dd style, if no precision then 6 digits, 'inf', nan', if 0 precision then ""
     */
    private fun decimalFormat(fractionPrecision: Int): DecimalFormat {
        val format: DecimalFormat
        if (fractionPrecision == 0) {
            format = DecimalFormat("###0")
        } else if (fractionPrecision > 0) {
            val formatSb = MutableString()
            formatSb.append("###0.")
            appendChars(formatSb, ZERO_CHARS, fractionPrecision)
            format = DecimalFormat(formatSb.toString())
        } else {
            format = DecimalFormat("###0.###")
        }
        return format
    }

    private fun scientificFormat(fractionPrecision: Int): DecimalFormat {
        val format: DecimalFormat
        if (fractionPrecision == 0) {
            format = DecimalFormat("0E0")
        } else if (fractionPrecision > 0) {
            val formatSb = MutableString()
            formatSb.append("0.")
            appendChars(formatSb, ZERO_CHARS, fractionPrecision)
            formatSb.append("E0")
            format = DecimalFormat(formatSb.toString())
        } else {
            format = DecimalFormat("0.###E0")
        }
        return format
    }*/

    companion object {

        private val ZERO_CHARS =
            "00000000000000000000000000000000000000000000000000000000000000000000000"
        private val SPACE_CHARS =
            "                                                                      "

        private val FORMAT_PATTERN =
            "%([0#+ -]*)([0-9]*)(\\.([0-9]+))?([${MagicFormatter.PATTERN_MODIFIERS}]*)([${MagicFormatter.FINAL_PATTERN_CHARS}])".toRegex()

        private fun readPrecision(string: String?, defaultVal: Int): Int {
            if (string == null || string.length == 0) {
                return defaultVal
            }
            return string.toIntOrNull() ?: defaultVal
        }

        private fun readFlag(flags: String?, flagChar: Char): Boolean {
            return if (flags != null && flags.indexOf(flagChar) >= 0) {
                true
            } else {
                false
            }
        }
    }
}
