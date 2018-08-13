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
import org.tobi29.stdex.splitToShorts
import kotlin.experimental.or
import kotlin.math.min

internal inline fun put_byte(s: deflate_state, c: Byte) {
    s.pending_buf!![s.pending++] = c
}

internal inline val MIN_LOOKAHEAD get() = (MAX_MATCH + MIN_MATCH + 1)
/* Minimum amount of lookahead, except at the end of the input file.
 * See deflate.c for comments about the MIN_MATCH+1.
 */

private inline fun MAX_DIST(s: deflate_state) = s.w_size - MIN_LOOKAHEAD
/* In order to simplify the code, particularly on 16 bit machines, match
 * distances are limited to MAX_DIST instead of WSIZE.
 */

private inline val WIN_INIT get() = MAX_MATCH
/* Number of bytes after end of data in window to initialize in order to avoid
   memory checker errors from longest match routines */

internal inline fun d_code(dist: Int) =
    if (dist < 256) TreesTables._dist_code[dist]
    else TreesTables._dist_code[256 + (dist ushr 7)]

private inline fun _tr_tally_lit(
    s: deflate_state,
    pending_buf: ByteArray,
    c: Byte
): Boolean {
    // return _tr_tally(s, 0, c)
    pending_buf[s.d_buf + s.last_lit * 2] = 0
    pending_buf[s.d_buf + s.last_lit * 2 + 1] = 0
    pending_buf[s.l_buf + s.last_lit] = c
    s.last_lit++
    s.dyn_ltree[freq(c.toUInt())]/*.freq*/++
    return s.last_lit == s.lit_bufsize - 1
}

private inline fun _tr_tally_dist(
    s: deflate_state,
    pending_buf: ByteArray,
    distance: UShort,
    length: Byte
): Boolean {
    // return _tr_tally(s, distance, length)
    distance.splitToBytes { b1, b0 ->
        pending_buf[s.d_buf + s.last_lit * 2] = b1
        pending_buf[s.d_buf + s.last_lit * 2 + 1] = b0
    }
    pending_buf[s.l_buf + s.last_lit] = length
    s.last_lit++
    val dist = (distance - 1).toShort()
    s.dyn_ltree[freq(TreesTables._length_code[length.toUInt()].toUInt() + LITERALS + 1)]/*.freq*/++
    s.dyn_dtree[freq(d_code(dist.toUInt()).toUInt())]/*.freq*/++
    return s.last_lit == s.lit_bufsize - 1
}

/*
 *  ALGORITHM
 *
 *      The "deflation" process depends on being able to identify portions
 *      of the input text which are identical to earlier input (within a
 *      sliding window trailing behind the input currently being processed).
 *
 *      The most straightforward technique turns out to be the fastest for
 *      most input files: try all possible matches and select the longest.
 *      The key feature of this algorithm is that insertions into the string
 *      dictionary are very simple and thus fast, and deletions are avoided
 *      completely. Insertions are performed at each input character, whereas
 *      string matches are performed only when the previous match ends. So it
 *      is preferable to spend more time in matches to allow very fast string
 *      insertions and avoid deletions. The matching algorithm for small
 *      strings is inspired from that of Rabin & Karp. A brute force approach
 *      is used to find longer strings when a small match has been found.
 *      A similar algorithm is used in comic (by Jan-Mark Wams) and freeze
 *      (by Leonid Broukhis).
 *         A previous version of this file used a more sophisticated algorithm
 *      (by Fiala and Greene) which is guaranteed to run in linear amortized
 *      time, but has a larger average cost, uses more memory and is patented.
 *      However the F&G algorithm may be faster for some highly redundant
 *      files if the parameter max_chain_length (described below) is too large.
 *
 *  ACKNOWLEDGEMENTS
 *
 *      The idea of lazy evaluation of matches is due to Jan-Mark Wams, and
 *      I found it in 'freeze' written by Leonid Broukhis.
 *      Thanks to many people for bug reports and testing.
 *
 *  REFERENCES
 *
 *      Deutsch, L.P.,"DEFLATE Compressed Data Format Specification".
 *      Available in http://tools.ietf.org/html/rfc1951
 *
 *      A description of the Rabin and Karp algorithm is given in the book
 *         "Algorithms" by R. Sedgewick, Addison-Wesley, p252.
 *
 *      Fiala,E.R., and Greene,D.H.
 *         Data Compression with Finite Windows, Comm.ACM, 32,4 (1989) 490-595
 *
 */
/*
  If you use the zlib library in a product, an acknowledgment is welcome
  in the documentation of your product. If for some reason you cannot
  include such an acknowledgment, I would appreciate that you keep this
  copyright string in the executable of your product.
 */

/* ===========================================================================
 *  Function prototypes.
 */
typealias compress_func = (z_stream, deflate_state, Int) -> block_state

/* ===========================================================================
 * Local data
 */

private const val NIL: Short = 0
/* Tail of hash chains */

//#ifndef TOO_FAR
private inline val TOO_FAR get() = 4096
//#endif
/* Matches of length 3 are discarded if their distance exceeds TOO_FAR */

/* Values for max_lazy_match, good_match and max_chain_length, depending on
 * the desired pack level (0..9). The values given below have been tuned to
 * exclude worst case performance for pathological files. Better values may be
 * found for specific files.
 */
private class config(
    val good_length: UShort,
    val max_lazy: UShort,
    val nice_length: UShort,
    val max_chain: UShort,
    val func: compress_func
)

/*#ifdef FASTEST
local const config configuration_table[2] = {
    /*      good lazy nice chain */
    /* 0 */ {0,    0,  0,    0, deflate_stored},  /* store only */
    /* 1 */ {4,    4,  8,    4, deflate_fast}}; /* max speed, no lazy matches */
#else*/
private object DeflateTables {
    val configuration_table = arrayOf(
        /*      good lazy nice chain */
        /* 0 */ config(0, 0, 0, 0, ::deflate_stored),  /* store only */
        /* 1 */
        config(4, 4, 8, 4, ::deflate_fast), /* max speed, no lazy matches */
        /* 2 */
        config(4, 5, 16, 8, ::deflate_fast),
        /* 3 */
        config(4, 6, 32, 32, ::deflate_fast),

        /* 4 */
        config(4, 4, 16, 16, ::deflate_slow),  /* lazy matches */
        /* 5 */
        config(8, 16, 32, 32, ::deflate_slow),
        /* 6 */
        config(8, 16, 128, 128, ::deflate_slow),
        /* 7 */
        config(8, 32, 128, 256, ::deflate_slow),
        /* 8 */
        config(32, 128, 258, 1024, ::deflate_slow),
        /* 9 */
        config(32, 258, 258, 4096, ::deflate_slow) /* max compression */
    )
}
//#endif

/* Note: the deflate() code requires max_lazy >= MIN_MATCH and max_chain >= 4
 * For deflate_fast() (levels <= 3) good is ignored and lazy has a different
 * meaning.
 */

/* rank Z_BLOCK between Z_NO_FLUSH and Z_PARTIAL_FLUSH */
private inline fun RANK(f: Int) = (f * 2) - (if (f > 4) 9 else 0)

/* ===========================================================================
 * Update a hash value with the given input byte
 * IN  assertion: all calls to UPDATE_HASH are made with consecutive input
 *    characters, so that a running hash key can be computed from the previous
 *    key instead of complete recalculation each time.
 */
private inline fun UPDATE_HASH(
    s: deflate_state,
    c: Byte
) {
    s.ins_h = ((s.ins_h shl s.hash_shift) xor c.toUInt()) and s.hash_mask
}


/* ===========================================================================
 * Insert string str in the dictionary and set match_head to the previous head
 * of the hash chain (the most recent string with same hash key). Return
 * the previous length of the hash chain.
 * If this file is compiled with -DFASTEST, the compression level is forced
 * to 1, and no hash chains are maintained.
 * IN  assertion: all calls to INSERT_STRING are made with consecutive input
 *    characters and the first MIN_MATCH bytes of str are valid (except for
 *    the last MIN_MATCH-1 bytes of the input file).
 */
/*#ifdef FASTEST
#define INSERT_STRING(s, str, match_head) \
(UPDATE_HASH(s, s.ins_h, s.window[(str) + (MIN_MATCH-1)]), \
match_head = s.head[s.ins_h], \
s.head[s.ins_h] = (Pos)(str))
#else*/
private inline fun INSERT_STRING(
    s: deflate_state,
    window: ByteArray,
    str: UInt
): Short {
    UPDATE_HASH(s, window[str + (MIN_MATCH - 1)])
    val match_head = s.head!![s.ins_h]
    s.prev!![str and s.w_mask] = match_head
    s.head!![s.ins_h] = str.toShort()
    return match_head
}
/*#define INSERT_STRING(s, str, match_head) \
(UPDATE_HASH(s, s.ins_h, s.window[(str) + (MIN_MATCH-1)]), \
match_head = s.prev[(str) & s.w_mask] = s.head[s.ins_h], \
s.head[s.ins_h] = (Pos)(str))*/
//#endif

/* ===========================================================================
 * Initialize the hash table (avoiding 64K overflow for 16 bit systems).
 * prev[] will be initialized on the fly.
 */
private inline fun CLEAR_HASH(s: deflate_state) {
    s.head!![s.hash_size - 1] = NIL
    zmemzero(s.head!!, 0, s.hash_size - 1)
}

/* ===========================================================================
 * Slide the hash table when sliding the window down (could be avoided with 32
 * bit values at the expense of memory usage). We slide even when level == 0 to
 * keep the hash table consistent if we switch back to level > 0 later.
 */
private fun slide_hash(
    s: deflate_state
) {
    val wsize = s.w_size

    var n = s.hash_size
    var p = n
    do {
        val m = s.head!![--p].toUInt()
        s.head!![p] = if (m >= wsize) (m - wsize).toShort() else NIL
    } while (--n != 0)
    n = wsize
    //#ifndef FASTEST
    p = n
    do {
        val m = s.prev!![--p].toUInt()
        s.prev!![p] = if (m >= wsize) (m - wsize).toShort() else NIL
        /* If n is not on any hash chain, prev[n] is garbage but
         * its value will never be used.
         */
    } while (--n != 0)
    //#endif
}

/* ========================================================================= */
fun deflateInit(
    strm: z_stream,
    s: deflate_state,
    level: Int = Z_DEFAULT_COMPRESSION,
    method: Int = Z_DEFLATED,
    windowBits: Int = DEF_WBITS,
    memLevel: Int = DEF_MEM_LEVEL,
    strategy: Int = Z_DEFAULT_STRATEGY
): Int {
    var wrap = 1

    //ushf *overlay;
    /* We overlay pending_buf and d_buf+l_buf. This works since the average
     * output size for (length,distance) codes is <= 24 bits.
     */

    /*if (version == Z_NULL || version[0] != my_version[0] ||
        stream_size != sizeof(z_stream)) {
        return Z_VERSION_ERROR;
    }*/
    if (strm == Z_NULL) return Z_STREAM_ERROR

    strm.msg = Z_NULL
    /*if (strm.zalloc == (alloc_func)0) {
    #ifdef Z_SOLO
        return Z_STREAM_ERROR;
    #else
    strm.zalloc = zcalloc;
    strm.opaque = (voidpf)0;
    #endif
}*/

    /*#ifdef FASTEST
        if (level != 0) level = 1;
    #else*/
    var level = level
    if (level == Z_DEFAULT_COMPRESSION) level = 6
    //#endif

    var windowBits = windowBits
    if (windowBits < 0) { /* suppress zlib wrapper */
        wrap = 0
        windowBits = -windowBits
    }
    //#ifdef GZIP
    else if (windowBits > 15) {
        wrap = 2       /* write gzip wrapper instead */
        windowBits -= 16
    }
    //#endif
    if (memLevel < 1 || memLevel > MAX_MEM_LEVEL || method != Z_DEFLATED ||
        windowBits < 8 || windowBits > 15 || level < 0 || level > 9 ||
        strategy < 0 || strategy > Z_FIXED || (windowBits == 8 && wrap != 1)) {
        return Z_STREAM_ERROR
    }
    if (windowBits == 8) windowBits = 9  /* until 256-byte window bug fixed */
    if (s == Z_NULL) return Z_MEM_ERROR
    s.status = INIT_STATE     /* to pass state test in deflateReset() */

    s.wrap = wrap
    s.gzhead = Z_NULL
    s.w_bits = windowBits
    s.w_size = 1 shl s.w_bits
    s.w_mask = s.w_size - 1

    s.hash_bits = memLevel + 7
    s.hash_size = 1 shl s.hash_bits
    s.hash_mask = s.hash_size - 1
    s.hash_shift = ((s.hash_bits + MIN_MATCH - 1) / MIN_MATCH)

    s.window = ByteArray(2 * s.w_size)
    s.prev = ShortArray(s.w_size)
    s.head = ShortArray(s.hash_size)

    s.high_water = 0      /* nothing written to s.window yet */

    s.lit_bufsize = 1 shl (memLevel + 6) /* 16K elements by default */

    s.pending_buf = ByteArray(s.lit_bufsize * 4)
    s.pending_buf_size = s.lit_bufsize * 4

    s.d_buf = s.lit_bufsize
    s.l_buf = (1 + 2) * s.lit_bufsize

    s.level = level
    s.strategy = strategy
    s.method = method

    return deflateReset(strm, s)
}

/* =========================================================================
 * Check for a valid deflate stream state. Return 0 if ok, 1 if not.
 */
fun deflateStateCheck(strm: z_stream, s: deflate_state): Int {
    if (strm == Z_NULL) return 1
    if (s == Z_NULL || strm != strm || (s.status != INIT_STATE &&
                //      # ifdef GZIP
                s.status != GZIP_STATE &&
                // #endif
                s.status != EXTRA_STATE &&
                s.status != NAME_STATE &&
                s.status != COMMENT_STATE &&
                s.status != HCRC_STATE &&
                s.status != BUSY_STATE &&
                s.status != FINISH_STATE))
        return 1
    return 0
}

/* ========================================================================= */
fun deflateSetDictionary(
    strm: z_stream,
    s: deflate_state,
    dictionary: ByteArray,
    dictionary_i: UInt,
    dictLength: Int
): Int {
    var dictionary_i = dictionary_i
    var dictLength = dictLength

    val window = s.window!!

    if (deflateStateCheck(strm, s) != 0 || dictionary == Z_NULL)
        return Z_STREAM_ERROR
    val wrap = s.wrap
    if (wrap == 2 || (wrap == 1 && s.status != INIT_STATE) || s.lookahead != 0)
        return Z_STREAM_ERROR

    /* when using zlib wrappers, compute Adler-32 for provided dictionary */
    if (wrap == 1)
        strm.adler = adler32(strm.adler, dictionary, dictionary_i, dictLength)
    s.wrap = 0                    /* avoid computing Adler-32 in read_buf */

    /* if dictionary would fill window, just replace the history */
    if (dictLength >= s.w_size) {
        if (wrap == 0) {            /* already empty otherwise */
            CLEAR_HASH(s)
            s.strstart = 0
            s.block_start = 0
            s.insert = 0
        }
        dictionary_i += dictLength - s.w_size  /* use the tail */
        dictLength = s.w_size
    }

    /* insert dictionary into window and hash */
    val avail = strm.avail_in
    val next = strm.next_in
    strm.avail_in = dictLength
    strm.next_in = dictionary
    fill_window(strm, s)
    while (s.lookahead >= MIN_MATCH) {
        var str = s.strstart
        var n = s.lookahead - (MIN_MATCH - 1)
        do {
            UPDATE_HASH(s, window[str + MIN_MATCH - 1])
            //#ifndef FASTEST
            s.prev!![str and s.w_mask] = s.head!![s.ins_h]
            //#endif
            s.head!![s.ins_h] = str.toShort()
            str++
        } while (--n != 0)
        s.strstart = str
        s.lookahead = MIN_MATCH - 1
        fill_window(strm, s)
    }
    s.strstart += s.lookahead
    s.block_start = s.strstart
    s.insert = s.lookahead
    s.lookahead = 0
    s.match_length = MIN_MATCH - 1
    s.prev_length = MIN_MATCH - 1
    s.match_available = 0
    strm.next_in = next
    strm.next_in_i = 0
    strm.avail_in = avail
    s.wrap = wrap
    return Z_OK
}

/* ========================================================================= */
fun deflateGetDictionary(
    strm: z_stream,
    s: deflate_state,
    dictionary: ByteArray,
    dictionary_i: UInt,
    dictLength: IntArray?
): Int {
    if (deflateStateCheck(strm, s) != 0)
        return Z_STREAM_ERROR
    var len = s.strstart + s.lookahead
    if (len > s.w_size) len = s.w_size
    if (dictionary != Z_NULL && len != 0)
        zmemcpy(
            dictionary, dictionary_i,
            s.window!!, s.strstart + s.lookahead - len,
            len
        )
    if (dictLength != Z_NULL) dictLength[0] = len
    return Z_OK
}

/* ========================================================================= */
fun deflateResetKeep(
    strm: z_stream,
    s: deflate_state
): Int {
    if (deflateStateCheck(strm, s) != 0) return Z_STREAM_ERROR

    strm.total_in = 0
    strm.total_in = 0
    strm.msg = Z_NULL /* use zfree if we ever allocate msg dynamically */
    strm.data_type = Z_UNKNOWN

    s.pending = 0
    s.pending_out = 0

    if (s.wrap < 0) {
        s.wrap = -s.wrap /* was made negative by deflate(..., Z_FINISH); */
    }
    s.status =
            //#ifdef GZIP
            if (s.wrap == 2) GZIP_STATE else
            //#endif
                if (s.wrap != 0) INIT_STATE else BUSY_STATE
    strm.adler =
            //#ifdef GZIP
            if (s.wrap == 2) crc32(0, Z_NULL, 0, 0) else
            //#endif
                adler32(0, Z_NULL, 0, 0)
    s.last_flush = Z_NO_FLUSH

    _tr_init(s)

    return Z_OK
}

/* ========================================================================= */
fun deflateReset(
    strm: z_stream,
    s: deflate_state
): Int {
    val ret = deflateResetKeep(strm, s)
    if (ret == Z_OK)
        lm_init(s)
    return ret
}

/* ========================================================================= */
fun deflateSetHeader(
    strm: z_stream,
    s: deflate_state,
    head: gz_header
): Int {
    if (deflateStateCheck(strm, s) != 0 || s.wrap != 2)
        return Z_STREAM_ERROR
    s.gzhead = head
    return Z_OK
}

/* ========================================================================= */
fun deflatePending(
    strm: z_stream,
    s: deflate_state,
    pending: IntArray,
    bits: IntArray
): Int {
    if (deflateStateCheck(strm, s) != 0) return Z_STREAM_ERROR
    if (pending != Z_NULL)
        pending[0] = s.pending
    if (bits != Z_NULL)
        bits[0] = s.bi_valid
    return Z_OK
}

/* ========================================================================= */
fun deflatePrime(
    strm: z_stream,
    s: deflate_state,
    bits: Int,
    value: Int
): Int {
    var bits = bits
    var value = value
    if (deflateStateCheck(strm, s) != 0) return Z_STREAM_ERROR
    if (s.d_buf < (Buf_size + 7) ushr 3) return Z_BUF_ERROR
    do {
        var put = Buf_size - s.bi_valid
        if (put > bits) put = bits
        s.bi_buf = s.bi_buf or
                ((value and ((1 shl put) - 1)) shl s.bi_valid).toShort()
        s.bi_valid += put
        _tr_flush_bits(s)
        value = value ushr put
        bits -= put
    } while (bits != 0)
    return Z_OK
}

/* ========================================================================= */
fun deflateParams(
    strm: z_stream,
    s: deflate_state,
    level: Int,
    strategy: Int
): Int {
    if (deflateStateCheck(strm, s) != 0) return Z_STREAM_ERROR

    /*#ifdef FASTEST
        if (level != 0) level = 1;
    #else*/
    var level = level
    if (level == Z_DEFAULT_COMPRESSION) level = 6
    //#endif
    if (level < 0 || level > 9 || strategy < 0 || strategy > Z_FIXED) {
        return Z_STREAM_ERROR
    }
    val configuration_table = DeflateTables.configuration_table
    val func = configuration_table[s.level].func

    if ((strategy != s.strategy || func != configuration_table[level].func) &&
        s.high_water != 0) {
        /* Flush the last buffer: */
        val err = deflate(strm, s, Z_BLOCK)
        if (err == Z_STREAM_ERROR)
            return err
        if (strm.avail_out == 0)
            return Z_BUF_ERROR
    }
    if (s.level != level) {
        if (s.level == 0 && s.matches != 0) {
            if (s.matches == 1)
                slide_hash(s)
            else
                CLEAR_HASH(s)
            s.matches = 0
        }
        s.level = level
        s.max_lazy_match = configuration_table[level].max_lazy.toUInt()
        s.good_match = configuration_table[level].good_length.toUInt()
        s.nice_match = configuration_table[level].nice_length.toUInt()
        s.max_chain_length = configuration_table[level].max_chain.toUInt()
    }
    s.strategy = strategy
    return Z_OK
}

/* ========================================================================= */
fun deflateTune(
    strm: z_stream,
    s: deflate_state,
    good_length: Int,
    max_lazy: Int,
    nice_length: Int,
    max_chain: Int
): Int {
    if (deflateStateCheck(strm, s) != 0) return Z_STREAM_ERROR
    s.good_match = good_length
    s.max_lazy_match = max_lazy
    s.nice_match = nice_length
    s.max_chain_length = max_chain
    return Z_OK
}

/* =========================================================================
 * For the default windowBits of 15 and memLevel of 8, this function returns
 * a close to exact, as well as small, upper bound on the compressed size.
 * They are coded as constants here for a reason--if the #define's are
 * changed, then this function needs to be changed as well.  The return
 * value for 15 and 8 only works for those exact settings.
 *
 * For any setting other than those defaults for windowBits and memLevel,
 * the value returned is a conservative worst case for the maximum expansion
 * resulting from using fixed blocks instead of stored blocks, which deflate
 * can emit on compressed data for some combinations of the parameters.
 *
 * This function could be more sophisticated to provide closer upper bounds for
 * every combination of windowBits and memLevel.  But even the conservative
 * upper bound of about 14% expansion does not seem onerous for output buffer
 * allocation.
 */
fun deflateBound(
    strm: z_stream,
    s: deflate_state,
    sourceLen: Long
): Long {
    /* conservative upper bound for compressed data */
    val complen = sourceLen +
            ((sourceLen + 7) ushr 3) + ((sourceLen + 63) ushr 6) + 5

    /* if can't get parameters, return conservative bound plus zlib wrapper */
    if (deflateStateCheck(strm, s) != 0)
        return complen + 6

    /* compute wrapper length */
    var wraplen: Int
    when (s.wrap) {
        0 -> {                                 /* raw deflate */
            wraplen = 0
        }
        1 -> {                                 /* zlib wrapper */
            wraplen = 6 + (if (s.strstart != 0) 4 else 0)
        }
        //#ifdef GZIP
        2 -> {                                 /* gzip wrapper */
            wraplen = 18
            val gzhead = s.gzhead
            if (gzhead != Z_NULL) {          /* user-supplied gzip header */
                if (gzhead.extra != Z_NULL)
                    wraplen += 2 + gzhead.extra_len
                var str = gzhead.name
                var i = 0
                if (str != Z_NULL)
                    do {
                        wraplen++
                    } while (str[i++] != 0.toByte())
                str = gzhead.comment
                i = 0
                if (str != Z_NULL)
                    do {
                        wraplen++
                    } while (str[i++] != 0.toByte())
                if (gzhead.hcrc != 0)
                    wraplen += 2
            }
        }
        //#endif
        else -> {                                /* for compiler happiness */
            wraplen = 6
        }
    }

    /* if not default parameters, return conservative bound */
    if (s.w_bits != 15 || s.hash_bits != 8 + 7)
        return complen + wraplen

    /* default settings: return tight bound for that case */
    return sourceLen + (sourceLen ushr 12) + (sourceLen ushr 14) +
            (sourceLen ushr 25) + 13 - 6 + wraplen
}

/* =========================================================================
 * Put a short in the pending buffer. The 16-bit value is put in MSB order.
 * IN assertion: the stream state is correct and there is enough room in
 * pending_buf.
 */
private fun putShortMSB(
    s: deflate_state,
    b: Short
) {
    b.splitToBytes { b1, b0 ->
        put_byte(s, b1)
        put_byte(s, b0)
    }
}

/* =========================================================================
 * Flush as much pending output as possible. All deflate() output, except for
 * some deflate_stored() output, goes through this function so some
 * applications may wish to modify it to avoid allocating a large
 * strm.next_out buffer and copying into it. (See also read_buf()).
 */
private fun flush_pending(
    strm: z_stream,
    s: deflate_state
) {
    _tr_flush_bits(s)
    var len = s.pending
    if (len > strm.avail_out) len = strm.avail_out
    if (len == 0) return

    zmemcpy(
        strm.next_out!!, strm.next_out_i,
        s.pending_buf!!, s.pending_out,
        len
    )
    strm.next_out_i += len
    s.pending_out += len
    strm.total_out += len
    strm.avail_out -= len
    s.pending -= len
    if (s.pending == 0) {
        s.pending_out = 0
    }
}

/* ===========================================================================
 * Update the header CRC with the bytes s.pending_buf[beg..s.pending - 1].
 */
private inline fun HCRC_UPDATE(
    strm: z_stream,
    s: deflate_state,
    beg: UInt
) {
    if (s.gzhead!!.hcrc != 0 && s.pending > (beg)) {
        strm.adler = crc32(strm.adler, s.pending_buf!!, beg, s.pending - beg)
    }
}

/* ========================================================================= */
fun deflate(
    strm: z_stream,
    s: deflate_state,
    flush: Int
): Int {
    if (deflateStateCheck(strm, s) != 0 || flush > Z_BLOCK || flush < 0) {
        return Z_STREAM_ERROR
    }

    if (strm.next_out == Z_NULL ||
        (strm.avail_in != 0 && strm.next_in == Z_NULL) ||
        (s.status == FINISH_STATE && flush != Z_FINISH)) {
        strm.msg = "stream error"
        return Z_STREAM_ERROR
    }
    if (strm.avail_out == 0) {
        strm.msg = "buffer error"
        return Z_BUF_ERROR
    }

    val old_flush =
        s.last_flush /* value of flush param for previous deflate call */
    s.last_flush = flush

    /* Flush as much pending output as possible */
    if (s.pending != 0) {
        flush_pending(strm, s)
        if (strm.avail_out == 0) {
            /* Since avail_out is 0, deflate will be called again with
             * more output space, but possibly with both pending and
             * avail_in equal to zero. There won't be anything to do,
             * but this is not an error situation so make sure we
             * return OK instead of BUF_ERROR at next call of deflate:
             */
            s.last_flush = -1
            return Z_OK
        }

        /* Make sure there is something to do and avoid duplicate consecutive
         * flushes. For repeated and useless calls with Z_FINISH, we keep
         * returning Z_STREAM_END instead of Z_BUF_ERROR.
         */
    } else if (strm.avail_in == 0 && RANK(flush) <= RANK(old_flush) &&
        flush != Z_FINISH) {
        strm.msg = "buffer error"
        return Z_BUF_ERROR
    }

    /* User must not provide more input after the first FINISH: */
    if (s.status == FINISH_STATE && strm.avail_in != 0) {
        strm.msg = "buffer error"
        return Z_BUF_ERROR
    }

    /* Write the header */
    if (s.status == INIT_STATE) {
        /* zlib header */
        var header = (Z_DEFLATED + ((s.w_bits - 8) shl 4)) shl 8
        val level_flags: UInt

        if (s.strategy >= Z_HUFFMAN_ONLY || s.level < 2)
            level_flags = 0
        else if (s.level < 6)
            level_flags = 1
        else if (s.level == 6)
            level_flags = 2
        else
            level_flags = 3
        header = header or (level_flags shl 6)
        if (s.strstart != 0) header = header or PRESET_DICT
        header += 31 - (header % 31)

        putShortMSB(s, header.toShort())

        /* Save the adler32 of the preset dictionary: */
        if (s.strstart != 0) {
            strm.adler.splitToShorts { s1, s0 ->
                putShortMSB(s, s1)
                putShortMSB(s, s0)
            }
        }
        strm.adler = adler32(0, Z_NULL, 0, 0)
        s.status = BUSY_STATE

        /* Compression must start with an empty pending buffer */
        flush_pending(strm, s)
        if (s.pending != 0) {
            s.last_flush = -1
            return Z_OK
        }
    }
    //#ifdef GZIP
    if (s.status == GZIP_STATE) {
        /* gzip header */
        strm.adler = crc32(0, Z_NULL, 0, 0)
        put_byte(s, 31)
        put_byte(s, 139.toByte())
        put_byte(s, 8)
        if (s.gzhead == Z_NULL) {
            put_byte(s, 0)
            put_byte(s, 0)
            put_byte(s, 0)
            put_byte(s, 0)
            put_byte(s, 0)
            put_byte(
                s, if (s.level == 9) 2.toByte() else
                    (if (s.strategy >= Z_HUFFMAN_ONLY) 1.toByte() else if (s.level < 2)
                        4.toByte() else 0.toByte())
            )
            put_byte(s, OS_CODE)
            s.status = BUSY_STATE

            /* Compression must start with an empty pending buffer */
            flush_pending(strm, s)
            if (s.pending != 0) {
                s.last_flush = -1
                return Z_OK
            }
        } else {
            put_byte(
                s, ((if (s.gzhead!!.text != 0) 1 else 0) +
                        (if (s.gzhead!!.hcrc != 0) 2 else 0) +
                        (if (s.gzhead!!.extra == Z_NULL) 0 else 4) +
                        (if (s.gzhead!!.name == Z_NULL) 0 else 8) +
                        (if (s.gzhead!!.comment == Z_NULL) 0 else 16)).toByte()
            )
            s.gzhead!!.time.toInt().splitToBytes { b3, b2, b1, b0 ->
                put_byte(s, b0)
                put_byte(s, b1)
                put_byte(s, b2)
                put_byte(s, b3)
            }
            put_byte(
                s, if (s.level == 9) 2.toByte() else
                    (if (s.strategy >= Z_HUFFMAN_ONLY || s.level < 2)
                        4.toByte() else 0.toByte())
            )
            put_byte(s, s.gzhead!!.os.toByte())
            if (s.gzhead!!.extra != Z_NULL) {
                s.gzhead!!.extra_len.toShort().splitToBytes { b1, b0 ->
                    put_byte(s, b0)
                    put_byte(s, b1)
                }
            }
            if (s.gzhead!!.hcrc != 0)
                strm.adler = crc32(
                    strm.adler, s.pending_buf!!, 0, s.pending
                )
            s.gzindex = 0
            s.status = EXTRA_STATE
        }
    }
    if (s.status == EXTRA_STATE) {
        if (s.gzhead!!.extra != Z_NULL) {
            var beg = s.pending   /* start of bytes to update crc */
            var left = (s.gzhead!!.extra_len and 0xffff) - s.gzindex
            while (s.pending + left > s.pending_buf_size) {
                val copy = s.pending_buf_size - s.pending
                zmemcpy(
                    s.pending_buf!!, s.pending,
                    s.gzhead!!.extra!!, s.gzindex,
                    copy
                )
                s.pending = s.pending_buf_size
                HCRC_UPDATE(strm, s, beg)
                s.gzindex += copy
                flush_pending(strm, s)
                if (s.pending != 0) {
                    s.last_flush = -1
                    return Z_OK
                }
                beg = 0
                left -= copy
            }
            zmemcpy(
                s.pending_buf!!, s.pending,
                s.gzhead!!.extra!!, s.gzindex,
                left
            )
            s.pending += left
            HCRC_UPDATE(strm, s, beg)
            s.gzindex = 0
        }
        s.status = NAME_STATE
    }
    if (s.status == NAME_STATE) {
        if (s.gzhead!!.name != Z_NULL) {
            var beg = s.pending   /* start of bytes to update crc */
            var `val`: Byte
            do {
                if (s.pending == s.pending_buf_size) {
                    HCRC_UPDATE(strm, s, beg)
                    flush_pending(strm, s)
                    if (s.pending != 0) {
                        s.last_flush = -1
                        return Z_OK
                    }
                    beg = 0
                }
                `val` = s.gzhead!!.name!![s.gzindex++]
                put_byte(s, `val`)
            } while (`val` != 0.toByte())
            HCRC_UPDATE(strm, s, beg)
            s.gzindex = 0
        }
        s.status = COMMENT_STATE
    }
    if (s.status == COMMENT_STATE) {
        if (s.gzhead!!.comment != Z_NULL) {
            var beg = s.pending   /* start of bytes to update crc */
            var `val`: Byte
            do {
                if (s.pending == s.pending_buf_size) {
                    HCRC_UPDATE(strm, s, beg)
                    flush_pending(strm, s)
                    if (s.pending != 0) {
                        s.last_flush = -1
                        return Z_OK
                    }
                    beg = 0
                }
                `val` = s.gzhead!!.comment!![s.gzindex++]
                put_byte(s, `val`)
            } while (`val` != 0.toByte())
            HCRC_UPDATE(strm, s, beg)
        }
        s.status = HCRC_STATE
    }
    if (s.status == HCRC_STATE) {
        if (s.gzhead!!.hcrc != 0) {
            if (s.pending + 2 > s.pending_buf_size) {
                flush_pending(strm, s)
                if (s.pending != 0) {
                    s.last_flush = -1
                    return Z_OK
                }
            }
            strm.adler.toShort().splitToBytes { b1, b0 ->
                put_byte(s, b0)
                put_byte(s, b1)
            }
            strm.adler = crc32(0, Z_NULL, 0, 0)
        }
        s.status = BUSY_STATE

        /* Compression must start with an empty pending buffer */
        flush_pending(strm, s)
        if (s.pending != 0) {
            s.last_flush = -1
            return Z_OK
        }
    }
    //#endif

    /* Start a new block or continue the current one.
     */
    if (strm.avail_in != 0 || s.lookahead != 0 ||
        (flush != Z_NO_FLUSH && s.status != FINISH_STATE)) {

        val bstate = if (s.level == 0) deflate_stored(strm, s, flush)
        else if (s.strategy == Z_HUFFMAN_ONLY) deflate_huff(strm, s, flush)
        else if (s.strategy == Z_RLE) deflate_rle(strm, s, flush)
        else DeflateTables.configuration_table[s.level].func(strm, s, flush)

        if (bstate == block_state.finish_started
            || bstate == block_state.finish_done) {
            s.status = FINISH_STATE
        }
        if (bstate == block_state.need_more
            || bstate == block_state.finish_started) {
            if (strm.avail_out == 0) {
                s.last_flush = -1 /* avoid BUF_ERROR next call, see above */
            }
            return Z_OK
            /* If flush != Z_NO_FLUSH && avail_out == 0, the next call
             * of deflate should use the same flush parameter to make sure
             * that the flush is complete. So we don't have to output an
             * empty block here, this will be done at next call. This also
             * ensures that for a very small output buffer, we emit at most
             * one empty block.
             */
        }
        if (bstate == block_state.block_done) {
            if (flush == Z_PARTIAL_FLUSH) {
                _tr_align(s)
            } else if (flush != Z_BLOCK) { /* FULL_FLUSH or SYNC_FLUSH */
                _tr_stored_block(s, null, 0, 0, 0)
                /* For a full flush, this empty block will be recognized
                 * as a special marker by inflate_sync().
                 */
                if (flush == Z_FULL_FLUSH) {
                    CLEAR_HASH(s)             /* forget history */
                    if (s.lookahead == 0) {
                        s.strstart = 0
                        s.block_start = 0
                        s.insert = 0
                    }
                }
            }
            flush_pending(strm, s)
            if (strm.avail_out == 0) {
                s.last_flush = -1 /* avoid BUF_ERROR at next call, see above */
                return Z_OK
            }
        }
    }

    if (flush != Z_FINISH) return Z_OK
    if (s.wrap <= 0) return Z_STREAM_END

    /* Write the trailer */
    //#ifdef GZIP
    if (s.wrap == 2) {
        strm.adler.splitToBytes { b3, b2, b1, b0 ->
            put_byte(s, b0)
            put_byte(s, b1)
            put_byte(s, b2)
            put_byte(s, b3)
        }
        strm.total_in.toInt().splitToBytes { b3, b2, b1, b0 ->
            put_byte(s, b0)
            put_byte(s, b1)
            put_byte(s, b2)
            put_byte(s, b3)
        }
    } else
    //#endif
        strm.adler.splitToShorts { s1, s0 ->
            putShortMSB(s, s1)
            putShortMSB(s, s0)
        }
    flush_pending(strm, s)
    /* If avail_out is zero, the application will call deflate again
     * to flush the rest.
     */
    if (s.wrap > 0) s.wrap = -s.wrap /* write the trailer only once! */
    return if (s.pending != 0) Z_OK else Z_STREAM_END
}

/* ========================================================================= */
fun deflateEnd(
    strm: z_stream,
    s: deflate_state
): Int {
    if (deflateStateCheck(strm, s) != 0) return Z_STREAM_ERROR

    /* Deallocate in reverse order of allocations: */
    s.pending_buf = null
    s.head = null
    s.prev = null
    s.window = null

    return if (s.status == BUSY_STATE) Z_DATA_ERROR else Z_OK
}

/* =========================================================================
 * Copy the source state to the destination state.
 * To simplify the source, this is not supported for 16-bit MSDOS (which
 * doesn't have enough memory anyway to duplicate compression states).
 */
fun deflateCopy(
    dest: z_stream,
    ds: deflate_state,
    source: z_stream,
    ss: deflate_state
): Int {
    /*#ifdef MAXSEG_64K
        return Z_STREAM_ERROR;
    #else*/

    if (deflateStateCheck(source, ss) != 0 || dest == Z_NULL) {
        return Z_STREAM_ERROR
    }

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
    ds.status = ss.status
    ds.pending_buf = ss.pending_buf?.copyOf()
    ds.pending_buf_size = ss.pending_buf_size
    ds.pending_out = ss.pending_out
    ds.pending = ss.pending
    ds.wrap = ss.wrap
    ds.gzhead = ss.gzhead
    ds.gzindex = ss.gzindex
    ds.method = ss.method
    ds.last_flush = ss.last_flush
    ds.w_size = ss.w_size
    ds.w_bits = ss.w_bits
    ds.w_mask = ss.w_mask
    ds.window = ss.window
    ds.window_size = ss.window_size
    ds.prev = ss.prev?.copyOf()
    ds.head = ss.head?.copyOf()
    ds.ins_h = ss.ins_h
    ds.hash_size = ss.hash_size
    ds.hash_bits = ss.hash_bits
    ds.hash_mask = ss.hash_mask
    ds.hash_shift = ss.hash_shift
    ds.block_start = ss.block_start
    ds.match_length = ss.match_length
    ds.prev_match = ss.prev_match
    ds.match_available = ss.match_available
    ds.strstart = ss.strstart
    ds.match_start = ss.match_start
    ds.lookahead = ss.lookahead
    ds.prev_length = ss.prev_length
    ds.max_chain_length = ss.max_chain_length
    ds.max_lazy_match = ss.max_lazy_match
    ds.level = ss.level
    ds.strategy = ss.strategy
    ds.good_match = ss.good_match
    ds.nice_match = ss.nice_match
    copy(ss.dyn_ltree, ds.dyn_ltree)
    copy(ss.dyn_dtree, ds.dyn_dtree)
    copy(ss.bl_tree, ds.bl_tree)
    ds.l_desc.setCopy(ss.l_desc)
    ds.d_desc.setCopy(ss.d_desc)
    ds.bl_desc.setCopy(ss.bl_desc)
    copy(ss.bl_count, ds.bl_count)
    copy(ss.heap, ds.heap)
    ds.heap_len = ss.heap_len
    ds.heap_max = ss.heap_max
    copy(ss.depth, ds.depth)
    ds.l_buf = ss.l_buf
    ds.lit_bufsize = ss.lit_bufsize
    ds.last_lit = ss.last_lit
    ds.d_buf = ss.d_buf
    ds.opt_len = ss.opt_len
    ds.static_len = ss.static_len
    ds.matches = ss.matches
    ds.insert = ss.insert
    ds.bi_buf = ss.bi_buf
    ds.bi_valid = ss.bi_valid
    ds.high_water = ss.high_water

    return Z_OK
    //#endif /* MAXSEG_64K */
}

/* ===========================================================================
 * Read a new buffer from the current input stream, update the adler32
 * and total number of bytes read.  All deflate() input goes through
 * this function so some applications may wish to modify it to avoid
 * allocating a large strm.next_in buffer and copying from it.
 * (See also flush_pending()).
 */
fun read_buf(
    strm: z_stream,
    s: deflate_state,
    buf: ByteArray,
    buf_i: UInt,
    size: UInt
): UInt {
    var len = strm.avail_in

    if (len > size) len = size
    if (len == 0) return 0

    strm.avail_in -= len

    zmemcpy(
        buf, buf_i,
        strm.next_in!!, strm.next_in_i,
        len
    )
    if (s.wrap == 1) {
        strm.adler = adler32(strm.adler, buf, buf_i, len)
    }
    //#ifdef GZIP
    else if (s.wrap == 2) {
        strm.adler = crc32(strm.adler, buf, buf_i, len)
    }
    //#endif
    strm.next_in_i += len
    strm.total_in += len

    return len
}

/* ===========================================================================
 * Initialize the "longest match" routines for a new zlib stream
 */
private fun lm_init(s: deflate_state) {
    s.window_size = 2 * s.w_size

    CLEAR_HASH(s)

    /* Set the default configuration parameters:
     */
    val configuration_table = DeflateTables.configuration_table
    s.max_lazy_match = configuration_table[s.level].max_lazy.toUInt()
    s.good_match = configuration_table[s.level].good_length.toUInt()
    s.nice_match = configuration_table[s.level].nice_length.toUInt()
    s.max_chain_length = configuration_table[s.level].max_chain.toUInt()

    s.strstart = 0
    s.block_start = 0
    s.lookahead = 0
    s.insert = 0
    s.match_length = MIN_MATCH - 1
    s.prev_length = s.match_length
    s.match_available = 0
    s.ins_h = 0
    /*#ifndef FASTEST
    #ifdef ASMV
        match_init(); /* initialize the asm code */
    #endif
    #endif*/
}

//#ifndef FASTEST
/* ===========================================================================
 * Set match_start to the longest match starting at the given string and
 * return its length. Matches shorter or equal to prev_length are discarded,
 * in which case the result is equal to prev_length and match_start is
 * garbage.
 * IN assertions: cur_match is the head of the hash chain for the current
 *   string (strstart) and its distance is <= MAX_DIST, and prev_length >= 1
 * OUT assertion: the match length is not greater than s.lookahead.
 */
//#ifndef ASMV
/* For 80x86 and 680x0, an optimized version will be provided in match.asm or
 * match.S. The code will be functionally equivalent.
 */
private fun longest_match(
    s: deflate_state,
    cur_match: IPos                             /* current match */
): UInt {
    val window = s.window!!

    var cur_match = cur_match
    var chain_length = s.max_chain_length/* max hash chain length */
    var scan = s.strstart /* current string */
    var best_len = s.prev_length         /* best match length so far */
    var nice_match = s.nice_match             /* stop if match long enough */
    val limit =
        if (s.strstart > MAX_DIST(s)) s.strstart - MAX_DIST(s) else NIL.toUInt()
    /* Stop when cur_match becomes <= limit. To simplify the code,
     * we prevent matches with the string of window index 0.
     */
    val prev = s.prev!!
    val wmask = s.w_mask

    /*#ifdef UNALIGNED_OK
        /* Compare two bytes at a time. Note: this is not always beneficial.
         * Try with and without -DUNALIGNED_OK to check.
         */
        register Bytef * strend = s . window +s.strstart + MAX_MATCH - 1;
    register ush scan_start = *(ushf *) scan;
    register ush scan_end = *(ushf *)(scan + best_len - 1);
    #else*/
    val strend = s.strstart + MAX_MATCH
    var scan_end1 = window[scan + best_len - 1]
    var scan_end = window[scan + best_len]
    //#endif

    /* The code is optimized for HASH_BITS >= 8 and MAX_MATCH-2 multiple of 16.
     * It is easy to get rid of this optimization if necessary.
     */
    //Assert(s.hash_bits >= 8 && MAX_MATCH == 258, "Code too clever");

    /* Do not waste too much time if we already have a good match: */
    if (s.prev_length >= s.good_match) {
        chain_length = chain_length ushr 2
    }
    /* Do not look for matches beyond the end of the input. This is necessary
     * to make deflate deterministic.
     */
    if (nice_match > s.lookahead) nice_match = s.lookahead

    //Assert(
    //    (ulg) s . strstart <= s . window_size -MIN_LOOKAHEAD,
    //    "need lookahead"
    //);

    do {
        //Assert(cur_match < s.strstart, "no future");
        var match = cur_match

        /* Skip to next match if the match length cannot increase
         * or if the match length is less than 2.  Note that the checks below
         * for insufficient lookahead only occur occasionally for performance
         * reasons.  Therefore uninitialized memory will be accessed, and
         * conditional jumps will be made that depend on those values.
         * However the length of the match is limited to the lookahead, so
         * the output of deflate is not affected by the uninitialized values.
         */
        /*#if (defined(UNALIGNED_OK) && MAX_MATCH == 258)
        /* This code assumes sizeof(unsigned short) == 2. Do not use
         * UNALIGNED_OK if your compiler uses a different size.
         */
            if ( * (ushf *)(match + best_len - 1) != scan_end ||
        *(ushf *) match != scan_start) continue;

        /* It is not necessary to compare scan[2] and match[2] since they are
         * always equal when the other bytes match, given that the hash keys
         * are equal and that HASH_BITS >= 8. Compare 2 bytes at a time at
         * strstart+3, +5, ... up to strstart+257. We check for insufficient
         * lookahead only every 4th comparison; the 128th check will be made
         * at strstart+257. If MAX_MATCH-2 is not a multiple of 8, it is
         * necessary to put more guard bytes at the end of the window, or
         * to check more often for insufficient lookahead.
         */
        Assert(scan[2] == match[2], "scan[2]?");
        scan++, match++;
        do {
        } while ( * (ushf *)(scan += 2) == *(ushf *)(match += 2) &&
        *(ushf *)(scan += 2) == *(ushf *)(match += 2) &&
        *(ushf *)(scan += 2) == *(ushf *)(match += 2) &&
        *(ushf *)(scan += 2) == *(ushf *)(match += 2) &&
                scan < strend);
        /* The funny "do {}" generates better code on most compilers */

        /* Here, scan <= window+strstart+257 */
        Assert(scan <= s.window + (unsigned)(s.window_size - 1), "wild scan");
        if ( * scan == * match) scan++;

        len = (MAX_MATCH - 1) - (int)(strend - scan);
        scan = strend - (MAX_MATCH - 1);

        #else /* UNALIGNED_OK */*/

        if (window[match + best_len] == scan_end &&
            window[match + best_len - 1] == scan_end1 &&
            window[match] == window[scan] &&
            window[++match] == window[scan + 1]) {

            /* The check at best_len-1 can be removed because it will be made
         * again later. (This heuristic is not always a win.)
         * It is not necessary to compare scan[2] and match[2] since they
         * are always equal when the other bytes match, given that
         * the hash keys are equal and that HASH_BITS >= 8.
         */
            scan += 2
            match++
            //Assert(*scan == * match, "match[2]?");

            /* We check for insufficient lookahead only every 8th comparison;
         * the 256th check will be made at strstart+258.
         */
            do {
            } while (window[++scan] == window[++match] &&
                window[++scan] == window[++match] &&
                window[++scan] == window[++match] &&
                window[++scan] == window[++match] &&
                window[++scan] == window[++match] &&
                window[++scan] == window[++match] &&
                window[++scan] == window[++match] &&
                window[++scan] == window[++match] &&
                scan < strend)

            //Assert(scan <= s.window + (unsigned)(s.window_size - 1), "wild scan");

            val len = MAX_MATCH - (strend - scan) /* length of current match */
            scan = strend - MAX_MATCH

            //#endif /* UNALIGNED_OK */

            if (len > best_len) {
                s.match_start = cur_match
                best_len = len
                if (len >= nice_match) break
                /*#ifdef UNALIGNED_OK
                    scan_end = *(ushf *)(scan + best_len - 1);
            #else*/
                scan_end1 = window[scan + best_len - 1]
                scan_end = window[scan + best_len]
                //#endif
            }
        }
        cur_match = prev[cur_match and wmask].toUInt()
    } while (cur_match > limit && --chain_length != 0)

    if (best_len <= s.lookahead) return best_len
    return s.lookahead
}
//#endif /* ASMV */

/*#else /* FASTEST */

/* ---------------------------------------------------------------------------
 * Optimized version for FASTEST only
 */
local uInt longest_match(s, cur_match)
deflate_state *s;
IPos cur_match;                             /* current match */
{
    register Bytef * scan = s . window +s.strstart; /* current string */
    register Bytef * match;                       /* matched string */
    register int len;                           /* length of current match */
    register Bytef * strend = s . window +s.strstart + MAX_MATCH;

    /* The code is optimized for HASH_BITS >= 8 and MAX_MATCH-2 multiple of 16.
     * It is easy to get rid of this optimization if necessary.
     */
    Assert(s.hash_bits >= 8 && MAX_MATCH == 258, "Code too clever");

    Assert(
        (ulg) s . strstart <= s . window_size -MIN_LOOKAHEAD,
        "need lookahead"
    );

    Assert(cur_match < s.strstart, "no future");

    match = s.window + cur_match;

    /* Return failure if the match length is less than 2:
     */
    if (match[0] != scan[0] || match[1] != scan[1]) return MIN_MATCH - 1;

    /* The check at best_len-1 can be removed because it will be made
     * again later. (This heuristic is not always a win.)
     * It is not necessary to compare scan[2] and match[2] since they
     * are always equal when the other bytes match, given that
     * the hash keys are equal and that HASH_BITS >= 8.
     */
    scan += 2, match += 2;
    Assert(*scan == * match, "match[2]?");

    /* We check for insufficient lookahead only every 8th comparison;
     * the 256th check will be made at strstart+258.
     */
    do {
    } while ( * ++scan == *++ match && * ++scan == *++ match &&
    *++scan == *++ match && * ++scan == *++ match &&
    *++scan == *++ match && * ++scan == *++ match &&
    *++scan == *++ match && * ++scan == *++ match &&
    scan < strend);

    Assert(scan <= s.window + (unsigned)(s.window_size - 1), "wild scan");

    len = MAX_MATCH - (int)(strend - scan);

    if (len < MIN_MATCH) return MIN_MATCH - 1;

    s.match_start = cur_match;
    return (uInt) len <= s . lookahead ?(uInt) len : s . lookahead;
}

#endif /* FASTEST */*/

/*#ifdef ZLIB_DEBUG

#define EQUAL 0
/* result of memcmp for equal strings */

/* ===========================================================================
 * Check that the match at match_start is indeed a match.
 */
local void check_match(s, start, match, length)
deflate_state *s;
IPos start, match;
int length;
{
    /* check that the match is indeed a match */
    if (zmemcmp(
            s.window + match,
            s.window + start, length
        ) != EQUAL) {
        fprintf(
            stderr, " start %u, match %u, length %d\n",
            start, match, length
        );
        do {
            fprintf(stderr, "%c%c", s.window[match++], s.window[start++]);
        } while (--length != 0);
        z_error("invalid match");
    }
    if (z_verbose > 1) {
        fprintf(stderr, "\\[%d,%d]", start - match, length);
        do {
            putc(s.window[start++], stderr); } while (--length != 0);
    }
}
#else*/
private inline fun check_match(
    s: deflate_state,
    start: IPos,
    match: IPos,
    length: Int
) {
}
//#endif /* ZLIB_DEBUG */

/* ===========================================================================
 * Fill the window when the lookahead becomes insufficient.
 * Updates strstart and lookahead.
 *
 * IN assertion: lookahead < MIN_LOOKAHEAD
 * OUT assertions: strstart <= window_size-MIN_LOOKAHEAD
 *    At least one byte has been read, or avail_in == 0; reads are
 *    performed for at least two bytes (required for the zip translate_eol
 *    option -- not supported here).
 */
fun fill_window(
    strm: z_stream,
    s: deflate_state
) {
    val window = s.window!!

    val wsize = s.w_size

    //Assert(s.lookahead < MIN_LOOKAHEAD, "already enough lookahead");

    do {
        var more: UInt = s.window_size - s.lookahead - s.strstart

        /* Deal with !@#$% 64K limit: */
        /*if (sizeof(int) <= 2) {
            if (more == 0 && s.strstart == 0 && s.lookahead == 0) {
                more = wsize;

            } else if (more == (unsigned)(-1)) {
                /* Very unlikely, but possible on 16 bit machine if
                 * strstart == 0 && lookahead == 1 (input done a byte at time)
                 */
                more--;
            }
        }*/

        /* If the window is almost full and there is insufficient lookahead,
         * move the upper half to the lower one to make room in the upper half.
         */
        if (s.strstart >= wsize + MAX_DIST(s)) {
            zmemcpy(window, 0, window, wsize, wsize - more)
            s.match_start -= wsize
            s.strstart -= wsize /* we now have strstart >= MAX_DIST */
            s.block_start -= wsize
            slide_hash(s)
            more += wsize
        }
        if (strm.avail_in == 0) break

        /* If there was no sliding:
         *    strstart <= WSIZE+MAX_DIST-1 && lookahead <= MIN_LOOKAHEAD - 1 &&
         *    more == window_size - lookahead - strstart
         * => more >= window_size - (MIN_LOOKAHEAD-1 + WSIZE + MAX_DIST-1)
         * => more >= window_size - 2*WSIZE + 2
         * In the BIG_MEM or MMAP case (not yet supported),
         *   window_size == input_size + MIN_LOOKAHEAD  &&
         *   strstart + s.lookahead <= input_size => more >= MIN_LOOKAHEAD.
         * Otherwise, window_size == 2*WSIZE so more >= 2.
         * If there was sliding, more >= WSIZE. So in all cases, more >= 2.
         */
        //Assert(more >= 2, "more < 2");

        val n = read_buf(
            strm, s, window, s.strstart + s.lookahead, more
        )
        s.lookahead += n

        /* Initialize the hash value now that we have some input: */
        if (s.lookahead + s.insert >= MIN_MATCH) {
            var str = s.strstart - s.insert
            s.ins_h = window[str].toUInt()
            UPDATE_HASH(s, window[str + 1])
            //#if MIN_MATCH != 3
            //Call UPDATE_HASH () MIN_MATCH -3 more times
            //#endif
            while (s.insert != 0) {
                UPDATE_HASH(s, window[str + MIN_MATCH - 1])
                //#ifndef FASTEST
                s.prev!![str and s.w_mask] = s.head!![s.ins_h]
                //#endif
                s.head!![s.ins_h] = str.toShort()
                str++
                s.insert--
                if (s.lookahead + s.insert < MIN_MATCH)
                    break
            }
        }
        /* If the whole input has less than MIN_MATCH bytes, ins_h is garbage,
         * but this is not important since only literal bytes will be emitted.
         */

    } while (s.lookahead < MIN_LOOKAHEAD && strm.avail_in != 0)

    /* If the WIN_INIT bytes after the end of the current data have never been
     * written, then zero those bytes in order to avoid memory check reports of
     * the use of uninitialized (or uninitialised as Julian writes) bytes by
     * the longest match routines.  Update the high water mark for the next
     * time through here.  WIN_INIT is set to MAX_MATCH since the longest match
     * routines allow scanning to strstart + MAX_MATCH, ignoring lookahead.
     */
    if (s.high_water < s.window_size) {
        val curr = s.strstart + s.lookahead

        if (s.high_water < curr) {
            /* Previous high water mark below current data -- zero WIN_INIT
             * bytes or up to end of window, whichever is less.
             */
            var init = s.window_size - curr
            if (init > WIN_INIT)
                init = WIN_INIT
            zmemzero(window, curr, init)
            s.high_water = curr + init
        } else if (s.high_water < curr + WIN_INIT) {
            /* High water mark at or above current data, but below current data
             * plus WIN_INIT -- zero out to current data plus WIN_INIT, or up
             * to end of window, whichever is less.
             */
            var init = curr + WIN_INIT - s.high_water
            if (init > s.window_size - s.high_water)
                init = s.window_size - s.high_water
            zmemzero(window, s.high_water, init)
            s.high_water += init
        }
    }

    //Assert(
    //    (ulg) s . strstart <= s . window_size -MIN_LOOKAHEAD,
    //    "not enough room for search"
    //);
}

/* ===========================================================================
 * Flush the current block, with given end-of-file flag.
 * IN assertion: strstart is set to the end of the current match.
 */
private inline fun FLUSH_BLOCK_ONLY(
    strm: z_stream,
    s: deflate_state,
    last: UInt
) {
    _tr_flush_block(
        strm, s,
        if (s.block_start >= 0) s.window else Z_NULL,
        if (s.block_start >= 0) s.block_start else 0,
        s.strstart - s.block_start, last
    )
    s.block_start = s.strstart
    flush_pending(strm, s)
    //Tracev((stderr, "[FLUSH]"));
}

/* Same but force premature exit if necessary. */
private inline fun FLUSH_BLOCK(
    strm: z_stream,
    s: deflate_state,
    last: UInt
): block_state? {
    FLUSH_BLOCK_ONLY(strm, s, last)
    if (strm.avail_out == 0)
        return if (last != 0) block_state.finish_started else block_state.need_more
    return null
}

/* Maximum stored block length in deflate format (not including header). */
private inline val MAX_STORED get() = 65535

/* Minimum of a and b. */
// #define MIN(a, b) ((a) > (b) ? (b) : (a))
private inline fun MIN(a: Int, b: Int) = min(a, b)

/* ===========================================================================
 * Copy without compression as much as possible from the input stream, return
 * the current block state.
 *
 * In case deflateParams() is used to later switch to a non-zero compression
 * level, s.matches (otherwise unused when storing) keeps track of the number
 * of hash table slides to perform. If s.matches is 1, then one hash table
 * slide will be done when switching. If s.matches is 2, the maximum value
 * allowed here, then the hash table will be cleared, since two or more slides
 * is the same as a clear.
 *
 * deflate_stored() is written to minimize the number of times an input byte is
 * copied. It is most efficient with large input and output buffers, which
 * maximizes the opportunites to have a single copy from next_in to next_out.
 */
private fun deflate_stored(
    strm: z_stream,
    s: deflate_state,
    flush: Int
): block_state {
    val window = s.window!!

    /* Smallest worthy block size when not flushing or finishing. By default
     * this is 32K. This can be as small as 507 bytes for memLevel == 1. For
     * large input and output buffers, the stored block size will be larger.
     */
    var min_block = MIN(s.pending_buf_size - 5, s.w_size)

    /* Copy as many min_block or larger stored blocks directly to next_out as
     * possible. If flushing, copy the remaining available input to next_out as
     * stored blocks, if there is enough space.
     */
    var last = 0
    var used = strm.avail_in
    do {
        /* Set len to the maximum size block that we can copy directly with the
         * available input data and output space. Set left to how much of that
         * would be copied from what's left in the window.
         */
        var len = MAX_STORED       /* maximum deflate stored block length */
        var have = (s.bi_valid + 42) ushr 3         /* number of header bytes */
        if (strm.avail_out < have)          /* need room for header */
            break
        /* maximum stored block length that will fit in avail_out: */
        have = strm.avail_out - have
        var left = s.strstart - s.block_start    /* bytes left in window */
        if (len > left + strm.avail_in)
            len = left + strm.avail_in     /* limit len to the input */
        if (len > have)
            len = have                         /* limit len to the output */

        /* If the stored block would be less than min_block in length, or if
         * unable to copy all of the available input when flushing, then try
         * copying to the window and the pending buffer instead. Also don't
         * write an empty block when flushing -- deflate() does that.
         */
        if (len < min_block && ((len == 0 && flush != Z_FINISH) ||
                    flush == Z_NO_FLUSH ||
                    len != left + strm.avail_in))
            break

        /* Make a dummy stored block in pending to get the header bytes,
         * including any pending bits. This also updates the debugging counts.
         */
        last = if (flush == Z_FINISH && len == left + strm.avail_in) 1 else 0
        _tr_stored_block(s, Z_NULL, 0, 0, last)

        /* Replace the lengths in the dummy stored block with len. */
        s.pending_buf!![s.pending - 4] = len.toByte()
        s.pending_buf!![s.pending - 3] = (len ushr 8).toByte()
        s.pending_buf!![s.pending - 2] = len.inv().toByte()
        s.pending_buf!![s.pending - 1] = (len.inv() ushr 8).toByte()

        /* Write the stored block header bytes. */
        flush_pending(strm, s)

        /*#ifdef ZLIB_DEBUG
                /* Update debugging counts for the data about to be copied. */
                s.compressed_len += len shl 3;
        s.bits_sent += len shl 3;
        #endif*/

        /* Copy uncompressed bytes from the window to next_out. */
        if (left != 0) {
            if (left > len)
                left = len
            zmemcpy(
                strm.next_out!!, strm.next_out_i,
                window, s.block_start,
                left
            )
            strm.next_out_i += left
            strm.avail_out -= left
            strm.total_out += left
            s.block_start += left
            len -= left
        }

        /* Copy uncompressed bytes directly from next_in to next_out, updating
         * the check value.
         */
        if (len != 0) {
            read_buf(strm, s, strm.next_out!!, strm.next_out_i, len)
            strm.next_out_i += len
            strm.avail_out -= len
            strm.total_out += len
        }
    } while (last == 0)

    /* Update the sliding window with the last s.w_size bytes of the copied
     * data, or append all of the copied data to the existing window if less
     * than s.w_size bytes were copied. Also update the number of bytes to
     * insert in the hash tables, in the event that deflateParams() switches to
     * a non-zero compression level.
     */
    used -= strm.avail_in      /* number of input bytes directly copied */
    if (used != 0) {
        /* If any input was used, then no unused input remains in the window,
         * therefore s.block_start == s.strstart.
         */
        if (used >= s.w_size) {    /* supplant the previous history */
            s.matches = 2         /* clear hash */
            zmemcpy(
                window, 0,
                strm.next_in!!, strm.next_in_i - s.w_size,
                s.w_size
            )
            s.strstart = s.w_size
        } else {
            if (s.window_size - s.strstart <= used) {
                /* Slide the window down. */
                s.strstart -= s.w_size
                zmemcpy(window, 0, window, s.w_size, s.strstart)
                if (s.matches < 2)
                    s.matches++   /* add a pending slide_hash() */
            }
            zmemcpy(
                window, s.strstart,
                strm.next_in!!, strm.next_in_i - used,
                used
            )
            s.strstart += used
        }
        s.block_start = s.strstart
        s.insert += MIN(used, s.w_size - s.insert)
    }
    if (s.high_water < s.strstart)
        s.high_water = s.strstart

    /* If the last block was written to next_out, then done. */
    if (last != 0)
        return block_state.finish_done

    /* If flushing and all input has been consumed, then done. */
    if (flush != Z_NO_FLUSH && flush != Z_FINISH &&
        strm.avail_in == 0 && s.strstart == s.block_start)
        return block_state.block_done

    /* Fill the window with any remaining input. */
    var have = s.window_size - s.strstart - 1
    if (strm.avail_in > have && s.block_start >= s.w_size) {
        /* Slide the window down. */
        s.block_start -= s.w_size
        s.strstart -= s.w_size
        zmemcpy(window, 0, window, s.w_size, s.strstart)
        if (s.matches < 2)
            s.matches++           /* add a pending slide_hash() */
        have += s.w_size          /* more space now */
    }
    if (have > strm.avail_in)
        have = strm.avail_in
    if (have != 0) {
        read_buf(strm, s, window, s.strstart, have)
        s.strstart += have
    }
    if (s.high_water < s.strstart)
        s.high_water = s.strstart

    /* There was not enough avail_out to write a complete worthy or flushed
     * stored block to next_out. Write a stored block to pending instead, if we
     * have enough input for a worthy block, or if flushing and there is enough
     * room for the remaining input as a stored block in the pending buffer.
     */
    have = (s.bi_valid + 42) ushr 3         /* number of header bytes */
    /* maximum stored block length that will fit in pending: */
    have = MIN(s.pending_buf_size - have, MAX_STORED)
    min_block = MIN(have, s.w_size)
    val left = s.strstart - s.block_start
    if (left >= min_block ||
        ((left != 0 || flush == Z_FINISH) && flush != Z_NO_FLUSH &&
                strm.avail_in == 0 && left <= have)) {
        val len = MIN(left, have)
        last = if (flush == Z_FINISH && strm.avail_in == 0 &&
            len == left) 1 else 0
        _tr_stored_block(s, window, s.block_start, len, last)
        s.block_start += len
        flush_pending(strm, s)
    }

    /* We've done all we can with the available input and output. */
    return if (last != 0) block_state.finish_started else block_state.need_more
}

/* ===========================================================================
 * Compress as much as possible from the input stream, return the current
 * block state.
 * This function does not perform lazy evaluation of matches and inserts
 * new strings in the dictionary only for unmatched strings or for short
 * matches. It is used only for the fast compression options.
 */
private fun deflate_fast(
    strm: z_stream,
    s: deflate_state,
    flush: Int
): block_state {
    val window = s.window!!
    val pending_buf = s.pending_buf!!

    while (true) {
        /* Make sure that we always have enough lookahead, except
         * at the end of the input file. We need MAX_MATCH bytes
         * for the next match, plus MIN_MATCH bytes to insert the
         * string following the next match.
         */
        if (s.lookahead < MIN_LOOKAHEAD) {
            fill_window(strm, s)
            if (s.lookahead < MIN_LOOKAHEAD && flush == Z_NO_FLUSH) {
                return block_state.need_more
            }
            if (s.lookahead == 0) break /* flush the current block */
        }

        /* Insert the string window[strstart .. strstart+2] in the
         * dictionary, and set hash_head to the head of the hash chain:
         */
        var hash_head = NIL /* head of the hash chain */
        if (s.lookahead >= MIN_MATCH) {
            hash_head = INSERT_STRING(s, window, s.strstart)
        }

        /* Find the longest match, discarding those <= prev_length.
         * At this point we have always match_length < MIN_MATCH
         */
        if (hash_head != NIL && s.strstart - hash_head.toUInt() <= MAX_DIST(s)) {
            /* To simplify the code, we prevent matches with the string
             * of window index 0 (in particular we have to avoid a match
             * of the string with itself at the start of the input file).
             */
            s.match_length = longest_match(s, hash_head.toUInt())
            /* longest_match() sets match_start */
        }
        val bflush: Boolean           /* set if current block must be flushed */
        if (s.match_length >= MIN_MATCH) {
            check_match(s, s.strstart, s.match_start, s.match_length)

            bflush = _tr_tally_dist(
                s, pending_buf, (s.strstart - s.match_start).toShort(),
                (s.match_length - MIN_MATCH).toByte()
            )

            s.lookahead -= s.match_length

            /* Insert new strings in the hash table only if the match length
             * is not too large. This saves time but degrades compression.
             */
            //#ifndef FASTEST
            if (s.match_length <= s.max_insert_length &&
                s.lookahead >= MIN_MATCH) {
                s.match_length-- /* string at strstart already in table */
                do {
                    s.strstart++
                    hash_head = INSERT_STRING(s, window, s.strstart)
                    /* strstart never exceeds WSIZE-MAX_MATCH, so there are
                     * always MIN_MATCH bytes ahead.
                     */
                } while (--s.match_length != 0)
                s.strstart++
            } else
            //#endif
            {
                s.strstart += s.match_length
                s.match_length = 0
                s.ins_h = window[s.strstart].toUInt()
                UPDATE_HASH(s, window[s.strstart + 1])
                /*#if MIN_MATCH != 3
                Call UPDATE_HASH () MIN_MATCH -3 more times
                #endif*/
                /* If lookahead < MIN_MATCH, ins_h is garbage, but it does not
                 * matter since it will be recomputed at next deflate call.
                 */
            }
        } else {
            /* No match, output a literal byte */
            //Tracevv((stderr, "%c", s.window[s.strstart]));
            bflush = _tr_tally_lit(s, pending_buf, window[s.strstart])
            s.lookahead--
            s.strstart++
        }
        if (bflush) FLUSH_BLOCK(strm, s, 0)?.let { return it }
    }
    s.insert = if (s.strstart < MIN_MATCH - 1) s.strstart else MIN_MATCH - 1
    if (flush == Z_FINISH) {
        FLUSH_BLOCK(strm, s, 1)?.let { return it }
        return block_state.finish_done
    }
    if (s.last_lit != 0)
        FLUSH_BLOCK(strm, s, 0)?.let { return it }
    return block_state.block_done
}

//#ifndef FASTEST
/* ===========================================================================
 * Same as above, but achieves better compression. We use a lazy
 * evaluation for matches: a match is finally adopted only if there is
 * no better match at the next window position.
 */
private fun deflate_slow(
    strm: z_stream,
    s: deflate_state,
    flush: Int
): block_state {
    val window = s.window!!
    val pending_buf = s.pending_buf!!

    /* Process the input block. */
    while (true) {
        /* Make sure that we always have enough lookahead, except
         * at the end of the input file. We need MAX_MATCH bytes
         * for the next match, plus MIN_MATCH bytes to insert the
         * string following the next match.
         */
        if (s.lookahead < MIN_LOOKAHEAD) {
            fill_window(strm, s)
            if (s.lookahead < MIN_LOOKAHEAD && flush == Z_NO_FLUSH) {
                return block_state.need_more
            }
            if (s.lookahead == 0) break /* flush the current block */
        }

        /* Insert the string window[strstart .. strstart+2] in the
         * dictionary, and set hash_head to the head of the hash chain:
         */
        var hash_head = NIL /* head of hash chain */
        if (s.lookahead >= MIN_MATCH) {
            hash_head = INSERT_STRING(s, window, s.strstart)
        }

        /* Find the longest match, discarding those <= prev_length.
         */
        s.prev_length = s.match_length
        s.prev_match = s.match_start
        s.match_length = MIN_MATCH - 1

        if (hash_head != NIL && s.prev_length < s.max_lazy_match &&
            s.strstart - hash_head.toUInt() <= MAX_DIST(s)) {
            /* To simplify the code, we prevent matches with the string
             * of window index 0 (in particular we have to avoid a match
             * of the string with itself at the start of the input file).
             */
            s.match_length = longest_match(s, hash_head.toUInt())
            /* longest_match() sets match_start */

            if (s.match_length <= 5 && (s.strategy == Z_FILTERED
                        //#if TOO_FAR <= 32767
                        || (s.match_length == MIN_MATCH &&
                        s.strstart - s.match_start > TOO_FAR)
                        //#endif
                        )) {

                /* If prev_match is also MIN_MATCH, match_start is garbage
                 * but we will ignore the current match anyway.
                 */
                s.match_length = MIN_MATCH - 1
            }
        }
        /* If there was a match at the previous step and the current
         * match is not better, output the previous match:
         */
        if (s.prev_length >= MIN_MATCH && s.match_length <= s.prev_length) {
            val max_insert = s.strstart + s.lookahead - MIN_MATCH
            /* Do not insert strings in hash table beyond this. */

            check_match(s, s.strstart - 1, s.prev_match, s.prev_length)

            val bflush = _tr_tally_dist(
                s, pending_buf, (s.strstart - 1 - s.prev_match).toShort(),
                (s.prev_length - MIN_MATCH).toByte()
            )

            /* Insert in hash table all strings up to the end of the match.
             * strstart-1 and strstart are already inserted. If there is not
             * enough lookahead, the last two strings are not inserted in
             * the hash table.
             */
            s.lookahead -= s.prev_length - 1
            s.prev_length -= 2
            do {
                if (++s.strstart <= max_insert) {
                    hash_head = INSERT_STRING(s, window, s.strstart)
                }
            } while (--s.prev_length != 0)
            s.match_available = 0
            s.match_length = MIN_MATCH - 1
            s.strstart++

            if (bflush) FLUSH_BLOCK(strm, s, 0)?.let { return it }

        } else if (s.match_available != 0) {
            /* If there was no match at the previous position, output a
             * single literal. If there was a match but the current match
             * is longer, truncate the previous match to a single literal.
             */
            //Tracevv((stderr, "%c", s.window[s.strstart - 1]));
            val bflush = _tr_tally_lit(s, pending_buf, window[s.strstart - 1])
            if (bflush) {
                FLUSH_BLOCK_ONLY(strm, s, 0)
            }
            s.strstart++
            s.lookahead--
            if (strm.avail_out == 0) return block_state.need_more
        } else {
            /* There is no previous match to compare with, wait for
             * the next step to decide.
             */
            s.match_available = 1
            s.strstart++
            s.lookahead--
        }
    }
    //Assert(flush != Z_NO_FLUSH, "no flush?");
    if (s.match_available != 0) {
        //Tracevv((stderr, "%c", s.window[s.strstart - 1]));
        _tr_tally_lit(s, pending_buf, window[s.strstart - 1])
        s.match_available = 0
    }
    s.insert = if (s.strstart < MIN_MATCH - 1) s.strstart else MIN_MATCH - 1
    if (flush == Z_FINISH) {
        FLUSH_BLOCK(strm, s, 1)?.let { return it }
        return block_state.finish_done
    }
    if (s.last_lit != 0)
        FLUSH_BLOCK(strm, s, 0)?.let { return it }
    return block_state.block_done
}
//#endif /* FASTEST */

/* ===========================================================================
 * For Z_RLE, simply look for runs of bytes, generate matches only of distance
 * one.  Do not maintain a hash table.  (It will be regenerated if this run of
 * deflate switches away from Z_RLE.)
 */
private fun deflate_rle(
    strm: z_stream,
    s: deflate_state,
    flush: Int
): block_state {
    val window = s.window!!
    val pending_buf = s.pending_buf!!

    while (true) {
        /* Make sure that we always have enough lookahead, except
         * at the end of the input file. We need MAX_MATCH bytes
         * for the longest run, plus one for the unrolled loop.
         */
        if (s.lookahead <= MAX_MATCH) {
            fill_window(strm, s)
            if (s.lookahead <= MAX_MATCH && flush == Z_NO_FLUSH) {
                return block_state.need_more
            }
            if (s.lookahead == 0) break /* flush the current block */
        }

        /* See how many times the previous byte repeats */
        s.match_length = 0
        if (s.lookahead >= MIN_MATCH && s.strstart > 0) {
            var scan =
                s.strstart - 1 /* scan goes up to strend for length of run */
            val prev = window[scan] /* byte at distance one to match */
            if (prev == window[++scan] && prev == window[++scan] && prev == window[++scan]) {
                val strend =
                    s.strstart + MAX_MATCH /* scan goes up to strend for length of run */
                do {
                } while (prev == window[++scan] &&
                    prev == window[++scan] &&
                    prev == window[++scan] &&
                    prev == window[++scan] &&
                    prev == window[++scan] &&
                    prev == window[++scan] &&
                    prev == window[++scan] &&
                    prev == window[++scan] &&
                    scan < strend)
                s.match_length = MAX_MATCH - (strend - scan)
                if (s.match_length > s.lookahead)
                    s.match_length = s.lookahead
            }
            //Assert(scan <= s.window + (uInt)(s.window_size - 1), "wild scan");
        }

        /* Emit match if have run of MIN_MATCH or longer, else emit literal */
        val bflush: Boolean /* set if current block must be flushed */
        if (s.match_length >= MIN_MATCH) {
            check_match(s, s.strstart, s.strstart - 1, s.match_length)

            bflush = _tr_tally_dist(
                s, pending_buf, 1, (s.match_length - MIN_MATCH).toByte()
            )

            s.lookahead -= s.match_length
            s.strstart += s.match_length
            s.match_length = 0
        } else {
            /* No match, output a literal byte */
            //Tracevv((stderr, "%c", s.window[s.strstart]));
            bflush = _tr_tally_lit(s, pending_buf, window[s.strstart])
            s.lookahead--
            s.strstart++
        }
        if (bflush) FLUSH_BLOCK(strm, s, 0)?.let { return it }
    }
    s.insert = 0
    if (flush == Z_FINISH) {
        FLUSH_BLOCK(strm, s, 1)?.let { return it }
        return block_state.finish_done
    }
    if (s.last_lit != 0)
        FLUSH_BLOCK(strm, s, 0)?.let { return it }
    return block_state.block_done
}

/* ===========================================================================
 * For Z_HUFFMAN_ONLY, do not look for matches.  Do not maintain a hash table.
 * (It will be regenerated if this run of deflate switches away from Huffman.)
 */
private fun deflate_huff(
    strm: z_stream,
    s: deflate_state,
    flush: Int
): block_state {
    val window = s.window!!
    val pending_buf = s.pending_buf!!

    while (true) {
        /* Make sure that we have a literal to write. */
        if (s.lookahead == 0) {
            fill_window(strm, s)
            if (s.lookahead == 0) {
                if (flush == Z_NO_FLUSH)
                    return block_state.need_more
                break      /* flush the current block */
            }
        }

        /* Output a literal byte */
        s.match_length = 0
        //Tracevv((stderr, "%c", s.window[s.strstart]));
        val bflush = _tr_tally_lit(s, pending_buf, window[s.strstart])
        s.lookahead--
        s.strstart++
        if (bflush) FLUSH_BLOCK(strm, s, 0)?.let { return it }
    }
    s.insert = 0
    if (flush == Z_FINISH) {
        FLUSH_BLOCK(strm, s, 1)?.let { return it }
        return block_state.finish_done
    }
    if (s.last_lit != 0)
        FLUSH_BLOCK(strm, s, 0)?.let { return it }
    return block_state.block_done
}
