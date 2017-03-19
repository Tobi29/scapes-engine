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

package org.tobi29.scapes.engine.utils.generation.maze

import org.tobi29.scapes.engine.utils.BitFieldGrid
import org.tobi29.scapes.engine.utils.getAt
import org.tobi29.scapes.engine.utils.math.Face
import kotlin.experimental.and

/**
 * Class representing a maze using one byte per cell
 */
class Maze
/**
 * Constructs a new maze
 * @param grid The cells of the maze
 */
(private val grid: BitFieldGrid) {
    /**
     * The width of the maze
     */
    val width get() = grid.width
    /**
     * The height of the maze
     */
    val height get() = grid.height

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
    fun isWall(x: Int,
               y: Int,
               direction: Face): Boolean {
        return isWall({ x, y, i -> getAt(x, y, i) }, x, y, direction)
    }

    /**
     * Checks if the given coordinates are inside of the maze
     * @param x x-coordinate of the cell
     * @param y y-coordinate of the cell
     * @return `true` if x and y are inside the maze
     */
    fun isInside(x: Int,
                 y: Int) = grid.isInside(x, y)

    /**
     * Returns a [BitFieldGrid] in order to modify a copy of this maze
     *
     * **Note**: All flags other than at index 0 and 1 get cleared
     * @return A new [BitFieldGrid]
     */
    fun edit(): BitFieldGrid {
        val cleanData = ByteArray(grid.data.size) { grid.data[it] and 0x3 }
        return BitFieldGrid(cleanData, grid.width, grid.height)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun getAt(x: Int,
                             y: Int,
                             i: Int): Boolean {
        if (!isInside(x, y)) {
            return true
        }
        return grid.getAt(x, y, i)
    }

    companion object {
        internal inline fun isWall(getAt: (Int, Int, Int) -> Boolean,
                                   x: Int,
                                   y: Int,
                                   direction: Face): Boolean {
            return when (direction) {
                Face.NORTH -> !getAt(x, y, 0)
                Face.WEST -> !getAt(x, y, 1)
                Face.SOUTH -> !getAt(x, y + 1, 0)
                Face.EAST -> !getAt(x + 1, y, 1)
                else -> throw IllegalArgumentException(
                        "Invalid direction: $direction")
            }
        }
    }
}
