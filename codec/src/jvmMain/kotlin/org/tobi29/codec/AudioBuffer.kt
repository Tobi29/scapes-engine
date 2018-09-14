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

package org.tobi29.codec

import org.tobi29.arrays.HeapFloats
import org.tobi29.arrays.sliceOver
import org.tobi29.stdex.assert

class AudioBuffer(private val size: Int) {
    var buffer = floatArrayOf().sliceOver(size = 0)
        private set
    private var channels = 0
    private var rate = 0
    private var empty = true
    var isDone = false
        private set

    fun buffer(channels: Int,
               rate: Int): HeapFloats {
        if (empty) {
            empty = false
            val bufferSize = size * channels
            if (buffer.size < bufferSize)
                buffer = FloatArray(bufferSize).sliceOver()
            this.channels = channels
            this.rate = rate
        } else {
            assert { channels == this.channels && rate == this.rate }
        }
        return buffer
    }

    fun done(size: Int) {
        assert { size % channels == 0 }
        buffer = buffer.slice(0, size)
        isDone = true
    }

    fun channels(): Int {
        return channels
    }

    fun rate(): Int {
        return rate
    }

    fun clear() {
        assert { isDone }
        empty = true
        isDone = false
    }
}

inline fun AudioBuffer.toPCM16(consumer: (Short) -> Unit) {
    for (i in 0 until buffer.size) {
        consumer(toInt16(buffer[i]))
    }
}
