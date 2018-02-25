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

import com.j256.simplemagik.ContentInfo
import com.j256.simplemagik.ErrorCallBack
import org.tobi29.arrays.ByteArraySliceRO
import org.tobi29.logging.KLogging

/**
 * Class which encompasses a set of entries and allows us to optimize their use.
 *
 * @author graywatson
 */
class MagicEntries {

    private val entryList = ArrayList<MagicEntry>()
    private val firstByteEntryLists =
        arrayOfNulls<ArrayList<MagicEntry>>(FIRST_BYTE_LIST_SIZE)

    /**
     * Read the entries so later we can find matches with them.
     */
    fun readEntries(
        lineReader: Iterator<String>,
        errorCallBack: ErrorCallBack?
    ) {
        val levelParents = arrayOfNulls<MagicEntry>(MAX_LEVELS)
        var previousEntry: MagicEntry? = null
        for (line in lineReader) {
            // skip blanks and comments
            if (line.length == 0 || line[0] == '#') {
                continue
            }

            val entry: MagicEntry?
            try {
                // we need the previous entry because of mime-type, etc. which augment the previous line
                entry = MagicEntryParser.parseLine(
                    previousEntry,
                    line,
                    errorCallBack
                )
                if (entry == null) {
                    continue
                }
            } catch (e: IllegalArgumentException) {
                errorCallBack?.invoke(line, e.message, e)
                continue
            }

            val level = entry.level
            if (previousEntry == null && level != 0) {
                errorCallBack?.invoke(
                    line,
                    "first entry of the file but the level $level should be 0",
                    null
                )
                continue
            }

            if (level == 0) {
                // top level entry
                entryList.add(entry)
            } else if (levelParents[level - 1] == null) {
                errorCallBack?.invoke(
                    line,
                    "entry has level " + level + " but no parent entry with level " + (level - 1),
                    null
                )
                continue
            } else {
                // we are a child of the one above us
                levelParents[level - 1]!!.addChild(entry)
            }
            levelParents[level] = entry
            previousEntry = entry
        }
    }

    /**
     * Optimize the magic entries by removing the first-bytes information into their own lists
     */
    fun optimizeFirstBytes() {
        // now we post process the entries and remove the first byte ones we can optimize
        for (entry in entryList) {
            val startingBytes = entry.startsWithByte
            if (startingBytes == null || startingBytes.size == 0) {
                continue
            }
            val index = 0xFF and startingBytes[0].toInt()
            if (firstByteEntryLists[index] == null) {
                firstByteEntryLists[index] = ArrayList<MagicEntry>()
            }
            firstByteEntryLists[index]!!.add(entry)
            /*
			 * We put an entry in the first-byte list but need to leave it in the main list because there may be
			 * optional characters or != or > comparisons in the match
			 */
        }
    }

    /**
     * Find and return a match for the associated bytes.
     */
    fun findMatch(bytes: ByteArraySliceRO): ContentInfo? {
        if (bytes.size == 0) {
            return ContentInfo.EMPTY_INFO
        }
        // first do the start byte ones
        val index = 0xFF and bytes[0].toInt()
        if (index < firstByteEntryLists.size && firstByteEntryLists[index] != null) {
            val info = findMatch(bytes, firstByteEntryLists[index]!!)
            if (info != null) {
                // this seems to be right to return even if only a partial match here
                return info
            }
        }
        return findMatch(bytes, entryList)
    }

    private tailrec fun findMatch(
        bytes: ByteArraySliceRO,
        entryList: List<MagicEntry>
    ): ContentInfo? {
        var partialMatchInfo: ContentInfo? = null
        for (entry in entryList) {
            val info =
                entry.matchBytes(bytes)?.takeIf { it.name != MagicEntry.UNKNOWN_NAME }
                        ?: continue
            if (info.indirect) {
                logger.trace { "found indirect match $entry" }
                return findMatch(bytes.slice(info.offset), entryList)
            }
            val contentInfo = ContentInfo(
                info.name,
                info.mimeType,
                info.sb.toString(),
                info.partial
            )
            if (!contentInfo.isPartial) {
                // first non-partial wins
                logger.trace { "found full match $entry" }
                logger.trace { "returning full match $contentInfo" }
                return contentInfo
            } else if (partialMatchInfo == null) {
                // first partial match may win
                logger.trace { "found partial match $entry" }
                partialMatchInfo = contentInfo
                // continue to look for non-partial
            } else {
                // already have a partial match
            }
        }
        if (partialMatchInfo == null) {
            logger.trace { "returning no match" }
            return null
        } else {
            // returning first partial match
            logger.trace { "returning partial match $partialMatchInfo" }
            return partialMatchInfo
        }
    }

    companion object : KLogging() {
        private const val MAX_LEVELS = 20
        private const val FIRST_BYTE_LIST_SIZE = 256
    }
}
