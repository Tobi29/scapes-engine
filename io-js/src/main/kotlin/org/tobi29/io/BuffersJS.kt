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

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.DataView
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.tobi29.arrays.BytesRO
import org.tobi29.arrays.HeapBytes
import org.tobi29.stdex.BIG_ENDIAN
import org.tobi29.stdex.NATIVE_ENDIAN
import org.tobi29.stdex.asTypedArray

fun BytesRO.asDataView(): DataView? = asInt8Array()?.let {
    DataView(it.buffer, it.byteOffset, it.byteLength)
}

fun BytesRO.asInt8Array(): Int8Array? = when (this) {
    is HeapBytes -> array.asTypedArray().subarray(offset,
            offset + size)
    else -> null
}

fun BytesRO.asUint8Array(): Uint8Array? = asInt8Array()?.let {
    Uint8Array(it.buffer, it.byteOffset, it.byteLength)
}

fun BytesRO.readAsArrayBuffer(): ArrayBuffer =
        readAsInt8Array().let { array ->
            if (array.byteOffset == 0 && array.byteLength == array.buffer.byteLength) array.buffer
            else ArrayBuffer(array.byteLength).also { Int8Array(it).set(array) }
        }

fun BytesRO.readAsDataView(): DataView =
        asDataView() ?: ByteArray(size).also {
            getBytes(0, it.view)
        }.asTypedArray().let {
            DataView(it.buffer, it.byteOffset, it.byteLength)
        }

fun BytesRO.readAsInt8Array(): Int8Array =
        asInt8Array() ?: ByteArray(size).also {
            getBytes(0, it.view)
        }.asTypedArray()

fun BytesRO.readAsUint8Array(): Uint8Array = readAsInt8Array().let {
    Uint8Array(it.buffer, it.byteOffset, it.byteLength)
}

val DataView.viewNative: TypedView
    get() = if (NATIVE_ENDIAN == BIG_ENDIAN) viewBE
    else viewLE

val DataView.viewBE: TypedViewBE
    get() = TypedViewBE(buffer, byteOffset, 0, byteLength)

val DataView.viewLE: TypedViewLE
    get() = TypedViewLE(buffer, byteOffset, 0, byteLength)
