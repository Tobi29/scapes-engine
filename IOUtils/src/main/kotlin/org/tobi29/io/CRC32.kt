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

package org.tobi29.io

@Suppress("NOTHING_TO_INLINE")
inline fun initChainCRC32(crc: Int = 0): Int =
        crc xor -1

@Suppress("NOTHING_TO_INLINE")
inline fun chainCRC32(crc: Int,
                      data: Byte,
                      table: IntArray): Int =
        table[(crc xor data.toInt()) and 0xFF] xor (crc ushr 8)

@Suppress("NOTHING_TO_INLINE")
inline fun Int.finishChainCRC32(): Int = initChainCRC32(this)

@Suppress("NOTHING_TO_INLINE")
inline fun nextCRC32(crc: Int,
                     data: Byte,
                     table: IntArray): Int =
        initChainCRC32(crc).let {
            chainCRC32(it, data, table)
        }.finishChainCRC32()

fun tableCRC32(key: Int): IntArray = IntArray(256) { i ->
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
