/*
 * Copyright 2012-2018 Tobi29
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

package org.tobi29.generation.maze

import org.tobi29.arrays.BitFieldGrid
import org.tobi29.arrays.ByteArray2
import org.tobi29.arrays.Vars2
import org.tobi29.math.Face
import org.tobi29.stdex.maskAt
import kotlin.experimental.and

/**
 * Class representing a maze using one byte per cell
 */
class Maze(private val array: ByteArray2) : Vars2 by array {
    /**
     * Returns `true` if the cell has a wall in the given direction
     *
     * Always returns `true` if the cell is outside of the maze bounds
     * @param x x-coordinate of the cell
     * @param y y-coordinate of the cell
     * @param direction direction to check, must be [Face.NORTH], [Face.EAST], [Face.SOUTH] or [Face.WEST]
     * @return `true` if the cell has a wall in the direction
     * @throws IllegalArgumentException When an invalid direction was given
     */
    fun isWall(x: Int, y: Int, direction: Face): Boolean =
        isWall({ xx, yy, i -> getAt(xx, yy, i) }, x, y, direction)

    /**
     * Checks if the given coordinates are inside of the maze
     * @param x x-coordinate of the cell
     * @param y y-coordinate of the cell
     * @return `true` if x and y are inside the maze
     */
    fun isInside(x: Int, y: Int): Boolean =
        x in 0 until width && y in 0 until height

    /**
     * Returns a [BitFieldGrid] in order to modify a copy of this maze
     *
     * **Note**: All flags other than at index 0 and 1 get cleared
     * @return A new [BitFieldGrid]
     */
    fun edit(): ByteArray2 =
        ByteArray2(array.width, array.size) { x, y -> array[x, y] and 0x3 }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun getAt(x: Int, y: Int, i: Int): Boolean =
        !isInside(x, y) || array[x, y].maskAt(i)
}

internal inline fun isWall(
    getAt: (Int, Int, Int) -> Boolean,
    x: Int, y: Int, direction: Face
): Boolean = when (direction) {
    Face.NORTH -> !getAt(x, y, 0)
    Face.WEST -> !getAt(x, y, 1)
    Face.SOUTH -> !getAt(x, y + 1, 0)
    Face.EAST -> !getAt(x + 1, y, 1)
    else -> throw IllegalArgumentException("Invalid direction: $direction")
}
