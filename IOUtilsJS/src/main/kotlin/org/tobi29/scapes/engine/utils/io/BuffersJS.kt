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

package org.tobi29.scapes.engine.utils.io

import org.khronos.webgl.*
import org.tobi29.scapes.engine.utils.HeapByteArraySlice
import org.tobi29.scapes.engine.utils.asTypedArray

fun ByteViewRO.asDataView(): DataView? = asInt8Array()?.let {
    DataView(it.buffer, it.byteOffset, it.byteLength)
}

fun ByteViewRO.asInt8Array(): Int8Array? = when (this) {
    is HeapByteArraySlice -> array.asTypedArray().subarray(offset,
            offset + size)
    else -> null
}

fun ByteViewRO.asUint8Array(): Uint8Array? = asInt8Array()?.let {
    Uint8Array(it.buffer, it.byteOffset, it.byteLength)
}

fun ByteViewRO.readAsDataView(): DataView =
        asDataView() ?: ByteArray(size).view.also {
            getBytes(0, it)
        }.array.asTypedArray().let {
            DataView(it.buffer, it.byteOffset, it.byteLength)
        }

fun ByteViewRO.readAsInt8Array(): Int8Array =
        asInt8Array() ?: ByteArray(size).view.also {
            getBytes(0, it)
        }.asTypedArray()

fun ByteViewRO.readAsUint8Array(): Uint8Array = readAsInt8Array().let {
    Uint8Array(it.buffer, it.byteOffset, it.byteLength)
}

val DataView.viewNative: TypedView
    get() = if (NATIVE_ENDIAN == BIG_ENDIAN) viewBE
    else viewLE

val DataView.viewBE: TypedViewBE
    get() = TypedViewBE(buffer, byteOffset, 0, byteLength)

val DataView.viewLE: TypedViewLE
    get() = TypedViewLE(buffer, byteOffset, 0, byteLength)

actual class ByteOrder actual private constructor(private val name: String) {
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

actual val NATIVE_ENDIAN = run {
    val buffer = ArrayBuffer(4)
    val buffer8 = Uint8Array(buffer)
    val buffer32 = Uint32Array(buffer)
    buffer32[0] = 0x01020304
    when (buffer8[0]) {
        0x01.toByte() -> BIG_ENDIAN
        0x04.toByte() -> LITTLE_ENDIAN
        else -> throw UnsupportedOperationException(
                "Endianness detection failed")
    }
}
