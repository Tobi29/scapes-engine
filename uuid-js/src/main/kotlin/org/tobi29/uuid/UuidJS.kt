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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.uuid

import org.khronos.webgl.ArrayBufferView
import org.khronos.webgl.Int32Array
import org.khronos.webgl.get
import org.tobi29.stdex.combineToLong
import org.tobi29.stdex.toString

actual fun String.toUuid(): Uuid? {
    val split = split('-')
    if (split.size != 5) return null
    val g0 = (split[0].toLongOrNull(16) ?: return null) and 0xFFFFFFFFL
    val g1 = (split[1].toLongOrNull(16) ?: return null) and 0xFFFFL
    val g2 = (split[2].toLongOrNull(16) ?: return null) and 0xFFFFL
    val g3 = (split[3].toLongOrNull(16) ?: return null) and 0xFFFFL
    val g4 = (split[4].toLongOrNull(16) ?: return null) and 0xFFFFFFFFFFFFL
    return Uuid(
        (g0 shl 32) or (g1 shl 16) or (g2),
        (g3 shl 48) or (g4)
    )
}

actual class Uuid actual constructor(
    private val mostSignificantBits: Long,
    private val leastSignificantBits: Long
) {
    actual open fun getMostSignificantBits() = mostSignificantBits
    actual open fun getLeastSignificantBits() = leastSignificantBits

    override fun toString(): String =
        "${(mostSignificantBits ushr 32
                and 0xFFFFFFFFL
                or 0x100000000L).toString(16).substring(1)}-${
        ((mostSignificantBits ushr 16).toInt()
                and 0xFFFF
                or 0x10000).toString(16).substring(1)}-${
        ((mostSignificantBits ushr 0).toInt()
                and 0xFFFF
                or 0x10000).toString(16).substring(1)}-${
        ((leastSignificantBits ushr 48).toInt()
                and 0xFFFF
                or 0x10000).toString(16).substring(1)}-${
        (leastSignificantBits ushr 0
                and 0xFFFFFFFFFFFFL
                or 0x1000000000000L).toString(16).substring(1)}"
}

actual fun randomUuid(): Uuid {
    val values = Int32Array(4)
    crypto.getRandomValues(values)
    val i3 = values[3]
    // val i2 = values[1] and 0xFFFF0FFF or 0x00004000
    val i2 = values[1] and -0xF001 or 0x00004000
    // val i1 = values[2] and 0x3FFFFFFF or 0x80000000
    val i1 = values[2] and 0x3FFFFFFF or -0x80000000
    val i0 = values[0]
    return Uuid(combineToLong(i3, i2), combineToLong(i1, i0))
}

private external val crypto: Crypto

private external class Crypto {
    fun getRandomValues(array: ArrayBufferView)
}
