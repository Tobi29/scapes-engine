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

impl class MutableString(private var str: String) : CharSequence, Appendable {
    impl override val length get() = str.length

    impl constructor() : this("")

    impl constructor(initial: Int) : this()

    impl override fun get(index: Int): Char {
        if (index < 0 || index >= str.length) {
            throw IndexOutOfBoundsException("$index")
        }
        return str[index]
    }

    impl override fun subSequence(startIndex: Int,
                                  endIndex: Int) =
            substring(startIndex, endIndex)

    impl override fun append(c: Char): MutableString = apply {
        str += c
    }

    impl override fun append(csq: CharSequence?): MutableString = apply {
        str += csq ?: "null"
    }

    impl override fun append(csq: CharSequence?,
                             start: Int,
                             end: Int): MutableString = apply {
        str += (csq ?: "null").subSequence(start, end)
    }

    impl fun insert(position: Int,
                    char: Char): MutableString = apply {
        if (position < 0 || position > str.length) {
            throw IndexOutOfBoundsException("$position")
        }
        str = "${str.substring(0, position)}$char${str.substring(position)}"
    }

    impl fun insert(position: Int,
                    str: String): MutableString = apply {
        if (position < 0 || position > this.str.length) {
            throw IndexOutOfBoundsException("$position")
        }
        this.str = "${this.str.substring(0, position)}$str${this.str.substring(
                position)}"
    }

    impl fun insert(position: Int,
                    array: CharArray): MutableString =
            insert(position, array, 0)

    impl fun insert(position: Int,
                    array: CharArray,
                    offset: Int): MutableString =
            insert(position, array, offset, array.size - offset)

    impl fun insert(position: Int,
                    array: CharArray,
                    offset: Int,
                    length: Int): MutableString =
            insert(position, array.copyToString(offset, length))

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
        if (startIndex < 0 || endIndex > str.length) {
            throw IndexOutOfBoundsException("$startIndex..$endIndex")
        }
        str = "${str.substring(0, startIndex)}${str.substring(endIndex)}"
    }

    impl fun clear(): MutableString = apply { str = "" }

    impl fun substring(startIndex: Int): String =
            substring(startIndex, str.length)

    impl fun substring(startIndex: Int,
                       endIndex: Int): String {
        if (endIndex < startIndex) {
            throw IllegalArgumentException(
                    "End index less than start index: $endIndex < $startIndex")
        }
        if (startIndex < 0 || endIndex > str.length) {
            throw IndexOutOfBoundsException("$startIndex..$endIndex")
        }
        return str.substring(startIndex, endIndex)
    }

    impl override fun toString() = str
}
