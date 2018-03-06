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

import com.j256.simplemagik.*
import org.tobi29.stdex.readOnly

/**
 * Loads the IANA databases (build on 10 august 2017).
 * IANA databases provides the following elements in a CSV file:
 *
 *  * Name of the file type
 *  * mime type
 *  * Name of the articles describing the mime type
 *
 * In addition to these elements, two URLs are created in order to locate the
 * description of the mime type and the URL of the articles.
 * @author Jean-Christophe Malapert (jcmalapert@gmail.com)
 */
object IanaEntries {
    private val _ianaDB = HashMap<String, IanaEntry>()

    /**
     * The database
     */
    val ianaDB = _ianaDB.readOnly()

    /**
     * Constructor.
     */
    init {
        // TODO: Multi-thread this on JVM? Might help with Android.
        loadDb(ianaApplication.iterator())
        loadDb(ianaAudio.iterator())
        loadDb(ianaFont.iterator())
        loadDb(ianaImage.iterator())
        loadDb(ianaMessage.iterator())
        loadDb(ianaModel.iterator())
        loadDb(ianaMultipart.iterator())
        loadDb(ianaText.iterator())
        loadDb(ianaVideo.iterator())
    }

    /**
     * Returns the IANA metadata for a specific mime type or null when
     * the mime type is not found.
     */
    fun getIanaMetadata(mimeType: String): IanaEntry? {
        return this._ianaDB[mimeType]
    }

    /**
     * Loads the IANA database
     * @param db
     */
    private fun loadDb(db: Iterator<String>) {
        if (db.hasNext()) db.next()
        while (db.hasNext()) {
            val line = db.next()
            // parse the CSV file. The CSV file contains
            // three elements per row
            val split1 = line.indexOf(',')
            if (split1 == -1) throw IllegalArgumentException("Invalid line: $line")
            val split2 = line.indexOf(',', split1 + 1)
            if (split2 == -1) throw IllegalArgumentException("Invalid line: $line")
            val ianaEntry = IanaEntry(
                line.substring(0, split1),
                line.substring(split1 + 1, split2),
                line.substring(split2 + 1)
            )
            _ianaDB[ianaEntry.mimeType] = ianaEntry
        }
    }
}
