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

import kotlin.math.max

internal class Tree {
    var dyn_tree: ShortArray? = null      // the dynamic tree
    var max_code: Int = 0      // largest code with non zero frequency
    var stat_desc: StaticTree? = null  // the corresponding static tree

    // Compute the optimal bit lengths for a tree and update the total bit length
    // for the current block.
    // IN assertion: the fields freq and dad are set, heap[heap_max] and
    //    above are the tree nodes sorted by increasing frequency.
    // OUT assertions: the field len is set to the optimal bit length, the
    //     array bl_count contains the frequencies for each bit length.
    //     The length opt_len is updated; static_len is also updated if stree is
    //     not null.
    private fun gen_bitlen(s: Deflate) {
        val tree = dyn_tree
        val stree = stat_desc!!.static_tree
        val extra = stat_desc!!.extra_bits
        val base = stat_desc!!.extra_base
        val max_length = stat_desc!!.max_length
        var h: Int = s.heap_max + 1              // heap index
        var n: Int
        var m: Int           // iterate over the tree elements
        var bits: Int           // bit length
        var xbits: Int          // extra bits
        var f: Short            // frequency
        var overflow = 0   // number of elements with bit length too large

        bits = 0
        while (bits <= MAX_BITS) {
            s.bl_count[bits] = 0
            bits++
        }

        // In a first pass, compute the optimal bit lengths (which may
        // overflow in the case of the bit length tree).
        tree!![s.heap[s.heap_max] * 2 + 1] = 0 // root of the heap

        while (h < HEAP_SIZE) {
            n = s.heap[h]
            bits = tree[tree[n * 2 + 1] * 2 + 1] + 1
            if (bits > max_length) {
                bits = max_length
                overflow++
            }
            tree[n * 2 + 1] = bits.toShort()
            // We overwrite tree[n*2+1] which is no longer needed

            if (n > max_code) {
                h++
                continue
            }  // not a leaf node

            s.bl_count[bits]++
            xbits = 0
            if (n >= base) xbits = extra[n - base]
            f = tree[n * 2]
            s.opt_len += f * (bits + xbits)
            if (stree != null) s.static_len += f * (stree[n * 2 + 1] + xbits)
            h++
        }
        if (overflow == 0) return

        // This happens for example on obj2 and pic of the Calgary corpus
        // Find the first bit length which could increase:
        do {
            bits = max_length - 1
            while (s.bl_count[bits].toInt() == 0) bits--
            s.bl_count[bits]--      // move one leaf down the tree
            s.bl_count[bits + 1] =
                    (s.bl_count[bits + 1] + 2).toShort()   // move one overflow item as its brother
            s.bl_count[max_length]--
            // The brother of the overflow item also moves one step up,
            // but this does not affect bl_count[max_length]
            overflow -= 2
        } while (overflow > 0)

        bits = max_length
        while (bits != 0) {
            n = s.bl_count[bits].toInt()
            while (n != 0) {
                m = s.heap[--h]
                if (m > max_code) continue
                if (tree[m * 2 + 1].toInt() != bits) {
                    s.opt_len += ((bits.toLong() - tree[m * 2 + 1].toLong()) * tree[m * 2].toLong()).toInt()
                    tree[m * 2 + 1] = bits.toShort()
                }
                n--
            }
            bits--
        }
    }

    // Construct one Huffman tree and assigns the code bit strings and lengths.
    // Update the total bit length for the current block.
    // IN assertion: the field freq is set for all tree elements.
    // OUT assertions: the fields len and code are set to the optimal bit length
    //     and corresponding code. The length opt_len is updated; static_len is
    //     also updated if stree is not null. The field max_code is set.
    fun build_tree(s: Deflate) {
        val tree = dyn_tree!!
        val stree = stat_desc!!.static_tree
        val elems = stat_desc!!.elems
        var n: Int
        var m: Int          // iterate over heap elements
        var max_code = -1   // largest code with non zero frequency
        var node: Int          // new node being created

        // Construct the initial heap, with least frequent element in
        // heap[1]. The sons of heap[n] are heap[2*n] and heap[2*n+1].
        // heap[0] is not used.
        s.heap_len = 0
        s.heap_max = HEAP_SIZE

        n = 0
        while (n < elems) {
            if (tree[n * 2].toInt() != 0) {
                max_code = n
                s.heap[++s.heap_len] = max_code
                s.depth[n] = 0
            } else {
                tree[n * 2 + 1] = 0
            }
            n++
        }

        // The pkzip format requires that at least one distance code exists,
        // and that at least one bit should be sent even if there is only one
        // possible code. So to avoid special checks later on we force at least
        // two codes of non zero frequency.
        while (s.heap_len < 2) {
            node = if (max_code < 2) ++max_code else 0
            s.heap[++s.heap_len] = node
            tree[node * 2] = 1
            s.depth[node] = 0
            s.opt_len--
            if (stree != null) s.static_len -= stree[node * 2 + 1].toInt()
            // node is 0 or 1 so it does not have extra bits
        }
        this.max_code = max_code

        // The elements heap[heap_len/2+1 .. heap_len] are leaves of the tree,
        // establish sub-heaps of increasing lengths:

        n = s.heap_len / 2
        while (n >= 1) {
            s.pqdownheap(tree, n)
            n--
        }

        // Construct the Huffman tree by repeatedly combining the least two
        // frequent nodes.

        node = elems                 // next internal node of the tree
        do {
            // n = node of least frequency
            n = s.heap[1]
            s.heap[1] = s.heap[s.heap_len--]
            s.pqdownheap(tree, 1)
            m = s.heap[1]                // m = node of next least frequency

            s.heap[--s.heap_max] = n // keep the nodes sorted by frequency
            s.heap[--s.heap_max] = m

            // Create a new node father of n and m
            tree[node * 2] = (tree[n * 2] + tree[m * 2]).toShort()
            s.depth[node] = (max(
                s.depth[n].toInt(),
                s.depth[m].toInt()
            ) + 1).toByte()
            tree[m * 2 + 1] = node.toShort()
            tree[n * 2 + 1] = tree[m * 2 + 1]

            // and insert the new node in the heap
            s.heap[1] = node++
            s.pqdownheap(tree, 1)
        } while (s.heap_len >= 2)

        s.heap[--s.heap_max] = s.heap[1]

        // At this point, the fields freq and dad are set. We can now
        // generate the bit lengths.

        gen_bitlen(s)

        // The field len is now set, we can generate the bit codes
        gen_codes(
            tree,
            max_code,
            s.bl_count,
            s.next_code
        )
    }
}

internal class StaticTree(
    var static_tree: ShortArray?, // static tree or null
    var extra_bits: IntArray, // extra bits for each code or null
    var extra_base: Int, // base index for extra_bits
    var elems: Int, // max number of elements in the tree
    var max_length: Int // max bit length for the codes
)

// extra bits for each length code
internal val extra_lbits = intArrayOf(
    0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2,
    3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 0
)

// extra bits for each distance code
internal val extra_dbits = intArrayOf(
    0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6,
    7, 7, 8, 8, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13
)

// extra bits for each bit length code
internal val extra_blbits =
    intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 7)

internal val bl_order = byteArrayOf(
    16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15
)

private val _dist_code = byteArrayOf(
    0, 1, 2, 3, 4, 4, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7,
    8, 8, 8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9, 9, 9,
    10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
    11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
    12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12,
    12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12,
    13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13,
    13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13,
    14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14,
    14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14,
    14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14,
    14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14,
    15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15,
    15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15,
    15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15,
    15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15,
    0, 0, 16, 17, 18, 18, 19, 19, 20, 20, 20, 20, 21, 21, 21, 21,
    22, 22, 22, 22, 22, 22, 22, 22, 23, 23, 23, 23, 23, 23, 23, 23,
    24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
    25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25,
    26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26,
    26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26,
    27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27,
    27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27,
    28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28,
    28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28,
    28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28,
    28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28,
    29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
    29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
    29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
    29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29
)

internal val _length_code = byteArrayOf(
    0, 1, 2, 3, 4, 5, 6, 7, 8, 8, 9, 9, 10, 10, 11, 11,
    12, 12, 12, 12, 13, 13, 13, 13, 14, 14, 14, 14, 15, 15, 15, 15,
    16, 16, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 17, 17,
    18, 18, 18, 18, 18, 18, 18, 18, 19, 19, 19, 19, 19, 19, 19, 19,
    20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
    21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21,
    22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22,
    23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
    24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
    24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
    25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25,
    25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25,
    26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26,
    26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26,
    27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27,
    27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 28
)

internal val base_length = intArrayOf(
    0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 12, 14, 16, 20, 24, 28, 32, 40, 48, 56,
    64, 80, 96, 112, 128, 160, 192, 224, 0
)

internal val base_dist = intArrayOf(
    0, 1, 2, 3, 4, 6, 8, 12, 16, 24,
    32, 48, 64, 96, 128, 192, 256, 384, 512, 768,
    1024, 1536, 2048, 3072, 4096, 6144, 8192, 12288, 16384, 24576
)

// Mapping from a distance to a distance code. dist is the distance - 1 and
// must not have side effects. _dist_code[256] and _dist_code[257] are never
// used.
internal fun d_code(dist: Int): Int {
    return (if (dist < 256) _dist_code[dist] else _dist_code[256 + dist.ushr(
        7
    )]).toInt()
}

// Generate the codes for a given tree and bit counts (which need not be
// optimal).
// IN assertion: the array bl_count contains the bit length statistics for
// the given tree and the field len is set for all tree elements.
// OUT assertion: the field code is set for all tree elements of non
//     zero code length.
private fun gen_codes(
    tree: ShortArray, // the tree to decorate
    max_code: Int, // largest code with non zero frequency
    bl_count: ShortArray, // number of codes at each bit length
    next_code: ShortArray
) {
    var code: Short = 0            // running code value
    var bits: Int = 1                  // bit index
    var n: Int = 0                     // code index

    // The distribution counts are first used to generate the code values
    // without bit reversal.
    next_code[0] = 0
    while (bits <= MAX_BITS) {
        code = (code + bl_count[bits - 1] shl 1).toShort()
        next_code[bits] = code
        bits++
    }

    // Check that the bit counts in bl_count are consistent. The last code
    // must be all ones.
    //Assert (code + bl_count[MAX_BITS]-1 == (1<<MAX_BITS)-1,
    //        "inconsistent bit counts");
    //Tracev((stderr,"\ngen_codes: max_code %d ", max_code));

    while (n <= max_code) {
        val len = tree[n * 2 + 1].toInt()
        if (len == 0) {
            n++
            continue
        }
        // Now reverse the bits
        tree[n * 2] =
                bi_reverse(
                    next_code[len]++.toInt(),
                    len
                ).toShort()
        n++
    }
}

// Reverse the first len bits of a code, using straightforward code (a faster
// method would use a table)
// IN assertion: 1 <= len <= 15
private fun bi_reverse(
    code: Int, // the value to invert
    len: Int   // its bit length
): Int {
    var code = code
    var len = len
    var res = 0
    do {
        res = res or (code and 1)
        code = code ushr 1
        res = res shl 1
    } while (--len > 0)
    return res.ushr(1)
}

private const val MAX_BITS = 15
private const val LITERALS = 256
private const val LENGTH_CODES = 29
private const val L_CODES = LITERALS + 1 + LENGTH_CODES
private const val HEAP_SIZE = 2 * L_CODES + 1

private const val BL_CODES = 19
private const val D_CODES = 30

// Bit length codes must not exceed MAX_BL_BITS bits
const val MAX_BL_BITS = 7

internal val STATIC_LTREE = shortArrayOf(
    12, 8, 140, 8, 76, 8, 204, 8, 44, 8,
    172, 8, 108, 8, 236, 8, 28, 8, 156, 8,
    92, 8, 220, 8, 60, 8, 188, 8, 124, 8,
    252, 8, 2, 8, 130, 8, 66, 8, 194, 8,
    34, 8, 162, 8, 98, 8, 226, 8, 18, 8,
    146, 8, 82, 8, 210, 8, 50, 8, 178, 8,
    114, 8, 242, 8, 10, 8, 138, 8, 74, 8,
    202, 8, 42, 8, 170, 8, 106, 8, 234, 8,
    26, 8, 154, 8, 90, 8, 218, 8, 58, 8,
    186, 8, 122, 8, 250, 8, 6, 8, 134, 8,
    70, 8, 198, 8, 38, 8, 166, 8, 102, 8,
    230, 8, 22, 8, 150, 8, 86, 8, 214, 8,
    54, 8, 182, 8, 118, 8, 246, 8, 14, 8,
    142, 8, 78, 8, 206, 8, 46, 8, 174, 8,
    110, 8, 238, 8, 30, 8, 158, 8, 94, 8,
    222, 8, 62, 8, 190, 8, 126, 8, 254, 8,
    1, 8, 129, 8, 65, 8, 193, 8, 33, 8,
    161, 8, 97, 8, 225, 8, 17, 8, 145, 8,
    81, 8, 209, 8, 49, 8, 177, 8, 113, 8,
    241, 8, 9, 8, 137, 8, 73, 8, 201, 8,
    41, 8, 169, 8, 105, 8, 233, 8, 25, 8,
    153, 8, 89, 8, 217, 8, 57, 8, 185, 8,
    121, 8, 249, 8, 5, 8, 133, 8, 69, 8,
    197, 8, 37, 8, 165, 8, 101, 8, 229, 8,
    21, 8, 149, 8, 85, 8, 213, 8, 53, 8,
    181, 8, 117, 8, 245, 8, 13, 8, 141, 8,
    77, 8, 205, 8, 45, 8, 173, 8, 109, 8,
    237, 8, 29, 8, 157, 8, 93, 8, 221, 8,
    61, 8, 189, 8, 125, 8, 253, 8, 19, 9,
    275, 9, 147, 9, 403, 9, 83, 9, 339, 9,
    211, 9, 467, 9, 51, 9, 307, 9, 179, 9,
    435, 9, 115, 9, 371, 9, 243, 9, 499, 9,
    11, 9, 267, 9, 139, 9, 395, 9, 75, 9,
    331, 9, 203, 9, 459, 9, 43, 9, 299, 9,
    171, 9, 427, 9, 107, 9, 363, 9, 235, 9,
    491, 9, 27, 9, 283, 9, 155, 9, 411, 9,
    91, 9, 347, 9, 219, 9, 475, 9, 59, 9,
    315, 9, 187, 9, 443, 9, 123, 9, 379, 9,
    251, 9, 507, 9, 7, 9, 263, 9, 135, 9,
    391, 9, 71, 9, 327, 9, 199, 9, 455, 9,
    39, 9, 295, 9, 167, 9, 423, 9, 103, 9,
    359, 9, 231, 9, 487, 9, 23, 9, 279, 9,
    151, 9, 407, 9, 87, 9, 343, 9, 215, 9,
    471, 9, 55, 9, 311, 9, 183, 9, 439, 9,
    119, 9, 375, 9, 247, 9, 503, 9, 15, 9,
    271, 9, 143, 9, 399, 9, 79, 9, 335, 9,
    207, 9, 463, 9, 47, 9, 303, 9, 175, 9,
    431, 9, 111, 9, 367, 9, 239, 9, 495, 9,
    31, 9, 287, 9, 159, 9, 415, 9, 95, 9,
    351, 9, 223, 9, 479, 9, 63, 9, 319, 9,
    191, 9, 447, 9, 127, 9, 383, 9, 255, 9,
    511, 9, 0, 7, 64, 7, 32, 7, 96, 7,
    16, 7, 80, 7, 48, 7, 112, 7, 8, 7,
    72, 7, 40, 7, 104, 7, 24, 7, 88, 7,
    56, 7, 120, 7, 4, 7, 68, 7, 36, 7,
    100, 7, 20, 7, 84, 7, 52, 7, 116, 7,
    3, 8, 131, 8, 67, 8, 195, 8, 35, 8,
    163, 8, 99, 8, 227, 8
)

internal val STATIC_DTREE = shortArrayOf(
    0, 5, 16, 5, 8, 5, 24, 5, 4, 5,
    20, 5, 12, 5, 28, 5, 2, 5, 18, 5,
    10, 5, 26, 5, 6, 5, 22, 5, 14, 5,
    30, 5, 1, 5, 17, 5, 9, 5, 25, 5,
    5, 5, 21, 5, 13, 5, 29, 5, 3, 5,
    19, 5, 11, 5, 27, 5, 7, 5, 23, 5
)

internal val STATIC_L_DESC = StaticTree(
    STATIC_LTREE,
    extra_lbits,
    LITERALS + 1,
    L_CODES,
    MAX_BITS
)

internal val STATIC_D_DESC = StaticTree(
    STATIC_DTREE,
    extra_dbits,
    0,
    D_CODES,
    MAX_BITS
)

internal val STATIC_BL_DESC = StaticTree(
    null,
    extra_blbits,
    0,
    BL_CODES,
    MAX_BL_BITS
)
