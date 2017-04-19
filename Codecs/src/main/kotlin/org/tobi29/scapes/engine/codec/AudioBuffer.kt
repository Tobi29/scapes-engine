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

package org.tobi29.scapes.engine.codec

import org.tobi29.scapes.engine.utils.assert
import org.tobi29.scapes.engine.utils.io.FloatBuffer

class AudioBuffer(private val size: Int) {
    private var buffer = FloatBuffer(0)
    private var channels = 0
    private var rate = 0
    private var empty = true
    var isDone = false
        private set

    fun buffer(channels: Int,
               rate: Int): FloatBuffer {
        if (empty) {
            empty = false
            val capacity = size * channels
            if (buffer.capacity() != capacity) {
                buffer = FloatBuffer(capacity)
            }
            this.channels = channels
            this.rate = rate
        } else {
            assert { channels == this.channels && rate == this.rate }
        }
        return buffer
    }

    fun done() {
        assert { buffer.position() % channels == 0 }
        buffer.flip()
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
        buffer.clear()
        isDone = false
    }

    fun getBuffer(): FloatBuffer {
        return buffer
    }
}

inline fun AudioBuffer.toPCM16(consumer: (Short) -> Unit) {
    val buffer = getBuffer()
    while (buffer.hasRemaining()) {
        consumer(toInt16(buffer.get()))
    }
}
