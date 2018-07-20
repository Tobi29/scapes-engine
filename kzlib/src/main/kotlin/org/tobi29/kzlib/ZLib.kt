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

import org.tobi29.stdex.combineToInt
import org.tobi29.stdex.copy
import org.tobi29.stdex.splitToBytes

/*
     The application must update next_in and avail_in when avail_in has dropped
   to zero.  It must update next_out and avail_out when avail_out has dropped
   to zero.  The application must initialize zalloc, zfree and opaque before
   calling the init function.  All other fields are set by the compression
   library and must not be updated by the application.
     The opaque value provided by the application will be passed as the first
   parameter for calls of zalloc and zfree.  This can be useful for custom
   memory management.  The compression library attaches no meaning to the
   opaque value.
     zalloc must return Z_NULL if there is not enough memory for the object.
   If zlib is used in a multi-threaded application, zalloc and zfree must be
   thread safe.  In that case, zlib is thread-safe.  When zalloc and zfree are
   Z_NULL on entry to the initialization function, they are set to internal
   routines that use the standard library functions malloc() and free().
     On 16-bit systems, the functions zalloc and zfree must be able to allocate
   exactly 65536 bytes, but will not be required to allocate more than this if
   the symbol MAXSEG_64K is defined (see zconf.h).  WARNING: On MSDOS, pointers
   returned by zalloc for objects of exactly 65536 bytes *must* have their
   offset normalized to zero.  The default allocation function provided by this
   library ensures this (see zutil.c).  To reduce memory requirements and avoid
   any allocation of 64K objects, at the expense of compression ratio, compile
   the library with -DMAX_WBITS=14 (see zconf.h).
     The fields total_in and total_out can be used for statistics or progress
   reports.  After compression, total_in holds the total size of the
   uncompressed data and may be saved for use by the decompressor (particularly
   if the decompressor wants to decompress everything in a single step).
*/

/* constants */

const val Z_NO_FLUSH = 0
const val Z_PARTIAL_FLUSH = 1
const val Z_SYNC_FLUSH = 2
const val Z_FULL_FLUSH = 3
const val Z_FINISH = 4
const val Z_BLOCK = 5
const val Z_TREES = 6
/* Allowed flush values; see deflate() and inflate() below for details */

const val Z_OK = 0
const val Z_STREAM_END = 1
const val Z_NEED_DICT = 2
const val Z_ERRNO = -1
const val Z_STREAM_ERROR = -2
const val Z_DATA_ERROR = -3
const val Z_MEM_ERROR = -4
const val Z_BUF_ERROR = -5
const val Z_VERSION_ERROR = -6
/* Return codes for the compression/decompression functions. Negative values
 * are errors, positive values are used for special but normal events.
 */

const val Z_NO_COMPRESSION = 0
const val Z_BEST_SPEED = 1
const val Z_BEST_COMPRESSION = 9
const val Z_DEFAULT_COMPRESSION = -1
/* compression levels */

const val Z_FILTERED = 1
const val Z_HUFFMAN_ONLY = 2
const val Z_RLE = 3
const val Z_FIXED = 4
const val Z_DEFAULT_STRATEGY = 0
/* compression strategy; see deflateInit2() below for details */

const val Z_BINARY = 0
const val Z_TEXT = 1
const val Z_ASCII = Z_TEXT   /* for compatibility with 1.2.2 and earlier */
const val Z_UNKNOWN = 2
/* Possible values of the data_type field for deflate() */

const val Z_DEFLATED = 8
/* The deflate compression method (the only one supported in this version) */

inline val Z_NULL get() = null  /* for initializing zalloc, zfree, opaque */

class z_stream {
    var next_in: ByteArray? = null /* next input byte */
    var next_in_i = 0
    var avail_in = 0 /* number of bytes available at next_in */
    var total_in = 0L /* total number of input bytes read so far */

    var next_out: ByteArray? = null /* next output byte will go here */
    var next_out_i = 0
    var avail_out = 0 /* remaining free space at next_out */
    var total_out = 0L /* total number of bytes output so far */

    var msg: String? = null /* last error message, NULL if no error */

    // alloc_func zalloc;  /* used to allocate the internal state */
    // free_func  zfree;   /* used to free the internal state */
    // voidpf     opaque;  /* private data object passed to zalloc and zfree */

    var data_type = 0 /* best guess about the data type: binary or text
                           for deflate, or the decoding state for inflate */
    var adler = 0 /* Adler-32 or CRC-32 value of the uncompressed data */
    // uLong   reserved;   /* reserved for future use */
}

class deflate_state {
    internal var status = 0 /* as the name implies */
    internal var pending_buf: ByteArray? = null /* output still pending */
    internal var pending_buf_size = 0 /* size of pending_buf */
    internal var pending_out: UInt =
        0 /* next pending byte to output to the stream */
    internal var pending = 0 /* nb of bytes in the pending buffer */
    internal var wrap = 0 /* bit 0 true for zlib, bit 1 true for gzip */
    internal var gzhead: gz_header? =
        null /* gzip header information to write */
    internal var gzindex = 0 /* where in extra, name, or comment */
    internal var method = 0 /* can only be DEFLATED */
    internal var last_flush =
        0 /* value of flush param for previous deflate call */

    /* used by deflate.c: */

    internal var w_size: UInt = 0 /* LZ77 window size (32K by default) */
    internal var w_bits: UInt = 0 /* log2(w_size)  (8..16) */
    internal var w_mask: UInt = 0 /* w_size - 1 */

    internal var window: ByteArray? = null
    /* Sliding window. Input bytes are read into the second half of the window,
     * and move to the first half later to keep a dictionary of at least wSize
     * bytes. With this organization, matches are limited to a distance of
     * wSize-MAX_MATCH bytes, but this ensures that IO is always
     * performed with a length multiple of the block size. Also, it limits
     * the window size to 64K, which is quite useful on MSDOS.
     * To do: use the user input buffer as sliding window.
     */

    internal var window_size = 0
    /* Actual size of window: 2*wSize, except when the user input buffer
     * is directly used as sliding window.
     */

    internal var prev: ShortArray? = null
    /* Link to older string with same hash index. To limit the size of this
     * array to 64K, this link is maintained only for the last 32K strings.
     * An index in this array is thus a window index modulo 32K.
     */

    internal var head: ShortArray? =
        null /* Heads of the hash chains or NIL. */

    internal var ins_h: UInt = 0 /* hash index of string to be inserted */
    internal var hash_size: UInt = 0 /* number of elements in hash table */
    internal var hash_bits: UInt = 0 /* log2(hash_size) */
    internal var hash_mask: UInt = 0 /* hash_size-1 */

    internal var hash_shift: UInt = 0
    /* Number of bits by which ins_h must be shifted at each input
     * step. It must be such that after MIN_MATCH steps, the oldest
     * byte no longer takes part in the hash key, that is:
     *   hash_shift * MIN_MATCH >= hash_bits
     */

    internal var block_start = 0
    /* Window position at the beginning of the current output block. Gets
     * negative when the window is moved backwards.
     */

    internal var match_length: UInt = 0 /* length of best match */
    internal var prev_match: IPos = 0 /* previous match */
    internal var match_available = 0 /* set if previous match exists */
    internal var strstart: UInt = 0 /* start of string to insert */
    internal var match_start: UInt = 0 /* start of matching string */
    internal var lookahead: UInt = 0 /* number of valid bytes ahead in window */

    internal var prev_length: UInt = 0
    /* Length of the best match at previous step. Matches not greater than this
     * are discarded. This is used in the lazy match evaluation.
     */

    internal var max_chain_length: UInt = 0
    /* To speed up deflation, hash chains are never searched beyond this
     * length.  A higher limit improves compression ratio but degrades the
     * speed.
     */

    internal var max_lazy_match: UInt = 0
    /* Attempt to find a better match only when the current match is strictly
     * smaller than this value. This mechanism is used only for compression
     * levels >= 4.
     */

    internal var max_insert_length
        get() = max_lazy_match
        set(value) {
            max_lazy_match = value
        }
    /* Insert new strings in the hash table only if the match length is not
     * greater than this length. This saves time but degrades compression.
     * max_insert_length is used only for compression levels <= 3.
     */

    internal var level = 0 /* compression level (1..9) */
    internal var strategy = 0 /* favor or force Huffman coding*/

    internal var good_match: UInt = 0
    /* Use a faster search when the previous match is longer than this */

    internal var nice_match =
        0 /* Stop searching when current match exceeds this */

    /* used by trees.c: */
    /* Didn't use ct_data typedef below to suppress compiler warning */
    internal val dyn_ltree =
        Array(HEAP_SIZE) { ct_data() } /* literal and length tree */
    internal val dyn_dtree =
        Array(2 * D_CODES + 1) { ct_data() } /* distance tree */
    internal val bl_tree =
        Array(2 * BL_CODES + 1) { ct_data() } /* Huffman tree for bit lengths */

    internal val l_desc = tree_desc() /* desc. for literal tree */
    internal val d_desc = tree_desc() /* desc. for distance tree */
    internal val bl_desc = tree_desc() /* desc. for bit length tree */

    internal val bl_count = ShortArray(MAX_BITS + 1)
    /* number of codes at each bit length for an optimal tree */

    internal val heap =
        IntArray(2 * L_CODES + 1) /* heap used to build the Huffman trees */
    internal var heap_len = 0 /* number of elements in the heap */
    internal var heap_max = 0 /* element of largest frequency */
    /* The sons of heap[n] are heap[2*n] and heap[2*n+1]. heap[0] is not used.
     * The same heap array is used to build all trees.
     */

    internal val depth = ByteArray(2 * L_CODES + 1)
    /* Depth of each subtree used as tie breaker for trees of equal frequency
     */

    internal var l_buf: UInt = 0          /* buffer for literals or lengths */

    internal var lit_bufsize: UInt = 0
    /* Size of match buffer for literals/lengths.  There are 4 reasons for
     * limiting lit_bufsize to 64K:
     *   - frequencies can be kept in 16 bit counters
     *   - if compression is not successful for the first block, all input
     *     data is still in the window so we can still emit a stored block even
     *     when input comes from standard input.  (This can also be done for
     *     all blocks if lit_bufsize is not greater than 32K.)
     *   - if compression is not successful for a file smaller than 64K, we can
     *     even emit a stored file instead of a stored block (saving 5 bytes).
     *     This is applicable only for zip (not gzip or zlib).
     *   - creating new Huffman trees less frequently may not provide fast
     *     adaptation to changes in the input data statistics. (Take for
     *     example a binary file with poorly compressible code followed by
     *     a highly compressible string table.) Smaller buffer sizes give
     *     fast adaptation but have of course the overhead of transmitting
     *     trees more frequently.
     *   - I can't count above 4
     */

    internal var last_lit: UInt = 0      /* running index in l_buf */

    internal var d_buf: UInt = 0
    /* Buffer for distances. To simplify the code, d_buf and l_buf have
     * the same number of elements. To use different lengths, an extra flag
     * array would be necessary.
     */

    internal var opt_len =
        0 /* bit length of current block with optimal trees */
    internal var static_len =
        0 /* bit length of current block with static trees */
    internal var matches: UInt =
        0 /* number of string matches in current block */
    internal var insert: UInt = 0 /* bytes at end of window left to insert */

    /*#ifdef ZLIB_DEBUG
    ulg compressed_len; /* total bit length of compressed file mod 2^32 */
    ulg bits_sent;      /* bit length of compressed data sent mod 2^32 */
    #endif*/

    internal var bi_buf: UShort = 0
    /* Output buffer. bits are inserted starting at the bottom (least
     * significant bits).
     */
    internal var bi_valid = 0
    /* Number of valid bits in bi_buf.  All bits above the last valid bit
     * are always zero.
     */

    internal var high_water = 0
    /* High water mark offset in window for initialized bytes -- bytes above
     * this are set to zero in order to avoid memory check warnings when
     * longest match routines access bytes past the input.  This is then
     * updated to the new high water mark.
     */
}

class inflate_state {
    internal var mode = inflate_mode.BAD          /* current inflate mode */
    internal var last = 0                   /* true if processing last block */
    internal var wrap = 0                   /* bit 0 true for zlib, bit 1 true for gzip,
                                   bit 2 true to validate check value */
    internal var havedict = 0               /* true if dictionary provided */
    internal var flags = 0        /* gzip header method and flags (0 if zlib) */
    internal var dmax: UInt =
        0       /* zlib header max distance (INFLATE_STRICT) */
    internal var check: UInt = 0        /* protected copy of check value */
    internal var total: UInt = 0        /* protected copy of output count */
    internal var head: gz_header? =
        null /* where to save gzip header information */
    /* sliding window */
    internal var wbits: UInt = 0      /* log base 2 of requested window size */
    internal var wsize: UInt =
        0      /* window size or zero if not using window */
    internal var whave: UInt = 0      /* valid bytes in the window */
    internal var wnext: UInt = 0      /* window write index */
    internal var window: ByteArray? =
        null  /* allocated sliding window, if needed */
    /* bit accumulator */
    internal var hold: UInt = 0         /* input bit accumulator */
    internal var bits: UInt = 0              /* number of bits in "in" */
    /* for string and stored block copying */
    internal var length: UInt =
        0            /* literal or length of data to copy */
    internal var offset: UInt =
        0            /* distance back to copy string from */
    /* for table and code decoding */
    internal var extra: UInt = 0             /* extra bits needed */
    /* fixed and dynamic code tables */
    internal var lencode: Array<code>? =
        null /* starting table for length/literal codes */
    internal var lencode_i: UInt = 0
    internal var distcode: Array<code>? =
        null /* starting table for distance codes */
    internal var distcode_i: UInt = 0
    internal var lenbits: UInt = 0 /* index bits for lencode */
    internal var distbits: UInt = 0 /* index bits for distcode */
    /* dynamic table building */
    internal var ncode: UInt = 0      /* number of code length code lengths */
    internal var nlen: UInt = 0       /* number of length code lengths */
    internal var ndist: UInt = 0      /* number of distance code lengths */
    internal var have: UInt = 0       /* number of code lengths in lens[] */
    internal var next: UInt = 0 /* next available space in codes[] */
    internal val lens =
        ShortArray(320)   /* temporary storage for code lengths */
    internal val work =
        ShortArray(288)   /* work area for code table building */
    internal val codes = Array(ENOUGH) { code() } /* space for code tables */
    internal var sane = false /* if false, allow invalid distance too far */
    internal var back =
        0          /* bits back of last unprocessed length/lit */
    internal var was: UInt = 0      /* initial length of match */
}

enum class inflate_mode {
    HEAD,       /* i: waiting for magic header */
    FLAGS,      /* i: waiting for method and flags (gzip) */
    TIME,       /* i: waiting for modification time (gzip) */
    OS,         /* i: waiting for extra flags and operating system (gzip) */
    EXLEN,      /* i: waiting for extra length (gzip) */
    EXTRA,      /* i: waiting for extra bytes (gzip) */
    NAME,       /* i: waiting for end of file name (gzip) */
    COMMENT,    /* i: waiting for end of comment (gzip) */
    HCRC,       /* i: waiting for header crc (gzip) */
    DICTID,     /* i: waiting for dictionary check value */
    DICT,       /* waiting for inflateSetDictionary() call */
    TYPE,       /* i: waiting for type bits, including last-flag bit */
    TYPEDO,     /* i: same, but skip check to exit inflate on new block */
    STORED,     /* i: waiting for stored size (length and complement) */
    COPY_,      /* i/o: same as COPY below, but only first time in */
    COPY,       /* i/o: waiting for input or output to copy stored block */
    TABLE,      /* i: waiting for dynamic block table lengths */
    LENLENS,    /* i: waiting for code length code lengths */
    CODELENS,   /* i: waiting for length/lit and distance code lengths */
    LEN_,       /* i: same as LEN below, but only first time in */
    LEN,        /* i: waiting for length/lit/eob code */
    LENEXT,     /* i: waiting for length extra bits */
    DIST,       /* i: waiting for distance code */
    DISTEXT,    /* i: waiting for distance extra bits */
    MATCH,      /* o: waiting for output space to copy string */
    LIT,        /* o: waiting for output space to write literal */
    CHECK,      /* i: waiting for 32-bit check value */
    LENGTH,     /* i: waiting for 32-bit length (gzip) */
    DONE,       /* finished check, done -- remain here until reset */
    BAD,        /* got a data error -- remain here until reset */
    MEM,        /* got an inflate() memory error -- remain here until reset */
    SYNC        /* looking for synchronization bytes to restart inflate() */
}

inline val inflate_mode.is_error: Boolean
    get() = this == inflate_mode.BAD
            || this == inflate_mode.MEM
            || this == inflate_mode.SYNC

inline val inflate_mode.is_finish: Boolean
    get() = this == inflate_mode.CHECK
            || this == inflate_mode.LENGTH
            || this == inflate_mode.DONE
            || is_error


typealias UByte = Byte
typealias UShort = Short
typealias UInt = Int
typealias ULong = Long

typealias Pos = UShort
typealias Posf = Pos
typealias IPos = UInt

class gz_header {
    var text = 0 /* true if compressed data believed to be text */
    var time: ULong = 0L /* modification time */
    var xflags = 0 /* extra flags (not used when writing a gzip file) */
    var os = 0 /* operating system */
    var extra: ByteArray? = null /* pointer to extra field or Z_NULL if none */
    var extra_len: UInt = 0 /* extra field length (valid if extra != Z_NULL) */
    var extra_max: UInt = 0 /* space at extra (only when reading header) */
    var name: ByteArray? =
        null /* pointer to zero-terminated file name or Z_NULL */
    var name_max: UInt = 0 /* space at name (only when reading header) */
    var comment: ByteArray? =
        null /* pointer to zero-terminated comment or Z_NULL */
    var comm_max: UInt = 0 /* space at comment (only when reading header) */
    var hcrc = 0 /* true if there was or will be a header crc */
    var done = 0 /* true when done reading gzip header (not used
                           when writing a gzip file) */
}

class code(
    var op: Byte = 0,
    var bits: Byte = 0,
    var `val`: Short = 0
) {

    fun set(other: code) {
        op = other.op
        bits = other.bits
        `val` = other.`val`
    }
}

class ct_data(
    var freq: UShort = 0,
    var dad: UShort = 0
) {
    fun set(other: ct_data) {
        freq = other.freq
        dad = other.dad
    }
}

inline var ct_data.code: UShort
    get() = freq
    set(value) {
        freq = value
    }

inline var ct_data.len: UShort
    get() = dad
    set(value) {
        dad = value
    }

class static_tree_desc(
    var static_tree: Array<ct_data>? = null, /* static tree or NULL */
    var extra_bits: IntArray? = null, /* extra bits for each code or NULL */
    var extra_base: Int = 0, /* base index for extra_bits */
    var elems: Int = 0, /* max number of elements in the tree */
    var max_length: Int = 0 /* max bit length for the codes */
)

class tree_desc(
    var dyn_tree: Array<ct_data>? = null, /* the dynamic tree */
    var max_code: Int = 0, /* largest code with non zero frequency */
    var stat_desc: static_tree_desc? = null /* the corresponding static tree */
) {
    fun set(other: tree_desc) {
        dyn_tree = other.dyn_tree
        max_code = other.max_code
        stat_desc = other.stat_desc
    }

    fun setCopy(other: tree_desc) {
        dyn_tree = other.dyn_tree?.copyOf()
        max_code = other.max_code
        stat_desc = other.stat_desc
    }
}

/* Type of code to build for inflate_table() */
enum class codetype {
    CODES,
    LENS,
    DISTS
}

enum class block_state {
    need_more,      /* block not completed, need more input or more output */
    block_done,     /* block flush performed */
    finish_started, /* finish started, need only more output at next deflate */
    finish_done     /* finish done, accept no more input or output */
}

internal const val OS_CODE = 0x00.toByte()

internal const val MAX_MEM_LEVEL = 9

internal const val MAX_WBITS = 15

const val DEF_WBITS = MAX_WBITS
/* default windowBits for decompression. MAX_WBITS is for compression only */


const val DEF_MEM_LEVEL = 8
/* default memLevel */

internal const val STORED_BLOCK = 0
internal const val STATIC_TREES = 1
internal const val DYN_TREES = 2
/* The three kinds of block type */

internal const val MIN_MATCH = 3
internal const val MAX_MATCH = 258
/* The minimum and maximum match lengths */

internal const val PRESET_DICT =
    0x20 /* preset dictionary flag in zlib header */

internal const val ENOUGH_LENS = 852
internal const val ENOUGH_DISTS = 592
internal const val ENOUGH = ENOUGH_LENS + ENOUGH_DISTS

internal const val LENGTH_CODES = 29
/* number of length codes, not counting the special END_BLOCK code */

internal const val LITERALS = 256
/* number of literal bytes 0..255 */

internal const val L_CODES = LITERALS + 1 + LENGTH_CODES
/* number of Literal or Length codes, including the END_BLOCK code */

internal const val D_CODES = 30
/* number of distance codes */

internal const val BL_CODES = 19
/* number of codes used to transfer the bit lengths */

internal const val HEAP_SIZE = 2 * L_CODES + 1
/* maximum heap size */

internal const val MAX_BITS = 15
/* All codes must not exceed MAX_BITS bits */

internal const val Buf_size = 16
/* size of bit buffer in bi_buf */

internal const val INIT_STATE = 42    /* zlib header -> BUSY_STATE */
//#ifdef GZIP
internal const val GZIP_STATE = 57 /* gzip header -> BUSY_STATE | EXTRA_STATE */
//#endif
internal const val EXTRA_STATE = 69    /* gzip extra block -> NAME_STATE */
internal const val NAME_STATE = 73    /* gzip file name -> COMMENT_STATE */
internal const val COMMENT_STATE = 91    /* gzip comment -> HCRC_STATE */
internal const val HCRC_STATE = 103    /* gzip header CRC -> BUSY_STATE */
internal const val BUSY_STATE = 113    /* deflate -> FINISH_STATE */
internal const val FINISH_STATE = 666    /* stream complete */

internal inline fun Byte.toUInt(): UInt = toInt() and 0xFF
internal inline fun Byte.toULong(): ULong = toLong() and 0xFF

internal inline fun Short.toUInt(): UInt = toInt() and 0xFFFF
internal inline fun Short.toULong(): ULong = toLong() and 0xFFFF

internal inline fun Int.toULong(): ULong = toLong() and 0xFFFFFFFF

internal inline fun ZSWAP32(value: Int) =
    value.splitToBytes { b3, b2, b1, b0 -> combineToInt(b0, b1, b2, b3) }

internal inline fun zmemcpy(
    dest: ByteArray, dest_i: UInt,
    src: ByteArray, src_i: UInt,
    len: UInt
) = copy(src, dest, len, src_i, dest_i)

internal inline fun zmemcpy(
    dest: ShortArray, dest_i: UInt,
    src: ShortArray, src_i: UInt,
    len: UInt
) = copy(src, dest, len, src_i, dest_i)

internal inline fun zmemzero(array: ByteArray, array_i: UInt, len: UInt) {
    for (i in array_i until array_i + len) {
        array[i] = 0
    }
}

internal inline fun zmemzero(array: ShortArray, array_i: UInt, len: UInt) {
    for (i in array_i until array_i + len) {
        array[i] = 0
    }
}
