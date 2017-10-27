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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.utils

actual inline fun copyArray(src: BooleanArray,
                          dest: BooleanArray,
                          length: Int,
                          offsetSrc: Int,
                          offsetDest: Int): Unit =
        System.arraycopy(src, offsetSrc, dest, offsetDest, length)

actual inline fun copyArray(src: ByteArray,
                          dest: ByteArray,
                          length: Int,
                          offsetSrc: Int,
                          offsetDest: Int): Unit =
        System.arraycopy(src, offsetSrc, dest, offsetDest, length)

actual inline fun copyArray(src: ShortArray,
                          dest: ShortArray,
                          length: Int,
                          offsetSrc: Int,
                          offsetDest: Int): Unit =
        System.arraycopy(src, offsetSrc, dest, offsetDest, length)

actual inline fun copyArray(src: IntArray,
                          dest: IntArray,
                          length: Int,
                          offsetSrc: Int,
                          offsetDest: Int): Unit =
        System.arraycopy(src, offsetSrc, dest, offsetDest, length)

actual inline fun copyArray(src: LongArray,
                          dest: LongArray,
                          length: Int,
                          offsetSrc: Int,
                          offsetDest: Int): Unit =
        System.arraycopy(src, offsetSrc, dest, offsetDest, length)

actual inline fun copyArray(src: FloatArray,
                          dest: FloatArray,
                          length: Int,
                          offsetSrc: Int,
                          offsetDest: Int): Unit =
        System.arraycopy(src, offsetSrc, dest, offsetDest, length)

actual inline fun copyArray(src: DoubleArray,
                          dest: DoubleArray,
                          length: Int,
                          offsetSrc: Int,
                          offsetDest: Int): Unit =
        System.arraycopy(src, offsetSrc, dest, offsetDest, length)

actual inline fun copyArray(src: CharArray,
                          dest: CharArray,
                          length: Int,
                          offsetSrc: Int,
                          offsetDest: Int): Unit =
        System.arraycopy(src, offsetSrc, dest, offsetDest, length)

actual inline fun <T> copyArray(src: Array<out T>,
                              dest: Array<in T>,
                              length: Int,
                              offsetSrc: Int,
                              offsetDest: Int): Unit =
        System.arraycopy(src, offsetSrc, dest, offsetDest, length)
