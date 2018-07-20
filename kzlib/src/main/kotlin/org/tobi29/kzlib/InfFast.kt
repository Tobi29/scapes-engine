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

package org.tobi29.kzlib

/*
   Decode literal, length, and distance codes and write out the resulting
   literal and match bytes until either not enough input or output is
   available, an end-of-block is encountered, or a data error is encountered.
   When large enough input and output buffers are supplied to inflate(), for
   example, a 16K input buffer and a 64K output buffer, more than 95% of the
   inflate execution time is spent in this routine.
   Entry assumptions:
        state->mode == LEN
        strm->avail_in >= 6
        strm->avail_out >= 258
        start >= strm->avail_out
        state->bits < 8
   On return, state->mode is one of:
        LEN -- ran out of enough output space or enough available input
        TYPE -- reached end of block code, inflate() to interpret next block
        BAD -- error in block data
   Notes:
    - The maximum input bits used by a length/distance pair is 15 bits for the
      length code, 5 bits for the length extra, 15 bits for the distance code,
      and 13 bits for the distance extra.  This totals 48 bits, or six bytes.
      Therefore if strm->avail_in >= 6, then there is enough input to avoid
      checking for available input while decoding.
    - The maximum bytes that a single length/distance pair can output is 258
      bytes, which is the maximum length that can be coded.  inflate_fast()
      requires strm->avail_out >= 258 for each loop to avoid checking for
      output space.
 */
internal fun inflate_fast(
    strm: z_stream,
    state: inflate_state,
    start: Int /* inflate()'s starting value for strm->avail_out */
) {
    val here = code()

    /* copy state to local variables */
    val `in` = strm.next_in!! /* local strm->next_in */
    var in_i = strm.next_in_i
    val last =
        in_i + (strm.avail_in - 5) /* have enough input while in < last */
    val out = strm.next_out!! /* local strm->next_out */
    var out_i = strm.next_out_i
    val beg =
        out_i - (start - strm.avail_out) /* inflate()'s initial strm->next_out */
    val end =
        out_i + (strm.avail_out - 257) /* while out < end, enough space available */
    // #ifdef INFLATE_STRICT
    val dmax = state.dmax /* maximum distance from zlib header */
    // #endif
    val wsize = state.wsize /* window size or zero if not using window */
    val whave = state.whave /* valid bytes in the window */
    val wnext = state.wnext /* window write index */
    val window =
        if (wsize == 0) ByteArray(0) else state.window!! /* allocated sliding window, if wsize != 0 */
    var hold = state.hold /* local strm->hold */
    var bits = state.bits /* local strm->bits */
    val lcode = state.lencode!! /* local strm->lencode */
    val lcode_i = state.lencode_i /* local strm->lencode */
    val dcode = state.distcode!! /* local strm->distcode */
    val dcode_i = state.distcode_i /* local strm->distcode */
    val lmask =
        (1 shl state.lenbits) - 1 /* mask for first level of length codes */
    val dmask =
        (1 shl state.distbits) - 1 /* mask for first level of distance codes */

    /* decode literals and length/distances until end-of-block or not enough
       input data or output space */
    decode@ do {
        if (bits < 15) {
            hold += `in`[in_i++].toUInt() shl bits
            bits += 8
            hold += `in`[in_i++].toUInt() shl bits
            bits += 8
        }
        here.set(lcode[lcode_i + (hold and lmask)]) /* retrieved table entry */
        dolen@ while (true) {
            var op =
                here.bits.toUInt() /* code bits, operation, extra bits, or */
            hold = hold ushr op
            bits -= op
            op = here.op.toUInt()
            if (op == 0) {                          /* literal */
                /*Tracevv(
                (stderr, here.
                    val >= 0x20 && here .val < 0x7f ?
            "inflate:         literal '%c'\n" :
            "inflate:         literal 0x%02x\n", here.val ));*/
                out[out_i++] = here.`val`.toByte()
            } else if (op and 16 != 0) {
                /* length base */
                var len = here.`val`.toUInt()
                op = op and 15 /* number of extra bits */
                if (op != 0) {
                    if (bits < op) {
                        hold += `in`[in_i++].toUInt() shl bits
                        bits += 8
                    }
                    len += hold and ((1 shl op) - 1)
                    hold = hold ushr op
                    bits -= op
                }
                // Tracevv((stderr, "inflate:         length %u\n", len));
                if (bits < 15) {
                    hold += `in`[in_i++].toUInt() shl bits
                    bits += 8
                    hold += `in`[in_i++].toUInt() shl bits
                    bits += 8
                }
                here.set(dcode[dcode_i + (hold and dmask)])
                dodist@ while (true) {
                    op = here.bits.toUInt()
                    hold = hold ushr op
                    bits -= op
                    op = here.op.toUInt()
                    if (op and 16 != 0) {
                        /* distance base */
                        var dist = here.`val`.toUInt() /* match distance */
                        op = op and 15 /* number of extra bits */
                        if (bits < op) {
                            hold += `in`[in_i++].toUInt() shl bits
                            bits += 8
                            if (bits < op) {
                                hold += `in`[in_i++].toUInt() shl bits
                                bits += 8
                            }
                        }
                        dist += hold and ((1 shl op) - 1)
                        // #ifdef INFLATE_STRICT
                        if (dist > dmax) {
                            strm.msg = "invalid distance too far back"
                            state.mode = inflate_mode.BAD
                            break@decode
                        }
                        // #endif
                        hold = hold ushr op
                        bits -= op
                        // Tracevv((stderr, "inflate:         distance %u\n", dist));
                        op = out_i - beg /* max distance in output */
                        if (dist > op) {                /* see if copy from window */
                            op = dist -
                                    op             /* distance back in window */
                            if (op > whave) {
                                if (state.sane) {
                                    strm.msg = "invalid distance too far back"
                                    state.mode = inflate_mode.BAD
                                    break@decode
                                }
                                /*#ifdef INFLATE_ALLOW_INVALID_DISTANCE_TOOFAR_ARRR
                                    if (len <= op - whave) {
                                        do {
                                            *out++ = 0;
                                        } while (--len);
                                        continue;
                                    }
                            len -= op - whave;
                            do {
                                *out++ = 0;
                            } while (--op > whave);
                            if (op == 0) {
                                from = out - dist;
                                do {
                                    *out++ = * from ++;
                                } while (--len);
                                continue;
                            }
                            #endif*/
                            }
                            val from: ByteArray /* where to copy match from */
                            var from_i = 0
                            if (wnext == 0) {           /* very common case */
                                from_i += wsize - op
                                if (op < len) {         /* some from window */
                                    len -= op
                                    do {
                                        out[out_i++] = window[from_i++]
                                    } while (--op != 0)
                                    from = out /* rest from output */
                                    from_i = out_i - dist
                                } else from = window
                            } else if (wnext < op) {      /* wrap around window */
                                from_i += wsize + wnext - op
                                op -= wnext
                                if (op < len) {         /* some from end of window */
                                    len -= op
                                    do {
                                        out[out_i++] = window[from_i++]
                                    } while (--op != 0)
                                    from_i = 0
                                    if (wnext < len) {  /* some from start of window */
                                        op = wnext
                                        len -= op
                                        do {
                                            out[out_i++] = window[from_i++]
                                        } while (--op != 0)
                                        from = out /* rest from output */
                                        from_i = out_i - dist
                                    } else from = window
                                } else from = window
                            } else {                      /* contiguous in window */
                                from_i += wnext - op
                                if (op < len) {         /* some from window */
                                    len -= op
                                    do {
                                        out[out_i++] = window[from_i++]
                                    } while (--op != 0)
                                    from = out /* rest from output */
                                    from_i = out_i - dist
                                } else from = window
                            }
                            while (len > 2) {
                                out[out_i++] = from[from_i++]
                                out[out_i++] = from[from_i++]
                                out[out_i++] = from[from_i++]
                                len -= 3
                            }
                            if (len != 0) {
                                out[out_i++] = from[from_i++]
                                if (len > 1)
                                    out[out_i++] = from[from_i++]
                            }
                        } else {
                            var from =
                                out_i - dist /* copy direct from output */
                            do { /* minimum length is three */
                                out[out_i++] = out[from++]
                                out[out_i++] = out[from++]
                                out[out_i++] = out[from++]
                                len -= 3
                            } while (len > 2)
                            if (len != 0) {
                                out[out_i++] = out[from++]
                                if (len > 1)
                                    out[out_i++] = out[from++]
                            }
                        }
                    } else if ((op and 64) == 0) {
                        /* 2nd level distance code */
                        here.set(dcode[dcode_i + (here.`val` + (hold and ((1 shl op) - 1)))])
                        continue@dodist
                    } else {
                        strm.msg = "invalid distance code"
                        state.mode = inflate_mode.BAD
                        break@decode
                    }
                    break@dodist
                }
            } else if ((op and 64) == 0) {
                /* 2nd level length code */
                here.set(lcode[lcode_i + (here.`val` + (hold and ((1 shl op) - 1)))])
                continue@dolen
            } else if (op and 32 != 0) {
                /* end-of-block */
                // Tracevv((stderr, "inflate:         end of block\n"));
                state.mode = inflate_mode.TYPE
                break@decode
            } else {
                strm.msg = "invalid literal/length code"
                state.mode = inflate_mode.BAD
                break@decode
            }
            break@dolen
        }
    } while (in_i < last && out_i < end)

    /* return unused bytes (on entry, bits < 8, so in won't go too far back) */
    val len = bits ushr 3 /* match length, unused bytes */
    in_i -= len
    bits -= len shl 3
    hold = hold and ((1 shl bits) - 1)

    /* update state and return */
    strm.next_in_i = in_i
    strm.next_out_i = out_i
    strm.avail_in = if (in_i < last) 5 + (last - in_i) else 5 - (in_i - last)
    strm.avail_out =
            if (out_i < end) 257 + (end - out_i) else 257 - (out_i - end)
    state.hold = hold
    state.bits = bits
    return
}

/*
   inflate_fast() speedups that turned out slower (on a PowerPC G3 750CXe):
   - Using bit fields for code structure
   - Different op definition to avoid & for extra bits (do & for table bits)
   - Three separate decoding do-loops for direct, window, and wnext == 0
   - Special case for distance > 1 copies to do overlapped load and store copy
   - Explicit branch predictions (based on measured branch probabilities)
   - Deferring match copy and interspersed it with decoding subsequent codes
   - Swapping literal/length else
   - Swapping window/direct else
   - Larger unrolled copy loops (three is about right)
   - Moving len -= 3 statement into middle of loop
 */

