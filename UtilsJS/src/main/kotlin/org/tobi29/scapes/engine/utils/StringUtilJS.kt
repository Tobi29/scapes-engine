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

package org.tobi29.scapes.engine.utils

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set

impl fun ByteArray.strUTF8(): String {
    val buffer = ArrayBuffer(size)
    val byteBuffer = Uint8Array(buffer)
    for ((i, v) in withIndex()) {
        byteBuffer[i] = v
    }
    return TextDecoder().decode(byteBuffer)
}

impl fun String.bytesUTF8(): ByteArray {
    val byteBuffer = TextEncoder().encode(this)
    val array = ByteArray(byteBuffer.length)
    for (i in array.indices) {
        array[i] = byteBuffer[i]
    }
    return array
}

impl internal fun CharArray.copyToStringImpl(offset: Int,
                                             length: Int) =
        StringBuilder(length).apply {
            for (i in offset until offset + length) {
                append(this@copyToStringImpl[i])
            }
        }.toString()

impl internal fun String.copyToArrayImpl(destination: CharArray,
                                         offset: Int,
                                         startIndex: Int,
                                         endIndex: Int): CharArray {
    val destOffset = offset - startIndex
    for (i in startIndex until endIndex) {
        destination[i + destOffset] = this[i]
    }
    return destination
}
