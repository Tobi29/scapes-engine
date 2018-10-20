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

package org.tobi29.io

import org.tobi29.arrays.BytesRO
import org.tobi29.arrays.sliceOver
import org.tobi29.stdex.toIntClamped
import org.tobi29.stdex.utf8ToString

inline fun ReadableByteStream.process(
    bufferSize: Int = 1024,
    sink: (BytesRO) -> Unit
) {
    val buffer = ByteArray(bufferSize).sliceOver()
    while (true) {
        val read = getSome(buffer)
        if (read < 0) break
        sink(buffer.slice(0, read))
    }
}

inline fun filter(
    input: ReadableByteStream,
    output: WritableByteStream,
    filterInput: (ReadableByteStream) -> Boolean,
    filterOutput: (WritableByteStream) -> Int,
    filterFinish: () -> Unit,
    filterNeedsInput: () -> Boolean,
    filterFinished: () -> Boolean
) {
    while (filterInput(input)) {
        while (!filterNeedsInput()) {
            val len = filterOutput(output)
            if (len <= 0) break
        }
    }
    filterFinish()
    while (!filterFinished()) {
        val len = filterOutput(output)
        if (len <= 0) break
    }
}

fun ReadableByteStream.asByteArray(): ByteArray =
    asByteView().readAsByteArray()

fun ReadableByteStream.asString(): String =
    asByteView().let { it.array.utf8ToString(it.offset, it.size) }

fun ReadableByteStream.asByteView(): HeapViewByteBE =
    (if (this is SeekableByteChannel) MemoryViewStreamDefault(
        ByteArray(remaining.toIntClamped()).viewBE
    )
    else MemoryViewStreamDefault()).also { stream ->
        process { stream.put(it) }
        stream.flip()
    }.bufferSlice()
