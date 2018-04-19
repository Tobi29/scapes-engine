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

package com.j256.simplemagik

import com.j256.simplemagik.entries.MagicEntries
import org.tobi29.arrays.sliceOver
import org.tobi29.logging.KLogging

/**
 *
 *
 * Class which reads in the magic files and determines the [ContentInfo] for files and byte arrays. You use the
 * default constructor [.ContentInfoUtil] to use the internal rules file or load in a local file from the
 * file-system using [.ContentInfoUtil]. Once the rules are loaded, you can use [.findMatch]
 * or other `findMatch(...)` methods to get the content-type of a file or bytes.
 *
 *
 * <pre>
 * // create a magic utility using the internal magic file
 * ContentInfoUtil util = new ContentInfoUtil();
 * // get the content info for this file-path or null if no match
 * ContentInfo info = util.findMatch(&quot;/tmp/upload.tmp&quot;);
 * // display content type information
 * if (info == null) {
 * System.out.println(&quot;Unknown content-type&quot;);
 * } else {
 * // other information in ContentInfo type
 * System.out.println(&quot;Content-type is: &quot; + info.getName());
 * }
</pre> *
 *
 * @author graywatson
 */
class ContentInfoUtil
/**
 * Construct a magic utility using the magic file entries from a reader.
 *
 * @param lines
 * Iterator returning all lines of the magic database
 * @param errorCallBack
 * Call back which shows any problems with the magic entries loaded.
 * @throws IOException
 * If there was a problem reading the magic entries from the reader.
 */(
    lines: Iterator<String> = magic.iterator(),
    errorCallBack: ErrorCallBack? = { error, description, e ->
        if (e == null)
            logger.debug { "Magic error: $error $description" }
        else
            logger.debug(e) { "Magic error: $error $description" }
    }
) {
    private val magicEntries: MagicEntries =
        MagicEntries().apply {
            readEntries(lines, errorCallBack)
            optimizeFirstBytes()
        }

    /**
     * Return the content type from the associated bytes or null if none of the magic entries matched.
     */
    fun findMatch(bytes: ByteArray): ContentInfo? {
        return try {
            if (bytes.isEmpty()) {
                ContentInfo.EMPTY_INFO
            } else {
                magicEntries.findMatch(bytes.sliceOver())
            }
        } catch (e: Throwable) {
            // FIXME: I really do not trust this code to not crash
            logger.error(e) { "Internal error in magic matcher" }
            null
        }
    }

    companion object : KLogging() {
        /**
         * Number of bytes that the utility class by default reads to determine the content type information.
         */
        val DEFAULT_READ_SIZE = 1024 * 1024

        /**
         * Return the content type if the extension from the file-name matches our internal list. This can either be just
         * the extension part or it will look for the last period and take the string after that as the extension.
         *
         * @return The matching content-info or null if no matches.
         */
        fun findExtensionMatch(name: String): ContentInfo? {
            var name = name
            name = name.toLowerCase()

            // look up the whole name first
            var type = ContentType.fromFileExtension(name)
            if (type != ContentType.OTHER) {
                return ContentInfo(type)
            }

            // now find the .ext part, if any
            val index = name.lastIndexOf('.')
            if (index < 0 || index == name.length - 1) {
                return null
            }

            type = ContentType.fromFileExtension(name.substring(index + 1))
            return if (type == ContentType.OTHER) {
                null
            } else {
                ContentInfo(type)
            }
        }

        /**
         * Return the content type if the mime-type matches our internal list.
         *
         * @return The matching content-info or null if no matches.
         */
        fun findMimeTypeMatch(mimeType: String): ContentInfo? {
            val type = ContentType.fromMimeType(mimeType.toLowerCase())
            return if (type == ContentType.OTHER) {
                null
            } else {
                ContentInfo(type)
            }
        }
    }
}
