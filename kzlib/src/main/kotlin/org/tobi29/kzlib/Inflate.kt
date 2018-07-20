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

import org.tobi29.stdex.copy
import org.tobi29.stdex.splitToBytes
import kotlin.experimental.and

//local unsigned syncsearch OF((unsigned FAR *have, const unsigned char FAR *buf,
//unsigned len));

private fun inflateStateCheck(
    strm: z_stream,
    state: inflate_state
): Boolean {
    if (strm == Z_NULL
    /* || strm.zalloc == (alloc_func)0 || strm.zfree == (free_func)0 */)
        return true
    if (state == Z_NULL
    /* || state.strm != strm || state.mode < inflate_mode.HEAD || state.mode > inflate_mode.SYNC */)
        return true
    return false
}

fun inflateResetKeep(
    strm: z_stream,
    state: inflate_state
): Int {
    if (inflateStateCheck(strm, state)) return Z_STREAM_ERROR
    strm.total_in = 0
    strm.total_out = 0
    state.total = 0
    strm.msg = Z_NULL
    if (state.wrap != 0)        /* to support ill-conceived Java test suite */
        strm.adler = state.wrap and 1
    state.mode = inflate_mode.HEAD
    state.last = 0
    state.havedict = 0
    state.dmax = 32768
    state.head = Z_NULL
    state.hold = 0
    state.bits = 0
    state.lencode = state.codes
    state.lencode_i = 0
    state.distcode = state.codes
    state.distcode_i = 0
    state.next = 0
    state.sane = true
    state.back = -1
    // Tracev((stderr, "inflate: reset\n"));
    return Z_OK
}

fun inflateReset(
    strm: z_stream,
    state: inflate_state
): Int {
    if (inflateStateCheck(strm, state)) return Z_STREAM_ERROR
    state.wsize = 0
    state.whave = 0
    state.wnext = 0
    return inflateResetKeep(strm, state)
}

fun inflateReset2(
    strm: z_stream,
    state: inflate_state,
    windowBits: Int
): Int {
    var windowBits = windowBits

    /* get the state */
    if (inflateStateCheck(strm, state)) return Z_STREAM_ERROR

    /* extract wrap request from windowBits parameter */
    val wrap: Int
    if (windowBits < 0) {
        wrap = 0
        windowBits = -windowBits
    } else {
        wrap = (windowBits ushr 4) + 5
        //#ifdef GUNZIP
        if (windowBits < 48)
            windowBits = windowBits and 15
        //#endif
    }

    /* set number of window bits, free window if different */
    if (windowBits != 0 && (windowBits < 8 || windowBits > 15))
        return Z_STREAM_ERROR
    if (state.window != Z_NULL && state.wbits != windowBits) {
        state.window = Z_NULL
    }

    /* update state and reset the rest of it */
    state.wrap = wrap
    state.wbits = windowBits
    return inflateReset(strm, state)
}

fun inflateInit(
    strm: z_stream,
    state: inflate_state,
    windowBits: Int = DEF_WBITS
): Int {
    if (strm == Z_NULL) return Z_STREAM_ERROR
    strm.msg = Z_NULL /* in case we return an error */
    if (state == Z_NULL) return Z_MEM_ERROR
    //Tracev((stderr, "inflate: allocated\n"));
    state.window = Z_NULL
    state.mode = inflate_mode.HEAD /* to pass state test in inflateReset2() */
    return inflateReset2(strm, state, windowBits)
}

fun inflatePrime(
    strm: z_stream,
    state: inflate_state,
    bits: Int,
    value: Int
): Int {
    if (inflateStateCheck(strm, state)) return Z_STREAM_ERROR
    if (bits < 0) {
        state.hold = 0
        state.bits = 0
        return Z_OK
    }
    if (bits > 16 || state.bits + bits > 32) return Z_STREAM_ERROR
    val value = value and (1 shl bits) - 1
    state.hold += value shl state.bits
    state.bits += bits
    return Z_OK
}

/*
   Return state with length and distance decoding tables and index sizes set to
   fixed code decoding.  Normally this returns fixed tables from inffixed.h.
   If BUILDFIXED is defined, then instead this routine builds the tables the
   first time it's called, and returns those tables the first time and
   thereafter.  This reduces the size of the code by about 2K bytes, in
   exchange for a little execution time.  However, BUILDFIXED should not be
   used for threaded applications, since the rewriting of the tables and virgin
   may not be thread-safe.
 */
private fun fixedtables(state: inflate_state) {
    state.lencode = fixed
    state.lencode_i = lenfix
    state.lenbits = 9
    state.distcode = fixed
    state.distcode_i = distfix
    state.distbits = 5
}

/*
   Update the window with the last wsize (normally 32K) bytes written before
   returning.  If window does not exist yet, create it.  This is only called
   when a window is already in use, or when output has been written during this
   inflate call, but the end of the deflate stream has not been reached yet.
   It is also called to create a window for dictionary data when a dictionary
   is loaded.
   Providing output buffers larger than 32K to inflate() should provide a speed
   advantage, since only the last 32K of output is copied to the sliding window
   upon return from inflate(), and since all distances after the first 32K of
   output will fall in the output data, making match copies simpler and faster.
   The advantage may be dependent on the size of the processor's data caches.
 */
private fun updatewindow(
    strm: z_stream,
    state: inflate_state,
    end: ByteArray,
    end_i: UInt,
    copy: UInt
): Int {
    /* if it hasn't been done already, allocate space for the window */
    val window =
        state.window ?: ByteArray(1 shl state.wbits).also { state.window = it }


    /* if window not in use yet, initialize */
    if (state.wsize == 0) {
        state.wsize = 1 shl state.wbits
        state.wnext = 0
        state.whave = 0
    }

    /* copy state.wsize or less output bytes into the circular window */
    if (copy >= state.wsize) {
        zmemcpy(
            window, 0,
            end, end_i - state.wsize,
            state.wsize
        )
        state.wnext = 0
        state.whave = state.wsize
    } else {
        var dist = state.wsize - state.wnext
        if (dist > copy) dist = copy
        zmemcpy(
            window, state.wnext,
            end, end_i - copy,
            dist
        )
        val copy = copy - dist
        if (copy != 0) {
            zmemcpy(
                window, 0,
                end, end_i - copy,
                copy
            )
            state.wnext = copy
            state.whave = state.wsize
        } else {
            state.wnext += dist
            if (state.wnext == state.wsize) state.wnext = 0
            if (state.whave < state.wsize) state.whave += dist
        }
    }

    return 0
}

/* Macros for inflate(): */

/* check function to use adler32() for zlib or crc32() for gzip */
//#ifdef GUNZIP
private inline fun UPDATE(
    state: inflate_state,
    check: UInt,
    buf: ByteArray,
    buf_i: Int,
    len: Int
): UInt = if (state.flags != 0) crc32(check, buf, buf_i, len)
else adler32(check, buf, buf_i, len)
//#else
//#  define UPDATE(check, buf, len) adler32(check, buf, len)
//#endif

/* check macros for header crc */
//#ifdef GUNZIP
private inline fun CRC2(
    hbuf: ByteArray,
    check: UInt,
    word: Short
): UInt {
    word.splitToBytes { b1, b0 ->
        hbuf[0] = b1
        hbuf[1] = b0
    }
    return crc32(check, hbuf, 0, 2)
}

private inline fun CRC4(
    hbuf: ByteArray,
    check: UInt,
    word: Int
): UInt {
    word.splitToBytes { b3, b2, b1, b0 ->
        hbuf[0] = b3
        hbuf[1] = b2
        hbuf[2] = b1
        hbuf[3] = b0
    }
    return crc32(check, hbuf, 0, 2)
}
//#endif

/* Load registers with state in inflate() for speed */
/*#define LOAD() \
do {
    \
    put = strm.next_out; \
    left = strm.avail_out; \
    next = strm.next_in; \
    have = strm.avail_in; \
    hold = state.hold; \
    bits = state.bits; \
} while (0)*/

/* Restore state from registers in inflate() */
/*#define RESTORE() \
do {
    \
    strm.next_out = put; \
    strm.avail_out = left; \
    strm.next_in = next; \
    strm.avail_in = have; \
    state.hold = hold; \
    state.bits = bits; \
} while (0)*/

/* Clear the input bit accumulator */
/*#define INITBITS() \
do {
    \
    hold = 0; \
    bits = 0; \
} while (0)*/

/* Get a byte of input into the bit accumulator, or return from inflate()
   if there is no input available. */
/*#define PULLBYTE() \
do {
    \
    if (have == 0) break@loop \
    have--; \
    hold += (unsigned long)(*next++) < < bits; \
    bits += 8; \
} while (0)*/

/* Assure that there are at least n bits in the bit accumulator.  If there is
   not enough available input to do that, then return from inflate(). */
/*#define NEEDBITS(n) \
do {
    \
    while (bits < (unsigned)(n)) \
    PULLBYTE(); \
} while (0)*/

/* Return the low n bits of the bit accumulator (n < 16) */
private inline fun BITS(hold: UInt, n: UInt) =
    hold and ((1 shl n) - 1)

/* Remove n bits from the bit accumulator */
/*#define DROPBITS(n) \
do {
    \
    hold > >=(n); \
    bits -= (unsigned)(n); \
} while (0)*/

/* Remove zero to seven bits as needed to go to a byte boundary */
/*#define BYTEBITS() \
do {
    \
    hold > >= bits & 7; \
    bits -= bits & 7; \
} while (0)*/

/*
   inflate() uses a state machine to process as much input data and generate as
   much output data as possible before returning.  The state machine is
   structured roughly as follows:
    for (;;) switch (state) {
    ...
    case STATEn:
        if (not enough input data or output space to make progress)
            return;
        ... make progress ...
        state = STATEm;
        break;
    ...
    }
   so when inflate() is called again, the same case is attempted again, and
   if the appropriate resources are provided, the machine proceeds to the
   next state.  The NEEDBITS() macro is usually the way the state evaluates
   whether it can proceed or should return.  NEEDBITS() does the return if
   the requested bits are not available.  The typical use of the BITS macros
   is:
        NEEDBITS(n);
        ... do something with BITS(n) ...
        DROPBITS(n);
   where NEEDBITS(n) either returns from inflate() if there isn't enough
   input left to load n bits into the accumulator, or it continues.  BITS(n)
   gives the low n bits in the accumulator.  When done, DROPBITS(n) drops
   the low n bits off the accumulator.  INITBITS() clears the accumulator
   and sets the number of available bits to zero.  BYTEBITS() discards just
   enough bits to put the accumulator on a byte boundary.  After BYTEBITS()
   and a NEEDBITS(8), then BITS(8) would return the next byte in the stream.
   NEEDBITS(n) uses PULLBYTE() to get an available byte of input, or to return
   if there is no input available.  The decoding of variable length codes uses
   PULLBYTE() directly in order to pull just enough bytes to decode the next
   code, and no more.
   Some states loop until they get enough input, making sure that enough
   state information is maintained to continue the loop where it left off
   if NEEDBITS() returns in the loop.  For example, want, need, and keep
   would all have to actually be part of the saved state in case NEEDBITS()
   returns:
    case STATEw:
        while (want < need) {
            NEEDBITS(n);
            keep[want++] = BITS(n);
            DROPBITS(n);
        }
        state = STATEx;
    case STATEx:
   As shown above, if the next state is also the next case, then the break
   is omitted.
   A state may also return if there is not enough output space available to
   complete that state.  Those states are copying stored data, writing a
   literal byte, and copying a matching string.
   When returning, a "goto inf_leave" is used to update the total counters,
   update the check value, and determine whether any progress has been made
   during that inflate() call in order to return the proper return code.
   Progress is defined as a change in either strm.avail_in or strm.avail_out.
   When there is a window, goto inf_leave will update the window with the last
   output written.  If a goto inf_leave occurs in the middle of decompression
   and there is no window currently, goto inf_leave will create one and copy
   output to the window for the next call of inflate().
   In this implementation, the flush parameter of inflate() only affects the
   return code (per zlib.h).  inflate() always writes as much as possible to
   strm.next_out, given the space available and the provided input--the effect
   documented in zlib.h of Z_SYNC_FLUSH.  Furthermore, inflate() always defers
   the allocation of and copying into a sliding window until necessary, which
   provides the effect documented in zlib.h for Z_FINISH when the entire input
   stream available.  So the only thing the flush parameter actually does is:
   when flush is set to Z_FINISH, inflate() cannot return Z_OK.  Instead it
   will return Z_BUF_ERROR if it has not reached the end of the stream.
 */

fun inflate(strm: z_stream, state: inflate_state, flush: Int): Int {
    val hbuf = ByteArray(4) /* buffer for gzip header crc calculation */
    val next_ref = IntArray(1)
    val lenbits_ref = IntArray(1)
    val distbits_ref = IntArray(1)

    if (inflateStateCheck(strm, state) || strm.next_out == Z_NULL ||
        (strm.next_in == Z_NULL && strm.avail_in != 0))
        return Z_STREAM_ERROR

    if (state.mode == inflate_mode.TYPE) state.mode =
            inflate_mode.TYPEDO /* skip check */

    // LOAD();
    var put = strm.next_out!! /* next output */
    var put_i = strm.next_out_i
    var left = strm.avail_out /* available output */
    var next = strm.next_in!! /* next input */
    var next_i = strm.next_in_i
    var have = strm.avail_in /* available input */
    var hold = state.hold /* bit buffer */
    var bits = state.bits /* bits in bit buffer */

    var `in` = have /* save starting available input */
    var out = left /* save starting available output */
    var ret = Z_OK /* return code */
    loop@ while (true) when (state.mode) {
        inflate_mode.HEAD -> {
            if (state.wrap == 0) {
                state.mode = inflate_mode.TYPEDO
                continue@loop
            }

            // NEEDBITS(16);
            while (bits < 16) {
                if (have == 0) break@loop
                have--
                hold += next[next_i++].toUInt() shl bits
                bits += 8
            }

            //#ifdef GUNZIP
            if ((state.wrap and 2) != 0 && hold == 0x8b1f) {
                /* gzip header */
                if (state.wbits == 0)
                    state.wbits = 15
                state.check = crc32(0, Z_NULL, 0, 0)
                CRC2(hbuf, state.check, hold.toShort())

                // INITBITS();
                hold = 0
                bits = 0

                state.mode = inflate_mode.FLAGS
                continue@loop
            }
            state.flags = 0           /* expect zlib header */
            if (state.head != Z_NULL)
                state.head!!.done = -1
            if ((state.wrap and 1) == 0 ||   /* check if zlib header allowed */
                /*#else
            if (
            #endif*/
                ((BITS(hold, 8) shl 8) + (hold ushr 8)) % 31 != 0) {
                strm.msg = "incorrect header check"
                state.mode = inflate_mode.BAD
                continue@loop
            }
            if (BITS(hold, 4) != Z_DEFLATED) {
                strm.msg = "unknown compression method"
                state.mode = inflate_mode.BAD
                continue@loop
            }

            // DROPBITS(4);
            hold = hold ushr 4
            bits -= 4

            val len = BITS(hold, 4) + 8
            if (state.wbits == 0)
                state.wbits = len
            if (len > 15 || len > state.wbits) {
                strm.msg = "invalid window size"
                state.mode = inflate_mode.BAD
                continue@loop
            }
            state.dmax = 1 shl len
            // Tracev((stderr, "inflate:   zlib header ok\n"));
            strm.adler = adler32(0, Z_NULL, 0, 0)
            state.check = strm.adler
            state.mode = if (hold and 0x200 != 0) inflate_mode.DICTID
            else inflate_mode.TYPE

            // INITBITS();
            hold = 0
            bits = 0

            //break;
            //#ifdef GUNZIP
        }
        inflate_mode.FLAGS -> {
            // NEEDBITS(16);
            while (bits < 16) {
                if (have == 0) break@loop
                have--
                hold += next[next_i++].toUInt() shl bits
                bits += 8
            }

            state.flags = hold
            if ((state.flags and 0xff) != Z_DEFLATED) {
                strm.msg = "unknown compression method"
                state.mode = inflate_mode.BAD
                continue@loop
            }
            if (state.flags and 0xe000 != 0) {
                strm.msg = "unknown header flags set"
                state.mode = inflate_mode.BAD
                continue@loop
            }
            if (state.head != Z_NULL)
                state.head!!.text = (hold ushr 8) and 1
            if ((state.flags and 0x0200) != 0 && (state.wrap and 4) != 0)
                CRC2(hbuf, state.check, hold.toShort())

            // INITBITS();
            hold = 0
            bits = 0

            state.mode = inflate_mode.TIME
        }
        inflate_mode.TIME -> {
            // NEEDBITS(32);
            while (bits < 32) {
                if (have == 0) break@loop
                have--
                hold += next[next_i++].toUInt() shl bits
                bits += 8
            }

            if (state.head != Z_NULL)
                state.head!!.time = hold.toULong()
            if ((state.flags and 0x0200) != 0 && (state.wrap and 4) != 0)
                CRC4(hbuf, state.check, hold)

            // INITBITS();
            hold = 0
            bits = 0

            state.mode = inflate_mode.OS
        }
        inflate_mode.OS -> {
            // NEEDBITS(16);
            while (bits < 16) {
                if (have == 0) break@loop
                have--
                hold += next[next_i++].toUInt() shl bits
                bits += 8
            }

            if (state.head != Z_NULL) {
                state.head!!.xflags = hold and 0xff
                state.head!!.os = hold ushr 8
            }
            if ((state.flags and 0x0200) != 0 && (state.wrap and 4) != 0)
                CRC2(hbuf, state.check, hold.toShort())

            // INITBITS();
            hold = 0
            bits = 0

            state.mode = inflate_mode.EXLEN
        }
        inflate_mode.EXLEN -> {
            if (state.flags and 0x0400 != 0) {
                // NEEDBITS(16);
                while (bits < 16) {
                    if (have == 0) break@loop
                    have--
                    hold += next[next_i++].toUInt() shl bits
                    bits += 8
                }

                state.length = hold
                if (state.head != Z_NULL)
                    state.head!!.extra_len = hold
                if ((state.flags and 0x0200) != 0 && (state.wrap and 4) != 0)
                    CRC2(hbuf, state.check, hold.toShort())

                // INITBITS();
                hold = 0
                bits = 0
            } else if (state.head != Z_NULL)
                state.head!!.extra = Z_NULL
            state.mode = inflate_mode.EXTRA
        }
        inflate_mode.EXTRA -> {
            if (state.flags and 0x0400 != 0) {
                var copy = state.length
                if (copy > have) copy = have
                if (copy != 0) {
                    if (state.head != Z_NULL &&
                        state.head!!.extra != Z_NULL) {
                        val len = state.head!!.extra_len - state.length
                        zmemcpy(
                            state.head!!.extra!!, len,
                            next, next_i,
                            if (len + copy > state.head!!.extra_max)
                                state.head!!.extra_max - len else copy
                        )
                    }
                    if ((state.flags and 0x0200) != 0 && (state.wrap and 4) != 0)
                        state.check = crc32(state.check, next, next_i, copy)
                    have -= copy
                    next_i += copy
                    state.length -= copy
                }
                if (state.length != 0) break@loop
            }
            state.length = 0
            state.mode = inflate_mode.NAME
        }
        inflate_mode.NAME -> {
            if (state.flags and 0x0800 != 0) {
                if (have == 0) break@loop
                var copy = 0
                var len: Int
                do {
                    len = next[next_i + copy].toUInt()
                    copy++
                    if (state.head != Z_NULL &&
                        state.head!!.name != Z_NULL &&
                        state.length < state.head!!.name_max)
                        state.head!!.name!![state.length] = len.toByte()
                    state.length++
                } while (len != 0 && copy < have)
                if ((state.flags and 0x0200) != 0 && (state.wrap and 4) != 0)
                    state.check = crc32(state.check, next, next_i, copy)
                have -= copy
                next_i += copy
                if (len != 0) break@loop
            } else if (state.head != Z_NULL)
                state.head!!.name = Z_NULL
            state.length = 0
            state.mode = inflate_mode.COMMENT
        }
        inflate_mode.COMMENT -> {
            if (state.flags and 0x1000 != 0) {
                if (have == 0) break@loop
                var copy = 0
                var len: UInt
                do {
                    len = next[next_i + copy].toUInt()
                    copy++
                    if (state.head != Z_NULL &&
                        state.head!!.comment != Z_NULL &&
                        state.length < state.head!!.comm_max)
                        state.head!!.comment!![state.length++] = len.toByte()
                } while (len != 0 && copy < have)
                if ((state.flags and 0x0200) != 0 && (state.wrap and 4) != 0)
                    state.check = crc32(state.check, next, next_i, copy)
                have -= copy
                next_i += copy
                if (len != 0) break@loop
            } else if (state.head != Z_NULL)
                state.head!!.comment = Z_NULL
            state.mode = inflate_mode.HCRC
        }
        inflate_mode.HCRC -> {
            if (state.flags and 0x0200 != 0) {
                // NEEDBITS(16);
                while (bits < 16) {
                    if (have == 0) break@loop
                    have--
                    hold += next[next_i++].toUInt() shl bits
                    bits += 8
                }

                if ((state.wrap and 4) != 0 && hold != (state.check and 0xffff)) {
                    strm.msg = "header crc mismatch"
                    state.mode = inflate_mode.BAD
                    continue@loop
                }

                // INITBITS();
                hold = 0
                bits = 0
            }
            if (state.head != Z_NULL) {
                state.head!!.hcrc = (state.flags ushr 9) and 1
                state.head!!.done = 1
            }
            strm.adler = crc32(0, Z_NULL, 0, 0)
            state.check = strm.adler
            state.mode = inflate_mode.TYPE
            //break;
            //#endif
        }
        inflate_mode.DICTID -> {
            // NEEDBITS(32);
            while (bits < 16) {
                if (have == 0) break@loop
                have--
                hold += next[next_i++].toUInt() shl bits
                bits += 8
            }

            strm.adler = ZSWAP32(hold)
            state.check = strm.adler

            // INITBITS();
            hold = 0
            bits = 0

            state.mode = inflate_mode.DICT
        }
        inflate_mode.DICT -> {
            if (state.havedict == 0) {

                // RESTORE();
                strm.next_out = put
                strm.next_out_i = put_i
                strm.avail_out = left
                strm.next_in = next
                strm.next_in_i = next_i
                strm.avail_in = have
                state.hold = hold
                state.bits = bits

                return Z_NEED_DICT
            }
            strm.adler = adler32(0, Z_NULL, 0, 0)
            state.check = strm.adler
            state.mode = inflate_mode.TYPE
        }
        inflate_mode.TYPE -> {
            if (flush == Z_BLOCK || flush == Z_TREES) break@loop
            state.mode = inflate_mode.TYPEDO
        }
        inflate_mode.TYPEDO -> {
            if (state.last != 0) {

                // BYTEBITS();
                hold = hold ushr (bits and 7)
                bits -= bits and 7

                state.mode = inflate_mode.CHECK
                continue@loop
            }

            // NEEDBITS(3);
            while (bits < 16) {
                if (have == 0) break@loop
                have--
                hold += next[next_i++].toUInt() shl bits
                bits += 8
            }

            state.last = BITS(hold, 1)

            // DROPBITS(1);
            hold = hold ushr 1
            bits -= 1

            when (BITS(hold, 2)) {
                0 -> {                             /* stored block */
                    /*Tracev(
                    (stderr, "inflate:     stored block%s\n",
                    state.last ? " (last)" : ""));*/
                    state.mode = inflate_mode.STORED
                }
                1 -> {                             /* fixed block */
                    fixedtables(state)
                    /*Tracev(
                    (stderr, "inflate:     fixed codes block%s\n",
                    state.last ? " (last)" : ""));*/
                    state.mode =
                            inflate_mode.LEN_             /* decode codes */
                    if (flush == Z_TREES) {
                        // DROPBITS(2);
                        hold = hold ushr 2
                        bits -= 2

                        break@loop
                    }
                }
                2 -> {                             /* dynamic block */
                    /*Tracev(
                    (stderr, "inflate:     dynamic codes block%s\n",
                    state.last ? " (last)" : ""));*/
                    state.mode = inflate_mode.TABLE
                }
                3 -> {
                    strm.msg = "invalid block type"
                    state.mode = inflate_mode.BAD
                }
            }

            // DROPBITS(2);
            hold = hold ushr 2
            bits -= 2

            //break;
        }
        inflate_mode.STORED -> {
            // BYTEBITS();                         /* go to byte boundary */
            hold = hold ushr (bits and 7)
            bits -= bits and 7

            // NEEDBITS(32);
            while (bits < 32) {
                if (have == 0) break@loop
                have--
                hold += next[next_i++].toUInt() shl bits
                bits += 8
            }

            if ((hold and 0xffff) != ((hold ushr 16) xor 0xffff)) {
                strm.msg = "invalid stored block lengths"
                state.mode = inflate_mode.BAD
                continue@loop
            }
            state.length = hold and 0xffff
            /*Tracev(
                (stderr, "inflate:       stored length %u\n",
                state.length
            ));*/

            // INITBITS();
            hold = 0
            bits = 0

            state.mode = inflate_mode.COPY_
            if (flush == Z_TREES) break@loop
        }
        inflate_mode.COPY_ -> {
            state.mode = inflate_mode.COPY
        }
        inflate_mode.COPY -> {
            var copy = state.length
            if (copy != 0) {
                if (copy > have) copy = have
                if (copy > left) copy = left
                if (copy == 0) break@loop
                zmemcpy(put, put_i, next, next_i, copy)
                have -= copy
                next_i += copy
                left -= copy
                put_i += copy
                state.length -= copy
                continue@loop
            }
            // Tracev((stderr, "inflate:       stored end\n"));
            state.mode = inflate_mode.TYPE
            //break;
        }
        inflate_mode.TABLE -> {
            // NEEDBITS(14);
            while (bits < 14) {
                if (have == 0) break@loop
                have--
                hold += next[next_i++].toUInt() shl bits
                bits += 8
            }

            state.nlen = BITS(hold, 5) + 257

            // DROPBITS(5);
            hold = hold ushr 5
            bits -= 5

            state.ndist = BITS(hold, 5) + 1

            // DROPBITS(5);
            hold = hold ushr 5
            bits -= 5

            state.ncode = BITS(hold, 4) + 4

            // DROPBITS(4);
            hold = hold ushr 4
            bits -= 4

            //#ifndef PKZIP_BUG_WORKAROUND
            if (state.nlen > 286 || state.ndist > 30) {
                strm.msg = "too many length or distance symbols"
                state.mode = inflate_mode.BAD
                continue@loop
            }
            //#endif
            //Tracev((stderr, "inflate:       table sizes ok\n"));
            state.have = 0
            state.mode = inflate_mode.LENLENS
        }
        inflate_mode.LENLENS -> {
            while (state.have < state.ncode) {
                // NEEDBITS(3);
                while (bits < 3) {
                    if (have == 0) break@loop
                    have--
                    hold += next[next_i++].toUInt() shl bits
                    bits += 8
                }

                state.lens[order[state.have++].toUInt()] =
                        BITS(hold, 3).toShort()

                // DROPBITS(3);
                hold = hold ushr 3
                bits -= 3
            }
            while (state.have < 19)
                state.lens[order[state.have++].toUInt()] = 0
            state.next = 0
            state.lencode = state.codes
            state.lencode_i = 0
            state.lenbits = 7
            next_ref[0] = state.next
            lenbits_ref[0] = state.lenbits
            ret = inflate_table(
                codetype.CODES, state.lens, 0, 19, state.codes, next_ref,
                lenbits_ref, state.work
            )
            state.next = next_ref[0]
            state.lenbits = lenbits_ref[0]
            if (ret != 0) {
                strm.msg = "invalid code lengths set"
                state.mode = inflate_mode.BAD
                continue@loop
            }
            //Tracev((stderr, "inflate:       code lengths ok\n"));
            state.have = 0
            state.mode = inflate_mode.CODELENS
        }
        inflate_mode.CODELENS -> {
            while (state.have < state.nlen + state.ndist) {
                val here = code()
                while (true) {
                    here.set(
                        state.lencode!![state.lencode_i +
                                BITS(hold, state.lenbits)]
                    )
                    if (here.bits.toUInt() <= bits) break

                    // PULLBYTE();
                    if (have == 0) break@loop
                    have--
                    hold += next[next_i++].toUInt() shl bits
                    bits += 8
                }
                if (here.`val`.toUInt() < 16) {

                    // DROPBITS(here.bits);
                    hold = hold ushr here.bits.toUInt()
                    bits -= here.bits.toUInt()

                    state.lens[state.have++] = here.`val`
                } else {
                    val len: UInt
                    var copy: UInt
                    if (here.`val` == 16.toShort()) {
                        // NEEDBITS(here.bits + 2);
                        while (bits < here.bits + 2) {
                            if (have == 0) break@loop
                            have--
                            hold += next[next_i++].toUInt() shl bits
                            bits += 8
                        }

                        // DROPBITS(here.bits.toUInt());
                        hold = hold ushr here.bits.toUInt()
                        bits -= here.bits.toUInt()

                        if (state.have == 0) {
                            strm.msg = "invalid bit length repeat"
                            state.mode = inflate_mode.BAD
                            break
                        }
                        len = state.lens[state.have - 1].toUInt()
                        copy = 3 + BITS(hold, 2)

                        // DROPBITS(2);
                        hold = hold ushr 2
                        bits -= 2

                    } else if (here.`val` == 17.toShort()) {
                        // NEEDBITS(here.bits + 3);
                        while (bits < here.bits + 3) {
                            if (have == 0) break@loop
                            have--
                            hold += next[next_i++].toUInt() shl bits
                            bits += 8
                        }

                        // DROPBITS(here.bits.toUInt());
                        hold = hold ushr here.bits.toUInt()
                        bits -= here.bits.toUInt()

                        len = 0
                        copy = 3 + BITS(hold, 3)

                        // DROPBITS(3);
                        hold = hold ushr 3
                        bits -= 3
                    } else {
                        // NEEDBITS(here.bits + 7);
                        while (bits < here.bits + 7) {
                            if (have == 0) break@loop
                            have--
                            hold += next[next_i++].toUInt() shl bits
                            bits += 8
                        }

                        // DROPBITS(here.bits.toUInt());
                        hold = hold ushr here.bits.toUInt()
                        bits -= here.bits.toUInt()

                        len = 0
                        copy = 11 + BITS(hold, 7)

                        // DROPBITS(7);
                        hold = hold ushr 7
                        bits -= 7
                    }
                    if (state.have + copy > state.nlen + state.ndist) {
                        strm.msg = "invalid bit length repeat"
                        state.mode = inflate_mode.BAD
                        break
                    }
                    while (copy != 0) {
                        copy--
                        state.lens[state.have++] = len.toShort()
                    }
                }
            }

            /* handle error breaks in while */
            if (state.mode == inflate_mode.BAD) continue@loop

            /* check for end-of-block code (better have one) */
            if (state.lens[256] == 0.toShort()) {
                strm.msg = "invalid code -- missing end-of-block"
                state.mode = inflate_mode.BAD
                continue@loop
            }

            /* build code tables -- note: do not change the lenbits or distbits
           values here (9 and 6) without reading the comments in inftrees.h
           concerning the ENOUGH constants, which depend on those values */
            state.next = 0
            state.lencode = state.codes
            state.lencode_i = state.next
            state.lenbits = 9
            next_ref[0] = state.next
            lenbits_ref[0] = state.lenbits
            ret = inflate_table(
                codetype.LENS, state.lens, 0, state.nlen, state.codes, next_ref,
                lenbits_ref, state.work
            )
            state.next = next_ref[0]
            state.lenbits = lenbits_ref[0]
            if (ret != 0) {
                strm.msg = "invalid literal/lengths set"
                state.mode = inflate_mode.BAD
                continue@loop
            }
            state.distcode = state.codes
            state.distcode_i = state.next
            state.distbits = 6
            next_ref[0] = state.next
            distbits_ref[0] = state.distbits
            ret = inflate_table(
                codetype.DISTS, state.lens, state.nlen, state.ndist,
                state.codes, next_ref, distbits_ref, state.work
            )
            state.next = next_ref[0]
            state.distbits = distbits_ref[0]
            if (ret != 0) {
                strm.msg = "invalid distances set"
                state.mode = inflate_mode.BAD
                continue@loop
            }
            // Tracev((stderr, "inflate:       codes ok\n"));
            state.mode = inflate_mode.LEN_
            if (flush == Z_TREES) break@loop
        }
        inflate_mode.LEN_ -> {
            state.mode = inflate_mode.LEN
        }
        inflate_mode.LEN -> {
            if (have >= 6 && left >= 258) {

                // RESTORE();
                strm.next_out = put
                strm.next_out_i = put_i
                strm.avail_out = left
                strm.next_in = next
                strm.next_in_i = next_i
                strm.avail_in = have
                state.hold = hold
                state.bits = bits

                inflate_fast(strm, state, out)

                // LOAD();
                put = strm.next_out!!
                put_i = strm.next_out_i
                left = strm.avail_out
                next = strm.next_in!!
                next_i = strm.next_in_i
                have = strm.avail_in
                hold = state.hold
                bits = state.bits

                if (state.mode == inflate_mode.TYPE)
                    state.back = -1
                continue@loop
            }
            state.back = 0
            var here: code
            while (true) {
                here = state.lencode!![state.lencode_i +
                        BITS(hold, state.lenbits)]
                if (here.bits.toUInt() <= bits) break

                // PULLBYTE();
                if (have == 0) break@loop
                have--
                hold += next[next_i++].toUInt() shl bits
                bits += 8
            }
            if (here.op != 0.toByte() && (here.op and 0xf0.toByte()) == 0.toByte()) {
                val last = here
                while (true) {
                    here = state.lencode!![state.lencode_i +
                            last.`val`.toUInt() + (BITS(
                        hold, last.bits.toUInt() + last.op.toUInt()
                    ) ushr last.bits.toUInt())]
                    if (last.bits.toUInt() + here.bits.toUInt() <= bits) break

                    // PULLBYTE();
                    if (have == 0) break@loop
                    have--
                    hold += next[next_i++].toUInt() shl bits
                    bits += 8

                }

                // DROPBITS(last.bits.toUInt());
                hold = hold ushr last.bits.toUInt()
                bits -= last.bits.toUInt()

                state.back += last.bits
            }

            // DROPBITS(here.bits.toUInt());
            hold = hold ushr here.bits.toUInt()
            bits -= here.bits.toUInt()

            state.back += here.bits
            state.length = here.`val`.toUInt()
            if (here.op == 0.toByte()) {
                /*Tracevv(
                (stderr, here.
                    val >= 0x20 && here .val < 0x7f ?
            "inflate:         literal '%c'\n" :
            "inflate:         literal 0x%02x\n", here.val ));*/
                state.mode = inflate_mode.LIT
                continue@loop
            }
            if (here.op and 32 != 0.toByte()) {
                //Tracevv((stderr, "inflate:         end of block\n"));
                state.back = -1
                state.mode = inflate_mode.TYPE
                continue@loop
            }
            if (here.op and 64 != 0.toByte()) {
                strm.msg = "invalid literal/length code"
                state.mode = inflate_mode.BAD
                continue@loop
            }
            state.extra = here.op.toUInt() and 15
            state.mode = inflate_mode.LENEXT
        }
        inflate_mode.LENEXT -> {
            if (state.extra != 0) {
                // NEEDBITS(state.extra);
                while (bits < state.extra) {
                    if (have == 0) break@loop
                    have--
                    hold += next[next_i++].toUInt() shl bits
                    bits += 8
                }

                state.length += BITS(hold, state.extra)

                // DROPBITS(state.extra);
                hold = hold ushr state.extra
                bits -= state.extra

                state.back += state.extra
            }
            //Tracevv((stderr, "inflate:         length %u\n", state.length));
            state.was = state.length
            state.mode = inflate_mode.DIST
        }
        inflate_mode.DIST -> {
            val here = code()
            while (true) {
                here.set(
                    state.distcode!![state.distcode_i +
                            BITS(hold, state.distbits)]
                )
                if (here.bits.toUInt() <= bits) break

                // PULLBYTE();
                if (have == 0) break@loop
                have--
                hold += next[next_i++].toUInt() shl bits
                bits += 8
            }
            if ((here.op and 0xf0.toByte()) == 0.toByte()) {
                val last = code()
                last.set(here)
                while (true) {
                    here.set(
                        state.distcode!![state.distcode_i + last.`val` + (
                                BITS(hold, last.bits + last.op) ushr
                                        last.bits.toUInt()
                                )]
                    )
                    if (last.bits.toUInt() + here.bits.toUInt() <= bits) break

                    // PULLBYTE();
                    if (have == 0) break@loop
                    have--
                    hold += next[next_i++].toUInt() shl bits
                    bits += 8
                }

                // DROPBITS(last.bits.toUInt());
                hold = hold ushr last.bits.toUInt()
                bits -= last.bits.toUInt()

                state.back += last.bits
            }

            // DROPBITS(here.bits.toUInt());
            hold = hold ushr here.bits.toUInt()
            bits -= here.bits.toUInt()

            state.back += here.bits
            if (here.op and 64 != 0.toByte()) {
                strm.msg = "invalid distance code"
                state.mode = inflate_mode.BAD
                continue@loop
            }
            state.offset = here.`val`.toUInt()
            state.extra = here.op.toUInt() and 15
            state.mode = inflate_mode.DISTEXT
        }
        inflate_mode.DISTEXT -> {
            if (state.extra != 0) {
                // NEEDBITS(state.extra);
                while (bits < state.extra) {
                    if (have == 0) break@loop
                    have--
                    hold += next[next_i++].toUInt() shl bits
                    bits += 8
                }

                state.offset += BITS(hold, state.extra)

                // DROPBITS(state.extra);
                hold = hold ushr state.extra
                bits -= state.extra

                state.back += state.extra
            }
            //#ifdef INFLATE_STRICT
            if (state.offset > state.dmax) {
                strm.msg = "invalid distance too far back"
                state.mode = inflate_mode.BAD
                continue@loop
            }
            //#endif
            //Tracevv((stderr, "inflate:         distance %u\n", state.offset));
            state.mode = inflate_mode.MATCH
        }
        inflate_mode.MATCH -> {
            if (left == 0) break@loop
            var copy = out - left
            val from: ByteArray
            var from_i: Int
            if (state.offset > copy) {
                /* copy from window */
                copy = state.offset - copy
                if (copy > state.whave) {
                    if (state.sane) {
                        strm.msg = "invalid distance too far back"
                        state.mode = inflate_mode.BAD
                        continue@loop
                    }
                    /*#ifdef INFLATE_ALLOW_INVALID_DISTANCE_TOOFAR_ARRR
                        Trace((stderr, "inflate.c too far\n"));
                copy -= state.whave;
                if (copy > state.length) copy = state.length;
                if (copy > left) copy = left;
                left -= copy;
                state.length -= copy;
                do {
                    *put++ = 0;
                } while (--copy);
                if (state.length == 0) state.mode = LEN;
                break;
                #endif*/
                }
                if (copy > state.wnext) {
                    copy -= state.wnext
                    from = state.window!!
                    from_i = state.wsize - copy
                } else {
                    from = state.window!!
                    from_i = state.wnext - copy
                }
                if (copy > state.length) copy = state.length
            } else {
                /* copy from output */
                from = put
                from_i = put_i - state.offset
                copy = state.length
            }
            if (copy > left) copy = left
            left -= copy
            state.length -= copy
            do {
                put[put_i++] = from[from_i++]
                copy--
            } while (copy != 0)
            if (state.length == 0) state.mode = inflate_mode.LEN
            //break;
        }
        inflate_mode.LIT -> {
            if (left == 0) break@loop
            put[put_i++] = state.length.toByte()
            left--
            state.mode = inflate_mode.LEN
            //break;
        }
        inflate_mode.CHECK -> {
            if (state.wrap != 0) {
                // NEEDBITS(32);
                while (bits < 32) {
                    if (have == 0) break@loop
                    have--
                    hold += next[next_i++].toUInt() shl bits
                    bits += 8
                }

                out -= left
                strm.total_out += out
                state.total += out
                if ((state.wrap and 4) != 0 && out != 0) {
                    strm.adler =
                            UPDATE(state, state.check, put, put_i - out, out)
                    state.check = strm.adler
                }
                out = left
                if ((state.wrap and 4) != 0 && (
                            //#ifdef GUNZIP
                            if (state.flags != 0) hold else
                            //#endif
                                ZSWAP32(hold)) != state.check) {
                    strm.msg = "incorrect data check"
                    state.mode = inflate_mode.BAD
                    continue@loop
                }

                // INITBITS();
                hold = 0
                bits = 0

                //Tracev((stderr, "inflate:   check matches trailer\n"));
            }
            //#ifdef GUNZIP
            state.mode = inflate_mode.LENGTH
        }
        inflate_mode.LENGTH -> {
            if (state.wrap != 0 && state.flags != 0) {
                // NEEDBITS(32);
                while (bits < 32) {
                    if (have == 0) break@loop
                    have--
                    hold += next[next_i++].toUInt() shl bits
                    bits += 8
                }

                if (hold != (state.total and -1 /* 0xffffffff */)) {
                    strm.msg = "incorrect length check"
                    state.mode = inflate_mode.BAD
                    continue@loop
                }

                // INITBITS();
                hold = 0
                bits = 0

                //Tracev((stderr, "inflate:   length matches trailer\n"));
            }
            //#endif
            state.mode = inflate_mode.DONE
        }
        inflate_mode.DONE -> {
            ret = Z_STREAM_END
            break@loop
        }
        inflate_mode.BAD -> {
            ret = Z_DATA_ERROR
            break@loop
        }
        inflate_mode.MEM -> {
            return Z_MEM_ERROR
        }
        inflate_mode.SYNC -> {
            return Z_STREAM_ERROR
        }
    }

    /*
       Return from inflate(), updating the total counts and the check value.
       If there was no progress during the inflate() call, return a buffer
       error.  Call updatewindow() to create and/or update the window state.
       Note: a memory error from inflate() is non-recoverable.
     */
    //inf_leave:

    // RESTORE();
    strm.next_out = put
    strm.next_out_i = put_i
    strm.avail_out = left
    strm.next_in = next
    strm.next_in_i = next_i
    strm.avail_in = have
    state.hold = hold
    state.bits = bits

    if (state.wsize != 0 || (out != strm.avail_out && !state.mode.is_error &&
                (!state.mode.is_finish || flush != Z_FINISH)))
        if (updatewindow(
                strm, state, strm.next_out!!, strm.next_out_i,
                out - strm.avail_out
            ) != 0) {
            state.mode = inflate_mode.MEM
            return Z_MEM_ERROR
        }
    `in` -= strm.avail_in
    out -= strm.avail_out
    strm.total_in += `in`
    strm.total_out += out
    state.total += out
    if ((state.wrap and 4) != 0 && out != 0) {
        strm.adler =
                UPDATE(
                    state, state.check,
                    strm.next_out!!, strm.next_out_i - out, out
                )
        state.check = strm.adler
    }
    strm.data_type = state.bits + (if (state.last != 0) 64 else 0) +
            (if (state.mode == inflate_mode.TYPE) 128 else 0) +
            (if (state.mode == inflate_mode.LEN_ || state.mode == inflate_mode.COPY_) 256 else 0)
    if (((`in` == 0 && out == 0) || flush == Z_FINISH) && ret == Z_OK)
        ret = Z_BUF_ERROR
    return ret
}

fun inflateEnd(
    strm: z_stream,
    state: inflate_state
): Int {
    if (inflateStateCheck(strm, state))
        return Z_STREAM_ERROR
    //Tracev((stderr, "inflate: end\n"));
    return Z_OK
}

fun inflateGetDictionary(
    strm: z_stream,
    state: inflate_state,
    dictionary: ByteArray,
    dictionary_i: Int,
    dictLength: IntArray
): Int {
    /* check state */
    if (inflateStateCheck(strm, state)) return Z_STREAM_ERROR

    /* copy dictionary */
    if (state.whave != 0 && dictionary != Z_NULL) {
        zmemcpy(
            dictionary, dictionary_i,
            state.window!!, state.wnext,
            state.whave - state.wnext
        )
        zmemcpy(
            dictionary, dictionary_i + state.whave - state.wnext,
            state.window!!, 0,
            state.wnext
        )
    }
    if (dictLength != Z_NULL)
        dictLength[0] = state.whave
    return Z_OK
}

fun inflateSetDictionary(
    strm: z_stream,
    state: inflate_state,
    dictionary: ByteArray,
    dictionary_i: Int,
    dictLength: UInt
): Int {
    /* check state */
    if (inflateStateCheck(strm, state)) return Z_STREAM_ERROR
    if (state.wrap != 0 && state.mode != inflate_mode.DICT)
        return Z_STREAM_ERROR

    /* check for correct dictionary identifier */
    if (state.mode == inflate_mode.DICT) {
        var dictid = adler32(0, Z_NULL, 0, 0)
        dictid = adler32(dictid, dictionary, dictionary_i, dictLength)
        if (dictid != state.check) return Z_DATA_ERROR
    }

    /* copy dictionary to window using updatewindow(), which will amend the
       existing dictionary if appropriate */
    val ret = updatewindow(
        strm, state, dictionary, dictionary_i + dictLength, dictLength
    )
    if (ret != 0) {
        state.mode = inflate_mode.MEM
        return Z_MEM_ERROR
    }
    state.havedict = 1
    //Tracev((stderr, "inflate:   dictionary set\n"));
    return Z_OK
}

fun inflateGetHeader(
    strm: z_stream,
    state: inflate_state,
    head: gz_header
): Int {
    /* check state */
    if (inflateStateCheck(strm, state)) return Z_STREAM_ERROR
    if ((state.wrap and 2) == 0) return Z_STREAM_ERROR

    /* save header structure */
    state.head = head
    head.done = 0
    return Z_OK
}

/*
   Search buf[0..len-1] for the pattern: 0, 0, 0xff, 0xff.  Return when found
   or when out of input.  When called, *have is the number of pattern bytes
   found in order so far, in 0..3.  On return *have is updated to the new
   state.  If on return *have equals four, then the pattern was found and the
   return value is how many bytes were read including the last byte of the
   pattern.  If *have is less than four, then the pattern has not been found
   yet and the return value is len.  In the latter case, syncsearch() can be
   called again with more data and the *have state.  *have is initialized to
   zero for the first call.
 */
private fun syncsearch(
    have: IntArray,
    buf: ByteArray,
    buf_i: Int,
    len: UInt
): UInt {
    var got: UInt = have[0]
    var next: UInt = buf_i
    while (next < len + buf_i && got < 4) {
        if (buf[next] == (if (got < 2) 0 else 0xff).toByte())
            got++
        else if (buf[next] != 0.toByte())
            got = 0
        else
            got = 4 - got
        next++
    }
    have[0] = got
    return next
}

fun inflateSync(
    strm: z_stream,
    state: inflate_state
): Int {
    val buf = ByteArray(4) /* to restore bit buffer to byte string */
    val have_ref = IntArray(1)

    /* check parameters */
    if (inflateStateCheck(strm, state)) return Z_STREAM_ERROR
    if (strm.avail_in == 0 && state.bits < 8) return Z_BUF_ERROR

    /* if first time, start search in bit buffer */
    if (state.mode != inflate_mode.SYNC) {
        state.mode = inflate_mode.SYNC
        state.hold = state.hold shl (state.bits and 7)
        state.bits -= state.bits and 7
        var len = 0
        while (state.bits >= 8) {
            buf[len++] = state.hold.toByte()
            state.hold = state.hold ushr 8
            state.bits -= 8
        }
        have_ref[0] = 0
        syncsearch(have_ref, buf, 0, len)
        state.have = have_ref[0]
    }

    /* search available input */
    have_ref[0] = state.have
    val len =
        syncsearch(have_ref, strm.next_in!!, strm.next_in_i, strm.avail_in)
    state.have = have_ref[0]
    strm.avail_in -= len
    strm.next_in_i += len
    strm.total_in += len

    /* return no joy or set up to restart inflate() on a new block */
    if (state.have != 4) return Z_DATA_ERROR
    val `in` = strm.total_in
    val out = strm.total_out
    inflateReset(strm, state)
    strm.total_in = `in`
    strm.total_out = out
    state.mode = inflate_mode.TYPE
    return Z_OK
}

/*
   Returns true if inflate is currently at the end of a block generated by
   Z_SYNC_FLUSH or Z_FULL_FLUSH. This function is used by one PPP
   implementation to provide an additional safety check. PPP uses
   Z_SYNC_FLUSH but removes the length bytes of the resulting empty stored
   block. When decompressing, PPP checks that at the end of input packet,
   inflate is waiting for these length bytes.
 */
fun inflateSyncPoint(
    strm: z_stream,
    state: inflate_state
): Int {
    if (inflateStateCheck(strm, state)) return Z_STREAM_ERROR
    return if (state.mode == inflate_mode.STORED && state.bits == 0) 1 else 0
}

fun inflateCopy(
    dest: z_stream,
    copy: inflate_state,
    source: z_stream,
    state: inflate_state
): Int {
    /* check input */
    if (inflateStateCheck(source, state) || dest == Z_NULL)
        return Z_STREAM_ERROR

    /* copy state */
    dest.data_type = source.data_type
    dest.next_in = source.next_in
    dest.next_in_i = source.next_in_i
    dest.avail_in = source.avail_in
    dest.total_in = source.total_in
    dest.next_out = source.next_out
    dest.next_out_i = source.next_out_i
    dest.avail_out = source.avail_out
    dest.total_out = source.total_out
    dest.msg = source.msg
    dest.data_type = source.data_type
    dest.adler = source.adler
    copy.mode = state.mode
    copy.last = state.last
    copy.wrap = state.wrap
    copy.havedict = state.havedict
    copy.flags = state.flags
    copy.dmax = state.dmax
    copy.check = state.check
    copy.total = state.total
    copy.head = state.head
    copy.wbits = state.wbits
    copy.wsize = state.wsize
    copy.whave = state.whave
    copy.wnext = state.wnext
    copy.window = state.window?.copyOf()
    copy.hold = state.hold
    copy.bits = state.bits
    copy.length = state.length
    copy.offset = state.offset
    copy.extra = state.extra
    copy.lencode =
            if (state.lencode === state.codes) copy.codes else state.lencode
    copy.lencode_i = state.lencode_i
    copy.distcode =
            if (state.distcode === state.codes) copy.codes else state.distcode
    copy.distcode_i = state.distcode_i
    copy.lenbits = state.lenbits
    copy.distbits = state.distbits
    copy.ncode = state.ncode
    copy.nlen = state.nlen
    copy.ndist = state.ndist
    copy.have = state.have
    copy.next = state.next
    copy(state.lens, copy.lens)
    copy(state.work, copy.work)
    copy(state.codes, copy.codes)
    copy.sane = state.sane
    copy.back = state.back
    copy.was = state.was
    return Z_OK
}

fun inflateUndermine(
    strm: z_stream,
    state: inflate_state,
    subvert: Int
): Int {
    if (inflateStateCheck(strm, state)) return Z_STREAM_ERROR
    /*#ifdef INFLATE_ALLOW_INVALID_DISTANCE_TOOFAR_ARRR
        state.sane = !subvert;
    return Z_OK;
    #else*/
    //(void) subvert;
    state.sane = true
    return Z_DATA_ERROR
    //#endif
}

fun inflateValidate(
    strm: z_stream,
    state: inflate_state,
    check: Boolean
): Int {
    if (inflateStateCheck(strm, state)) return Z_STREAM_ERROR
    if (check)
        state.wrap = state.wrap or 4
    else
        state.wrap = state.wrap and 4.inv()
    return Z_OK
}

fun inflateMark(
    strm: z_stream,
    state: inflate_state
): Long {
    if (inflateStateCheck(strm, state)) return -(1L shl 16)
    return (state.back.toLong() shl 16) +
            (if (state.mode == inflate_mode.COPY) state.length else
                (if (state.mode == inflate_mode.MATCH) state.was - state.length else 0))
}

fun inflateCodesUsed(
    strm: z_stream,
    state: inflate_state
): ULong {
    if (inflateStateCheck(strm, state)) return -1L
    return state.next.toULong()
}

private val order = shortArrayOf( /* permutation of code lengths */
    16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15
)
