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

import com.j256.simplemagik.ErrorCallBack
import com.j256.simplemagik.endian.EndianConverter
import com.j256.simplemagik.endian.EndianType
import com.j256.simplemagik.entries.MagicEntry.OffsetInfo
import com.j256.simplemagik.types.TestOperator
import com.j256.simplemagik.types.UnknownType

private const val UNKNOWN_NAME = "unknown"
// special lines, others are put into the extensionMap
private const val MIME_TYPE_LINE = "!:mime"
private const val OPTIONAL_LINE = "!:optional"

private val OFFSET_PATTERN =
    """\((&)?([0-9a-fA-Fx]+)\.?([bsilBSILm]?)([*+\-]?)([0-9a-fA-Fx]*)\)""".toRegex()

/**
 * Parse a line from the magic configuration file into an entry.
 */
internal fun parseMagicLine(
    previous: MagicEntry?,
    line: String,
    errorCallBack: ErrorCallBack?,
    parts: Array<String>
): MagicEntry? {
    if (line.startsWith("!:")) {
        if (previous != null) {
            // we ignore it if there is no previous entry to add it to
            handleSpecial(previous, line, errorCallBack)
        }
        return null
    }

    // 0[ ]string[ ]%PDF-[ ]PDF document
    // !:mime[ ]application/pdf
    // >5[ ]byte[ ]x[ ]\b, version %c
    // >7[ ]byte[ ]x[ ]\b.%c

    // unfortunately, we cannot use split or even regex since the whitespace is not reliable (grumble)
    val l = splitLine(line, errorCallBack, parts)
    if (l == -1) return null

    // level and offset
    val level: Int
    var sindex = parts[0].lastIndexOf('>')
    var offsetString: String
    if (sindex < 0) {
        level = 0
        offsetString = parts[0]
    } else {
        level = sindex + 1
        offsetString = parts[0].substring(sindex + 1, parts[0].length)
    }

    val offset: Int
    val offsetInfo: OffsetInfo?
    if (offsetString.isEmpty()) {
        errorCallBack?.invoke(
            line, "invalid offset number: $offsetString", null
        )
        return null
    }
    var addOffset = false
    if (offsetString[0] == '&') {
        addOffset = true
        offsetString = offsetString.substring(1)
    }
    if (offsetString[0] == '(') {
        offset = -1
        offsetInfo = parseOffset(offsetString, line, errorCallBack)
        if (offsetInfo == null) {
            return null
        }
    } else {
        try {
            offset = decodeInt(offsetString)
            offsetInfo = null
        } catch (e: NumberFormatException) {
            errorCallBack?.invoke(
                line, "invalid offset number: $offsetString", e
            )
            return null
        }

    }

    // process the AND (&) part of the type
    var typeStr = parts[1]
    sindex = typeStr.indexOf('&')
    // we use long because of overlaps
    var andValue: Long? = null
    if (sindex >= 0) {
        val andStr = typeStr.substring(sindex + 1)
        try {
            andValue = decodeLong(andStr)
        } catch (e: NumberFormatException) {
            errorCallBack?.invoke(
                line, "invalid type AND-number: $andStr", e
            )
            return null
        }

        typeStr = typeStr.substring(0, sindex)
    }
    if (typeStr.isEmpty()) {
        errorCallBack?.invoke(line, "blank type string", null)
        return null
    }

    // process the type string
    var unsignedType = false
    var matcher = matcherfromString(typeStr)
    if (matcher == null) {
        if (typeStr[0] == 'u') {
            matcher = matcherfromString(typeStr.substring(1))
            unsignedType = true
        } else {
            val index = typeStr.indexOf('/')
            if (index > 0) {
                matcher = matcherfromString(
                    typeStr.substring(0, index)
                )
            }
        }
        if (matcher == null) {
            errorCallBack?.invoke(
                line, "unknown magic type string: $typeStr", null
            )
            matcher = UnknownType
        }
    }

    // process the test-string
    val testValue: Any?
    val testStr = parts[2]
    if (testStr == "x") {
        testValue = null
    } else {
        try {
            testValue = matcher.convertTestString(typeStr, testStr)
        } catch (e: Exception) {
            errorCallBack?.invoke(
                line, "could not convert magic test string: $testStr", e
            )
            return null
        }

    }

    val formatter: Lazy<MagicFormatter>?
    val name: String
    var formatSpacePrefix = true
    var clearFormat = false
    if (l == 3) {
        formatter = null
        name = UNKNOWN_NAME
    } else {
        var format = parts[3]
        // a starting \\b or ^H means don't prepend a space when chaining content details
        if (format.startsWith("\\b")) {
            format = format.substring(2)
            formatSpacePrefix = false
        } else if (format.startsWith("\u0008")) {
            // NOTE: sometimes the \b is expressed as a ^H character (grumble)
            format = format.substring(1)
            formatSpacePrefix = false
        } else if (format.startsWith("\\r")) {
            format = format.substring(2)
            clearFormat = true
        }
        formatter = lazy { MagicFormatter(format) }

        val trimmedFormat = format.trim { it <= ' ' }
        var spaceIndex = trimmedFormat.indexOf(' ')
        if (spaceIndex < 0) {
            spaceIndex = trimmedFormat.indexOf('\t')
        }
        if (spaceIndex > 0) {
            name = trimmedFormat.substring(0, spaceIndex)
        } else if (trimmedFormat.isEmpty()) {
            name = UNKNOWN_NAME
        } else {
            name = trimmedFormat
        }
    }
    return MagicEntry(
        name,
        level,
        addOffset,
        offset,
        offsetInfo,
        matcher,
        andValue,
        unsignedType,
        testValue,
        formatSpacePrefix,
        clearFormat,
        formatter
    )
}

private fun splitLine(
    line: String,
    errorCallBack: ErrorCallBack?,
    output: Array<String>
): Int {
    // skip opening whitespace if any
    var startPos = findNonWhitespace(line, 0)
    if (startPos < 0) {
        return -1
    }

    // find the level info
    var endPos = findWhitespaceWithoutEscape(line, startPos)
    if (endPos < 0) {
        errorCallBack?.invoke(
            line,
            "invalid number of whitespace separated fields, must be >= 4",
            null
        )
        return -1
    }
    val levelStr = line.substring(startPos, endPos)

    // skip whitespace
    startPos = findNonWhitespace(line, endPos + 1)
    if (startPos < 0) {
        errorCallBack?.invoke(
            line,
            "invalid number of whitespace separated fields, must be >= 4",
            null
        )
        return -1
    }
    // find the type string
    endPos = findWhitespaceWithoutEscape(line, startPos)
    if (endPos < 0) {
        errorCallBack?.invoke(
            line,
            "invalid number of whitespace separated fields, must be >= 4",
            null
        )
        return -1
    }
    val typeStr = line.substring(startPos, endPos)

    // skip whitespace
    startPos = findNonWhitespace(line, endPos + 1)
    if (startPos < 0) {
        errorCallBack?.invoke(
            line,
            "invalid number of whitespace separated fields, must be >= 4",
            null
        )
        return -1
    }
    // find the test string
    endPos = findWhitespaceWithoutEscape(line, startPos)
    if (endPos - startPos == 1) {
        if (TestOperator.of(line[startPos]) != null) {
            endPos = findNonWhitespace(line, endPos)
            if (endPos >= 0)
                endPos = findWhitespaceWithoutEscape(line, endPos)
        }
    }
    if (endPos < 0) {
        endPos = line.length
    }
    val testStr = line.substring(startPos, endPos)

    // skip any whitespace
    startPos = findNonWhitespace(line, endPos + 1)
    // format is optional, this could return length
    output[0] = levelStr
    output[1] = typeStr
    output[2] = testStr
    return if (startPos < 0) {
        3
    } else {
        // format is the rest of the line
        output[3] = line.substring(startPos)
        4
    }
}

private fun handleSpecial(
    previous: MagicEntry?,
    line: String,
    errorCallBack: ErrorCallBack?
) {
    if (line == OPTIONAL_LINE) {
        previous!!.isOptional = true
        return
    }
    var startPos = findNonWhitespace(line, 0)
    var index = findWhitespaceWithoutEscape(line, startPos)
    if (index < 0) {
        errorCallBack?.invoke(
            line,
            "invalid extension line has less than 2 whitespace separated fields",
            null
        )
        return
    }
    val key = line.substring(startPos, index)
    startPos = findNonWhitespace(line, index)
    if (startPos < 0) {
        errorCallBack?.invoke(
            line,
            "invalid extension line has less than 2 whitespace separated fields",
            null
        )
        return
    }
    // find whitespace after value, if any
    index = findWhitespaceWithoutEscape(line, startPos)
    if (index < 0) {
        index = line.length
    }
    val value = line.substring(startPos, index)

    if (key == MIME_TYPE_LINE) {
        previous!!.mimeType = value
    } else {
        // unknown extension key
    }
}

private fun findNonWhitespace(line: String, startPos: Int): Int {
    for (pos in startPos until line.length) {
        if (!line[pos].isWhitespace()) {
            return pos
        }
    }
    return -1
}

private fun findWhitespaceWithoutEscape(line: String, startPos: Int): Int {
    var lastEscape = false
    for (pos in startPos until line.length) {
        val ch = line[pos]
        if (ch == ' ') {
            if (!lastEscape) {
                return pos
            }
            lastEscape = false
        } else if (line[pos].isWhitespace()) {
            return pos
        } else if (ch == '\\') {
            lastEscape = true
        } else {
            lastEscape = false
        }
    }
    return -1
}

/**
 * Copied from the magic(5) man page:
 *
 *
 *
 * Offsets do not need to be constant, but can also be read from the file being examined. If the first character
 * following the last '>' is a '(' then the string after the parenthesis is interpreted as an indirect offset. That
 * means that the number after the parenthesis is used as an offset in the file. The value at that offset is read,
 * and is used again as an offset in the file. Indirect offsets are of the form: ((x[.[bsilBSILm]][+-]y). The value
 * of x is used as an offset in the file. A byte, id3 length, short or long is read at that offset depending on the
 * [bislBISLm] type specifier. The capitalized types interpret the number as a big-endian value, whereas the small
 * letter versions interpret the number as a little-endian value; the 'm' type interprets the number as a
 * middle-endian (PDP-11) value. To that number the value of y is added and the result is used as an offset in the
 * file. The default type if one is not specified is 4-byte long.
 *
 */
private fun parseOffset(
    offsetString: String,
    line: String,
    errorCallBack: ErrorCallBack?
): OffsetInfo? {
    // (9.b+19)
    // (0x3c.l)
    // (8.s*16)
    val matcher = OFFSET_PATTERN.matchEntire(offsetString)
    if (matcher == null) {
        errorCallBack?.invoke(
            line,
            "invalid offset pattern: " + offsetString,
            null
        )
        return null
    }
    val addOffset = matcher.groups[1] != null
    var offset: Int
    try {
        offset = decodeInt(matcher.groupValues[2])
    } catch (e: NumberFormatException) {
        errorCallBack?.invoke(
            line,
            "invalid long offset number: " + offsetString,
            e
        )
        return null
    }

    if (matcher.groups[3] == null) {
        errorCallBack?.invoke(
            line,
            "invalid long offset type: " + offsetString,
            null
        )
        return null
    }
    val ch: Char
    if (matcher.groupValues[3].length == 1) {
        ch = matcher.groupValues[3][0]
    } else {
        // it will use the default
        ch = '\u0000'
    }
    var converter: EndianConverter? = null
    var isId3 = false
    var size = 0
    when (ch) {
    // little-endian byte
        'b' -> {
            // endian doesn't really matter for 1 byte
            converter = EndianType.LITTLE.converter
            size = 1
        }
    // little-endian short
        's' -> {
            converter = EndianType.LITTLE.converter
            size = 2
        }
    // little-endian integer
        'i' -> {
            converter = EndianType.LITTLE.converter
            size = 4
            isId3 = true
        }
    // little-endian long (4 byte)
        'l' -> {
            converter = EndianType.LITTLE.converter
            size = 4
        }
    // big-endian byte
        'B' -> {
            // endian doesn't really matter for 1 byte
            converter = EndianType.BIG.converter
            size = 1
        }
    // big-endian short
        'S' -> {
            converter = EndianType.BIG.converter
            size = 2
        }
    // big-endian integer
        'I' -> {
            converter = EndianType.BIG.converter
            size = 4
            isId3 = true
        }
    // big-endian long (4 byte)
        'L' -> {
            converter = EndianType.BIG.converter
            size = 4
        }
    // big-endian integer
        'm' -> {
            converter = EndianType.MIDDLE.converter
            size = 4
        }
        else -> {
            converter = EndianType.LITTLE.converter
            size = 4
        }
    }
    var add = 0
    // the +# section is optional
    if (matcher.groups[5] != null && matcher.groupValues[5].length > 0) {
        try {
            add = decodeInt(matcher.groupValues[5])
        } catch (e: NumberFormatException) {
            errorCallBack?.invoke(
                line,
                "invalid long add value: " + matcher.groupValues[5],
                e
            )
            return null
        }

        // decode doesn't work with leading '+', grumble
        val offsetOperator = matcher.groupValues[4]
        if ("-" == offsetOperator) {
            add = -add
        } else if ("-" == offsetOperator) {
            offset = add
            add = 0
        }
    }
    return OffsetInfo(offset, converter, addOffset, isId3, size, add)
}
