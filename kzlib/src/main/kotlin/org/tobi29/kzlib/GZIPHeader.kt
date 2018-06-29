/*
 * Copyright (c) 2000,2001,2002,2003 ymnk, JCraft,Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in
 *      the documentation and/or other materials provided with the distribution.
 *
 *   3. The names of the authors may not be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
 * INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This program is based on zlib-1.1.3, so all credit should go authors
 * Jean-loup Gailly(jloup@gzip.org) and Mark Adler(madler@alumni.caltech.edu)
 * and contributors of zlib.
 */

package org.tobi29.kzlib

/**
 * @see "http://www.ietf.org/rfc/rfc1952.txt"
 */
internal class GZIPHeader {
    private var text = false
    private val fhcrc = false
    var time: Long = 0
    var xflags: Int = 0
    var os = 0
    var extra: ByteArray? = null
    var name: ByteArray? = null
    var comment: ByteArray? = null
    var hcrc: Int = 0
    var crc: Int = 0
    private var modifiedTime: Long = 0

    internal fun put(d: Deflate) {
        var flag = 0
        if (text) {
            flag = flag or 1     // FTEXT
        }
        if (fhcrc) {
            flag = flag or 2     // FHCRC
        }
        if (extra != null) {
            flag = flag or 4     // FEXTRA
        }
        if (name != null) {
            flag = flag or 8    // FNAME
        }
        if (comment != null) {
            flag = flag or 16   // FCOMMENT
        }
        var xfl = 0
        if (d.level == Z_BEST_SPEED) {
            xfl = xfl or 4
        } else if (d.level == Z_BEST_COMPRESSION) {
            xfl = xfl or 2
        }

        d.put_short(0x8b1f.toShort().toInt())  // ID1 ID2
        d.put_byte(8.toByte())         // CM(Compression Method)
        d.put_byte(flag.toByte())
        d.put_byte(modifiedTime.toByte())
        d.put_byte((modifiedTime shr 8).toByte())
        d.put_byte((modifiedTime shr 16).toByte())
        d.put_byte((modifiedTime shr 24).toByte())
        d.put_byte(xfl.toByte())
        d.put_byte(os.toByte())

        if (extra != null) {
            d.put_byte(extra!!.size.toByte())
            d.put_byte((extra!!.size shr 8).toByte())
            d.put_byte(extra, 0, extra!!.size)
        }

        if (name != null) {
            d.put_byte(name, 0, name!!.size)
            d.put_byte(0.toByte())
        }

        if (comment != null) {
            d.put_byte(comment, 0, comment!!.size)
            d.put_byte(0.toByte())
        }
    }
}

internal const val OS_MSDOS = 0x00.toByte()
internal const val OS_AMIGA = 0x01.toByte()
internal const val OS_VMS = 0x02.toByte()
internal const val OS_UNIX = 0x03.toByte()
internal const val OS_ATARI = 0x05.toByte()
internal const val OS_OS2 = 0x06.toByte()
internal const val OS_MACOS = 0x07.toByte()
internal const val OS_TOPS20 = 0x0a.toByte()
internal const val OS_WIN32 = 0x0b.toByte()
internal const val OS_VMCMS = 0x04.toByte()
internal const val OS_ZSYSTEM = 0x08.toByte()
internal const val OS_CPM = 0x09.toByte()
internal const val OS_QDOS = 0x0c.toByte()
internal const val OS_RISCOS = 0x0d.toByte()
internal const val OS_UNKNOWN = 0xFF.toByte()
