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

expect class MutableString : CharSequence, Appendable {
    override val length: Int

    override fun get(index: Int): Char

    override fun subSequence(startIndex: Int,
                             endIndex: Int): String

    constructor()

    constructor(initial: Int)

    constructor(str: String)

    override fun append(c: Char): MutableString

    override fun append(csq: CharSequence?): MutableString

    override fun append(csq: CharSequence?,
                        start: Int,
                        end: Int): MutableString

    fun insert(position: Int,
               char: Char): MutableString

    fun insert(position: Int,
               str: String): MutableString

    fun insert(position: Int,
               array: CharArray): MutableString

    fun insert(position: Int,
               array: CharArray,
               offset: Int): MutableString

    fun insert(position: Int,
               array: CharArray,
               offset: Int,
               length: Int): MutableString

    fun delete(range: IntRange): MutableString

    fun delete(startIndex: Int): MutableString

    fun delete(startIndex: Int,
               endIndex: Int): MutableString

    fun clear(): MutableString

    fun substring(startIndex: Int): String

    fun substring(startIndex: Int,
                  endIndex: Int): String

    override fun toString(): String
}
