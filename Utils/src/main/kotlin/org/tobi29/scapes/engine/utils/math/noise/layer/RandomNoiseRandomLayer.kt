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

package org.tobi29.scapes.engine.utils.math.noise.layer

import java.util.Random

class RandomNoiseRandomLayer(seed: Long, private val maxRandom: Int) : RandomNoiseLayer {
    private val perm = IntArray(512)

    init {
        val random = Random(seed)
        var v: Int
        for (i in 0..255) {
            v = random.nextInt(256)
            perm[i] = v
            perm[i + 256] = v
        }
    }

    override fun getInt(x: Int,
                        y: Int): Int {
        var x = x
        var y = y
        x = x and 255
        y = y and 255
        return perm[x + perm[y + perm[x + y and 255]]] % maxRandom
    }
}
