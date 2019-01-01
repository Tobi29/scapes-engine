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

@file:JvmName("MutableStringJVMKt")

package org.tobi29.utils

actual class MutableString(
    val builder: StringBuilder
) : CharSequence, Appendable {
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

    actual override fun subSequence(startIndex: Int, endIndex: Int) =
        substring(startIndex, endIndex)

    actual override fun append(
        c: Char
    ) = apply { builder.append(c) }

    actual override fun append(
        csq: CharSequence?
    ) = apply { builder.append(csq) }

    actual override fun append(
        csq: CharSequence?, start: Int, end: Int
    ) = apply { builder.append(csq, start, end) }

    actual fun append(
        array: CharArray
    ) = apply { builder.append(array) }

    actual fun append(
        array: CharArray, offset: Int, length: Int
    ) = apply { builder.append(array, offset, length) }

    actual fun insert(
        position: Int,
        char: Char
    ) = apply { builder.insert(position, char) }

    actual fun insert(
        position: Int,
        csq: CharSequence?
    ) = apply { builder.insert(position, csq) }

    actual fun insert(
        position: Int,
        csq: CharSequence?,
        start: Int,
        end: Int
    ) = apply { builder.insert(position, csq, start, end) }

    actual fun insert(
        position: Int,
        array: CharArray
    ) = apply { builder.insert(position, array) }

    actual fun insert(
        position: Int,
        array: CharArray,
        offset: Int,
        length: Int
    ) = apply { builder.insert(position, array, offset, length) }

    actual fun delete(range: IntRange): MutableString =
        delete(range.start, range.endInclusive + 1)

    actual fun delete(startIndex: Int): MutableString =
        delete(startIndex, startIndex + 1)

    actual fun delete(startIndex: Int, endIndex: Int): MutableString = apply {
        if (endIndex < startIndex) {
            throw IllegalArgumentException(
                "End index less than start index: $endIndex < $startIndex"
            )
        }
        builder.delete(startIndex, endIndex)
    }

    actual fun clear(): MutableString = apply {
        builder.setLength(0)
    }

    actual fun substring(startIndex: Int, endIndex: Int): String {
        if (endIndex < startIndex) {
            throw IllegalArgumentException(
                "End index less than start index: $endIndex < $startIndex"
            )
        }
        return builder.substring(startIndex, endIndex)
    }

    actual override fun toString() = builder.toString()

    // Delegates
    actual fun append(array: CharArray, offset: Int) =
        append(array, offset, array.size - offset)

    actual fun insert(position: Int, array: CharArray, offset: Int) =
        insert(position, array, offset, array.size - offset)

    actual fun substring(startIndex: Int): String =
        substring(startIndex, builder.length)
}

@Suppress("NOTHING_TO_INLINE")
inline fun StringBuilder.toMutableString(): MutableString = MutableString(
    this
)
