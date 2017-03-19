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

package org.tobi29.scapes.engine.utils.generation.layer

import org.tobi29.scapes.engine.utils.generation.value.ValueNoise
import java.util.*

typealias RandomPermutation = IntArray

fun RandomPermutation(random: Random) = IntArray(512).apply {
    var v: Int
    for (i in 0..255) {
        v = random.nextInt(256)
        this[i] = v
        this[i + 256] = v
    }
}

inline fun RandomPermutation.random(maxRandom: Int,
                                    x: Int,
                                    y: Int): Int {
    assert(size == 512)
    val xx = x and 255
    val yy = y and 255
    return this[xx + this[yy + this[xx + yy and 255]]] % maxRandom
}

inline fun <T> RandomPermutation.randomOffset(factor: Int,
                                              x: Int,
                                              y: Int,
                                              parent: (Int, Int) -> T): T {
    assert(size == 512)
    val xx = x and 255
    val yy = y and 255
    val dx = this[xx + this[xx + yy and 255 + this[yy]]] % factor
    val dy = this[xx + yy and 255 + this[xx + this[yy]]] % factor
    val dir = this[xx + this[yy + this[xx + yy and 255]]] % 4
    // TODO: Optimize
    val ox = when (dir) {
        0 -> dx
        1 -> -dx
        2 -> dx
        3 -> -dx
        else -> throw IllegalArgumentException("Invalid arithmetic")
    }
    val oy = when (dir) {
        0 -> dx
        1 -> dx
        2 -> -dy
        3 -> -dy
        else -> throw IllegalArgumentException("Invalid arithmetic")
    }
    return parent(x + ox, y + oy)
}

inline fun <T> ValueNoise.randomOffset(factor: Double,
                                       x: Double,
                                       y: Double,
                                       parent: (Double, Double) -> T): T {
    return parent(x + noise(x / factor, y / factor) * factor,
            y + noise(x / factor, y / factor) * factor)
}
