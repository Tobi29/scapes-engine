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

impl class MutableString(val builder: StringBuilder) : CharSequence, Appendable {
    impl override val length get() = builder.length

    impl constructor() : this(0)

    impl constructor(str: String) : this(StringBuilder(str))

    impl constructor(initial: Int) : this(StringBuilder(initial))

    impl override fun get(index: Int): Char {
        if (index < 0 || index >= builder.length) {
            throw IndexOutOfBoundsException("$index")
        }
        return builder[index]
    }

    impl override fun subSequence(startIndex: Int,
                                  endIndex: Int) =
            substring(startIndex, endIndex)

    impl override fun append(c: Char): MutableString = apply {
        builder.append(c)
    }

    impl override fun append(csq: CharSequence?): MutableString = apply {
        builder.append(csq)
    }

    impl override fun append(csq: CharSequence?,
                             start: Int,
                             end: Int): MutableString = apply {
        builder.append(csq, start, end)
    }

    impl fun insert(position: Int,
                    char: Char): MutableString = apply {
        if (position < 0 || position > builder.length) {
            throw IndexOutOfBoundsException("$position")
        }
        builder.insert(position, char)
    }

    impl fun insert(position: Int,
                    str: String): MutableString = apply {
        if (position < 0 || position > builder.length) {
            throw IndexOutOfBoundsException("$position")
        }
        builder.insert(position, str)
    }

    impl fun insert(position: Int,
                    array: CharArray): MutableString = apply {
        builder.insert(position, array)
    }

    impl fun insert(position: Int,
                    array: CharArray,
                    offset: Int): MutableString =
            insert(position, array, offset, array.size - offset)

    impl fun insert(position: Int,
                    array: CharArray,
                    offset: Int,
                    length: Int): MutableString = apply {
        builder.insert(position, array, offset, length)
    }

    impl fun delete(range: IntRange): MutableString =
            delete(range.start, range.endInclusive + 1)

    impl fun delete(startIndex: Int): MutableString =
            delete(startIndex, startIndex + 1)

    impl fun delete(startIndex: Int,
                    endIndex: Int): MutableString = apply {
        if (endIndex < startIndex) {
            throw IllegalArgumentException(
                    "End index less than start index: $endIndex < $startIndex")
        }
        builder.delete(startIndex, endIndex)
    }

    impl fun clear(): MutableString = apply {
        builder.delete(0, builder.length)
    }

    impl fun substring(startIndex: Int): String =
            substring(startIndex, builder.length)

    impl fun substring(startIndex: Int,
                       endIndex: Int): String {
        if (endIndex < startIndex) {
            throw IllegalArgumentException(
                    "End index less than start index: $endIndex < $startIndex")
        }
        return builder.substring(startIndex, endIndex)
    }

    impl override fun toString() = builder.toString()
}

@Suppress("NOTHING_TO_INLINE")
inline fun StringBuilder.toMutableString(): MutableString = MutableString(this)
