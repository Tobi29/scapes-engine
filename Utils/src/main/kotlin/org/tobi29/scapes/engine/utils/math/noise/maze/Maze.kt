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

package org.tobi29.scapes.engine.utils.math.noise.maze

import org.tobi29.scapes.engine.utils.math.Face
import org.tobi29.scapes.engine.utils.math.and
import org.tobi29.scapes.engine.utils.math.or

/**
 * Class representing a maze using one byte per cell
 */
class Maze
/**
 * Constructs a new maze
 * @param data The cells of the maze
 * @param width The width of the maze
 * @param height The height of the maze
 */
(private val data: ByteArray,
 /**
  * The width of the maze
  */
 val width: Int,
 /**
  * The height of the maze
  */
 val height: Int) {

    /**
     * Constructs a new maze and allocates the data for it
     * @param width The width of the maze
     * @param height The height of the maze
     */
    constructor(width: Int, height: Int) : this(
            ByteArray(width * height), width, height)

    /**
     * Returns `true` if the cell has a wall in the given direction
     *
     * Always returns `true` if the cell is outside of the maze bounds
     * @param x x-coordinate of the cell
     * @param y y-coordinate of the cell
     * @param direction direction to check, must be [Face.NORTH], [Face.EAST], [Face.SOUTH] or [Face.WEST]
     * @returns `true` if the cell has a wall in the direction
     * @throws IllegalArgumentException When an invalid direction was given
     */
    fun isWall(x: Int,
               y: Int,
               direction: Face): Boolean {
        return isWall({ x, y -> getAt(x, y) }, x, y, direction)
    }

    /**
     * Checks if the given coordinates are inside of the maze
     * @param x x-coordinate of the cell
     * @param y y-coordinate of the cell
     * @returns `true` if x and y are inside the maze
     */
    fun isInside(x: Int,
                 y: Int): Boolean {
        return x >= 0 && y >= 0 && x < width && y < height
    }

    /**
     * Returns a [MutableMaze] in order to modify a copy of this maze
     *
     * **Note**: All bit-fields other than [MASK_NORTH] and [MASK_WEST] get cleared
     * @returns A new [MutableMaze]
     */
    fun edit(): MutableMaze {
        val cleanData = ByteArray(
                data.size) { data[it] and (MASK_NORTH or MASK_WEST) }
        return MutableMaze(cleanData, width, height)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun getAt(x: Int,
                             y: Int): Byte {
        if (!isInside(x, y)) {
            return OUTSIDE
        }
        return data[index(x, y)]
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun index(x: Int,
                             y: Int): Int {
        return y * width + x
    }

    companion object {
        val MASK_NORTH: Byte = 0x1
        val MASK_WEST: Byte = 0x2
        val OUTSIDE = MASK_NORTH or MASK_WEST

        internal inline fun isWall(getAt: (Int, Int) -> Byte,
                                   x: Int,
                                   y: Int,
                                   direction: Face): Boolean {
            return when (direction) {
                Face.NORTH -> getAt(x, y) and MASK_NORTH == 0.toByte()
                Face.WEST -> getAt(x, y) and MASK_WEST == 0.toByte()
                Face.SOUTH -> getAt(x, y + 1) and MASK_NORTH == 0.toByte()
                Face.EAST -> getAt(x + 1, y) and MASK_WEST == 0.toByte()
                else -> throw IllegalArgumentException(
                        "Invalid direction: $direction")
            }
        }
    }
}
