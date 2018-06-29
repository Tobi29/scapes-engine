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
 * ZStream
 *
 */
open class ZStream {
    internal var adler: Checksum = Adler32()
    var next_in: ByteArray? = null // next input byte
    var next_in_index: Int = 0
    var avail_in: Int = 0 // number of bytes available at next_in
    var total_in: Long = 0 // total nb of input bytes read so far
    var next_out: ByteArray? = null // next output byte should be put there
    var next_out_index: Int = 0
    var avail_out: Int = 0 // remaining free space at next_out
    var total_out: Long = 0 // total nb of bytes output so far
    var msg: String? = null
    var data_type: Int = 0 // best guess about the data type: ascii or binary

    fun free() {
        next_in = null
        next_out = null
        msg = null
    }

    fun setOutput(buf: ByteArray, off: Int = 0, len: Int = buf.size) {
        next_out = buf
        next_out_index = off
        avail_out = len
    }

    fun setInput(
        buf: ByteArray,
        off: Int = 0,
        len: Int = buf.size
    ) {
        next_in = buf
        next_in_index = off
        avail_in = len
    }
}
