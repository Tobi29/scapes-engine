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

import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.tobi29.stdex.ArrayDeque
import org.tobi29.stdex.asArray

actual class ZDeflater actual constructor(
    private val level: Int,
    buffer: Int
) : CompressionUtil.Filter {
    private var deflater = Pako.Deflate(DeflateOptions(level))
    private var input = ByteArray(buffer).view
    private val outputs = ArrayDeque<Uint8Array>()

    init {
        deflater.onData = { outputs.add(it) }
    }

    actual override fun input(buffer: ReadableByteStream): Boolean {
        val read = buffer.getSome(input)
        if (read < 0) return false
        deflater.push(input.slice(0, read).readAsUint8Array(), 0)
        return true
    }

    actual override fun output(buffer: WritableByteStream): Int {
        val output = outputs.poll() ?: return -1
        buffer.put(
            Int8Array(
                output.buffer, output.byteOffset,
                output.byteLength
            ).asArray().view
        )
        return output.length
    }

    actual override fun finish() {
        deflater.push(Uint8Array(0), 4)
    }

    actual override fun reset() {
        deflater = Pako.Deflate(DeflateOptions(level))
    }

    actual override fun needsInput(): Boolean {
        return outputs.isEmpty()
    }

    actual override fun finished(): Boolean {
        return outputs.isEmpty()
    }

    actual override fun close() {
    }
}

actual class ZInflater actual constructor(
    buffer: Int
) : CompressionUtil.Filter {
    private var inflater = Pako.Inflate()
    private var input = ByteArray(buffer).view
    private val outputs = ArrayDeque<Uint8Array>()

    init {
        inflater.onData = { outputs.add(it) }
    }

    actual override fun input(buffer: ReadableByteStream): Boolean {
        val read = buffer.getSome(input)
        if (read < 0) return false
        inflater.push(input.slice(0, read).readAsUint8Array(), 0)
        return true
    }

    actual override fun output(buffer: WritableByteStream): Int {
        val output = outputs.poll() ?: return -1
        buffer.put(
            Int8Array(
                output.buffer, output.byteOffset,
                output.byteLength
            ).asArray().view
        )
        return output.length
    }

    actual override fun finish() {
        inflater.push(Uint8Array(0), 4)
    }

    actual override fun reset() {
        inflater = Pako.Inflate()
    }

    actual override fun needsInput(): Boolean {
        return outputs.isEmpty()
    }

    actual override fun finished(): Boolean {
        return outputs.isEmpty()
    }

    actual override fun close() {
    }
}

@JsModule("pako")
@JsNonModule
private external object Pako {
    class Deflate(options: dynamic = definedExternally) {
        val err: Int
        val msg: String
        val ended: Boolean
        val result: Uint8Array
        var onData: (Uint8Array) -> Unit
        var onEnd: (Int) -> Unit
        fun push(
            data: Uint8Array,
            mode: Int
        ): Boolean
    }

    class Inflate {
        val err: Int
        val msg: String
        val ended: Boolean
        val result: Uint8Array
        var onData: (Uint8Array) -> Unit
        var onEnd: (Int) -> Unit
        fun push(
            data: Uint8Array,
            mode: Int
        ): Boolean
    }
}

private fun DeflateOptions(level: Int): dynamic {
    val options = object {}.asDynamic()
    options.level = level
    return options
}
