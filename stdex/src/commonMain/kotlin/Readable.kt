/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.stdex

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
     * Reads a single character
     * @return The read character or -1 on end of stream
     */
    fun readTry(): Int

    /**
     * Reads characters by filling the given array
     * @param array The array to write to
     * @param offset First index in the array to write to
     * @param size Amount of characters to read
     */
    fun read(array: CharArray, offset: Int, size: Int) {
        if (offset < 0 || size < 0 || offset + size > array.size)
            throw IndexOutOfBoundsException("Invalid offset or size")
        for (i in offset until offset + size) {
            array[i] = read()
        }
    }

    /**
     * Reads characters by filling the given array as much as possible
     * @param array The array to write to
     * @param offset First index in the array to write to
     * @param size Maximum amount of characters to read
     * @return The amount of characters read or -1 on end of stream
     */
    fun readSome(array: CharArray, offset: Int, size: Int): Int {
        if (offset < 0 || size < 0 || offset + size > array.size)
            throw IndexOutOfBoundsException("Invalid offset or size")
        for (i in offset until offset + size) {
            array[i] = readTry().let {
                if (it < 0) return i - offset
                i.toChar()
            }
        }
        return size
    }
}

/**
 * Reads a single codepoint
 * @return The read codepoint
 */
fun Readable.readCodepoint(): Codepoint {
    val c0 = read()
    return if (c0.isHighSurrogate()) {
        val c1 = read()
        surrogateCodepoint(c0, c1)
    } else c0.toCP()
}

/**
 * Reads a single codepoint
 * @return The read codepoint or -1 on end of stream
 */
fun Readable.readCodepointTry(): Int {
    val c0 = readTry().let {
        if (it < 0) return -1
        it.toChar()
    }
    return if (c0.isHighSurrogate()) {
        val c1 = readTry().let {
            if (it < 0) return -1
            it.toChar()
        }
        surrogateCodepoint(c0, c1)
    } else c0.toCP()
}
