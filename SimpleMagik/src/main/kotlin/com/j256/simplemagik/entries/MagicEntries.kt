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
internal class MagicEntries {

    private val entryList = ArrayList<MagicEntry>()
    private val firstByteEntryLists =
        Array(FIRST_BYTE_LIST_SIZE) { ArrayList<MagicEntry>() }

    /**
     * Read the entries so later we can find matches with them.
     */
    fun readEntries(
        lineReader: Iterator<String>,
        errorCallBack: ErrorCallBack?
    ) {
        val levelParents = arrayOfNulls<MagicEntry>(MAX_LEVELS)
        var previousEntry: MagicEntry? = null
        var parsed = 0
        val parts = Array(4) { "" }
        for (line in lineReader) {
            parsed++
            // skip blanks and comments
            if (line.isBlank() || line[0] == '#') continue

            val entry: MagicEntry?
            try {
                // we need the previous entry because of mime-type, etc. which augment the previous line
                entry = parseMagicLine(
                    previousEntry, line, errorCallBack, parts
                )
                if (entry == null) continue
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
        val iterator = entryList.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val startingBytes = entry.startsWithByte
            if (startingBytes == null || startingBytes.isEmpty()) continue
            val index = startingBytes[0].toInt() and 0xFF
            firstByteEntryLists[index].add(entry)
            iterator.remove()
        }
    }

    /**
     * Find and return a match for the associated bytes.
     */
    tailrec fun findMatch(
        bytes: ByteArraySliceRO
    ): ContentInfo? {
        if (bytes.size == 0) return ContentInfo.EMPTY_INFO

        val index = bytes[0].toInt() and 0xFF
        val dataFast = findMatch(bytes, firstByteEntryLists[index])
        if (dataFast != null)
            return if (dataFast.indirect) findMatch(bytes.slice(dataFast.offset))
            else ContentInfo(dataFast)

        val data = findMatch(bytes, entryList)
        if (data != null)
            return if (data.indirect) findMatch(bytes.slice(data.offset))
            else ContentInfo(data)

        return null
    }

    private fun findMatch(
        bytes: ByteArraySliceRO,
        entryList: List<MagicEntry>
    ): MagicEntry.ContentData? {
        var partialMatchInfo: MagicEntry.ContentData? = null
        for (entry in entryList) {
            val info =
                entry.matchBytes(bytes)?.takeIf { it.name != MagicEntry.UNKNOWN_NAME }
                        ?: continue
            if (info.indirect) {
                logger.trace { "found indirect match $entry" }
                return info
            }
            if (!info.partial) {
                // first non-partial wins
                logger.trace { "found full match $entry" }
                return info
            } else if (partialMatchInfo == null) {
                // first partial match may win
                logger.trace { "found partial match $entry" }
                partialMatchInfo = info
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
