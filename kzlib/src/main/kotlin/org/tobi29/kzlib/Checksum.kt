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

import org.tobi29.checksums.*

internal interface Checksum {
    val value: Int
    val defaultInit: Int get() = 0
    fun update(buf: ByteArray, index: Int, len: Int)
    fun reset(init: Int = defaultInit)
}

internal class Adler32 : Checksum {
    private var a: Short = 0
    private var b: Short = 0

    override val value: Int get() = finishChainAdler32(a, b)
    override val defaultInit: Int get() = 1

    init {
        reset()
    }

    override fun reset(init: Int) {
        initChainAdler32(init) { na, nb ->
            a = na
            b = nb
        }
    }

    override fun update(buf: ByteArray, index: Int, len: Int) {
        chainAdler32(a, b, buf, index, len) { na, nb ->
            a = na
            b = nb
        }
    }
}

internal class Crc32 : Checksum {
    private var v = 0

    init {
        reset()
    }

    override val value: Int get() = v.finishChainCrc32()

    override fun update(buf: ByteArray, index: Int, len: Int) {
        v = chainCrc32(v, buf, index, len, gzipCrcTable)
    }

    override fun reset(init: Int) {
        v = initChainCrc32(init)
    }
}

private val gzipCrcTable = tableCrc32(-0x12477ce0)