/*
 * Copyright 2012-2016 Tobi29
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
package org.tobi29.scapes.engine.utils.math.noise.value

import org.tobi29.scapes.engine.utils.math.floor
import org.tobi29.scapes.engine.utils.math.vector.dot
import java.util.*

class SimplexNoise(random: Random) : ValueNoise {
    private val perm = IntArray(512)

    constructor(seed: Long) : this(Random(seed))

    init {
        var v: Int
        for (i in 0..255) {
            v = random.nextInt(256)
            perm[i] = v
            perm[i + 256] = v
        }
    }

    override fun noise(x: Double,
                       y: Double): Double {
        val s = (x + y) * F2
        val i = floor(x + s)
        val j = floor(y + s)
        val t = (i + j) * G2
        val x0 = x - i + t
        val y0 = y - j + t
        val i1: Int
        val j1: Int
        if (x0 > y0) {
            i1 = 1
            j1 = 0
        } else {
            i1 = 0
            j1 = 1
        }
        val x1 = x0 - i1 + G2
        val y1 = y0 - j1 + G2
        val x2 = x0 - 1.0 + G22
        val y2 = y0 - 1.0 + G22
        val i2 = i and 255
        val j2 = j and 255
        var t0 = 0.5 - x0 * x0 - y0 * y0
        val n0: Double
        if (t0 < 0) {
            n0 = 0.0
        } else {
            t0 *= t0
            val gi0 = perm[i2 + perm[j2]] % 12
            n0 = t0 * t0 *
                    dot(GRAD_3[gi0][0].toDouble(), GRAD_3[gi0][1].toDouble(),
                            x0, y0)
        }
        var t1 = 0.5 - x1 * x1 - y1 * y1
        val n1: Double
        if (t1 < 0) {
            n1 = 0.0
        } else {
            t1 *= t1
            val gi1 = perm[i2 + i1 + perm[j2 + j1]] % 12
            n1 = t1 * t1 *
                    dot(GRAD_3[gi1][0].toDouble(), GRAD_3[gi1][1].toDouble(),
                            x1, y1)
        }
        var t2 = 0.5 - x2 * x2 - y2 * y2
        val n2: Double
        if (t2 < 0) {
            n2 = 0.0
        } else {
            t2 *= t2
            val gi2 = perm[i2 + 1 + perm[j2 + 1]] % 12
            n2 = t2 * t2 *
                    dot(GRAD_3[gi2][0].toDouble(), GRAD_3[gi2][1].toDouble(),
                            x2, y2)
        }
        return 70.0 * (n0 + n1 + n2)
    }

    override fun noise(x: Double,
                       y: Double,
                       z: Double): Double {
        val n0: Double
        val n1: Double
        val n2: Double
        val n3: Double
        val s = (x + y + z) * F3
        val i = floor(x + s)
        val j = floor(y + s)
        val k = floor(z + s)
        val t = (i + j + k) * G3
        val xx0 = i - t
        val yy0 = j - t
        val zz0 = k - t
        val x0 = x - xx0
        val y0 = y - yy0
        val z0 = z - zz0
        val i1: Int
        val j1: Int
        val k1: Int
        val i2: Int
        val j2: Int
        val k2: Int
        if (x0 >= y0) {
            if (y0 >= z0) {
                i1 = 1
                j1 = 0
                k1 = 0
                i2 = 1
                j2 = 1
                k2 = 0
            } else if (x0 >= z0) {
                i1 = 1
                j1 = 0
                k1 = 0
                i2 = 1
                j2 = 0
                k2 = 1
            } else {
                i1 = 0
                j1 = 0
                k1 = 1
                i2 = 1
                j2 = 0
                k2 = 1
            }
        } else {
            if (y0 < z0) {
                i1 = 0
                j1 = 0
                k1 = 1
                i2 = 0
                j2 = 1
                k2 = 1
            } else if (x0 < z0) {
                i1 = 0
                j1 = 1
                k1 = 0
                i2 = 0
                j2 = 1
                k2 = 1
            } else {
                i1 = 0
                j1 = 1
                k1 = 0
                i2 = 1
                j2 = 1
                k2 = 0
            }
        }
        val x1 = x0 - i1 + G3
        val y1 = y0 - j1 + G3
        val z1 = z0 - k1 + G3
        val x2 = x0 - i2 + G32
        val y2 = y0 - j2 + G32
        val z2 = z0 - k2 + G32
        val x3 = x0 - 1.0 + G33
        val y3 = y0 - 1.0 + G33
        val z3 = z0 - 1.0 + G33
        val i3 = i and 255
        val j3 = j and 255
        val k3 = k and 255
        var t0 = 0.5 - x0 * x0 - y0 * y0 - z0 * z0
        if (t0 < 0) {
            n0 = 0.0
        } else {
            t0 *= t0
            val gi0 = perm[i3 + perm[j3 + perm[k3]]] % 12
            n0 = t0 * t0 * dot(GRAD_3[gi0][0].toDouble(),
                    GRAD_3[gi0][1].toDouble(), GRAD_3[gi0][2].toDouble(), x0,
                    y0,
                    z0)
        }
        var t1 = 0.5 - x1 * x1 - y1 * y1 - z1 * z1
        if (t1 < 0) {
            n1 = 0.0
        } else {
            t1 *= t1
            val gi1 = perm[i3 + i1 + perm[j3 + j1 + perm[k3 + k1]]] % 12
            n1 = t1 * t1 * dot(GRAD_3[gi1][0].toDouble(),
                    GRAD_3[gi1][1].toDouble(), GRAD_3[gi1][2].toDouble(), x1,
                    y1,
                    z1)
        }
        var t2 = 0.5 - x2 * x2 - y2 * y2 - z2 * z2
        if (t2 < 0) {
            n2 = 0.0
        } else {
            t2 *= t2
            val gi2 = perm[i3 + i2 + perm[j3 + j2 + perm[k3 + k2]]] % 12
            n2 = t2 * t2 * dot(GRAD_3[gi2][0].toDouble(),
                    GRAD_3[gi2][1].toDouble(), GRAD_3[gi2][2].toDouble(), x2,
                    y2,
                    z2)
        }
        var t3 = 0.5 - x3 * x3 - y3 * y3 - z3 * z3
        if (t3 < 0) {
            n3 = 0.0
        } else {
            t3 *= t3
            val gi3 = perm[i3 + 1 + perm[j3 + 1 + perm[k3 + 1]]] % 12
            n3 = t3 * t3 * dot(GRAD_3[gi3][0].toDouble(),
                    GRAD_3[gi3][1].toDouble(), GRAD_3[gi3][2].toDouble(), x3,
                    y3,
                    z3)
        }
        return 32.0 * (n0 + n1 + n2 + n3)
    }

    companion object {
        private val F2 = 0.5 * (1.7320508075688772 - 1.0)
        private val G2 = (3.0 - 1.7320508075688772) / 6.0
        private val G22 = (3.0 - 1.7320508075688772) / 3.0
        private val F3 = 1.0 / 3.0
        private val G3 = 1.0 / 6.0
        private val G32 = 1.0 / 3.0
        private val G33 = 0.5
        private val GRAD_3 = arrayOf(intArrayOf(1, 1, 0), intArrayOf(-1, 1, 0),
                intArrayOf(1, -1, 0), intArrayOf(-1, -1, 0),
                intArrayOf(1, 0, 1), intArrayOf(-1, 0, 1), intArrayOf(1, 0, -1),
                intArrayOf(-1, 0, -1), intArrayOf(0, 1, 1),
                intArrayOf(0, -1, 1), intArrayOf(0, 1, -1),
                intArrayOf(0, -1, -1))
    }
}
