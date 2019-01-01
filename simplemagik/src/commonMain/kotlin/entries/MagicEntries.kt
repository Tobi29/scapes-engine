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
import org.tobi29.arrays.BytesRO
import org.tobi29.logging.KLogger
import org.tobi29.stdex.maskAt

/**
 * Class which encompasses a set of entries and allows us to optimize their use.
 *
 * @author graywatson
 */
internal class MagicEntries(
    entries: List<MagicEntry>,
    private val names: Map<String, MagicEntry>
) {
    private val entries = Array(entries.size) { i ->
        val entry = entries[i]
        val possible = LongArray(4)
        for (j in 0 until 256) {
            if (!entry.canStartWithByte(j.toByte())) continue
            possible[j ushr 6] = possible[j ushr 6] or (1L shl (j and 63))
        }
        possible to entry
    }

    /**
     * Find and return a match for the associated bytes.
     */
    fun findMatch(
        bytes: BytesRO
    ): ContentInfo? = findMatch(bytes, null)

    private tailrec fun findMatch(
        bytes: BytesRO,
        indirect: MagicEntry.ContentData?
    ): ContentInfo? {
        if (bytes.size == 0) return ContentInfo.EMPTY_INFO

        val index = bytes[0].toInt() and 0xFF
        val dataFast = findMatch(bytes, indirect, index)
        if (dataFast != null)
            return if (dataFast.indirect)
                findMatch(bytes.slice(dataFast.offset), dataFast)
            else ContentInfo(dataFast)

        return null
    }

    private fun findMatch(
        bytes: BytesRO,
        indirect: MagicEntry.ContentData?,
        index: Int
    ): MagicEntry.ContentData? {
        var partialMatchInfo: MagicEntry.ContentData? = null
        for ((possible, entry) in entries) {
            if (!possible[index ushr 6].maskAt(index)) continue
            val info = entry.matchBytes(
                bytes, names, indirect
            )?.takeIf { it.name != null } ?: continue
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

    companion object {
        private val logger = KLogger<MagicEntries>()
    }
}
