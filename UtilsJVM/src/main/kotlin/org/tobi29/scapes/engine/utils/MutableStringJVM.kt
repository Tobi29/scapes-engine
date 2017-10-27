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

package org.tobi29.scapes.engine.utils

actual class MutableString(val builder: StringBuilder) : CharSequence, Appendable {
    actual override val length get() = builder.length

    actual constructor() : this(0)

    actual constructor(str: String) : this(StringBuilder(str))

    actual constructor(initial: Int) : this(StringBuilder(initial))

    actual override fun get(index: Int): Char {
        if (index < 0 || index >= builder.length) {
            throw IndexOutOfBoundsException("$index")
        }
        return builder[index]
    }

    actual override fun subSequence(startIndex: Int,
                                  endIndex: Int) =
            substring(startIndex, endIndex)

    actual override fun append(c: Char): MutableString = apply {
        builder.append(c)
    }

    actual override fun append(csq: CharSequence?): MutableString = apply {
        builder.append(csq)
    }

    actual override fun append(csq: CharSequence?,
                             start: Int,
                             end: Int): MutableString = apply {
        builder.append(csq, start, end)
    }

    actual fun insert(position: Int,
                    char: Char): MutableString = apply {
        if (position < 0 || position > builder.length) {
            throw IndexOutOfBoundsException("$position")
        }
        builder.insert(position, char)
    }

    actual fun insert(position: Int,
                    str: String): MutableString = apply {
        if (position < 0 || position > builder.length) {
            throw IndexOutOfBoundsException("$position")
        }
        builder.insert(position, str)
    }

    actual fun insert(position: Int,
                    array: CharArray): MutableString = apply {
        builder.insert(position, array)
    }

    actual fun insert(position: Int,
                    array: CharArray,
                    offset: Int): MutableString =
            insert(position, array, offset, array.size - offset)

    actual fun insert(position: Int,
                    array: CharArray,
                    offset: Int,
                    length: Int): MutableString = apply {
        builder.insert(position, array, offset, length)
    }

    actual fun delete(range: IntRange): MutableString =
            delete(range.start, range.endInclusive + 1)

    actual fun delete(startIndex: Int): MutableString =
            delete(startIndex, startIndex + 1)

    actual fun delete(startIndex: Int,
                    endIndex: Int): MutableString = apply {
        if (endIndex < startIndex) {
            throw IllegalArgumentException(
                    "End index less than start index: $endIndex < $startIndex")
        }
        builder.delete(startIndex, endIndex)
    }

    actual fun clear(): MutableString = apply {
        builder.delete(0, builder.length)
    }

    actual fun substring(startIndex: Int): String =
            substring(startIndex, builder.length)

    actual fun substring(startIndex: Int,
                       endIndex: Int): String {
        if (endIndex < startIndex) {
            throw IllegalArgumentException(
                    "End index less than start index: $endIndex < $startIndex")
        }
        return builder.substring(startIndex, endIndex)
    }

    actual override fun toString() = builder.toString()
}

@Suppress("NOTHING_TO_INLINE")
inline fun StringBuilder.toMutableString(): MutableString = MutableString(this)
