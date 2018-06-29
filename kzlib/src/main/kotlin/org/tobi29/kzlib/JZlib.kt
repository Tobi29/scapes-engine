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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.kzlib

enum class WrapperType {
    NONE, ZLIB, GZIP, ANY
}

const val Z_MAX_WBITS = 15        // 32K LZ77 window
const val Z_DEF_WBITS = Z_MAX_WBITS

inline val W_NONE get() = WrapperType.NONE
inline val W_ZLIB get() = WrapperType.ZLIB
inline val W_GZIP get() = WrapperType.GZIP
inline val W_ANY get() = WrapperType.ANY

// compression levels
const val Z_NO_COMPRESSION = 0
const val Z_BEST_SPEED = 1
const val Z_BEST_COMPRESSION = 9
const val Z_DEFAULT_COMPRESSION = -1

// memory levels
const val Z_DEF_MEM_LEVEL = 8
const val Z_MAX_MEM_LEVEL = 9

// compression strategy
const val Z_FILTERED = 1
const val Z_HUFFMAN_ONLY = 2
const val Z_DEFAULT_STRATEGY = 0

const val Z_NO_FLUSH = 0
const val Z_PARTIAL_FLUSH = 1
const val Z_SYNC_FLUSH = 2
const val Z_FULL_FLUSH = 3
const val Z_FINISH = 4

const val Z_OK = 0
const val Z_STREAM_END = 1
const val Z_NEED_DICT = 2
const val Z_ERRNO = -1
const val Z_STREAM_ERROR = -2
const val Z_DATA_ERROR = -3
const val Z_MEM_ERROR = -4
const val Z_BUF_ERROR = -5
const val Z_VERSION_ERROR = -6

// The three kinds of block type
const val Z_BINARY: Byte = 0
const val Z_ASCII: Byte = 1
const val Z_UNKNOWN: Byte = 2

typealias Deflater = Pair<Deflate, ZStream>
typealias Inflater = Pair<Inflate, ZStream>

inline fun Deflater(
    level: Int,
    bits: Int = Z_DEF_WBITS,
    memlevel: Int = Z_DEF_MEM_LEVEL,
    wrapperType: WrapperType = W_ZLIB
): Deflater = ZStream().let {
    Deflate(it).apply {
        init(level, bits, memlevel, wrapperType)
    } to it
}

inline fun Inflater(
    bits: Int = Z_DEF_WBITS,
    wrapperType: WrapperType = W_ZLIB
): Inflater = ZStream().let {
    Inflate(it).apply {
        init(bits, wrapperType)
    } to it
}
