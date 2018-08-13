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

package org.tobi29.stdex

import org.khronos.webgl.*

actual class ByteOrder private actual constructor(private val name: String) {
    override fun toString(): String {
        return name
    }

    companion object {
        val BIG_ENDIAN = ByteOrder("BIG_ENDIAN")
        val LITTLE_ENDIAN = ByteOrder("LITTLE_ENDIAN")
    }
}

actual inline val BIG_ENDIAN: ByteOrder get() = ByteOrder.BIG_ENDIAN

actual inline val LITTLE_ENDIAN: ByteOrder get() = ByteOrder.LITTLE_ENDIAN

@PublishedApi
internal val NATIVE_BIG_ENDIAN = run {
    val buffer = ArrayBuffer(4)
    val buffer8 = Uint8Array(buffer)
    val buffer32 = Uint32Array(buffer)
    buffer32[0] = 0x01020304
    when (buffer8[0]) {
        0x01.toByte() -> true
        0x04.toByte() -> false
        else -> throw UnsupportedOperationException("Endianness detection failed")
    }
}

actual inline val NATIVE_ENDIAN: ByteOrder
    get() = if (NATIVE_BIG_ENDIAN) BIG_ENDIAN else LITTLE_ENDIAN
