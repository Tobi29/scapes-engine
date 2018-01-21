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

package org.tobi29.math

import org.tobi29.math.vector.Vector3i

enum class Face(val value: Int,
                val x: Int,
                val y: Int,
                val z: Int,
                val data: Byte) {
    NONE(-1, 0, 0, 1, -1),
    UP(0, 0, 0, 1, 0),
    DOWN(1, 0, 0, -1, 1),
    NORTH(2, 0, -1, 0, 2),
    EAST(3, 1, 0, 0, 3),
    SOUTH(4, 0, 1, 0, 4),
    WEST(5, -1, 0, 0, 5);

    val delta: Vector3i = Vector3i(x, y, z)

    val opposite by lazy {
        when (this) {
            NONE -> NONE
            UP -> DOWN
            DOWN -> UP
            NORTH -> SOUTH
            EAST -> WEST
            SOUTH -> NORTH
            WEST -> EAST
        }
    }

    companion object {
        operator fun get(data: Int): Face {
            return when (data) {
                0 -> UP
                1 -> DOWN
                2 -> NORTH
                3 -> EAST
                4 -> SOUTH
                5 -> WEST
                else -> NONE
            }
        }
    }
}
