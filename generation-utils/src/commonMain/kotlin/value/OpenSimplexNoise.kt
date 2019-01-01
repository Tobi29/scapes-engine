/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.generation.value

import org.tobi29.arrays.sliceOver
import org.tobi29.math.Random
import org.tobi29.stdex.math.floorToInt

// Taken from: https://gist.github.com/digitalshadow/134a3a02b67cecd72181
// Many thanks to the original author :)
class OpenSimplexNoise(random: Random) : ValueNoise {
    private val perm = ByteArray(256)
    private val perm2D = ByteArray(256)
    private val perm3D = ByteArray(256)

    constructor(seed: Long) : this(Random(seed))

    init {
        random.nextBytes(perm.sliceOver())
        for (i in 255 downTo 0) {
            perm2D[i] = random.nextInt(15).toByte()
            // TODO: In the original this forced the value to always be a multiple of 3
            // So far it appears to work fine without that, just like 2d
            perm3D[i] = random.nextInt(70).toByte()
        }
    }

    override fun noise(x: Double, y: Double): Double {
        val stretchOffset = (x + y) * STRETCH_2D
        val xs = x + stretchOffset
        val ys = y + stretchOffset

        val xsb = (xs).floorToInt()
        val ysb = (ys).floorToInt()

        val squishOffset = (xsb + ysb) * SQUISH_2D
        val dx0 = x - (xsb + squishOffset)
        val dy0 = y - (ysb + squishOffset)

        val xins = xs - xsb
        val yins = ys - ysb

        val inSum = xins + yins

        val hash = ((xins - yins + 1).toInt()) or
                ((inSum).toInt() shl 1) or
                ((inSum + yins).toInt() shl 2) or
                ((inSum + xins).toInt() shl 4)

        var c: Contribution2? = lookup2D[hash]

        var value = 0.0
        while (c != null) {
            val dx = dx0 + c.dx
            val dy = dy0 + c.dy
            var attn = 2 - dx * dx - dy * dy
            if (attn > 0) {
                val px = xsb + c.xsb
                val py = ysb + c.ysb

                val i = perm2D[(perm[px and 0xFF] + py) and 0xFF].toInt()
                val valuePart = gradients2D[i] * dx + gradients2D[i + 1] * dy

                attn *= attn
                value += attn * attn * valuePart
            }
            c = c.next
        }
        return value * NORM_2D
    }

    override fun noise(x: Double, y: Double, z: Double): Double {
        val stretchOffset = (x + y + z) * STRETCH_3D
        val xs = x + stretchOffset
        val ys = y + stretchOffset
        val zs = z + stretchOffset

        val xsb = (xs).floorToInt()
        val ysb = (ys).floorToInt()
        val zsb = (zs).floorToInt()

        val squishOffset = (xsb + ysb + zsb) * SQUISH_3D
        val dx0 = x - (xsb + squishOffset)
        val dy0 = y - (ysb + squishOffset)
        val dz0 = z - (zsb + squishOffset)

        val xins = xs - xsb
        val yins = ys - ysb
        val zins = zs - zsb

        val inSum = xins + yins + zins

        val hash =
            ((yins - zins + 1).toInt()) or
                    ((xins - yins + 1).toInt() shl 1) or
                    ((xins - zins + 1).toInt() shl 2) or
                    ((inSum).toInt() shl 3) or
                    ((inSum + zins).toInt() shl 5) or
                    ((inSum + yins).toInt() shl 7) or
                    ((inSum + xins).toInt() shl 9)

        var c: Contribution3? = lookup3D[hash]

        var value = 0.0
        while (c != null) {
            val dx = dx0 + c.dx
            val dy = dy0 + c.dy
            val dz = dz0 + c.dz
            var attn = 2 - dx * dx - dy * dy - dz * dz
            if (attn > 0) {
                val px = xsb + c.xsb
                val py = ysb + c.ysb
                val pz = zsb + c.zsb

                val i =
                    perm3D[(perm[(perm[px and 0xFF] + py) and 0xFF] + pz) and 0xFF].toInt()
                val valuePart =
                    gradients3D[i] * dx + gradients3D[i + 1] * dy + gradients3D[i + 2] * dz

                attn *= attn
                value += attn * attn * valuePart
            }

            c = c.next
        }
        return value * NORM_3D
    }
}

private const val STRETCH_2D = -0.211324865405187
private const val STRETCH_3D = -1.0 / 6.0
private const val SQUISH_2D = 0.366025403784439
private const val SQUISH_3D = 1.0 / 3.0
private const val NORM_2D = 1.0 / 47.0
private const val NORM_3D = 1.0 / 103.0

private val gradients2D = doubleArrayOf(
    5.0, 2.0, 2.0, 5.0,
    -5.0, 2.0, -2.0, 5.0,
    5.0, -2.0, 2.0, -5.0,
    -5.0, -2.0, -2.0, -5.0
)

private val gradients3D = doubleArrayOf(
    -11.0, 4.0, 4.0, -4.0, 11.0, 4.0, -4.0, 4.0, 11.0,
    11.0, 4.0, 4.0, 4.0, 11.0, 4.0, 4.0, 4.0, 11.0,
    -11.0, -4.0, 4.0, -4.0, -11.0, 4.0, -4.0, -4.0, 11.0,
    11.0, -4.0, 4.0, 4.0, -11.0, 4.0, 4.0, -4.0, 11.0,
    -11.0, 4.0, -4.0, -4.0, 11.0, -4.0, -4.0, 4.0, -11.0,
    11.0, 4.0, -4.0, 4.0, 11.0, -4.0, 4.0, 4.0, -11.0,
    -11.0, -4.0, -4.0, -4.0, -11.0, -4.0, -4.0, -4.0, -11.0,
    11.0, -4.0, -4.0, 4.0, -11.0, -4.0, 4.0, -4.0, -11.0
)

private val lookup2D: Array<Contribution2> = run {
    val base2D = arrayOf(
        intArrayOf(
            1, 1, 0, 1, 0, 1, 0, 0, 0
        ),
        intArrayOf(
            1, 1, 0, 1, 0, 1, 2, 1, 1
        )
    )
    val p2D = intArrayOf(
        0, 0, 1, -1, 0, 0, -1, 1, 0, 2, 1, 1,
        1, 2, 2, 0, 1, 2, 0, 2, 1, 0, 0, 0
    )
    val lookupPairs2D = intArrayOf(
        0, 1, 1, 0, 4, 1, 17, 0, 20, 2, 21, 2,
        22, 5, 23, 5, 26, 4, 39, 3, 42, 4, 43, 3
    )

    val contributions2D = arrayOfNulls<Contribution2>(p2D.size / 4)
    for (i in 0 until p2D.size step 4) {
        val baseSet = base2D[p2D[i]]
        var previous: Contribution2? = null
        var current: Contribution2? = null
        var k = 0
        while (k < baseSet.size) {
            current = Contribution2(
                baseSet[k].toDouble(),
                baseSet[k + 1],
                baseSet[k + 2]
            )
            if (previous == null) {
                contributions2D[i / 4] = current
            } else {
                previous.next = current
            }
            previous = current
            k += 3
        }
        current!!.next =
                Contribution2(p2D[i + 1].toDouble(), p2D[i + 2], p2D[i + 3])
    }

    val lookup2D = arrayOfNulls<Contribution2>(64)
    for (i in 0 until lookupPairs2D.size step 2) {
        lookup2D[lookupPairs2D[i]] = contributions2D[lookupPairs2D[i + 1]]
    }
    @Suppress("UNCHECKED_CAST")
    lookup2D as Array<Contribution2>

}

private val lookup3D: Array<Contribution3> = run {
    val base3D = arrayOf(
        intArrayOf(
            0, 0, 0, 0, 1, 1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1
        ),
        intArrayOf(
            2, 1, 1, 0, 2, 1, 0, 1, 2, 0, 1, 1, 3, 1, 1, 1
        ),
        intArrayOf(
            1, 1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1, 2, 1, 1, 0,
            2, 1, 0, 1, 2, 0, 1, 1
        )
    )
    val p3D = intArrayOf(
        0, 0, 1, -1, 0, 0, 1, 0, -1, 0, 0, -1, 1, 0, 0, 0, 1, -1, 0, 0, -1, 0,
        1, 0, 0, -1, 1, 0, 2, 1, 1, 0, 1, 1, 1, -1, 0, 2, 1, 0, 1, 1, 1, -1, 1,
        0, 2, 0, 1, 1, 1, -1, 1, 1, 1, 3, 2, 1, 0, 3, 1, 2, 0, 1, 3, 2, 0, 1, 3,
        1, 0, 2, 1, 3, 0, 2, 1, 3, 0, 1, 2, 1, 1, 1, 0, 0, 2, 2, 0, 0, 1, 1, 0,
        1, 0, 2, 0, 2, 0, 1, 1, 0, 0, 1, 2, 0, 0, 2, 2, 0, 0, 0, 0, 1, 1, -1, 1,
        2, 0, 0, 0, 0, 1, -1, 1, 1, 2, 0, 0, 0, 0, 1, 1, 1, -1, 2, 3, 1, 1, 1,
        2, 0, 0, 2, 2, 3, 1, 1, 1, 2, 2, 0, 0, 2, 3, 1, 1, 1, 2, 0, 2, 0, 2, 1,
        1, -1, 1, 2, 0, 0, 2, 2, 1, 1, -1, 1, 2, 2, 0, 0, 2, 1, -1, 1, 1, 2, 0,
        0, 2, 2, 1, -1, 1, 1, 2, 0, 2, 0, 2, 1, 1, 1, -1, 2, 2, 0, 0, 2, 1, 1,
        1, -1, 2, 0, 2, 0
    )
    val lookupPairs3D = intArrayOf(
        0, 2, 1, 1, 2, 2, 5, 1, 6, 0, 7, 0, 32, 2, 34, 2, 129, 1, 133, 1, 160,
        5, 161, 5, 518, 0, 519, 0, 546, 4, 550, 4, 645, 3, 647, 3, 672, 5, 673,
        5, 674, 4, 677, 3, 678, 4, 679, 3, 680, 13, 681, 13, 682, 12, 685, 14,
        686, 12, 687, 14, 712, 20, 714, 18, 809, 21, 813, 23, 840, 20, 841, 21,
        1198, 19, 1199, 22, 1226, 18, 1230, 19, 1325, 23, 1327, 22, 1352, 15,
        1353, 17, 1354, 15, 1357, 17, 1358, 16, 1359, 16, 1360, 11, 1361, 10,
        1362, 11, 1365, 10, 1366, 9, 1367, 9, 1392, 11, 1394, 11, 1489, 10,
        1493, 10, 1520, 8, 1521, 8, 1878, 9, 1879, 9, 1906, 7, 1910, 7, 2005, 6,
        2007, 6, 2032, 8, 2033, 8, 2034, 7, 2037, 6, 2038, 7, 2039, 6
    )

    val contributions3D = arrayOfNulls<Contribution3>(p3D.size / 9)
    for (i in 0 until p3D.size step 9) {
        val baseSet = base3D[p3D[i]]
        var previous: Contribution3? = null
        var current: Contribution3? = null
        var k = 0
        while (k < baseSet.size) {
            current = Contribution3(
                baseSet[k].toDouble(),
                baseSet[k + 1],
                baseSet[k + 2],
                baseSet[k + 3]
            )
            if (previous == null) {
                contributions3D[i / 9] = current
            } else {
                previous.next = current
            }
            previous = current
            k += 4
        }
        current!!.next = Contribution3(
            p3D[i + 1].toDouble(),
            p3D[i + 2],
            p3D[i + 3],
            p3D[i + 4]
        )
        current.next!!.next = Contribution3(
            p3D[i + 5].toDouble(),
            p3D[i + 6],
            p3D[i + 7],
            p3D[i + 8]
        )
    }

    val lookup3D = arrayOfNulls<Contribution3>(2048)
    for (i in 0 until lookupPairs3D.size step 2) {
        lookup3D[lookupPairs3D[i]] = contributions3D[lookupPairs3D[i + 1]]
    }
    @Suppress("UNCHECKED_CAST")
    lookup3D as Array<Contribution3>
}

private class Contribution2(multiplier: Double, var xsb: Int, var ysb: Int) {
    var dx = -xsb - multiplier * SQUISH_2D
    var dy = -ysb - multiplier * SQUISH_2D
    var next: Contribution2? = null
}

private class Contribution3(
    multiplier: Double,
    var xsb: Int,
    var ysb: Int,
    var zsb: Int
) {
    var dx = -xsb - multiplier * SQUISH_3D
    var dy = -ysb - multiplier * SQUISH_3D
    var dz = -zsb - multiplier * SQUISH_3D
    var next: Contribution3? = null
}
