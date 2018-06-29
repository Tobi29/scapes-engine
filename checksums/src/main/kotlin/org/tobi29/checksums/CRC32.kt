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

package org.tobi29.checksums

import org.tobi29.arrays.BytesRO

inline fun initChainCrc32(crc: Int = 0): Int = crc.inv()

inline fun Int.finishChainCrc32(): Int = initChainCrc32(this)

inline fun chainCrc32(
    crc: Int,
    data: Byte,
    table: IntArray
): Int = table[(crc xor data.toInt()) and 0xFF] xor (crc ushr 8)

inline fun computeCrc32(
    data: Byte,
    table: IntArray
): Int = computeCrc32(0, data, table)

inline fun computeCrc32(
    crc: Int,
    data: Byte,
    table: IntArray
): Int = initChainCrc32(crc).let {
    chainCrc32(it, data, table)
}.finishChainCrc32()

inline fun chainCrc32(
    crc: Int,
    data: ByteArray,
    table: IntArray
): Int = chainCrc32(crc, data, 0, data.size, table)

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

inline fun computeCrc32(
    data: ByteArray,
    table: IntArray
): Int = computeCrc32(0, data, table)

inline fun computeCrc32(
    crc: Int,
    data: ByteArray,
    table: IntArray
): Int = computeCrc32(crc, data, 0, data.size, table)

inline fun computeCrc32(
    crc: Int,
    data: ByteArray,
    offset: Int = 0,
    size: Int = data.size - offset,
    table: IntArray
): Int = initChainCrc32(crc).let {
    chainCrc32(it, data, offset, size, table)
}.finishChainCrc32()

inline fun chainCrc32(
    crc: Int,
    data: BytesRO,
    table: IntArray
): Int = data.fold(crc) { c, d -> chainCrc32(c, d, table) }

inline fun computeCrc32(
    data: BytesRO,
    table: IntArray
): Int = computeCrc32(0, data, table)

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

// TODO: Remove after 0.0.14

@Deprecated(
    "Renamed",
    ReplaceWith("initChainCrc32(crc)", "org.tobi29.checksums.initChainCrc32")
)
inline fun initChainCRC32(crc: Int = 0): Int = initChainCrc32(crc)

@Deprecated(
    "Renamed",
    ReplaceWith("finishChainCrc32()", "org.tobi29.checksums.initChainCrc32")
)
inline fun Int.finishChainCRC32(): Int = finishChainCrc32()

@Deprecated(
    "Renamed",
    ReplaceWith(
        "chainCrc32(crc, data, table)",
        "org.tobi29.checksums.chainCrc32"
    )
)
inline fun chainCRC32(crc: Int, data: Byte, table: IntArray): Int =
    chainCrc32(crc, data, table)

@Deprecated(
    "Renamed",
    ReplaceWith(
        "computeCrc32(data, table)",
        "org.tobi29.checksums.computeCrc32"
    )
)
inline fun computeCRC32(data: Byte, table: IntArray): Int =
    computeCrc32(data, table)

@Deprecated(
    "Renamed",
    ReplaceWith(
        "computeCrc32(crc, data, table)",
        "org.tobi29.checksums.computeCrc32"
    )
)
inline fun computeCRC32(crc: Int, data: Byte, table: IntArray): Int =
    computeCrc32(crc, data, table)

@Deprecated(
    "Renamed",
    ReplaceWith(
        "chainCrc32(crc, data, table)",
        "org.tobi29.checksums.chainCrc32"
    )
)
inline fun chainCRC32(crc: Int, data: ByteArray, table: IntArray): Int =
    chainCrc32(crc, data, table)

@Deprecated(
    "Renamed",
    ReplaceWith(
        "chainCrc32(crc, data, offset, size, table)",
        "org.tobi29.checksums.chainCrc32"
    )
)
inline fun chainCRC32(
    crc: Int,
    data: ByteArray,
    offset: Int = 0,
    size: Int = data.size - offset,
    table: IntArray
): Int = chainCrc32(crc, data, offset, size, table)

@Deprecated(
    "Renamed",
    ReplaceWith(
        "computeCrc32(data, table)",
        "org.tobi29.checksums.computeCrc32"
    )
)
inline fun computeCRC32(data: ByteArray, table: IntArray): Int =
    computeCrc32(data, table)

@Deprecated(
    "Renamed",
    ReplaceWith(
        "computeCrc32(crc, data, table)",
        "org.tobi29.checksums.computeCrc32"
    )
)
inline fun computeCRC32(crc: Int, data: ByteArray, table: IntArray): Int =
    computeCrc32(crc, data, table)

@Deprecated(
    "Renamed",
    ReplaceWith(
        "computeCrc32(crc, data, table)",
        "org.tobi29.checksums.computeCrc32"
    )
)
inline fun computeCRC32(
    crc: Int,
    data: ByteArray,
    offset: Int = 0,
    size: Int = data.size - offset,
    table: IntArray
): Int = computeCrc32(crc, data, table)

@Deprecated(
    "Renamed",
    ReplaceWith(
        "chainCrc32(crc, data, table)",
        "org.tobi29.checksums.chainCrc32"
    )
)
inline fun chainCRC32(crc: Int, data: BytesRO, table: IntArray): Int =
    chainCrc32(crc, data, table)

@Deprecated(
    "Renamed",
    ReplaceWith(
        "computeCrc32(data, table)",
        "org.tobi29.checksums.computeCrc32"
    )
)
inline fun computeCRC32(data: BytesRO, table: IntArray): Int =
    computeCrc32(data, table)

@Deprecated(
    "Renamed",
    ReplaceWith(
        "computeCrc32(crc, data, table)",
        "org.tobi29.checksums.computeCrc32"
    )
)
inline fun computeCRC32(crc: Int, data: BytesRO, table: IntArray): Int =
    computeCrc32(crc, data, table)

@Deprecated(
    "Renamed",
    ReplaceWith("tableCrc32(key)", "org.tobi29.checksums.tableCrc32")
)
inline fun tableCRC32(key: Int): IntArray = tableCrc32(key)
