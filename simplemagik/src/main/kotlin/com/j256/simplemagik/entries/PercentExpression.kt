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

import org.tobi29.arrays.BytesRO
import org.tobi29.arrays.readAsByteArray
import org.tobi29.stdex.combineToShort
import org.tobi29.stdex.copyToString
import org.tobi29.stdex.toString
import org.tobi29.stdex.utf8ToString
import org.tobi29.utils.toStringDecimal
import org.tobi29.utils.toStringExponential

/**
 * Representation of our percent expression used by the [MagicFormatter] class.
 *
 * @author graywatson
 */
internal class PercentExpression(expression: String) {
    private val alternativeForm: Boolean
    private val zeroPrefix: Boolean
    private val plusPrefix: Boolean
    private val spacePrefix: Boolean
    private val leftAdjust: Boolean
    private val totalWidth: Int
    private val patternChar: Char
    private val dotPrecision: Int

    override fun toString(): String = buildString {
        append('%')
        if (alternativeForm) append('#')
        if (zeroPrefix) append('0')
        if (plusPrefix) append('+')
        if (spacePrefix) append(' ')
        if (leftAdjust) append('-')
        if (totalWidth != -1) append(totalWidth)
        if (dotPrecision != -1) append(dotPrecision)
        append(patternChar)
    }

    init {
        val matcher = FORMAT_PATTERN.matchEntire(expression)
        if (matcher == null || matcher.groups[6] == null
            || matcher.groupValues[6].length != 1) {
            throw IllegalArgumentException("Invalid format expression: $expression")
        } else {
            val flags = matcher.groupValues[1]
            alternativeForm = flags.contains('#')
            zeroPrefix = flags.contains('0')
            plusPrefix = flags.contains('+')
            spacePrefix = !plusPrefix && flags.contains(' ')
            leftAdjust = flags.contains('-')
            totalWidth = matcher.groupValues[2].toIntOrNull() ?: -1
            dotPrecision = matcher.groupValues[4].toIntOrNull() ?: -1
            // 5 is ignored
            patternChar = matcher.groupValues[6][0]
        }
    }

    fun append(extractedValue: Any, sb: Appendable) {
        // %bcdeEfFgGiosuxX
        when (patternChar) {
            'b', 's' -> {
                val truncateWidth = dotPrecision
                // same as s but interpret character escapes in backslash notation
                var strValue = when (extractedValue) {
                    is BytesRO -> extractedValue.slice(
                        0, extractedValue.size.coerceAtMost(truncateWidth)
                    ).readAsByteArray().utf8ToString()
                    else -> extractedValue.toString()
                }
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
                val decimalPrecision: Int
                val exponentialPrecision: Int
                when (patternChar) {
                    'e', 'E' -> {
                        decimalPrecision = -2
                        exponentialPrecision = dotPrecision
                    }
                    'f', 'F' -> {
                        decimalPrecision = dotPrecision
                        exponentialPrecision = -2
                    }
                    'g', 'G' -> {
                        // will take the shorter of the two
                        decimalPrecision = dotPrecision
                        exponentialPrecision = dotPrecision
                    }
                    else -> error("Impossible")
                }
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

    fun appendUtf8(extractedValue: BytesRO, sb: Appendable) {
        var size = 0
        for (i in 0 until extractedValue.size) {
            if (extractedValue[i] == 0.toByte()) break
            size++
        }
        val strValue = extractedValue.slice(
            0, if (dotPrecision < 0) size else size.coerceAtMost(dotPrecision)
        ).readAsByteArray().utf8ToString()
        append(strValue, sb)
    }

    fun appendUtf16BE(extractedValue: BytesRO, sb: Appendable) {
        var size = 0
        for (i in 0..extractedValue.size - 2 step 2) {
            if (extractedValue[i] == 0.toByte()
                && extractedValue[i + 1] == 0.toByte()) break
            size++
        }
        if (dotPrecision >= 0) size = size.coerceAtMost(dotPrecision)
        val strValue = extractedValue.slice(
            0, size shl 1
        ).let { array ->
            CharArray(size) {
                combineToShort(array[it shl 1], array[(it shl 1) + 1]).toChar()
            }.copyToString()
        }
        append(strValue, sb)
    }

    fun appendUtf16LE(extractedValue: BytesRO, sb: Appendable) {
        var size = 0
        for (i in 0..extractedValue.size - 2 step 2) {
            if (extractedValue[i] == 0.toByte()
                && extractedValue[i + 1] == 0.toByte()) break
            size++
        }
        if (dotPrecision >= 0) size = size.coerceAtMost(dotPrecision)
        val strValue = extractedValue.slice(
            0, size shl 1
        ).let { array ->
            CharArray(size) {
                combineToShort(array[it shl 1], array[(it shl 1) + 1]).toChar()
                combineToShort(array[(it shl 1) + 1], array[it shl 1]).toChar()
            }.copyToString()
        }
        append(strValue, sb)
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
        var sign = sign
        var prefix = prefix
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
                repeat(diff) { sb.append('0') }
            } else {
                repeat(diff) { sb.append(' ') }
            }
        }
        if (sign != null) sb.append(sign)
        if (prefix != null) sb.append(prefix)
        sb.append(value)
        if (leftAdjust) {
            // always space if left-adjust
            repeat(diff) { sb.append(' ') }
        }
    }
}

private val FORMAT_PATTERN =
    "%([0#+ -]*)([0-9]*)(\\.([0-9]+))?([$PATTERN_MODIFIERS]*)([$FINAL_PATTERN_CHARS])".toRegex()
