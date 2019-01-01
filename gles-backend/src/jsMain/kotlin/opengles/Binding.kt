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

package org.tobi29.scapes.engine.backends.opengles

import net.gitout.ktbindings.utils.DataBuffer
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.ArrayBufferView
import org.khronos.webgl.DataView
import org.khronos.webgl.Int8Array
import org.tobi29.arrays.Bytes
import org.tobi29.arrays.BytesRO
import org.tobi29.arrays.sliceOver
import org.tobi29.io.asDataView
import org.tobi29.io.readAsDataView
import org.tobi29.stdex.asArray

actual class BytesRODataBuffer actual constructor(
    private val bytes: BytesRO
) : DataBuffer {
    override fun read(): Pair<ArrayBufferView, () -> Unit> =
        bytes.readAsDataView() to {}

    override fun write() =
        error("Buffer not writeable")
}

actual class BytesDataBuffer actual constructor(
    private val bytes: Bytes
) : DataBuffer {
    override fun read(): Pair<ArrayBufferView, () -> Unit> =
        bytes.readAsDataView() to {}

    override fun write(): Pair<ArrayBufferView, () -> Unit> {
        var buffer = bytes.asDataView()
        if (buffer == null) {
            val arrayBuffer = ArrayBuffer(bytes.size)
            buffer = DataView(arrayBuffer)
            val view = Int8Array(arrayBuffer).asArray().sliceOver()
            bytes.getBytes(0, view)
            return buffer to { view.getBytes(0, bytes) }
        } else {
            return buffer to {}
        }
    }
}
