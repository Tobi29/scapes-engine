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

import com.j256.simplemagik.endian.EndianType
import com.j256.simplemagik.endian.convert
import com.j256.simplemagik.types.IndirectType
import com.j256.simplemagik.types.NameType
import com.j256.simplemagik.types.UseType
import com.j256.simplemagik.types.parseId3
import org.tobi29.arrays.BytesRO
import org.tobi29.io.*
import org.tobi29.logging.KLogging
import org.tobi29.stdex.*
import kotlin.experimental.or

/**
 * Representation of a line of information from the magic (5) format.
 *
 * @author graywatson
 */
data class MagicEntry(
    val name: String?,
    val mimeType: String?,
    val matcher: MagicMatcher,
    val offset: Int,
    val offsetInfo: OffsetInfo?,
    val addOffset: Boolean,
    val formatSpacePrefix: Boolean,
    val clearFormat: Boolean,
    val isOptional: Boolean,
    val formatter: MagicFormatter?,
    val children: Lazy<List<MagicEntry>>
) {
    internal fun canStartWithByte(value: Byte): Boolean =
        if (offset != 0) true else matcher.canStartWithByte(value)

    constructor(
        name: String?,
        mimeType: String?,
        matcher: MagicMatcher,
        offset: Int,
        offsetInfo: OffsetInfo?,
        addOffset: Boolean,
        formatSpacePrefix: Boolean,
        clearFormat: Boolean,
        isOptional: Boolean,
        formatter: MagicFormatter?,
        children: List<MagicEntry>
    ) : this(
        name,
        mimeType,
        matcher,
        offset,
        offsetInfo,
        addOffset,
        formatSpacePrefix,
        clearFormat,
        isOptional,
        formatter,
        lazyOf(children)
    )

    /**
     * Main processing method which can go recursive.
     */
    internal fun matchBytes(
        bytes: BytesRO,
        names: Map<String, MagicEntry>,
        indirect: ContentData? = null,
        contentData: ContentData? = null,
        prevOffset: Int = 0,
        level: Int = 0
    ): ContentData? {
        var contentData = contentData
        val offset2 = (offsetInfo?.getOffset(bytes, prevOffset) ?: this.offset)
            .let { if (addOffset) it + prevOffset else it }
        if (offset2 !in 0..bytes.size) return null
        val (offset1, match) =
                matcher.isMatch(bytes.slice(offset2), formatter != null)
                        ?: return null
        val offset = offset1 + offset2

        if (contentData == null) {
            contentData = ContentData()
            if (indirect != null) {
                contentData.mimeType = indirect.mimeType
                contentData.mimeTypeLevel = indirect.mimeTypeLevel
                contentData.messageBuilder.addAll(indirect.messageBuilder)
            }
            // default is a child didn't match, set a partial so the matcher will keep looking
            contentData.partial = true
        }
        if (formatter != null) {
            if (clearFormat) {
                contentData.messageBuilder.clear()
            }
            contentData.messageBuilder.add {
                // if we are appending and need a space then prepend one
                if (formatSpacePrefix && isNotEmpty()) append(' ')
                match(this, formatter)
            }
        }
        if (matcher is UseType) return (names[matcher.name]
                ?: error("Unknown name: ${matcher.name}"))
            .matchBytes(bytes, names, indirect, contentData, prevOffset, level)
        val children = children.value
        if (matcher is IndirectType) contentData.indirect = true
        if (children.isEmpty()) contentData.offset = offset
        logger.trace { "matched data: $this: $contentData" }

        if (name != null) {
            contentData.name = name
        }

        if (children.isEmpty()) {
            // no children so we have a full match and can set partial to false
            contentData.partial = false
        } else {
            // run through the children to add more content-type details
            var allOptional = true
            for (entry in children) {
                if (!entry.isOptional) {
                    allOptional = false
                }
                // goes recursive here
                val returned = entry.matchBytes(
                    bytes, names, indirect, contentData, offset, level + 1
                )
                assert { returned === null || returned === contentData }
                // we continue to match to see if we can add additional children info to the name
            }
            if (allOptional) {
                contentData.partial = false
            }
        }

        /*
		 * Set the mime-type if it is not set already or if we've gotten more specific in the processing of a pattern
		 * and determine that it's actually a different type so we can override the previous mime-type. Example of this
		 * is Adobe Illustrator which looks like a PDF but has extra stuff in it.
		 */
        if (mimeType != null && (contentData.mimeType == null || level > contentData.mimeTypeLevel)) {
            contentData.mimeType = mimeType
            contentData.mimeTypeLevel = level
        }
        return contentData
    }

    /**
     * Internal processing data about the content.
     */
    data class ContentData(
        var name: String? = null,
        var mimeType: String? = null,
        var mimeTypeLevel: Int = -1,
        var partial: Boolean = false,
        var indirect: Boolean = false,
        var offset: Int = 0,
        val messageBuilder: MutableList<StringBuilder.() -> Unit> = ArrayList()
    ) {
        val message get() = buildString { messageBuilder.forEach { it() } }
    }

    /**
     * Information about the extended offset.
     */
    data class OffsetInfo(
        val offset: Int,
        val endianType: EndianType,
        val addOffset: Boolean,
        val isId3: Boolean,
        val size: Int,
        val add: Int
    ) {
        fun getOffset(
            bytes: BytesRO,
            prevOffset: Int
        ): Int? {
            if (bytes.size - offset < size) return null
            val value = if (isId3) when (size) {
                4 -> combineToInt(
                    bytes[offset + 0],
                    bytes[offset + 1],
                    bytes[offset + 2],
                    bytes[offset + 3]
                ).convert(endianType).parseId3()
                else -> error("Invalid size: $size")
            } else when (size) {
                1 -> bytes[offset + 0].toInt() and 0xFF
                2 -> combineToShort(
                    bytes[offset + 0],
                    bytes[offset + 1]
                ).convert(endianType).toInt() and 0xFFFF
                4 -> combineToInt(
                    bytes[offset + 0],
                    bytes[offset + 1],
                    bytes[offset + 2],
                    bytes[offset + 3]
                ).convert(endianType)
                else -> error("Invalid size: $size")
            }
            return (value + add).let { if (addOffset) it + prevOffset else it }
        }
    }

    companion object : KLogging()
}

internal fun MagicEntry.write(stream: WritableByteStream) {
    stream.put(
        0.toByte()
            .setAt(0, name != null)
            .setAt(1, mimeType != null)
            .setAt(2, offsetInfo != null)
            .setAt(3, formatter != null)
            .setAt(4, addOffset)
            .setAt(5, formatSpacePrefix)
            .setAt(6, clearFormat)
            .setAt(7, isOptional)
    )
    matcher.write(stream)
    stream.putCompactInt(offset)
    if (name != null) {
        stream.putCompactString(name)
    }
    if (mimeType != null) {
        stream.putCompactString(mimeType)
    }
    if (offsetInfo != null) {
        offsetInfo.write(stream)
    }
    if (formatter != null) {
        formatter.write(stream)
    }
    val childrenStream = MemoryViewStreamDefault()
    for (child in children.value) {
        child.write(childrenStream)
    }
    childrenStream.flip()
    val childrenBuffer = childrenStream.bufferSlice()
    stream.putCompactInt(childrenBuffer.size)
    stream.put(childrenBuffer)
}

internal fun readMagicEntry(
    stream: MemoryViewReadableStream<HeapViewByteBE>,
    names: MutableMap<String, MagicEntry>? = null
): MagicEntry {
    val flags = stream.get()
    val nameHas = flags.maskAt(0)
    val mimeTypeHas = flags.maskAt(1)
    val offsetInfoHas = flags.maskAt(2)
    val formatterHas = flags.maskAt(3)
    val matcher = readMagicMatcher(stream)
    val offset = stream.getCompactInt()
    val name = if (nameHas) {
        stream.getCompactString()
    } else null
    val mimeType = if (mimeTypeHas) {
        stream.getCompactString()
    } else null
    val offsetInfo = if (offsetInfoHas) {
        readMagicMatcherOffsetInfo(stream)
    } else null
    val addOffset = flags.maskAt(4)
    val formatSpacePrefix = flags.maskAt(5)
    val clearFormat = flags.maskAt(6)
    val isOptional = flags.maskAt(7)
    val formatter = if (formatterHas) {
        readMagicFormatter(stream)
    } else null
    val childrenBufferSize = stream.getCompactInt()
    val childrenStream = MemoryViewReadableStream(
        stream.buffer().slice(stream.position, childrenBufferSize)
    )
    stream.skip(childrenBufferSize)
    val children = lazy {
        ArrayList<MagicEntry>().apply {
            while (childrenStream.hasRemaining) {
                add(readMagicEntry(childrenStream, names))
            }
        }
    }
    val entry = MagicEntry(
        name,
        mimeType,
        matcher,
        offset,
        offsetInfo,
        addOffset,
        formatSpacePrefix,
        clearFormat,
        isOptional,
        formatter,
        children
    )
    if (names != null && matcher is NameType) names[matcher.name] = entry
    return entry
}

internal fun MagicEntry.OffsetInfo.write(stream: WritableByteStream) {
    stream.put(
        0.toByte()
            .setAt(0, addOffset)
            .setAt(1, isId3) or
                (endianType.id shl 2).toByte()
    )
    stream.putCompactInt(offset)
    stream.putCompactInt(size)
    stream.putCompactInt(add)
}

internal fun readMagicMatcherOffsetInfo(stream: MemoryViewReadableStream<HeapViewByteBE>): MagicEntry.OffsetInfo {
    val flags = stream.get()
    val offset = stream.getCompactInt()
    val endianType = EndianType.of((flags.toInt() ushr 2) and 3)
            ?: throw IOException("Invalid endian type")
    val addOffset = flags.maskAt(0)
    val isId3 = flags.maskAt(1)
    val size = stream.getCompactInt()
    val add = stream.getCompactInt()
    return MagicEntry.OffsetInfo(
        offset,
        endianType,
        addOffset,
        isId3,
        size,
        add
    )
}
