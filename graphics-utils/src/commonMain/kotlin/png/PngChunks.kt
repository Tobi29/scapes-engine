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

package org.tobi29.graphics.png

import org.tobi29.arrays.BytesRO
import org.tobi29.arrays.HeapBytes
import org.tobi29.arrays.sliceOver
import org.tobi29.checksums.chainCrc32
import org.tobi29.checksums.finishChainCrc32
import org.tobi29.checksums.initChainCrc32
import org.tobi29.io.IOException
import org.tobi29.io.ReadableByteStream
import org.tobi29.io.WritableByteStream
import org.tobi29.stdex.splitToBytes

internal inline fun <R> ReadableByteStream.readChunk(
    array: ByteArray, maxLength: Int, output: (Int, HeapBytes) -> R
): R {
    val length = getInt()
    if (length < 0)
        throw IOException("Negative length: $length") // Allowed according to spec
    if (length > maxLength)
        throw IOException("Length too great: $length") // Avoid dos attacks
    val type = getInt()
    val chunk = (if (array.size >= length) array else ByteArray(length))
        .sliceOver(0, length)
    get(chunk)
    val crc = getInt()
    var checkCrc = initChainCrc32()
    type.splitToBytes { b3, b2, b1, b0 ->
        checkCrc = chainCrc32(checkCrc, b3, zlibTable)
        checkCrc = chainCrc32(checkCrc, b2, zlibTable)
        checkCrc = chainCrc32(checkCrc, b1, zlibTable)
        checkCrc = chainCrc32(checkCrc, b0, zlibTable)
    }
    checkCrc = chainCrc32(checkCrc, chunk, zlibTable)
    checkCrc = checkCrc.finishChainCrc32()
    if (crc != checkCrc)
        throw IOException("Invalid CRC-32 check, computed $checkCrc, got $crc")
    return output(type, chunk)
}

internal fun WritableByteStream.writeChunk(
    type: Int,
    chunk: BytesRO? = null
) {
    var crc = initChainCrc32()
    type.splitToBytes { b3, b2, b1, b0 ->
        crc = chainCrc32(crc, b3, zlibTable)
        crc = chainCrc32(crc, b2, zlibTable)
        crc = chainCrc32(crc, b1, zlibTable)
        crc = chainCrc32(crc, b0, zlibTable)
    }
    if (chunk == null) {
        putInt(0)
        putInt(type)
    } else {
        crc = chainCrc32(crc, chunk, zlibTable)
        putInt(chunk.size)
        putInt(type)
        put(chunk)
    }
    putInt(crc.finishChainCrc32())
}
