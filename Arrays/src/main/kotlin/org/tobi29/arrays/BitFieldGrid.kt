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

package org.tobi29.arrays

/**
 * Class for conveniently managing an 8-bit bit field mapped as a 2d grid
 */
class BitFieldGrid(
    /**
     * The data of the bit field, exposed for inlined functions
     */
    val data: ByteArray,
    /**
     * The width of the grid
     */
    val width: Int,
    /**
     * The width of the grid
     */
    val height: Int
) {

    /**
     * Constructs a new bit field and allocates the data for it
     * @param width The width of the grid
     * @param height The height of the grid
     */
    constructor(width: Int, height: Int) :
            this(ByteArray(width * height), width, height)

    /**
     * Checks if the given coordinates are inside of the grid
     * @param x x-coordinate of the cell
     * @param y y-coordinate of the cell
     * @return `true` if x and y are inside the grid
     */
    fun isInside(x: Int, y: Int): Boolean =
        x >= 0 && y >= 0 && x < width && y < height

    /**
     * Changes the cell at the given coordinates
     *
     * **Note**: Does nothing if out of bounds
     * @param x x-coordinate of the cell
     * @param y y-coordinate of the cell
     * @param block change operation to execute
     */
    inline fun changeAt(x: Int, y: Int, block: (Byte) -> Byte) {
        if (!isInside(x, y)) {
            return
        }
        val index = index(x, y)
        data[index] = block(data[index])
    }

    /**
     * Returns the cell at the given coordinates
     * @param x x-coordinate of the cell
     * @param y y-coordinate of the cell
     * @throws IndexOutOfBoundsException if the given coords are outside of the grid
     * @return the value of the cell
     */
    fun getAt(x: Int, y: Int): Byte {
        if (!isInside(x, y)) {
            throw IndexOutOfBoundsException("$x $y")
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
    fun setAt(x: Int, y: Int, value: Byte) {
        if (!isInside(x, y)) {
            return
        }
        data[index(x, y)] = value
    }

    /**
     * Iterates through the entire grid and sets its values
     * @param block maps old values to new ones
     */
    inline fun fill(block: (Byte) -> Byte) {
        for (i in 0..data.lastIndex) {
            data[i] = block(data[i])
        }
    }

    /**
     * Returns the index of the given coordinates
     * @param x x-coordinate of the cell
     * @param y y-coordinate of the cell
     */
    @Suppress("NOTHING_TO_INLINE")
    inline fun index(x: Int, y: Int): Int {
        return y * width + x
    }
}

/**
 * Returns the flag at the given coordinates
 * @param x x-coordinate of the cell
 * @param y y-coordinate of the cell
 * @param i the index of the flag, 0 <= i < 8
 * @throws IndexOutOfBoundsException if the given coords are outside of the grid
 * @return the value of the flag
 */
@Suppress("NOTHING_TO_INLINE")
inline fun BitFieldGrid.getAt(x: Int, y: Int, i: Int): Boolean {
    if (i < 0 || i >= 8) {
        throw IllegalArgumentException("Invalid flag index: $i")
    }
    return getAt(x, y).maskAt(i)
}

/**
 * Sets the cell at the given coordinates
 *
 * **Note**: Does nothing if out of bounds
 * @param x x-coordinate of the cell
 * @param y y-coordinate of the cell
 * @param value the value to set to the cell
 */
fun BitFieldGrid.setAt(x: Int, y: Int, i: Int, value: Boolean) {
    changeAt(x, y) { if (value) it.setAt(i) else it.unsetAt(i) }
}
