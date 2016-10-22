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

class RandomNoisePickLayer(private val parent: RandomNoiseLayer, private val pick: IntArray, private val drop: IntArray,
                           private val stay: Int) : RandomNoiseLayer {

    init {
        if (pick.size > drop.size) {
            throw IllegalArgumentException(
                    "Pick can't have more elements than drop!")
        }
    }

    override fun getInt(x: Int,
                        y: Int): Int {
        val value = parent.getInt(x, y)
        for (i in pick.indices) {
            if (value == pick[i]) {
                return drop[i]
            }
        }
        return stay
    }
}
