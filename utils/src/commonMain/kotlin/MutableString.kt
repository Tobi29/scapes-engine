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

expect class MutableString() : CharSequence,
    Appendable {
    constructor(initial: Int)

    constructor(str: String)

    override val length: Int

    override fun get(index: Int): Char

    override fun subSequence(startIndex: Int, endIndex: Int): String

    override fun append(c: Char): MutableString
    override fun append(csq: CharSequence?): MutableString
    override fun append(
        csq: CharSequence?, start: Int, end: Int
    ): MutableString

    fun append(array: CharArray): MutableString
    fun append(array: CharArray, offset: Int): MutableString
    fun append(
        array: CharArray, offset: Int, length: Int
    ): MutableString

    fun insert(position: Int, char: Char): MutableString
    fun insert(position: Int, csq: CharSequence?): MutableString
    fun insert(
        position: Int, csq: CharSequence?, start: Int, end: Int
    ): MutableString

    fun insert(position: Int, array: CharArray): MutableString
    fun insert(position: Int, array: CharArray, offset: Int): MutableString
    fun insert(
        position: Int, array: CharArray, offset: Int, length: Int
    ): MutableString

    fun delete(range: IntRange): MutableString
    fun delete(startIndex: Int): MutableString
    fun delete(startIndex: Int, endIndex: Int): MutableString

    fun clear(): MutableString

    fun substring(startIndex: Int): String
    fun substring(startIndex: Int, endIndex: Int): String

    override fun toString(): String
}

/**
 * Converts the given string to a mutable one
 * @receiver String to copy from
 * @return A mutable copy of the string
 */
@Suppress("NOTHING_TO_INLINE")
inline fun String.toMutableString(): MutableString = MutableString(this)
