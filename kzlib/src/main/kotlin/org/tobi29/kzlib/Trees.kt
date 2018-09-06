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

import org.tobi29.stdex.Constant
import org.tobi29.stdex.combineToShort
import org.tobi29.stdex.splitToBytes
import kotlin.experimental.inv
import kotlin.experimental.or

/*
 *  ALGORITHM
 *
 *      The "deflation" process uses several Huffman trees. The more
 *      common source values are represented by shorter bit sequences.
 *
 *      Each code tree is stored in a compressed form which is itself
 * a Huffman encoding of the lengths of all the code strings (in
 * ascending order by source values).  The actual code strings are
 * reconstructed from the lengths in the inflate process, as described
 * in the deflate specification.
 *
 *  REFERENCES
 *
 *      Deutsch, L.P.,"'Deflate' Compressed Data Format Specification".
 *      Available in ftp.uu.net:/pub/archiving/zip/doc/deflate-1.1.doc
 *
 *      Storer, James A.
 *          Data Compression:  Methods and Theory, pp. 49-50.
 *          Computer Science Press, 1988.  ISBN 0-7167-8156-5.
 *
 *      Sedgewick, R.
 *          Algorithms, p290.
 *          Addison-Wesley, 1983. ISBN 0-201-06672-6.
 */

/* ===========================================================================
 * Constants
 */

@Constant
internal inline val MAX_BL_BITS
    get() = 7
/* Bit length codes must not exceed MAX_BL_BITS bits */

@Constant
internal inline val END_BLOCK
    get() = 256
/* end of block literal code */

@Constant
internal inline val REP_3_6
    get() = 16
/* repeat previous bit length 3-6 times (2 bits of repeat count) */

@Constant
internal inline val REPZ_3_10
    get() = 17
/* repeat a zero length 3-10 times  (3 bits of repeat count) */

@Constant
internal inline val REPZ_11_138
    get() = 18
/* repeat a zero length 11-138 times  (7 bits of repeat count) */

@Constant
inline val DIST_CODE_LEN
    get() = 512 /* see definition of array dist_code below */

@Constant
private inline val LC_LENGTH
    get() = MAX_MATCH - MIN_MATCH + 1

internal object TreesTables {
    val extra_lbits = intArrayOf( /* extra bits for each length code */
        0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4,
        5, 5, 5, 5, 0
    )

    val extra_dbits =
        intArrayOf( /* extra bits for each distance code */
            0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9,
            10, 10, 11, 11, 12, 12, 13, 13
        )

    val extra_blbits =
        intArrayOf(/* extra bits for each bit length code */
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 7
        )

    val bl_order = byteArrayOf(
        16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15
    )
    /* The lengths of the bit length codes are sent in order of decreasing
     * probability, to avoid transmitting the lengths for unused bit length codes.
     */

    /* ===========================================================================
     * Local data. These are initialized only once.
     */

    val static_ltree =
        ShortArray(2 * (L_CODES + 2)).also { static_ltree ->
            val bl_count = ShortArray(MAX_BITS + 1)
            /* number of codes at each bit length for an optimal tree */

            /* Construct the codes of the static literal tree */
            for (bits in 0..MAX_BITS) bl_count[bits] = 0
            var n = 0
            while (n <= 143) {
                static_ltree[len(n++)]/*.len*/ = 8
                bl_count[8]++
            }
            while (n <= 255) {
                static_ltree[len(n++)]/*.len*/ = 9
                bl_count[9]++
            }
            while (n <= 279) {
                static_ltree[len(n++)]/*.len*/ = 7
                bl_count[7]++
            }
            while (n <= 287) {
                static_ltree[len(n++)]/*.len*/ = 8
                bl_count[8]++
            }
            /* Codes 286 and 287 do not exist, but we must include them in the
             * tree construction to get a canonical Huffman tree (longest code
             * all ones)
             */
            gen_codes(static_ltree, L_CODES + 1, bl_count)
        }

    val static_dtree = ShortArray(2 * D_CODES).also { static_dtree ->
        /* The static distance tree is trivial: */
        for (n in 0 until D_CODES) {
            static_dtree[len(n)]/*.len*/ = 5
            static_dtree[code(n)]/*.code*/ = bi_reverse(n, 5).toShort()
        }
    }

    val _dist_code = ByteArray(DIST_CODE_LEN).also { _dist_code ->
        /* Initialize the mapping dist (0..32K)  . dist code (0..29) */
        var dist = 0 /* distance index */
        for (code in 0 until 16) {
            for (n in 0 until (1 shl extra_dbits[code])) {
                _dist_code[dist++] = code.toByte()
            }
        }
        //Assert (dist == 256, "tr_static_init: dist != 256");
        dist = dist ushr 7 /* from now on, all distances are divided by 128 */
        for (code in 16 until 30/*D_CODES*/) {
            for (n in 0 until (1 shl (extra_dbits[code] - 7))) {
                _dist_code[256 + dist++] = code.toByte()
            }
        }
        //Assert (dist == 256, "tr_static_init: 256+dist != 512");
    }

    val _length_code = ByteArray(LC_LENGTH).also { _length_code ->
        /* Initialize the mapping length (0..255)  . length code (0..28) */
        var length = 0 /* length value */
        for (code in 0 until LENGTH_CODES - 1) {
            for (n in 0 until (1 shl extra_lbits[code])) {
                _length_code[length++] = code.toByte()
            }
        }
        //Assert (length == 256, "tr_static_init: length != 256");
        /* Note that the length 255 (match length 258) can be represented
         * in two different ways: code 284 + 5 bits or code 285, so we
         * overwrite length_code[255] to use the best encoding:
         */
        _length_code[length - 1] = (LENGTH_CODES - 1).toByte()
    }

    val base_length = IntArray(LENGTH_CODES).also { base_length ->
        var length = 0 /* length value */
        for (code in 0 until LENGTH_CODES - 1) {
            base_length[code] = length
            length += 1 shl extra_lbits[code]
        }
    }

    val base_dist = IntArray(D_CODES).also { base_dist ->
        /* Initialize the mapping dist (0..32K)  . dist code (0..29) */
        var dist = 0 /* distance index */
        for (code in 0 until 16) {
            base_dist[code] = dist
            dist += 1 shl extra_dbits[code]
        }
        //Assert (dist == 256, "tr_static_init: dist != 256");
        dist = dist ushr 7 /* from now on, all distances are divided by 128 */
        for (code in 16 until D_CODES) {
            base_dist[code] = dist shl 7
            dist += 1 shl (extra_dbits[code] - 7)
        }
        //Assert (dist == 256, "tr_static_init: 256+dist != 512");
    }

    val static_l_desc = static_tree_desc(
        static_ltree, extra_lbits, LITERALS + 1, L_CODES, MAX_BITS
    )

    val static_d_desc = static_tree_desc(
        static_dtree, extra_dbits, 0, D_CODES, MAX_BITS
    )

    val static_bl_desc = static_tree_desc(
        null, extra_blbits, 0, BL_CODES, MAX_BL_BITS
    )
}

/* ===========================================================================
 * Local (static) routines in this file.
 */

/*local void tr_static_init OF((void));
local void init_block     OF((deflate_state *s));
local void pqdownheap     OF((deflate_state *s, ct_data *tree, int k));
local void gen_bitlen     OF((deflate_state *s, tree_desc *desc));
local void gen_codes      OF((ct_data *tree, int max_code, ushf *bl_count));
local void build_tree     OF((deflate_state *s, tree_desc *desc));
local void scan_tree      OF((deflate_state *s, ct_data *tree, int max_code));
local void send_tree      OF((deflate_state *s, ct_data *tree, int max_code));
local int  build_bl_tree  OF((deflate_state *s));
local void send_all_trees OF((deflate_state *s, int lcodes, int dcodes,
int blcodes));
local void compress_block OF((deflate_state *s, const ct_data *ltree,
const ct_data *dtree));
local int  detect_data_type OF((deflate_state *s));
local unsigned bi_reverse OF((unsigned value, int length));
local void bi_windup      OF((deflate_state *s));
local void bi_flush       OF((deflate_state *s));*/

/*#ifdef GEN_TREES_H
local void gen_trees_header OF((void));
#endif*/

//#ifndef ZLIB_DEBUG
internal inline fun send_code(s: deflate_state, c: UInt, tree: ShortArray) =
    send_bits(s, tree[freq(c)]/*.code*/.toUInt(), tree[len(c)]/*.len*/.toUInt())
/* Send a code of the given tree. c and tree must not have side effects */

/*#else /* !ZLIB_DEBUG */
#  define send_code(s, c, tree) \
{ if (z_verbose>2) fprintf(stderr,"\ncd %3d ",(c)); \
    send_bits(s, tree[c].code, tree[c].len); }
#endif*/

/* ===========================================================================
 * Output a short LSB first on the stream.
 * IN assertion: there is enough room in pendingBuf.
 */
internal inline fun put_short(
    s: deflate_state,
    w: Short
) = w.splitToBytes { b1, b0 ->
    put_byte(s, b0)
    put_byte(s, b1)
}

/* ===========================================================================
 * Send a value on a given number of bits.
 * IN assertion: length <= 16 and value fits in length bits.
 */
/*#ifdef ZLIB_DEBUG
local void send_bits      OF((deflate_state *s, int value, int length));

local void send_bits(s, value, length)
deflate_state *s;
int value;  /* value to send */
int length; /* number of bits */
{
    Tracevv((stderr," l %2d v %4x ", length, value));
    Assert(length > 0 && length <= 15, "invalid length");
    s .bits_sent += (ulg)length;

    /* If not enough room in bi_buf, use (valid) bits from bi_buf and
     * (16 - bi_valid) bits from value, leaving (width - (16-bi_valid))
     * unused bits in value.
     */
    if (s .bi_valid > (int)Buf_size - length) {
        s .bi_buf |= (ush)value  shl  s .bi_valid;
    put_short(s, s .bi_buf);
    s .bi_buf = (ush)value  ushr  (Buf_size - s .bi_valid);
    s .bi_valid += length - Buf_size;
} else {
        s .bi_buf |= (ush)value  shl  s .bi_valid;
    s .bi_valid += length;
}
}
#else /* !ZLIB_DEBUG */*/

internal inline fun send_bits(s: deflate_state, value: Int, length: Int) {
    if (s.bi_valid > Buf_size - length) {
        s.bi_buf = s.bi_buf or (value shl s.bi_valid).toShort()
        put_short(s, s.bi_buf)
        s.bi_buf = (value ushr (Buf_size - s.bi_valid)).toShort()
        s.bi_valid += length - Buf_size
    } else {
        s.bi_buf = s.bi_buf or (value shl s.bi_valid).toShort()
        s.bi_valid += length
    }
}
//#endif /* ZLIB_DEBUG */


/* the arguments must not have side effects */

/* ===========================================================================
 * Initialize the various 'constant' tables.
 */
private inline fun tr_static_init() {
    // Already initialized in TreesH.kt
}

/*
/* ===========================================================================
 * Genererate the file trees.h describing the static trees.
 */
#ifdef GEN_TREES_H
#  ifndef ZLIB_DEBUG
#    include <stdio.h>
#  endif

#  define SEPARATOR(i, last, width) \
((i) == (last)? "\n};\n\n" :    \
((i) % (width) == (width)-1 ? ",\n" : ", "))

void gen_trees_header()
{
    FILE *header = fopen("trees.h", "w");
    int i;

    Assert (header != NULL, "Can't open trees.h");
    fprintf(header,
        "/* header created automatically with -DGEN_TREES_H */\n\n");

    fprintf(header, "local const ct_data static_ltree[L_CODES+2] = {\n");
    for (i = 0; i < L_CODES+2; i++) {
    fprintf(header, "{{%3u},{%3u}}%s", static_ltree[i].code,
        static_ltree[i].len, SEPARATOR(i, L_CODES+1, 5));
}

    fprintf(header, "local const ct_data static_dtree[D_CODES] = {\n");
    for (i = 0; i < D_CODES; i++) {
    fprintf(header, "{{%2u},{%2u}}%s", static_dtree[i].code,
        static_dtree[i].len, SEPARATOR(i, D_CODES-1, 5));
}

    fprintf(header, "const uch ZLIB_INTERNAL _dist_code[DIST_CODE_LEN] = {\n");
    for (i = 0; i < DIST_CODE_LEN; i++) {
    fprintf(header, "%2u%s", _dist_code[i],
        SEPARATOR(i, DIST_CODE_LEN-1, 20));
}

    fprintf(header,
        "const uch ZLIB_INTERNAL _length_code[MAX_MATCH-MIN_MATCH+1]= {\n");
    for (i = 0; i < MAX_MATCH-MIN_MATCH+1; i++) {
    fprintf(header, "%2u%s", _length_code[i],
        SEPARATOR(i, MAX_MATCH-MIN_MATCH, 20));
}

    fprintf(header, "local const int base_length[LENGTH_CODES] = {\n");
    for (i = 0; i < LENGTH_CODES; i++) {
    fprintf(header, "%1u%s", base_length[i],
        SEPARATOR(i, LENGTH_CODES-1, 20));
}

    fprintf(header, "local const int base_dist[D_CODES] = {\n");
    for (i = 0; i < D_CODES; i++) {
    fprintf(header, "%5u%s", base_dist[i],
        SEPARATOR(i, D_CODES-1, 10));
}

    fclose(header);
}
#endif /* GEN_TREES_H */*/

/* ===========================================================================
 * Initialize the tree data structures for a new zlib stream.
 */
fun _tr_init(s: deflate_state) {
    tr_static_init()

    s.l_desc.dyn_tree = s.dyn_ltree
    s.l_desc.stat_desc = TreesTables.static_l_desc

    s.d_desc.dyn_tree = s.dyn_dtree
    s.d_desc.stat_desc = TreesTables.static_d_desc

    s.bl_desc.dyn_tree = s.bl_tree
    s.bl_desc.stat_desc = TreesTables.static_bl_desc

    s.bi_buf = 0
    s.bi_valid = 0
    /*#ifdef ZLIB_DEBUG
        s .compressed_len = 0L;
    s .bits_sent = 0L;
    #endif*/

    /* Initialize the first block of the first file: */
    init_block(s)
}

/* ===========================================================================
 * Initialize a new block.
 */
fun init_block(s: deflate_state) {
    /* Initialize the trees. */
    for (n in 0 until L_CODES) s.dyn_ltree[freq(n)]/*.freq*/ = 0
    for (n in 0 until D_CODES) s.dyn_dtree[freq(n)]/*.freq*/ = 0
    for (n in 0 until BL_CODES) s.bl_tree[freq(n)]/*.freq*/ = 0

    s.dyn_ltree[freq(END_BLOCK)]/*.freq*/ = 1
    s.opt_len = 0
    s.static_len = 0
    s.last_lit = 0
    s.matches = 0
}

private const val SMALLEST = 1
/* Index within the heap array of least frequent node in the Huffman tree */


/* ===========================================================================
 * Remove the smallest element from the heap and recreate the heap with
 * one less element. Updates heap and heap_len.
 */
internal inline fun pqremove(s: deflate_state, tree: ShortArray): Int {
    val top = s.heap[SMALLEST]
    s.heap[SMALLEST] = s.heap[s.heap_len--]
    pqdownheap(s, tree, SMALLEST)
    return top
}

/* ===========================================================================
 * Compares to subtrees, using the tree depth as tie breaker when
 * the subtrees have equal frequency. This minimizes the worst case length.
 */
internal inline fun smaller(
    tree: ShortArray,
    n: UInt,
    m: UInt,
    depth: ByteArray
) =
    (tree[freq(n)]/*.freq*/.toUInt() < tree[freq(m)]/*.freq*/.toUInt() ||
            (tree[freq(n)]/*.freq*/ == tree[freq(m)]/*.freq*/ &&
                    depth[n].toUInt() <= depth[m].toUInt()))


/* ===========================================================================
 * Restore the heap property by moving down the tree starting at node k,
 * exchanging a node with the smallest of its two sons if necessary, stopping
 * when the heap property is re-established (each father smaller than its
 * two sons).
 */
fun pqdownheap(
    s: deflate_state,
    tree: ShortArray, /* the tree to restore */
    k: Int /* node to move down */
) {
    var k = k
    val v = s.heap[k]
    var j = k shl 1  /* left son of k */
    while (j <= s.heap_len) {
        /* Set j to the smallest of the two sons: */
        if (j < s.heap_len &&
            smaller(tree, s.heap[j + 1], s.heap[j], s.depth)) {
            j++
        }
        /* Exit if v is smaller than both sons */
        if (smaller(tree, v, s.heap[j], s.depth)) break

        /* Exchange v with the smallest son */
        s.heap[k] = s.heap[j]
        k = j

        /* And continue down the tree, setting j to the left son of k */
        j = j shl 1
    }
    s.heap[k] = v
}

/* ===========================================================================
 * Compute the optimal bit lengths for a tree and update the total bit length
 * for the current block.
 * IN assertion: the fields freq and dad are set, heap[heap_max] and
 *    above are the tree nodes sorted by increasing frequency.
 * OUT assertions: the field len is set to the optimal bit length, the
 *     array bl_count contains the frequencies for each bit length.
 *     The length opt_len is updated; static_len is also updated if stree is
 *     not null.
 */
fun gen_bitlen(
    s: deflate_state,
    desc: tree_desc /* the tree descriptor */
) {
    val tree = desc.dyn_tree!!
    val max_code = desc.max_code
    val stree = desc.stat_desc!!.static_tree
    val extra = desc.stat_desc!!.extra_bits!!
    val base = desc.stat_desc!!.extra_base
    val max_length = desc.stat_desc!!.max_length
    var overflow = 0   /* number of elements with bit length too large */

    for (bits in 0..MAX_BITS) s.bl_count[bits] = 0

    /* In a first pass, compute the optimal bit lengths (which may
     * overflow in the case of the bit length tree).
     */
    tree[len(s.heap[s.heap_max])]/*.len*/ = 0 /* root of the heap */

    for (h in s.heap_max + 1 until HEAP_SIZE) {
        val n = s.heap[h] /* iterate over the tree elements */
        var bits =
            tree[len(tree[dad(n)]/*.dad*/.toUInt())]/*.len*/ + 1 /* bit length */
        if (bits > max_length) {
            bits = max_length
            overflow++
        }
        tree[len(n)]/*.len*/ = bits.toShort()
        /* We overwrite tree[n].dad which is no longer needed */

        if (n > max_code) continue /* not a leaf node */

        s.bl_count[bits]++
        var xbits = 0 /* extra bits */
        if (n >= base) xbits = extra[n - base]
        val f = tree[freq(n)]/*.freq*/ /* frequency */
        s.opt_len += f.toUInt() * (bits + xbits)
        if (stree != null) s.static_len += f.toUInt() * (stree[len(n)]/*.len*/ + xbits)
    }
    if (overflow == 0) return

    //Tracev((stderr,"\nbit length overflow\n"));
    /* This happens for example on obj2 and pic of the Calgary corpus */

    /* Find the first bit length which could increase: */
    do {
        var bits = max_length - 1 /* bit length */
        while (s.bl_count[bits] == 0.toShort()) bits--
        s.bl_count[bits]--      /* move one leaf down the tree */
        s.bl_count[bits + 1] =
                (s.bl_count[bits + 1] + 2).toShort() /* move one overflow item as its brother */
        s.bl_count[max_length]--
        /* The brother of the overflow item also moves one step up,
         * but this does not affect bl_count[max_length]
         */
        overflow -= 2
    } while (overflow > 0)

    /* Now recompute all bit lengths, scanning in increasing frequency.
     * h is still equal to HEAP_SIZE. (It is simpler to reconstruct all
     * lengths instead of fixing only the wrong ones. This idea is taken
     * from 'ar' written by Haruhiko Okumura.)
     */
    var h = HEAP_SIZE
    for (bits in max_length downTo 1) {
        var n = s.bl_count[bits].toUInt()
        while (n != 0) {
            val m = s.heap[--h]
            if (m > max_code) continue
            if (tree[len(m)]/*.len*/.toUInt() != bits) {
                //Tracev((stderr,"code %d bits %d .%d\n", m, tree[m].len, bits));
                s.opt_len += (bits - tree[len(m)]/*.len*/.toUInt()) *
                        tree[freq(m)]/*.freq*/.toUInt()
                tree[len(m)]/*.len*/ = bits.toShort()
            }
            n--
        }
    }
}

/* ===========================================================================
 * Generate the codes for a given tree and bit counts (which need not be
 * optimal).
 * IN assertion: the array bl_count contains the bit length statistics for
 * the given tree and the field len is set for all tree elements.
 * OUT assertion: the field code is set for all tree elements of non
 *     zero code length.
 */
fun gen_codes(
    tree: ShortArray,             /* the tree to decorate */
    max_code: Int,              /* largest code with non zero frequency */
    bl_count: ShortArray            /* number of codes at each bit length */
) {
    val next_code =
        ShortArray(MAX_BITS + 1) /* next code value for each bit length */
    var code: UInt = 0         /* running code value */

    /* The distribution counts are first used to generate the code values
     * without bit reversal.
     */
    for (bits in 1..MAX_BITS) {
        code = (code + bl_count[bits - 1].toUInt()) shl 1
        next_code[bits] = code.toShort()
    }
    /* Check that the bit counts in bl_count are consistent. The last code
     * must be all ones.
     */
    //Assert (code + bl_count[MAX_BITS]-1 == (1 shl MAX_BITS)-1,
    //    "inconsistent bit counts");
    //Tracev((stderr,"\ngen_codes: max_code %d ", max_code));

    for (n in 0..max_code) {
        val len = tree[len(n)]/*.len*/.toUInt()
        if (len == 0) continue
        /* Now reverse the bits */
        tree[code(n)]/*.code*/ =
                bi_reverse(next_code[len].toUInt(), len).toShort()
        next_code[len]++

        //Tracecv(tree != static_ltree, (stderr,"\nn %3d %c l %2d c %4x (%x) ",
        //    n, (isgraph(n) ? n : ' '), len, tree[n].code, next_code[len]-1));
    }
}

/* ===========================================================================
 * Construct one Huffman tree and assigns the code bit strings and lengths.
 * Update the total bit length for the current block.
 * IN assertion: the field freq is set for all tree elements.
 * OUT assertions: the fields len and code are set to the optimal bit length
 *     and corresponding code. The length opt_len is updated; static_len is
 *     also updated if stree is not null. The field max_code is set.
 */
fun build_tree(
    s: deflate_state,
    desc: tree_desc /* the tree descriptor */
) {
    val tree = desc.dyn_tree!!
    val stree = desc.stat_desc!!.static_tree
    val elems = desc.stat_desc!!.elems
    var max_code = -1 /* largest code with non zero frequency */

    /* Construct the initial heap, with least frequent element in
     * heap[SMALLEST]. The sons of heap[n] are heap[2*n] and heap[2*n+1].
     * heap[0] is not used.
     */
    s.heap_len = 0
    s.heap_max = HEAP_SIZE

    for (n in 0 until elems) {
        if (tree[freq(n)]/*.freq*/ != 0.toShort()) {
            max_code = n
            s.heap[++(s.heap_len)] = n
            s.depth[n] = 0
        } else {
            tree[len(n)]/*.len*/ = 0
        }
    }

    /* The pkzip format requires that at least one distance code exists,
     * and that at least one bit should be sent even if there is only one
     * possible code. So to avoid special checks later on we force at least
     * two codes of non zero frequency.
     */
    while (s.heap_len < 2) {
        val node =
            if (max_code < 2) ++max_code else 0 /* new node being created */
        s.heap[++(s.heap_len)] = node
        tree[freq(node)]/*.freq*/ = 1
        s.depth[node] = 0
        s.opt_len--
        if (stree != null) s.static_len -= stree[len(node)]/*.len*/
        /* node is 0 or 1 so it does not have extra bits */
    }
    desc.max_code = max_code

    /* The elements heap[heap_len/2+1 .. heap_len] are leaves of the tree,
     * establish sub-heaps of increasing lengths:
     */
    for (n in s.heap_len / 2 downTo 1) pqdownheap(s, tree, n)

    /* Construct the Huffman tree by repeatedly combining the least two
     * frequent nodes.
     */
    var node = elems              /* next internal node of the tree */
    do {
        val n = pqremove(s, tree)  /* n = node of least frequency */
        val m = s.heap[SMALLEST] /* m = node of next least frequency */

        s.heap[--(s.heap_max)] = n /* keep the nodes sorted by frequency */
        s.heap[--(s.heap_max)] = m

        /* Create a new node father of n and m */
        tree[freq(node)]/*.freq*/ =
                (tree[freq(n)]/*.freq*/ + tree[freq(m)]/*.freq*/).toShort()
        s.depth[node] = ((if (s.depth[n].toUInt() >= s.depth[m].toUInt())
            s.depth[n] else s.depth[m]) + 1).toByte()
        tree[dad(n)]/*.dad*/ = node.toShort()
        tree[dad(m)]/*.dad*/ = node.toShort()
        /*#ifdef DUMP_BL_TREE
                if (tree == s .bl_tree) {
            fprintf(stderr,"\nnode %d(%d), sons %d(%d) %d(%d)",
                node, tree[node].freq, n, tree[n].freq, m, tree[m].freq);
        }
        #endif*/
        /* and insert the new node in the heap */
        s.heap[SMALLEST] = node++
        pqdownheap(s, tree, SMALLEST)

    } while (s.heap_len >= 2)

    s.heap[--(s.heap_max)] = s.heap[SMALLEST]

    /* At this point, the fields freq and dad are set. We can now
     * generate the bit lengths.
     */
    gen_bitlen(s, desc)

    /* The field len is now set, we can generate the bit codes */
    gen_codes(tree, max_code, s.bl_count)
}

/* ===========================================================================
 * Scan a literal or distance tree to determine the frequencies of the codes
 * in the bit length tree.
 */
fun scan_tree(
    s: deflate_state,
    tree: ShortArray,   /* the tree to be scanned */
    max_code: Int    /* and its largest code of non zero frequency */
) {
    var prevlen = -1          /* last emitted length */
    var nextlen = tree[len(0)]/*.len*/ /* length of next code */
    var count = 0             /* repeat count of the current code */
    var max_count = 7         /* max repeat count */
    var min_count = 4         /* min repeat count */

    if (nextlen == 0.toShort()) {
        max_count = 138
        min_count = 3
    }
    tree[len(max_code + 1)]/*.len*/ = 0xffff.toShort() /* guard */

    for (n in 0..max_code) {
        val curlen = nextlen.toUInt() /* length of current code */
        nextlen = tree[len(n + 1)]/*.len*/
        if (++count < max_count && curlen == nextlen.toUInt()) {
            continue
        } else if (count < min_count) {
            s.bl_tree[freq(curlen)]/*.freq*/ =
                    (s.bl_tree[freq(curlen)]/*.freq*/ + count).toShort()
        } else if (curlen != 0) {
            if (curlen != prevlen) s.bl_tree[freq(curlen)]/*.freq*/++
            s.bl_tree[freq(REP_3_6)]/*.freq*/++
        } else if (count <= 10) {
            s.bl_tree[freq(REPZ_3_10)]/*.freq*/++
        } else {
            s.bl_tree[freq(REPZ_11_138)]/*.freq*/++
        }
        count = 0
        prevlen = curlen
        if (nextlen == 0.toShort()) {
            max_count = 138
            min_count = 3
        } else if (curlen == nextlen.toUInt()) {
            max_count = 6
            min_count = 3
        } else {
            max_count = 7
            min_count = 4
        }
    }
}

/* ===========================================================================
 * Send a literal or distance tree in compressed form, using the codes in
 * bl_tree.
 */
fun send_tree(
    s: deflate_state,
    tree: ShortArray, /* the tree to be scanned */
    max_code: Int       /* and its largest code of non zero frequency */
) {
    var prevlen = -1          /* last emitted length */
    var nextlen = tree[len(0)]/*.len*/ /* length of next code */
    var count = 0             /* repeat count of the current code */
    var max_count = 7         /* max repeat count */
    var min_count = 4         /* min repeat count */

    /* tree[max_code+1].len = -1; */  /* guard already set */
    if (nextlen == 0.toShort()) {
        max_count = 138
        min_count = 3
    }

    for (n in 0..max_code) {
        val curlen = nextlen.toUInt()
        nextlen = tree[len(n + 1)]/*.len*/ /* length of current code */
        if (++count < max_count && curlen == nextlen.toUInt()) {
            continue
        } else if (count < min_count) {
            do {
                send_code(s, curlen, s.bl_tree); } while (--count != 0)

        } else if (curlen != 0) {
            if (curlen != prevlen) {
                send_code(s, curlen, s.bl_tree); count--
            }
            //Assert(count >= 3 && count <= 6, " 3_6?");
            send_code(s, REP_3_6, s.bl_tree); send_bits(s, count - 3, 2)

        } else if (count <= 10) {
            send_code(s, REPZ_3_10, s.bl_tree); send_bits(s, count - 3, 3)

        } else {
            send_code(s, REPZ_11_138, s.bl_tree); send_bits(s, count - 11, 7)
        }
        count = 0
        prevlen = curlen
        if (nextlen == 0.toShort()) {
            max_count = 138
            min_count = 3
        } else if (curlen == nextlen.toUInt()) {
            max_count = 6
            min_count = 3
        } else {
            max_count = 7
            min_count = 4
        }
    }
}

/* ===========================================================================
 * Construct the Huffman tree for the bit lengths and return the index in
 * bl_order of the last bit length code to send.
 */
fun build_bl_tree(
    s: deflate_state
): Int {
    val bl_order = TreesTables.bl_order

    /* Determine the bit length frequencies for literal and distance trees */
    scan_tree(s, s.dyn_ltree, s.l_desc.max_code)
    scan_tree(s, s.dyn_dtree, s.d_desc.max_code)

    /* Build the bit length tree: */
    build_tree(s, s.bl_desc)
    /* opt_len now includes the length of the tree representations, except
     * the lengths of the bit lengths codes and the 5+5+4 bits for the counts.
     */

    /* Determine the number of bit length codes to send. The pkzip format
     * requires that at least 4 bit length codes be sent. (appnote.txt says
     * 3 but the actual value used is 4.)
     */
    var max_blindex =
        BL_CODES - 1 /* index of last bit length code of non zero freq */
    while (max_blindex >= 3) {
        if (s.bl_tree[len(bl_order[max_blindex].toUInt())]/*.len*/ != 0.toShort()) break
        max_blindex--
    }
    /* Update opt_len to include the bit length tree and counts */
    s.opt_len += 3 * (max_blindex + 1) + 5 + 5 + 4
    //Tracev((stderr, "\ndyn trees: dyn %ld, stat %ld",
    //    s .opt_len, s .static_len));

    return max_blindex
}

/* ===========================================================================
 * Send the header for a block using dynamic Huffman trees: the counts, the
 * lengths of the bit length codes, the literal tree and the distance tree.
 * IN assertion: lcodes >= 257, dcodes >= 1, blcodes >= 4.
 */
fun send_all_trees(
    s: deflate_state,
    lcodes: Int,
    dcodes: Int,
    blcodes: Int /* number of codes for each tree */
) {
    val bl_order = TreesTables.bl_order

    //Assert (lcodes >= 257 && dcodes >= 1 && blcodes >= 4, "not enough codes");
    //Assert (lcodes <= L_CODES && dcodes <= D_CODES && blcodes <= BL_CODES,
    //    "too many codes");
    //Tracev((stderr, "\nbl counts: "));
    send_bits(s, lcodes - 257, 5) /* not +255 as stated in appnote.txt */
    send_bits(s, dcodes - 1, 5)
    send_bits(s, blcodes - 4, 4) /* not -3 as stated in appnote.txt */
    for (rank in 0 until blcodes) {
        //Tracev((stderr, "\nbl code %2d ", bl_order[rank]));
        send_bits(
            s, s.bl_tree[len(bl_order[rank].toUInt())]/*.len*/.toUInt(), 3
        )
    }
    //Tracev((stderr, "\nbl tree: sent %ld", s .bits_sent));

    send_tree(s, s.dyn_ltree, lcodes - 1) /* literal tree */
    //Tracev((stderr, "\nlit tree: sent %ld", s .bits_sent));

    send_tree(s, s.dyn_dtree, dcodes - 1) /* distance tree */
    //Tracev((stderr, "\ndist tree: sent %ld", s .bits_sent));
}

/* ===========================================================================
 * Send a stored block
 */
fun _tr_stored_block(
    s: deflate_state,
    buf: ByteArray?,       /* input block */
    buf_i: UInt,
    stored_len: UInt,   /* length of input block */
    last: Int         /* one if this is the last block for a file */
) {
    send_bits(s, (STORED_BLOCK shl 1) + last, 3)    /* send block type */
    bi_windup(s)        /* align on byte boundary */
    put_short(s, stored_len.toShort())
    put_short(s, stored_len.toShort().inv())
    if (stored_len > 0) {
        zmemcpy(
            s.pending_buf!!, s.pending,
            buf!!, buf_i,
            stored_len
        )
        s.pending += stored_len
    }
    /*#ifdef ZLIB_DEBUG
        s .compressed_len = (s .compressed_len + 3 + 7) & (ulg)~7L;
    s .compressed_len += (stored_len + 4)  shl  3;
    s .bits_sent += 2*16;
    s .bits_sent += stored_len shl 3;
    #endif*/
}

/* ===========================================================================
 * Flush the bits in the bit buffer to pending output (leaves at most 7 bits)
 */
fun _tr_flush_bits(s: deflate_state) {
    bi_flush(s)
}

/* ===========================================================================
 * Send one empty static block to give enough lookahead for inflate.
 * This takes 10 bits, of which 7 may remain in the bit buffer.
 */
fun _tr_align(s: deflate_state) {
    send_bits(s, STATIC_TREES shl 1, 3)
    send_code(s, END_BLOCK, TreesTables.static_ltree)
    /*#ifdef ZLIB_DEBUG
        s .compressed_len += 10L; /* 3 for block type, 7 for EOB */
    #endif*/
    bi_flush(s)
}

/* ===========================================================================
 * Determine the best encoding for the current block: dynamic trees, static
 * trees or store, and write out the encoded block.
 */
fun _tr_flush_block(
    strm: z_stream,
    s: deflate_state,
    buf: ByteArray?,       /* input block, or NULL if too old */
    buf_i: UInt,
    stored_len: UInt,   /* length of input block */
    last: Int         /* one if this is the last block for a file */
) {
    var opt_lenb: UInt /* opt_len in bytes */
    val static_lenb: UInt /* static_len in bytes */
    var max_blindex = 0  /* index of last bit length code of non zero freq */

    /* Build the Huffman trees unless a stored block is forced */
    if (s.level > 0) {

        /* Check if the file is binary or text */
        if (strm.data_type == Z_UNKNOWN)
            strm.data_type = detect_data_type(s)

        /* Construct the literal and distance trees */
        build_tree(s, s.l_desc)
        //Tracev((stderr, "\nlit data: dyn %ld, stat %ld", s .opt_len,
        //s .static_len));

        build_tree(s, s.d_desc)
        //Tracev((stderr, "\ndist data: dyn %ld, stat %ld", s .opt_len,
        //s .static_len));
        /* At this point, opt_len and static_len are the total bit lengths of
         * the compressed block data, excluding the tree representations.
         */

        /* Build the bit length tree for the above two trees, and get the index
         * in bl_order of the last bit length code to send.
         */
        max_blindex = build_bl_tree(s)

        /* Determine the best encoding. Compute the block lengths in bytes. */
        opt_lenb = (s.opt_len + 3 + 7) ushr 3 /* opt_len in bytes */
        static_lenb = (s.static_len + 3 + 7) ushr 3 /* static_len in bytes */

        //Tracev((stderr, "\nopt %lu(%lu) stat %lu(%lu) stored %lu lit %u ",
        //    opt_lenb, s .opt_len, static_lenb, s .static_len, stored_len,
        //s .last_lit));

        if (static_lenb <= opt_lenb) opt_lenb = static_lenb

    } else {
        //Assert(buf != (char*)0, "lost buf");
        opt_lenb = stored_len + 5 /* force a stored block */
        static_lenb = opt_lenb
    }

    /*#ifdef FORCE_STORED
        if (buf != (char*)0) { /* force stored block */
    #else*/
    if (stored_len + 4 <= opt_lenb && buf != Z_NULL) {
        /* 4: two words for the lengths */
        //#endif
        /* The test buf != NULL is only necessary if LIT_BUFSIZE > WSIZE.
         * Otherwise we can't have processed more than WSIZE input bytes since
         * the last block flush, because compression would have been
         * successful. If LIT_BUFSIZE <= WSIZE, it is never too late to
         * transform a block into a stored block.
         */
        _tr_stored_block(s, buf, buf_i, stored_len, last)

        /*#ifdef FORCE_STATIC
    } else if (static_lenb >= 0) { /* force static trees */
        #else*/
    } else if (s.strategy == Z_FIXED || static_lenb == opt_lenb) {
        //#endif
        send_bits(s, (STATIC_TREES shl 1) + last, 3)
        compress_block(s, TreesTables.static_ltree, TreesTables.static_dtree)
        /*#ifdef ZLIB_DEBUG
            s .compressed_len += 3 + s .static_len;
        #endif*/
    } else {
        send_bits(s, (DYN_TREES shl 1) + last, 3)
        send_all_trees(
            s, s.l_desc.max_code + 1, s.d_desc.max_code + 1,
            max_blindex + 1
        )
        compress_block(s, s.dyn_ltree, s.dyn_dtree)
        /*#ifdef ZLIB_DEBUG
            s .compressed_len += 3 + s .opt_len;
        #endif*/
    }
    //Assert (s .compressed_len == s .bits_sent, "bad compressed size");
    /* The above check is made mod 2^32, for files larger than 512 MB
     * and uLong implemented on 32 bits.
     */
    init_block(s)

    if (last != 0) {
        bi_windup(s)
        /*#ifdef ZLIB_DEBUG
                s .compressed_len += 7;  /* align on byte boundary */
        #endif*/
    }
    //Tracev((stderr,"\ncomprlen %lu(%lu) ", s .compressed_len ushr 3,
    //s .compressed_len-7*last));
}

/* ===========================================================================
 * Save the match info and tally the frequency counts. Return true if
 * the current block must be flushed.
 */
fun _tr_tally(
    s: deflate_state,
    dist: UShort,  /* distance of matched string */
    lc: Byte    /* match length-MIN_MATCH or unmatched char (if dist==0) */
): Boolean {
    dist.splitToBytes { b1, b0 ->
        s.pending_buf!![s.d_buf + s.last_lit * 2] = b1
        s.pending_buf!![s.d_buf + s.last_lit * 2 + 1] = b0
    }
    s.pending_buf!![s.l_buf + s.last_lit] = lc
    s.last_lit++
    if (dist == 0.toShort()) {
        /* lc is the unmatched char */
        s.dyn_ltree[freq(lc.toUInt())]/*.freq*/++
    } else {
        s.matches++
        /* Here, lc is the match length - MIN_MATCH */
        val dist = dist - 1             /* dist = match distance - 1 */
        //Assert((ush)dist < (ush)MAX_DIST(s) &&
        //        (ush)lc <= (ush)(MAX_MATCH-MIN_MATCH) &&
        //        (ush)d_code(dist) < (ush)D_CODES,  "_tr_tally: bad match");

        s.dyn_ltree[freq(TreesTables._length_code[lc.toUInt()].toUInt() + LITERALS + 1)]/*.freq*/++
        s.dyn_dtree[freq(d_code(dist).toUInt())]/*.freq*/++
    }

    /*#ifdef TRUNCATE_BLOCK
            /* Try to guess if it is profitable to stop the current block here */
            if ((s .last_lit & 0x1fff) == 0 && s .level > 2) {
        /* Compute an upper bound for the compressed length */
        ulg out_length = (ulg)s .last_lit*8L;
        ulg in_length = (ulg)((long)s .strstart - s .block_start);
        int dcode;
        for (dcode = 0; dcode < D_CODES; dcode++) {
        out_length += (ulg)s .dyn_dtree[dcode].freq *
        (5L+extra_dbits[dcode]);
    }
        out_length  ushr = 3;
        Tracev((stderr,"\nlast_lit %u, in %ld, out ~%ld(%ld%%) ",
            s .last_lit, in_length, out_length,
        100L - out_length*100L/in_length));
        if (s .matches < s .last_lit/2 && out_length < in_length/2) return 1;
    }
    #endif*/

    return (s.last_lit == s.lit_bufsize - 1)
    /* We avoid equality with lit_bufsize because of wraparound at 64K
     * on 16 bit machines and because stored blocks are restricted to
     * 64K-1 bytes.
     */
}

/* ===========================================================================
 * Send the block data compressed using the given Huffman trees
 */
fun compress_block(
    s: deflate_state,
    ltree: ShortArray, /* literal tree */
    dtree: ShortArray /* distance tree */
) {
    var lx: UInt = 0    /* running index in l_buf */

    if (s.last_lit != 0) do {
        var dist = combineToShort(
            s.pending_buf!![s.d_buf + lx * 2],
            s.pending_buf!![s.d_buf + lx * 2 + 1]
        ).toUInt() /* distance of matched string */
        var lc =
            s.pending_buf!![s.l_buf + lx].toUInt() /* match length or unmatched char (if dist == 0) */
        lx++
        if (dist == 0) {
            send_code(s, lc, ltree) /* send a literal byte */
            //Tracecv(isgraph(lc), (stderr," '%c' ", lc));
        } else {
            /* Here, lc is the match length - MIN_MATCH */
            var code =
                TreesTables._length_code[lc].toUInt() /* the code to send */
            send_code(s, code + LITERALS + 1, ltree) /* send the length code */
            var extra =
                TreesTables.extra_lbits[code] /* number of extra bits to send */
            if (extra != 0) {
                lc -= TreesTables.base_length[code]
                send_bits(s, lc, extra)       /* send the extra length bits */
            }
            dist-- /* dist is now the match distance - 1 */
            code = d_code(dist).toUInt()
            //Assert (code < D_CODES, "bad d_code");

            send_code(s, code, dtree) /* send the distance code */
            extra = TreesTables.extra_dbits[code]
            if (extra != 0) {
                dist -= TreesTables.base_dist[code]
                send_bits(s, dist, extra)   /* send the extra distance bits */
            }
        } /* literal or match pair ? */

        /* Check that the overlay between pending_buf and d_buf+l_buf is ok: */
        //Assert((uInt)(s .pending) < s .lit_bufsize + 2*lx,
        //"pendingBuf overflow");

    } while (lx < s.last_lit)

    send_code(s, END_BLOCK, ltree)
}

/* ===========================================================================
 * Check if the data type is TEXT or BINARY, using the following algorithm:
 * - TEXT if the two conditions below are satisfied:
 *    a) There are no non-portable control characters belonging to the
 *       "black list" (0..6, 14..25, 28..31).
 *    b) There is at least one printable character belonging to the
 *       "white list" (9 {TAB}, 10 {LF}, 13 {CR}, 32..255).
 * - BINARY otherwise.
 * - The following partially-portable control characters form a
 *   "gray list" that is ignored in this detection algorithm:
 *   (7 {BEL}, 8 {BS}, 11 {VT}, 12 {FF}, 26 {SUB}, 27 {ESC}).
 * IN assertion: the fields Freq of dyn_ltree are set.
 */
fun detect_data_type(
    s: deflate_state
): Int {
    /* black_mask is the bit mask of black-listed bytes
     * set bits 0..6, 14..25, and 28..31
     * 0xf3ffc07f = binary 11110011111111111100000001111111
     */
    var black_mask = 0xf3ffc07f.toInt()

    /* Check for non-textual ("black-listed") bytes. */
    for (n in 0..31) {
        if ((black_mask and 1) != 0 && (s.dyn_ltree[freq(n)] != 0.toShort()))
            return Z_BINARY
        black_mask = black_mask ushr 1
    }

    /* Check for textual ("white-listed") bytes. */
    if (s.dyn_ltree[freq(9)]/*.freq*/ != 0.toShort()
        || s.dyn_ltree[freq(10)]/*.freq*/ != 0.toShort()
        || s.dyn_ltree[freq(13)]/*.freq*/ != 0.toShort())
        return Z_TEXT
    for (n in 32 until LITERALS)
        if (s.dyn_ltree[freq(n)]/*.freq*/ != 0.toShort())
            return Z_TEXT

    /* There are no "black-listed" or "white-listed" bytes:
     * this stream either is empty or has tolerated ("gray-listed") bytes only.
     */
    return Z_BINARY
}

/* ===========================================================================
 * Reverse the first len bits of a code, using straightforward code (a faster
 * method would use a table)
 * IN assertion: 1 <= len <= 15
 */
fun bi_reverse(
    code: UInt, /* the value to invert */
    len: Int       /* its bit length */
): UInt {
    var code = code
    var len = len
    var res = 0
    do {
        res = res or (code and 1)
        code = code ushr 1
        res = res shl 1
        len--
    } while (len > 0)
    return res ushr 1
}

/* ===========================================================================
 * Flush the bit buffer, keeping at most 7 bits in it.
 */
fun bi_flush(
    s: deflate_state
) {
    if (s.bi_valid == 16) {
        put_short(s, s.bi_buf)
        s.bi_buf = 0
        s.bi_valid = 0
    } else if (s.bi_valid >= 8) {
        put_byte(s, s.bi_buf.toByte())
        s.bi_buf = (s.bi_buf.toUInt() ushr 8).toShort()
        s.bi_valid -= 8
    }
}

/* ===========================================================================
 * Flush the bit buffer and align the output on a byte boundary
 */
fun bi_windup(
    s: deflate_state
) {
    if (s.bi_valid > 8) {
        put_short(s, s.bi_buf)
    } else if (s.bi_valid > 0) {
        put_byte(s, s.bi_buf.toByte())
    }
    s.bi_buf = 0
    s.bi_valid = 0
    /*#ifdef ZLIB_DEBUG
        s .bits_sent = (s .bits_sent+7) & ~7;
    #endif*/
}
