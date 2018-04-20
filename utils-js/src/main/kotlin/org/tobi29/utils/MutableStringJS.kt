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

package org.tobi29.utils

import org.tobi29.stdex.copyToString

actual class MutableString actual constructor(
    private var str: String
) : CharSequence, Appendable {
    actual override val length get() = str.length

    actual constructor() : this("")

    actual constructor(initial: Int) : this()

    actual override fun get(index: Int): Char {
        if (index < 0 || index >= str.length) {
            throw IndexOutOfBoundsException("$index")
        }
        return str[index]
    }

    actual override fun subSequence(startIndex: Int, endIndex: Int) =
        substring(startIndex, endIndex)

    actual override fun append(
        c: Char
    ) = apply {
        str += c
    }

    actual override fun append(
        csq: CharSequence?
    ) = apply {
        str += csq ?: "null"
    }

    actual fun insert(
        position: Int,
        char: Char
    ) = apply {
        if (position < 0 || position > str.length) {
            throw IndexOutOfBoundsException("$position")
        }
        str = "${str.substring(0, position)}$char${str.substring(position)}"
    }

    actual fun insert(
        position: Int,
        csq: CharSequence?
    ) = apply {
        if (position < 0 || position > this.str.length) {
            throw IndexOutOfBoundsException("$position")
        }
        this.str = "${this.str.substring(0, position)}$str${
        this.str.substring(position)}"
    }

    actual fun delete(
        startIndex: Int,
        endIndex: Int
    ) = apply {
        if (endIndex < startIndex) {
            throw IllegalArgumentException(
                "End index less than start index: $endIndex < $startIndex"
            )
        }
        if (startIndex < 0 || endIndex > str.length) {
            throw IndexOutOfBoundsException("$startIndex..$endIndex")
        }
        str = "${str.substring(0, startIndex)}${str.substring(endIndex)}"
    }

    actual fun clear(): MutableString = apply { str = "" }

    actual fun substring(
        startIndex: Int,
        endIndex: Int
    ): String {
        if (endIndex < startIndex) {
            throw IllegalArgumentException(
                "End index less than start index: $endIndex < $startIndex"
            )
        }
        if (startIndex < 0 || endIndex > str.length) {
            throw IndexOutOfBoundsException("$startIndex..$endIndex")
        }
        return str.substring(startIndex, endIndex)
    }

    actual override fun toString() = str

    // Delegates
    actual override fun append(
        csq: CharSequence?, start: Int, end: Int
    ): MutableString = append((csq ?: "null").subSequence(start, end))

    actual fun append(
        array: CharArray
    ): MutableString = append(array, 0)

    actual fun append(
        array: CharArray, offset: Int
    ): MutableString = append(array, offset, array.size - offset)

    actual fun append(
        array: CharArray, offset: Int, length: Int
    ): MutableString = append(array.copyToString(offset, length))

    actual fun insert(
        position: Int, csq: CharSequence?, start: Int, end: Int
    ): MutableString = insert(position, (csq ?: "null").subSequence(start, end))

    actual fun insert(
        position: Int, array: CharArray
    ): MutableString = insert(position, array, 0)

    actual fun insert(
        position: Int, array: CharArray, offset: Int
    ): MutableString = insert(position, array, offset, array.size - offset)

    actual fun insert(
        position: Int, array: CharArray, offset: Int, length: Int
    ): MutableString = insert(position, array.copyToString(offset, length))

    actual fun delete(range: IntRange) =
        delete(range.start, range.endInclusive + 1)

    actual fun delete(startIndex: Int) =
        delete(startIndex, startIndex + 1)

    actual fun substring(startIndex: Int): String =
        substring(startIndex, str.length)
}
