package org.tobi29.scapes.engine.utils

/**
 * Source of characters
 */
interface Readable {
    /**
     * Reads a single character
     * @return The read character
     */
    fun read(): Char

    /**
     * Reads characters by filling the given array
     * @param array The array to write to
     * @param offset First index in the array to write to
     * @param size Amount of characters to read
     */
    fun read(array: CharArray,
             offset: Int,
             size: Int) {
        if (offset < 0 || size < 0 || offset + size > array.size)
            throw IndexOutOfBoundsException("Invalid offset or size")
        for (i in offset until offset + size) {
            array[i] = read()
        }
    }
}
