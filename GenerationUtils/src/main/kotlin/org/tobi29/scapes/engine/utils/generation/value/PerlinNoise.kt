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
package org.tobi29.scapes.engine.utils.generation.value

import org.tobi29.scapes.engine.utils.Random
import org.tobi29.scapes.engine.utils.math.floor
import org.tobi29.scapes.engine.utils.math.mix
import org.tobi29.scapes.engine.utils.math.vector.dot

class PerlinNoise(random: Random) : ValueNoise {
    private val perm = IntArray(512)
    private val grad3 = GRAD_3 // Avoid getter call in noise functions

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
        var xx = floor(x)
        var yy = floor(y)
        val xxx = x - xx
        val yyy = y - yy
        xx = xx and 255
        yy = yy and 255
        val gi000 = perm[xx + perm[yy]] % 12
        val gi010 = perm[xx + perm[yy + 1]] % 12
        val gi100 = perm[xx + 1 + perm[yy]] % 12
        val gi110 = perm[xx + 1 + perm[yy + 1]] % 12
        val n000 = dot(grad3[gi000][0], grad3[gi000][1], xxx, yyy)
        val n100 = dot(grad3[gi100][0], grad3[gi100][1], xxx - 1, yyy)
        val n010 = dot(grad3[gi010][0], grad3[gi010][1], xxx, yyy - 1)
        val n110 = dot(grad3[gi110][0], grad3[gi110][1], xxx - 1, yyy - 1)
        val u = fade(xxx)
        val v = fade(yyy)
        val nx00 = mix(n000, n100, u)
        val nx10 = mix(n010, n110, u)
        return mix(nx00, nx10, v)
    }

    override fun noise(x: Double,
                       y: Double,
                       z: Double): Double {
        var xx = floor(x)
        var yy = floor(y)
        var zz = floor(z)
        val xxx = x - xx
        val yyy = y - yy
        val zzz = z - zz
        xx = xx and 255
        yy = yy and 255
        zz = zz and 255
        val gi000 = perm[xx + perm[yy + perm[zz]]] % 12
        val gi001 = perm[xx + perm[yy + perm[zz + 1]]] % 12
        val gi010 = perm[xx + perm[yy + 1 + perm[zz]]] % 12
        val gi011 = perm[xx + perm[yy + 1 + perm[zz + 1]]] % 12
        val gi100 = perm[xx + 1 + perm[yy + perm[zz]]] % 12
        val gi101 = perm[xx + 1 + perm[yy + perm[zz + 1]]] % 12
        val gi110 = perm[xx + 1 + perm[yy + 1 + perm[zz]]] % 12
        val gi111 = perm[xx + 1 + perm[yy + 1 + perm[zz + 1]]] % 12
        val n000 = dot(grad3[gi000][0], grad3[gi000][1], grad3[gi000][2],
                xxx, yyy, zzz)
        val n100 = dot(grad3[gi100][0], grad3[gi100][1], grad3[gi100][2],
                xxx - 1, yyy, zzz)
        val n010 = dot(grad3[gi010][0], grad3[gi010][1], grad3[gi010][2],
                xxx, yyy - 1, zzz)
        val n110 = dot(grad3[gi110][0], grad3[gi110][1], grad3[gi110][2],
                xxx - 1, yyy - 1, zzz)
        val n001 = dot(grad3[gi001][0], grad3[gi001][1], grad3[gi001][2],
                xxx, yyy, zzz - 1)
        val n101 = dot(grad3[gi101][0], grad3[gi101][1], grad3[gi101][2],
                xxx - 1, yyy, zzz - 1)
        val n011 = dot(grad3[gi011][0], grad3[gi011][1], grad3[gi011][2],
                xxx, yyy - 1, zzz - 1)
        val n111 = dot(grad3[gi111][0], grad3[gi111][1], grad3[gi111][2],
                xxx - 1, yyy - 1, zzz - 1)
        val u = fade(xxx)
        val v = fade(yyy)
        val w = fade(zzz)
        val nx00 = mix(n000, n100, u)
        val nx01 = mix(n001, n101, u)
        val nx10 = mix(n010, n110, u)
        val nx11 = mix(n011, n111, u)
        val nxy0 = mix(nx00, nx10, v)
        val nxy1 = mix(nx01, nx11, v)
        return mix(nxy0, nxy1, w)
    }

    companion object {
        private val GRAD_3 = arrayOf(
                doubleArrayOf(1.0, 1.0, 0.0),
                doubleArrayOf(-1.0, 1.0, 0.0),
                doubleArrayOf(1.0, -1.0, 0.0),
                doubleArrayOf(-1.0, -1.0, 0.0),
                doubleArrayOf(1.0, 0.0, 1.0),
                doubleArrayOf(-1.0, 0.0, 1.0),
                doubleArrayOf(1.0, 0.0, -1.0),
                doubleArrayOf(-1.0, 0.0, -1.0),
                doubleArrayOf(0.0, 1.0, 1.0),
                doubleArrayOf(0.0, -1.0, 1.0),
                doubleArrayOf(0.0, 1.0, -1.0),
                doubleArrayOf(0.0, -1.0, -1.0))
    }
}

@Suppress("NOTHING_TO_INLINE")
// Not placed in the companion object due to wonky bytecode generation from
// Kotlin
private inline fun fade(t: Double): Double {
    return t * t * t * (t * (t * 6 - 15) + 10)
}
