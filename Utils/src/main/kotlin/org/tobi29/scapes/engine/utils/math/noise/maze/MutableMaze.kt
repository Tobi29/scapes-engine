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

/**
 * Class representing a mutable maze using one byte per cell
 */
class MutableMaze
/**
 * Constructs a new maze
 * @param data The cells of the maze
 * @param width The width of the maze
 * @param height The height of the maze
 */
(
        /**
         * The cells of the maze, exposed for inlined functions
         */
        val data: ByteArray,
        /**
         * The width of the maze
         */
        val width: Int,
        /**
         * The width of the maze
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
        return Maze.isWall({ x, y -> getAt(x, y) }, x, y, direction)
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
     * Changes the cell at the given coordinates
     *
     * **Note**: Does nothing if out of bounds
     * @param x x-coordinate of the cell
     * @param y y-coordinate of the cell
     * @param block change operation to execute
     */
    inline fun changeAt(x: Int,
                        y: Int,
                        block: (Byte) -> Byte) {
        if (!isInside(x, y)) {
            return
        }
        val index = index(x, y)
        data[index] = block(data[index])
    }

    /**
     * Returns the cell at the given coordinates
     *
     * **Note**: Returns [Maze.OUTSIDE] if out of bounds
     * @param x x-coordinate of the cell
     * @param y y-coordinate of the cell
     * @returns the value of the cell
     */
    fun getAt(x: Int,
              y: Int): Byte {
        if (!isInside(x, y)) {
            return Maze.OUTSIDE
        }
        return data[index(x, y)]
    }

    /**
     * Sets the cell at the given coordinates
     *
     * **Note**: Does nothing if out of bounds
     * @param x x-coordinate of the cell
     * @param y y-coordinate of the cell
     * @param value the value to set to the cell
     */
    fun setAt(x: Int,
              y: Int,
              value: Byte) {
        if (!isInside(x, y)) {
            return
        }
        data[index(x, y)] = value
    }

    /**
     * Iterates through the entire maze and sets its values
     * @param block maps old values to new ones
     */
    inline fun fill(block: (Byte) -> Byte) {
        for (i in 0..data.lastIndex) {
            data[i] = block(data[i])
        }
    }

    /**
     * Returns an immutable version of this maze
     *
     * **Note**: The data is **not** copied!
     */
    fun toMaze(): Maze {
        return Maze(data, width, height)
    }

    /**
     * Returns the index of the given coordinates
     * @param x x-coordinate of the cell
     * @param y y-coordinate of the cell
     */
    @Suppress("NOTHING_TO_INLINE")
    inline fun index(x: Int,
                     y: Int): Int {
        return y * width + x
    }
}
