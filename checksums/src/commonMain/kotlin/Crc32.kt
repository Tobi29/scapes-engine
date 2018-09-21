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

package org.tobi29.checksums

import org.tobi29.arrays.BytesRO
import org.tobi29.arrays.asIterable
import org.tobi29.stdex.InlineUtility

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun initChainCrc32(crc: Int = 0): Int = crc.inv()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.finishChainCrc32(): Int = initChainCrc32(this)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun chainCrc32(
    crc: Int,
    data: Byte,
    table: IntArray
): Int = table[(crc xor data.toInt()) and 0xFF] xor (crc ushr 8)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun computeCrc32(
    data: Byte,
    table: IntArray
): Int = computeCrc32(0, data, table)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun computeCrc32(
    crc: Int,
    data: Byte,
    table: IntArray
): Int = initChainCrc32(crc).let {
    chainCrc32(it, data, table)
}.finishChainCrc32()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun chainCrc32(
    crc: Int,
    data: ByteArray,
    table: IntArray
): Int = chainCrc32(crc, data, 0, data.size, table)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun chainCrc32(
    crc: Int,
    data: ByteArray,
    offset: Int = 0,
    size: Int = data.size - offset,
    table: IntArray
): Int {
    var c = crc
    for (i in offset until offset + size) {
        c = chainCrc32(c, data[i], table)
    }
    return c
}

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun computeCrc32(
    data: ByteArray,
    table: IntArray
): Int = computeCrc32(0, data, table)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun computeCrc32(
    crc: Int,
    data: ByteArray,
    table: IntArray
): Int = computeCrc32(crc, data, 0, data.size, table)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun computeCrc32(
    crc: Int,
    data: ByteArray,
    offset: Int = 0,
    size: Int = data.size - offset,
    table: IntArray
): Int = initChainCrc32(crc).let {
    chainCrc32(it, data, offset, size, table)
}.finishChainCrc32()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun chainCrc32(
    crc: Int,
    data: BytesRO,
    table: IntArray
): Int = data.asIterable().fold(crc) { c, d -> chainCrc32(c, d, table) }

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun computeCrc32(
    data: BytesRO,
    table: IntArray
): Int = computeCrc32(0, data, table)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun computeCrc32(
    crc: Int,
    data: BytesRO,
    table: IntArray
): Int = initChainCrc32(crc).let {
    chainCrc32(it, data, table)
}.finishChainCrc32()

fun tableCrc32(key: Int): IntArray = IntArray(256) { i ->
    var crc = i
    repeat(8) {
        crc = if (crc and 1 != 0) {
            key xor (crc ushr 1)
        } else {
            crc ushr 1
        }
    }
    crc
}
