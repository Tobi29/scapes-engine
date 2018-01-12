/*
 * Copyright 2012-2017 Tobi29
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tobi29.scapes.engine.utils

// Min and max unsigned numbers
internal const val MINVH = 0L
internal const val MINVL = 0L
internal const val MAXVH = -1L
internal const val MAXVL = -1L

// Min and max signed numbers
internal const val SMINVH = Long.MIN_VALUE
internal const val SMINVL = 0L
internal const val SMAXVH = Long.MAX_VALUE
internal const val SMAXVL = -1L

// The digitsInLong are generated by the following code, the power is simply
// base ^ digits
// tailrec fun oi(max: BigDecimal, radix: BigDecimal, i: Int): Int = if(max > radix) oi(max / radix, radix, i + 1) else i
// fun oi(radix: Int) = oi(BigDecimal(Long.MAX_VALUE.toString()), BigDecimal(radix.toString()), 0)

// Maximum amount of digits for a given base that always fit into a signed long
internal val digitsInLong = intArrayOf(
        62, 39, 31, 27, 24, 22, 20, 19, 18, 18, 17, 17, 16, 16, 15, 15, 15, 14,
        14, 14, 14, 13, 13, 13, 13, 13, 13, 12, 12, 12, 12, 12, 12, 12, 12)

// Maximum signed long that is a power of a given base
internal val powerInLong = longArrayOf(
        4611686018427387904L, 4052555153018976267L, 4611686018427387904L,
        7450580596923828125L, 4738381338321616896L, 3909821048582988049L,
        1152921504606846976L, 1350851717672992089L, 1000000000000000000L,
        5559917313492231481L, 2218611106740436992L, 8650415919381337933L,
        2177953337809371136L, 6568408355712890625L, 1152921504606846976L,
        2862423051509815793L, 6746640616477458432L, 799006685782884121L,
        1638400000000000000L, 3243919932521508681L, 6221821273427820544L,
        504036361936467383L, 876488338465357824L, 1490116119384765625L,
        2481152873203736576L, 4052555153018976267L, 6502111422497947648L,
        353814783205469041L, 531441000000000000L, 787662783788549761L,
        1152921504606846976L, 1667889514952984961L, 2386420683693101056L,
        3379220508056640625L, 4738381338321616896L)

// Split 128-bit int into four 32-bit ints
internal inline fun <R> slice32(ah: Long,
                                al: Long,
                                output: (Long, Long, Long, Long) -> R): R {
    val a96 = ah ushr 32
    val a64 = ah and 0xFFFFFFFFL
    val a32 = al ushr 32
    val a00 = al and 0xFFFFFFFFL
    return output(a96, a64, a32, a00)
}

// Combine four 32-bit ints into a 128-bit int, discarding leading bits in the
// long values
internal inline fun <R> combine32(a96: Long,
                                  a64: Long,
                                  a32: Long,
                                  a00: Long,
                                  output: (Long, Long) -> R): R =
        output(((a96 and 0xFFFFFFFFL) shl 32) or (a64 and 0xFFFFFFFFL),
                ((a32 and 0xFFFFFFFFL) shl 32) or (a00 and 0xFFFFFFFFL))

internal inline fun <R> invImpl(ah: Long,
                                al: Long,
                                output: (Long, Long) -> R): R =
        output(ah.inv(), al.inv())

internal inline fun <R> negateImpl(ah: Long,
                                   al: Long,
                                   output: (Long, Long) -> R): R =
        invImpl(ah, al) { ih, il ->
            plusImpl(ih, il, 0L, 1L, output)
        }

internal inline fun <R> andImpl(ah: Long,
                                al: Long,
                                bh: Long,
                                bl: Long,
                                output: (Long, Long) -> R): R =
        output(ah and bh, al and bl)

internal inline fun <R> orImpl(ah: Long,
                               al: Long,
                               bh: Long,
                               bl: Long,
                               output: (Long, Long) -> R): R =
        output(ah or bh, al or bl)

internal inline fun <R> xorImpl(ah: Long,
                                al: Long,
                                bh: Long,
                                bl: Long,
                                output: (Long, Long) -> R): R =
        output(ah xor bh, al xor bl)

internal inline fun <R> shlImpl(ah: Long,
                                al: Long,
                                bitCount: Int,
                                output: (Long, Long) -> R): R {
    val shift = bitCount and 127
    val oh: Long
    val ol: Long
    when {
        shift == 0 -> {
            oh = ah
            ol = al
        }
        shift < 64 -> {
            oh = (ah shl shift) or (al ushr (64 - shift))
            ol = al shl shift
        }
        else -> {
            oh = al shl (shift - 64)
            ol = 0L
        }
    }
    return output(oh, ol)
}

internal inline fun <R> shrImpl(ah: Long,
                                al: Long,
                                bitCount: Int,
                                output: (Long, Long) -> R): R {
    val shift = bitCount and 127
    val oh: Long
    val ol: Long
    when {
        shift == 0 -> {
            oh = ah
            ol = al
        }
        shift < 64 -> {
            oh = ah ushr shift
            ol = (ah shl (64 - shift)) or (al ushr shift)
        }
        else -> {
            oh = 0L
            ol = ah ushr (shift - 64)
        }
    }
    return output(oh, ol)
}

internal inline fun <R> sshrImpl(ah: Long,
                                 al: Long,
                                 bitCount: Int,
                                 output: (Long, Long) -> R): R {
    val shift = bitCount and 127
    val oh: Long
    val ol: Long
    when {
        shift == 0 -> {
            oh = ah
            ol = al
        }
        shift < 64 -> {
            oh = ah shr shift
            ol = (ah shl (64 - shift)) or (al ushr shift)
        }
        ah >= 0 -> {
            oh = 0L
            ol = ah shr (shift - 64)
        }
        else -> {
            oh = -1L
            ol = ah shr (shift - 64)
        }
    }
    return output(oh, ol)
}

internal inline fun <R> plusImpl(ah: Long,
                                 al: Long,
                                 bh: Long,
                                 bl: Long,
                                 output: (Long, Long) -> R): R =
        slice32(ah, al) { a96, a64, a32, a00 ->
            slice32(bh, bl) { b96, b64, b32, b00 ->
                val c00 = (0L ushr 32) + a00 + b00
                val c32 = (c00 ushr 32) + a32 + b32
                val c64 = (c32 ushr 32) + a64 + b64
                val c96 = (c64 ushr 32) + a96 + b96
                combine32(c96, c64, c32, c00, output)
            }
        }

internal inline fun <R> minusImpl(ah: Long,
                                  al: Long,
                                  bh: Long,
                                  bl: Long,
                                  output: (Long, Long) -> R): R =
        negateImpl(bh, bl) { nh, nl ->
            plusImpl(ah, al, nh, nl, output)
        }

internal inline fun <R> timesImpl(ah: Long,
                                  al: Long,
                                  bh: Long,
                                  bl: Long,
                                  output: (Long, Long) -> R): R =
        slice32(ah, al) { a96, a64, a32, a00 ->
            slice32(bh, bl) { b96, b64, b32, b00 ->
                var c00 = 0L
                var c32 = 0L
                var c64 = 0L
                var c96 = 0L

                c00 += a00 * b00
                c32 += c00 ushr 32

                c32 += a32 * b00
                c64 += c32 ushr 32
                c32 = (c32 and 0xFFFFFFFFL) + a00 * b32
                c64 += c32 ushr 32

                c64 += a64 * b00
                c96 += c64 ushr 32
                c64 = (c64 and 0xFFFFFFFFL) + a32 * b32
                c96 += c64 ushr 32
                c64 = (c64 and 0xFFFFFFFFL) + a00 * b64
                c96 += c64 ushr 32

                c96 += a96 * b00
                c96 += a64 * b32
                c96 += a32 * b64
                c96 += a00 * b96
                combine32(c96, c64, c32, c00, output)
            }
        }

// TODO: Consider using better algorithm
@Suppress("NAME_SHADOWING")
internal fun divImpl(ah: Long,
                     al: Long,
                     bh: Long,
                     bl: Long): UInt128 {
    var ah = ah
    var al = al
    var bh = bh
    var bl = bl

    var oh: Long
    var ol: Long

    if (compareImpl(ah, al, bh, bl) < 0) {
        oh = 0L
        ol = 0L
    } else {
        val s = clzImpl(bh, bl) - clzImpl(ah, al)

        shlImpl(bh, bl, s) { sh, sl -> bh = sh; bl = sl }

        oh = 0L
        ol = 0L

        repeat(s + 1) {
            oh = (oh shl 1) or (ol ushr 63)
            ol = ol shl 1
            if (compareImpl(ah, al, bh, bl) >= 0) {
                minusImpl(ah, al, bh, bl) { mh, ml -> ah = mh; al = ml }
                ol = ol or 1L
            }

            bl = (bl ushr 1) or (bh shl 63)
            bh = bh ushr 1
        }
    }
    return UInt128(oh, ol)
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun equalsImpl(ah: Long,
                               al: Long,
                               bh: Long,
                               bl: Long) = ah == bh && al == bl

internal fun compareImpl(ah: Long,
                         al: Long,
                         bh: Long,
                         bl: Long) =
        slice32(ah, al) { a96, a64, a32, a00 ->
            s@ slice32(bh, bl) { b96, b64, b32, b00 ->
                val c96 = a96.compareTo(b96)
                if (c96 == 0) {
                    val c64 = a64.compareTo(b64)
                    if (c64 == 0) {
                        val c32 = a32.compareTo(b32)
                        if (c32 == 0) {
                            a00.compareTo(b00)
                        } else c32
                    } else c64
                } else c96
            }
        }

internal fun scompareImpl(ah: Long,
                          al: Long,
                          bh: Long,
                          bl: Long): Int {
    if (equalsImpl(ah, al, bh, bl)) return 0

    val thisNegative = ah < 0L
    val otherNegative = bh < 0L
    if (thisNegative && !otherNegative) return -1
    if (!thisNegative && otherNegative) return 1

    assert { thisNegative == otherNegative }

    return compareImpl(ah, al, bh, bl).let { if (thisNegative) -it else it }
}

@Suppress("NAME_SHADOWING")
internal fun sdivImpl(ah: Long,
                      al: Long,
                      bh: Long,
                      bl: Long): Int128 {
    var ah = ah
    var al = al
    var bh = bh
    var bl = bl

    if (equalsImpl(ah, al, SMINVH, SMINVL)) {
        return sdivImplAMinValue(bh, bl)
    } else if (equalsImpl(bh, bl, SMINVH, SMINVL)) {
        return Int128(0L, 0L)
    }

    var negate = false

    if (ah < 0) {
        negateImpl(ah, al) { nh, nl -> ah = nh; al = nl }
        negate = !negate
    }

    if (bh < 0) {
        negateImpl(bh, bl) { nh, nl -> bh = nh; bl = nl }
        negate = !negate
    }

    val q = divImpl(ah, al, bh, bl)

    return if (negate) negateImpl(q.high, q.low, ::Int128)
    else Int128(q.high, q.low)
}

private fun sdivImplAMinValue(bh: Long,
                              bl: Long): Int128 {
    var oh = 0L
    var ol = 0L

    if (equalsImpl(SMINVH, SMINVL, 0L, 1L)
            || equalsImpl(SMINVH, SMINVL, -1L, -1L)) {
        oh = SMINVH
        ol = SMINVL
    } else if (equalsImpl(bh, bl, SMINVH, SMINVL)) {
        oh = 0L
        ol = 1L
    } else {
        sshrImpl(SMINVH, SMINVL, 1) { hh, hl ->
            sdivImpl(hh, hl, bh, bl).let {
                shlImpl(it.high, it.low, 1) { sh, sl ->
                    if (equalsImpl(sh, sl, 0L, 0L)) {
                        if (bh < 0L) {
                            oh = 0L
                            ol = 1L
                        } else {
                            oh = -1L
                            ol = -1L
                        }
                    } else {
                        timesImpl(bh, bl, sh, sl) { th, tl ->
                            minusImpl(SMINVH, SMINVL, th, tl) { mh, ml ->
                                sdivImpl(mh, ml, bh, bl).let {
                                    plusImpl(sh, sl, it.high,
                                            it.low) { ph, pl ->
                                        oh = ph
                                        ol = pl
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    return Int128(oh, ol)
}

internal fun clzImpl(ah: Long,
                     al: Long): Int =
        clzImpl(ah).let {
            if (it < 64) it
            else it + clzImpl(al)
        }

internal fun clzImpl(value: Long) =
        (value ushr 32).toInt().let {
            if (it == 0) clzImpl(value.toInt()) + 32
            else clzImpl(it)
        }

internal fun clzImpl(value: Int): Int {
    if (value == 0) return 32

    var n = 1
    var x = value
    if (x ushr 16 == 0) {
        n += 16
        x = x shl 16
    }
    if (x ushr 24 == 0) {
        n += 8
        x = x shl 8
    }
    if (x ushr 28 == 0) {
        n += 4
        x = x shl 4
    }
    if (x ushr 30 == 0) {
        n += 2
        x = x shl 2
    }
    n -= x ushr 31

    return n
}

@Suppress("NAME_SHADOWING")
internal fun stringImpl(ah: Long,
                        al: Long,
                        radix: Int): String {
    if (equalsImpl(ah, al, 0L, 0L)) return "0"

    if (radix == 2 || radix == 8 || radix == 16) {
        return stringFastImpl(ah, al, radix)
    }

    val digits = digitsInLong[radix - 2]
    val radixToPower = powerInLong[radix - 2].toUInt128()
    var ah = ah
    var al = al
    var result = ""
    while (!equalsImpl(ah, al, 0L, 0L)) {
        val remDiv = divImpl(ah, al, radixToPower.high, radixToPower.low)
        timesImpl(remDiv.high, remDiv.low, radixToPower.high,
                radixToPower.low) { th, tl ->
            minusImpl(ah, al, th, tl) { mh, ml ->
                assert { mh == 0L }
                val batch = if (remDiv == 0.toUInt128()) ml.toString(radix)
                else ml.toString(radix, digits)
                result = batch + result
                ah = remDiv.high
                al = remDiv.low
            }
        }
    }
    return result
}

private fun stringFastImpl(ah: Long,
                           al: Long,
                           radix: Int): String {
    val digits = when (radix) {
        2 -> 32
        8 -> 16
        16 -> 8
        else -> throw IllegalStateException(
                "Tried fast path for wrong radix")
    }
    return slice32(ah, al) { a96, a64, a32, a00 ->
        return@slice32 if (a96 == 0L) {
            if (a64 == 0L) {
                if (a32 == 0L) {
                    a00.toString(radix)
                } else {
                    a32.toString(radix) +
                            a00.toString(radix, digits)
                }
            } else {
                a64.toString(radix) +
                        a32.toString(radix, digits) +
                        a00.toString(radix, digits)
            }
        } else {
            a96.toString(radix) +
                    a64.toString(radix, digits) +
                    a32.toString(radix, digits) +
                    a00.toString(radix, digits)
        }
    }
}

internal fun sstringImpl(ah: Long,
                         al: Long,
                         radix: Int): String {
    return if (ah < 0L) {
        if (equalsImpl(ah, al, SMINVH, SMINVL)) {
            val div = sdivImpl(ah, al, 0L, radix.toLong())
            timesImpl(div.high, div.low, 0L, radix.toLong()) { th, tl ->
                minusImpl(th, tl, ah, al) { mh, ml ->
                    assert { mh == 0L }
                    "${sstringImpl(div.high, div.low, radix)}${ml.toString(
                            radix)}"
                }
            }
        } else {
            negateImpl(ah, al) { nh, nl ->
                "-${stringImpl(nh, nl, radix)}"
            }
        }
    } else stringImpl(ah, al, radix)
}
