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

package org.tobi29.arrays

import org.khronos.webgl.DataView
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.tobi29.stdex.asTypedArray
import org.tobi29.stdex.asUnsignedTypedArray

inline fun BytesRO.asDataView(): DataView? = when (this) {
    is HeapBytes -> array.asTypedArray().let {
        DataView(it.buffer, it.byteOffset + offset, size)
    }
    else -> null
}

inline fun BytesRO.asTypedArray(): Int8Array? = when (this) {
    is HeapBytes -> array.asTypedArray().subarray(offset, offset + size)
    else -> null
}

inline fun BytesRO.asUnsignedTypedArray(): Uint8Array? = when (this) {
    is HeapBytes -> array.asUnsignedTypedArray().subarray(offset, offset + size)
    else -> null
}
