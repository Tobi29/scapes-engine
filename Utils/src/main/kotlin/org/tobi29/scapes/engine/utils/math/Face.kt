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

package org.tobi29.scapes.engine.utils.math

import org.tobi29.scapes.engine.utils.math.vector.Vector3i

enum class Face(val value: Int, val x: Int, val y: Int, val z: Int, val data: Byte) {
    NONE(-1, 0, 0, 1, -1),
    UP(0, 0, 0, 1, 0),
    DOWN(1, 0, 0, -1, 1),
    NORTH(2, 0, -1, 0, 2),
    EAST(3, 1, 0, 0, 3),
    SOUTH(4, 0, 1, 0, 4),
    WEST(5, -1, 0, 0, 5);

    val delta: Vector3i
    lateinit var opposite: Face
        private set

    init {
        delta = Vector3i(x, y, z)
    }

    companion object {

        init {
            NONE.opposite = NONE
            UP.opposite = DOWN
            DOWN.opposite = UP
            NORTH.opposite = SOUTH
            EAST.opposite = WEST
            SOUTH.opposite = NORTH
            WEST.opposite = EAST
        }

        operator fun get(data: Int): Face {
            when (data) {
                0 -> return UP
                1 -> return DOWN
                2 -> return NORTH
                3 -> return EAST
                4 -> return SOUTH
                5 -> return WEST
                else -> return NONE
            }
        }
    }
}