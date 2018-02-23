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
 * [IANA metadata](https://www.iana.org/assignments/media-types/media-types.xhtml) coming from
 * @author Jean-Christophe Malapert (jcmalapert@gmail.com)
 */
class IanaEntry(
    /**
     * Name of the file type.
     */
    /**
     * Returns the name of the file type.
     */
    val name: String,
    /**
     * Mime type.
     */
    /**
     * Returns the mime type.
     */
    val mimeType: String, ref: String
) {

    /**
     * URL describing the mime type.
     */
    /**
     * Returns the URL of the web page describing the mime type.
     */
    val mimeTypeUrl: String

    /**
     * Reference describing the mime type such as RFC document.
     */
    /**
     * Returns the references of the mime type such as RFC documents.
     */
    val reference: List<String>

    /**
     * URL of the reference
     */
    /**
     * Returns the URL of the references such as the URL of the RFC documents.
     */
    val referenceURL: List<String>

    init {
        this.reference = parseReference(ref)
        this.mimeTypeUrl = MIME_TYPE_BASE_URL + mimeType
        this.referenceURL = buildUrl(reference)
    }

    /**
     * Parses the references (such as RFC document) associated to a mime type.
     * One or several references can be associated to a mime type. Each
     * reference is encompassed by this pattern [ ]
     */
    private fun parseReference(reference: String): List<String> {
        val refValues = ArrayList<String>()
        for (match in PATTERN_REGEX.findAll(reference)) {
            refValues.add(match.groupValues[1])
        }
        return refValues
    }

    /**
     * Creates the URL of each reference (such as RFC document)
     */
    private fun buildUrl(references: List<String>): List<String> {
        val urls = ArrayList<String>()
        val iter = references.listIterator()
        while (iter.hasNext()) {
            var url = iter.next()
            if (url.toUpperCase().startsWith("RFC")) {
                url = RFC_REFERENCE_BASE_URL + url
            } else if (url.startsWith("http")) {
                // do nothing
            } else {
                url = MIME_TYPE_REFERENCE_BASE_URL + "#" + url
            }
            urls.add(url)
        }
        return urls
    }

    companion object {

        private val MIME_TYPE_BASE_URL =
            "https://www.iana.org/assignments/media-types/"
        private val RFC_REFERENCE_BASE_URL = "https://tools.ietf.org/html/"
        private val MIME_TYPE_REFERENCE_BASE_URL =
            "https://www.iana.org/assignments/media-types/media-types.xhtm"
        private val PATTERN_REGEX = "\\[(.+?)\\]".toRegex()
    }
}
