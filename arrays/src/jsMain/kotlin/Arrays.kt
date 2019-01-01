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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.arrays

import org.khronos.webgl.DataView
import org.khronos.webgl.Int16Array
import org.khronos.webgl.Int32Array
import org.khronos.webgl.Int8Array
import org.tobi29.stdex.asArray
import org.tobi29.stdex.asTypedArray

inline fun BytesRO.asDataView(): DataView? = when (this) {
    is HeapBytes -> array.asTypedArray().let {
        DataView(it.buffer, it.byteOffset + offset, size)
    }
    else -> null
}

inline fun BytesRO.asTypedArray(): Int8Array? = when (this) {
    is HeapBytes ->
        array.asTypedArray().subarray(offset, offset + size)
    else -> null
}

inline fun Bytes.toTypedArray(): Int8Array = toByteArray().asTypedArray()

inline fun <R> Bytes.mutateAsTypedArray(block: (Int8Array) -> R): R {
    var array = asTypedArray()
    val mapped = if (array == null) {
        array = toTypedArray()
        false
    } else true
    return try {
        block(array)
    } finally {
        if (!mapped) getBytes(0, array.asArray().sliceOver())
    }
}

inline fun ShortsRO.asDataView(): DataView? = when (this) {
    is HeapShorts -> array.asTypedArray().let {
        DataView(it.buffer, it.byteOffset + offset, size)
    }
    else -> null
}

inline fun ShortsRO.asTypedArray(): Int16Array? = when (this) {
    is HeapShorts ->
        array.asTypedArray().subarray(offset, offset + size)
    else -> null
}

inline fun Shorts.toTypedArray(): Int16Array = toShortArray().asTypedArray()

inline fun <R> Shorts.mutateAsTypedArray(block: (Int16Array) -> R): R {
    var array = asTypedArray()
    val mapped = if (array == null) {
        array = toTypedArray()
        false
    } else true
    return try {
        block(array)
    } finally {
        if (!mapped) getShorts(0, array.asArray().sliceOver())
    }
}

inline fun IntsRO.asDataView(): DataView? = when (this) {
    is HeapInts -> array.asTypedArray().let {
        DataView(it.buffer, it.byteOffset + offset, size)
    }
    else -> null
}

inline fun IntsRO.asTypedArray(): Int32Array? = when (this) {
    is HeapInts ->
        array.asTypedArray().subarray(offset, offset + size)
    else -> null
}

inline fun Ints.toTypedArray(): Int32Array = toIntArray().asTypedArray()

inline fun <R> Ints.mutateAsTypedArray(block: (Int32Array) -> R): R {
    var array = asTypedArray()
    val mapped = if (array == null) {
        array = toTypedArray()
        false
    } else true
    return try {
        block(array)
    } finally {
        if (!mapped) getInts(0, array.asArray().sliceOver())
    }
}
