/*
 * KZLib - Kotlin port of ZLib
 *
 * Copyright of original source:
 *
 * Copyright (C) 1995-2017 Jean-loup Gailly and Mark Adler
 *
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will the authors be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 *
 * Jean-loup Gailly        Mark Adler
 * jloup@gzip.org          madler@alumni.caltech.edu
 *
 *
 * The data format used by the zlib library is described by RFCs (Request for
 * Comments) 1950 to 1952 in the files http://tools.ietf.org/html/rfc1950
 * (zlib format), rfc1951 (deflate format) and rfc1952 (gzip format).
 */

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.kzlib

import org.tobi29.stdex.JvmName

enum class WrapperType {
    NONE, ZLIB, GZIP
}

class ZLibException(message: String) : Exception(message)

typealias Deflater = Pair<z_stream, deflate_state>
typealias Inflater = Pair<z_stream, inflate_state>

inline fun Deflater(
    level: Int = Z_DEFAULT_COMPRESSION,
    method: Int = Z_DEFLATED,
    windowBits: Int = DEF_WBITS,
    memLevel: Int = DEF_MEM_LEVEL,
    strategy: Int = Z_DEFAULT_STRATEGY,
    wrapperType: WrapperType = WrapperType.ZLIB
): Deflater {
    val strm = z_stream()
    val state = deflate_state()
    check(
        strm,
        deflateInit(
            strm, state, level, method, when (wrapperType) {
                WrapperType.NONE -> -windowBits
                WrapperType.ZLIB -> windowBits
                WrapperType.GZIP -> windowBits + 16
            }, memLevel, strategy
        )
    )
    return strm to state
}

inline fun Deflater.deflate(flush: Int): Int {
    val (strm, state) = this
    return check(
        strm,
        deflate(strm, state, flush)
    )
}

@JvmName("resetDeflater")
inline fun Deflater.reset(): Int {
    val (strm, state) = this
    return check(
        strm,
        deflateReset(strm, state)
    )
}

@JvmName("endDeflater")
inline fun Deflater.end(): Int {
    val (strm, state) = this
    return check(
        strm,
        deflateEnd(strm, state)
    )
}

inline fun Inflater(
    windowBits: Int = DEF_WBITS,
    wrapperType: WrapperType = WrapperType.ZLIB
): Inflater {
    val strm = z_stream()
    val state = inflate_state()
    check(
        strm,
        inflateInit(
            strm, state, when (wrapperType) {
                WrapperType.NONE -> -windowBits
                WrapperType.ZLIB -> windowBits
                WrapperType.GZIP -> windowBits + 16
            }
        )
    )
    return strm to state
}

@JvmName("resetInflater")
inline fun Inflater.inflate(flush: Int): Int {
    val (strm, state) = this
    return check(
        strm,
        inflate(strm, state, flush)
    )
}

@JvmName("endInflater")
inline fun Inflater.reset(): Int {
    val (strm, state) = this
    return check(
        strm,
        inflateReset(strm, state)
    )
}

inline fun Inflater.end(): Int {
    val (strm, state) = this
    return check(
        strm,
        inflateEnd(strm, state)
    )
}

@PublishedApi
internal inline fun check(strm: z_stream, ret: Int): Int {
    if (ret != Z_OK && ret != Z_STREAM_END)
        throw ZLibException(strm.msg ?: "?")
    return ret
}
