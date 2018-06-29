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

import org.tobi29.io.HeapViewByteBE
import org.tobi29.io.MemoryViewStream
import org.tobi29.io.MemoryViewStreamDefault
import org.tobi29.io.asByteArray
import org.tobi29.stdex.Throws
import org.tobi29.stdex.combineToInt
import org.tobi29.stdex.copy
import org.tobi29.stdex.splitToBytes

class Inflate(private val z: ZStream) {

    var mode: Int = 0                            // current inflate mode

    // mode dependent information
    var method: Int = 0        // if FLAGS, method byte

    // if CHECK, check values to compare
    var was = -1           // computed check value
    var need = 0               // stream check value

    // if BAD, inflateSync's marker bytes count
    var marker: Int = 0

    // mode independent information
    var wrap: Int = 0          // flag for no wrapper
    // 0: no wrapper
    // 1: zlib header
    // 2: gzip header
    // 4: auto detection

    var wbits: Int = 0            // log2(window size)  (8..15, defaults to 15)

    var blocks: InfBlocks? = null     // current inflate_blocks state

    private var flags: Int = 0

    private var need_bytes = -1
    private val crcbuf = ByteArray(4)

    internal var gzipHeader: GZIPHeader? = null

    private var tmp_string: MemoryViewStream<HeapViewByteBE>? = null

    fun reset(): Int {
        z.total_out = 0
        z.total_in = z.total_out
        z.msg = null
        mode = HEAD
        need_bytes = -1
        blocks?.reset()
        return Z_OK
    }

    fun end(): Int {
        blocks?.reset()
        return Z_OK
    }

    fun init(w: Int = Z_DEF_WBITS, wrapperType: WrapperType = W_ZLIB): Int {
        var w = w
        if (wrapperType == W_NONE) {
            w = -w
        } else if (wrapperType == W_GZIP) {
            w += 16
        } else if (wrapperType == W_ANY) {
            w = w or INFLATE_ANY
        } else if (wrapperType == W_ZLIB) {
        }
        z.msg = null
        blocks = null

        // handle undocumented wrap option (no zlib header or check)
        wrap = 0
        if (w < 0) {
            w = -w
        } else if (w and INFLATE_ANY != 0) {
            wrap = 4
            w = w and INFLATE_ANY.inv()
            if (w < 48)
                w = w and 15
        } else if (w and 31.inv() != 0) { // for example, DEF_WBITS + 32
            wrap =
                    4               // zlib and gzip wrapped data should be accepted.
            w = w and 15
        } else {
            wrap = (w shr 4) + 1
            if (w < 48)
                w = w and 15
        }

        if (w < 8 || w > 15) {
            end()
            return Z_STREAM_ERROR
        }
        if (wbits != w) {
            blocks?.reset()
            blocks = null
        }

        // set window size
        wbits = w

        this.blocks = InfBlocks(z!!, 1 shl w, wrap)

        // reset state
        reset()

        return Z_OK
    }

    fun inflate(f: Int): Int {
        val hold = 0

        if (z == null || z.next_in == null) {
            return if (f == Z_FINISH && this.mode == HEAD) Z_OK else Z_STREAM_ERROR
        }

        var f = if (f == Z_FINISH) Z_BUF_ERROR else Z_OK
        var r = Z_BUF_ERROR
        loop@ while (true) {
            if (mode == HEAD) {
                if (wrap == 0) {
                    this.mode =
                            BLOCKS
                    continue@loop
                }

                try {
                    r = readBytes(2, r, f)
                } catch (e: Return) {
                    return e.r
                }

                if ((wrap == 4 || wrap and 2 != 0) && this.need == 0x8b1f) {   // gzip header
                    if (wrap == 4) {
                        wrap = 2
                    }
                    z.adler = Crc32()
                    checksum(2, this.need)

                    if (gzipHeader == null)
                        gzipHeader = GZIPHeader()

                    this.mode =
                            FLAGS
                    continue@loop
                }

                if (wrap and 2 != 0) {
                    this.mode = BAD
                    z.msg = "incorrect header check"
                    continue@loop
                }

                flags = 0

                this.method = this.need.toInt() and 0xFF
                var b = (this.need shr 8).toInt() and 0xFF

                if ((wrap and 1 == 0 ||  // check if zlib header allowed
                            ((this.method shl 8) + b) % 31 != 0) && this.method and 0xf != Z_DEFLATED) {
                    if (wrap == 4) {
                        z.next_in_index -= 2
                        z.avail_in += 2
                        z.total_in -= 2
                        wrap = 0
                        this.mode =
                                BLOCKS
                        continue@loop
                    }
                    this.mode = BAD
                    z.msg = "incorrect header check"
                    // since zlib 1.2, it is allowted to inflateSync for this case.
                    /*
          this.marker = 5;       // can't try inflateSync
          */
                    continue@loop
                }

                if (this.method and 0xf != Z_DEFLATED) {
                    this.mode = BAD
                    z.msg = "unknown compression method"
                    // since zlib 1.2, it is allowted to inflateSync for this case.
                    /*
          this.marker = 5;       // can't try inflateSync
	  */
                    continue@loop
                }

                if (wrap == 4) {
                    wrap = 1
                }

                if ((this.method shr 4) + 8 > this.wbits) {
                    this.mode = BAD
                    z.msg = "invalid window size"
                    // since zlib 1.2, it is allowted to inflateSync for this case.
                    /*
          this.marker = 5;       // can't try inflateSync
	  */
                    continue@loop
                }

                z.adler = Adler32()

                if (b and PRESET_DICT == 0) {
                    this.mode =
                            BLOCKS
                    continue@loop
                }
                this.mode = DICT4
            }
            if (mode == DICT4) {
                if (z.avail_in == 0) return r
                r = f

                z.avail_in--
                z.total_in++
                this.need = z.next_in!![z.next_in_index++].toInt() shl 24 and
                        0xFF000000.toInt()
                this.mode = DICT3
            }
            if (mode == DICT3) {
                if (z.avail_in == 0) return r
                r = f

                z.avail_in--
                z.total_in++
                this.need += z.next_in!![z.next_in_index++].toInt() shl 16 and 0xFF0000
                this.mode = DICT2
            }
            if (mode == DICT2) {
                if (z.avail_in == 0) return r
                r = f

                z.avail_in--
                z.total_in++
                this.need += z.next_in!![z.next_in_index++].toInt() shl 8 and 0xFF00
                this.mode = DICT1
            }
            if (mode == DICT1) {
                if (z.avail_in == 0) return r
                r = f

                z.avail_in--
                z.total_in++
                this.need += z.next_in!![z.next_in_index++].toInt() shl 0 and 0xFF
                z.adler.reset(this.need.toInt())
                this.mode = DICT0
                return Z_NEED_DICT
            }
            if (mode == DICT0) {
                this.mode = BAD
                z.msg = "need dictionary"
                this.marker = 0       // can try inflateSync
                return Z_STREAM_ERROR
            }
            if (mode == BLOCKS) {
                r = this.blocks!!.proc(r)
                if (r == Z_DATA_ERROR) {
                    this.mode = BAD
                    this.marker = 0     // can try inflateSync
                    continue@loop
                }
                if (r == Z_OK) {
                    r = f
                }
                if (r != Z_STREAM_END) return r
                r = f
                this.was = z.adler.value
                this.blocks!!.reset()
                if (this.wrap == 0) {
                    this.mode =
                            DONE
                    continue@loop
                }
                this.mode = CHECK4
            }
            if (mode == CHECK4) {
                if (z.avail_in == 0) return r
                r = f

                z.avail_in--
                z.total_in++
                this.need = z.next_in!![z.next_in_index++].toInt() shl 24 and
                        0xFF000000.toInt()
                this.mode = CHECK3
            }
            if (mode == CHECK3) {
                if (z.avail_in == 0) return r
                r = f

                z.avail_in--
                z.total_in++
                this.need += z.next_in!![z.next_in_index++].toInt() shl 16 and 0xFF0000
                this.mode = CHECK2
            }
            if (mode == CHECK2) {
                if (z.avail_in == 0) return r
                r = f

                z.avail_in--
                z.total_in++
                this.need += z.next_in!![z.next_in_index++].toInt() shl 8 and 0xFF00
                this.mode = CHECK1
            }
            if (mode == CHECK1) {
                if (z.avail_in == 0) return r
                r = f

                z.avail_in--
                z.total_in++
                this.need += z.next_in!![z.next_in_index++].toInt() shl 0 and 0xFF

                if (flags != 0) {  // gzip
                    this.need = need.splitToBytes { b3, b2, b1, b0 ->
                        combineToInt(b0, b1, b2, b3)
                    }
                }

                if (this.was.toInt() != this.need.toInt()) {
                    z.msg = "incorrect data check"
                    // chack is delayed
                    /*
      this.mode = BAD;
      this.marker = 5;       // can't try inflateSync
      break;
  */
                } else if (flags != 0 && gzipHeader != null) {
                    gzipHeader!!.crc = this.need.toInt()
                }

                this.mode = LENGTH
            }
            if (mode == LENGTH) {
                if (wrap != 0 && flags != 0) {

                    try {
                        r = readBytes(4, r, f)
                    } catch (e: Return) {
                        return e.r
                    }

                    if (z.msg != null && z.msg == "incorrect data check") {
                        this.mode =
                                BAD
                        this.marker = 5       // can't try inflateSync
                        continue@loop
                    }

                    if (this.need != z.total_out.toInt()) {
                        z.msg = "incorrect length check"
                        this.mode =
                                BAD
                        continue@loop
                    }
                    z.msg = null
                } else {
                    if (z.msg != null && z.msg == "incorrect data check") {
                        this.mode =
                                BAD
                        this.marker = 5       // can't try inflateSync
                        continue@loop
                    }
                }

                this.mode = DONE
            }
            if (mode == DONE) {
                return Z_STREAM_END
            }
            if (mode == BAD) {
                return Z_DATA_ERROR
            }
            if (mode == FLAGS) {
                try {
                    r = readBytes(2, r, f)
                } catch (e: Return) {
                    return e.r
                }

                flags = this.need.toInt() and 0xFFFF

                if (flags and 0xFF != Z_DEFLATED) {
                    z.msg = "unknown compression method"
                    this.mode = BAD
                    continue@loop
                }
                if (flags and 0xe000 != 0) {
                    z.msg = "unknown header flags set"
                    this.mode = BAD
                    continue@loop
                }

                if (flags and 0x0200 != 0) {
                    checksum(2, this.need)
                }

                this.mode = TIME
            }
            if (mode == TIME) {
                try {
                    r = readBytes(4, r, f)
                } catch (e: Return) {
                    return e.r
                }

                if (gzipHeader != null)
                    gzipHeader!!.time = this.need.toLong() and 0xFFFFFFFFL
                if (flags and 0x0200 != 0) {
                    checksum(4, this.need)
                }
                this.mode = OS
            }
            if (mode == OS) {
                try {
                    r = readBytes(2, r, f)
                } catch (e: Return) {
                    return e.r
                }

                if (gzipHeader != null) {
                    gzipHeader!!.xflags = this.need.toInt() and 0xFF
                    gzipHeader!!.os = this.need.toInt() shr 8 and 0xFF
                }
                if (flags and 0x0200 != 0) {
                    checksum(2, this.need)
                }
                this.mode = EXLEN
            }
            if (mode == EXLEN) {
                if (flags and 0x0400 != 0) {
                    try {
                        r = readBytes(2, r, f)
                    } catch (e: Return) {
                        return e.r
                    }

                    if (gzipHeader != null) {
                        gzipHeader!!.extra =
                                ByteArray(this.need.toInt() and 0xFFFF)
                    }
                    if (flags and 0x0200 != 0) {
                        checksum(2, this.need)
                    }
                } else if (gzipHeader != null) {
                    gzipHeader!!.extra = null
                }
                this.mode = EXTRA
            }
            if (mode == EXTRA) {
                if (flags and 0x0400 != 0) {
                    try {
                        r = readBytes(r, f)
                        if (gzipHeader != null) {
                            val foo =
                                tmp_string!!.bufferSlice().asByteArray()
                            tmp_string = null
                            if (foo.size == gzipHeader!!.extra!!.size) {
                                /*System.arraycopy(
                                                foo,
                                                0,
                                                gzipHeader!!.extra!!,
                                                0,
                                                foo.size
                                            )*/
                                copy(foo, gzipHeader!!.extra!!)
                            } else {
                                z.msg = "bad extra field length"
                                this.mode =
                                        BAD
                                continue@loop
                            }
                        }
                    } catch (e: Return) {
                        return e.r
                    }

                } else if (gzipHeader != null) {
                    gzipHeader!!.extra = null
                }
                this.mode = NAME
            }
            if (mode == NAME) {
                if (flags and 0x0800 != 0) {
                    try {
                        r = readString(r, f)
                        if (gzipHeader != null) {
                            gzipHeader!!.name =
                                    tmp_string!!.bufferSlice().asByteArray()
                        }
                        tmp_string = null
                    } catch (e: Return) {
                        return e.r
                    }

                } else if (gzipHeader != null) {
                    gzipHeader!!.name = null
                }
                this.mode = COMMENT
            }
            if (mode == COMMENT) {
                if (flags and 0x1000 != 0) {
                    try {
                        r = readString(r, f)
                        if (gzipHeader != null) {
                            gzipHeader!!.comment =
                                    tmp_string!!.bufferSlice().asByteArray()
                        }
                        tmp_string = null
                    } catch (e: Return) {
                        return e.r
                    }

                } else if (gzipHeader != null) {
                    gzipHeader!!.comment = null
                }
                this.mode = HCRC
            }
            if (mode == HCRC) {
                if (flags and 0x0200 != 0) {
                    try {
                        r = readBytes(2, r, f)
                    } catch (e: Return) {
                        return e.r
                    }

                    if (gzipHeader != null) {
                        gzipHeader!!.hcrc = (this.need and 0xFFFF).toInt()
                    }
                    if (this.need != z.adler.value and 0xFFFF) {
                        this.mode = BAD
                        z.msg = "header crc mismatch"
                        this.marker = 5
                        continue@loop
                    }
                }

                z.adler = Crc32()
                this.mode = BLOCKS
                continue@loop
            }

            return Z_STREAM_ERROR;
        }
    }

    fun setDictionary(dictionary: ByteArray, dictLength: Int): Int {
        if (z == null || this.mode != DICT0 && this.wrap != 0) {
            return Z_STREAM_ERROR
        }

        var index = 0
        var length = dictLength

        if (this.mode == DICT0) {
            val adler_need = z.adler.value
            z.adler.reset()
            z.adler.update(dictionary, 0, dictLength)
            if (z.adler.value != adler_need) {
                return Z_DATA_ERROR
            }
        }

        z.adler.reset()

        if (length >= 1 shl this.wbits) {
            length = (1 shl this.wbits) - 1
            index = dictLength - length
        }
        this.blocks!!.set_dictionary(dictionary, index, length)
        this.mode = BLOCKS
        return Z_OK
    }

    fun sync(): Int {
        var n: Int       // number of bytes to look at
        var p: Int       // pointer to bytes
        var m: Int       // number of marker bytes found in a row
        val r: Long
        val w: Long   // temporaries to save total_in and total_out

        // set up
        if (this.mode != BAD) {
            this.mode = BAD
            this.marker = 0
        }
        n = z.avail_in
        if (n == 0)
            return Z_BUF_ERROR

        p = z.next_in_index
        m = this.marker
        // search
        while (n != 0 && m < 4) {
            if (z.next_in!![p] == mark[m]) {
                m++
            } else if (z.next_in!![p].toInt() != 0) {
                m = 0
            } else {
                m = 4 - m
            }
            p++
            n--
        }

        // restore
        z.total_in += (p - z.next_in_index).toLong()
        z.next_in_index = p
        z.avail_in = n
        this.marker = m

        // return no joy or set up to restart on a new block
        if (m != 4) {
            return Z_DATA_ERROR
        }
        r = z.total_in
        w = z.total_out
        reset()
        z.total_in = r
        z.total_out = w
        this.mode = BLOCKS

        return Z_OK
    }

    // Returns true if inflate is currently at the end of a block generated
    // by Z_SYNC_FLUSH or Z_FULL_FLUSH. This function is used by one PPP
    // implementation to provide an additional safety check. PPP uses Z_SYNC_FLUSH
    // but removes the length bytes of the resulting empty stored block. When
    // decompressing, PPP checks that at the end of input packet, inflate is
    // waiting for these length bytes.
    fun syncPoint(): Int {
        return if (z == null || this.blocks == null) Z_STREAM_ERROR else this.blocks!!.sync_point()
    }

    @Throws(Return::class)
    private fun readBytes(n: Int, r: Int, f: Int): Int {
        var r = r
        if (need_bytes == -1) {
            need_bytes = n
            this.need = 0
        }
        while (need_bytes > 0) {
            if (z.avail_in == 0) {
                throw Return(r)
            }
            r = f
            z.avail_in--
            z.total_in++
            this.need = this.need or
                    (z.next_in!![z.next_in_index++].toInt() and 0xFF shl (n - need_bytes) * 8)
            need_bytes--
        }
        if (n == 2) {
            this.need = this.need and 0xFFFF
        } else if (n == 4) {
            this.need = this.need // TODO: What do here?
        }
        need_bytes = -1
        return r
    }

    internal inner class Return(var r: Int) : Exception()

    @Throws(Return::class)
    private fun readString(r: Int, f: Int): Int {
        var r = r
        if (tmp_string == null) {
            tmp_string = MemoryViewStreamDefault()
        }
        var b = 0
        do {
            if (z.avail_in == 0) {
                throw Return(r)
            }
            r = f
            z.avail_in--
            z.total_in++
            b = z.next_in!![z.next_in_index].toInt()
            if (b != 0) tmp_string!!.put(z.next_in!![z.next_in_index])
            z.adler.update(z.next_in!!, z.next_in_index, 1)
            z.next_in_index++
        } while (b != 0)
        return r
    }

    @Throws(Return::class)
    private fun readBytes(r: Int, f: Int): Int {
        var r = r
        if (tmp_string == null) {
            tmp_string = MemoryViewStreamDefault()
        }
        var b = 0
        while (this.need > 0) {
            if (z.avail_in == 0) {
                throw Return(r)
            }
            r = f
            z.avail_in--
            z.total_in++
            b = z.next_in!![z.next_in_index].toInt()
            tmp_string!!.put(z.next_in!![z.next_in_index])
            z.adler.update(z.next_in!!, z.next_in_index, 1)
            z.next_in_index++
            this.need--
        }
        return r
    }

    private fun checksum(n: Int, v: Int) {
        var v = v
        for (i in 0 until n) {
            crcbuf[i] = (v and 0xFF).toByte()
            v = v shr 8
        }
        z.adler.update(crcbuf, 0, n)
    }

    fun inParsingHeader(): Boolean {
        when (mode) {
            HEAD, DICT4, DICT3, DICT2, DICT1, FLAGS, TIME, OS, EXLEN, EXTRA, NAME, COMMENT, HCRC -> return true
            else -> return false
        }
    }
}

// preset dictionary flag in zlib header
private const val PRESET_DICT = 0x20

private const val METHOD = 0   // waiting for method byte
private const val FLAG = 1     // waiting for flag byte
private const val DICT4 = 2    // four dictionary check bytes to go
private const val DICT3 = 3    // three dictionary check bytes to go
private const val DICT2 = 4    // two dictionary check bytes to go
private const val DICT1 = 5    // one dictionary check byte to go
private const val DICT0 = 6    // waiting for inflateSetDictionary
private const val BLOCKS = 7   // decompressing blocks
private const val CHECK4 = 8   // four check bytes to go
private const val CHECK3 = 9   // three check bytes to go
private const val CHECK2 = 10  // two check bytes to go
private const val CHECK1 = 11  // one check byte to go
private const val DONE = 12    // finished check, done
private const val BAD = 13     // got an error--stay here

private const val HEAD = 14
private const val LENGTH = 15
private const val TIME = 16
private const val OS = 17
private const val EXLEN = 18
private const val EXTRA = 19
private const val NAME = 20
private const val COMMENT = 21
private const val HCRC = 22
private const val FLAGS = 23

internal const val INFLATE_ANY = 0x40000000

private val mark =
    byteArrayOf(0.toByte(), 0.toByte(), 0xFF.toByte(), 0xFF.toByte())
