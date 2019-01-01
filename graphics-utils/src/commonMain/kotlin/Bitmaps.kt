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

package org.tobi29.graphics

import org.tobi29.arrays.*
import org.tobi29.math.vector.Vector2i

data class BitmapC<DF : Vars2, out D : DF, out F : ColorFormat<DF>>(
    val data: D,
    val format: F
)

typealias Bitmap<D, F> = BitmapC<*, D, F>

inline val <D : Vars2> Bitmap<D, *>.width: Int get() = data.width

inline val <D : Vars2> Bitmap<D, *>.height: Int get() = data.height

inline val <D : Vars2> Bitmap<D, *>.size: Vector2i
    get() = Vector2i(width, height)

@Suppress("UNCHECKED_CAST")
inline fun <reified D : Vars2, reified F : ColorFormat<D>> Bitmap<*, *>.cast(
): Bitmap<D, F>? =
    if (data is D && this.format == format) this as Bitmap<D, F> else null

@Suppress("UNCHECKED_CAST")
inline fun <reified D : Vars2, reified F : ColorFormat<D>> Bitmap<*, *>.cast(
    format: F
): Bitmap<D, F>? =
    if (data is D && this.format == format) this as Bitmap<D, F> else null

inline operator fun Bitmap<IntsRO2, ColorFormatInt>.get(
    x: Int, y: Int
): Int = data[x, y]

inline operator fun Bitmap<Ints2, ColorFormatInt>.set(
    x: Int, y: Int,
    value: Int
) {
    data[x, y] = value
}

typealias Ints2BytesROBitmap<F> = Bitmap<Ints2BytesRO<*>, F>
typealias Ints2BytesBitmap<F> = Bitmap<Ints2Bytes<*>, F>

inline fun <F : ColorFormatInt> Ints2BytesROBitmap(
    data: BytesRO, width: Int, height: Int, format: F
): Ints2BytesROBitmap<F> = BitmapC(
    Ints2BytesRO(data, width, height),
    format
)

inline fun <F : ColorFormatInt> Ints2BytesBitmap(
    data: Bytes, width: Int, height: Int, format: F
): Ints2BytesBitmap<F> = BitmapC(
    Ints2Bytes(data, width, height),
    format
)

typealias Ints2ByteArrayBitmap<F> = Bitmap<Ints2ByteArray, F>

inline fun <F : ColorFormatInt> Ints2ByteArrayBitmap(
    width: Int, height: Int, format: F
): Ints2ByteArrayBitmap<F> = BitmapC(
    Ints2ByteArray(width, height),
    format
)

// TODO: Remove after 0.0.14

@Deprecated("Use new array wrappers")
typealias IntByteViewBitmap<F> = Bitmap<Int2ByteArrayRO<BytesRO>, F>

@Deprecated("Use new array wrappers")
typealias MutableIntByteViewBitmap<F> = Bitmap<Int2ByteArray<Bytes>, F>

@Deprecated("Use new array wrappers")
inline fun <F : ColorFormatInt> IntByteViewBitmap(
    data: BytesRO, width: Int, height: Int, format: F
): IntByteViewBitmap<F> = BitmapC(
    Int2ByteArrayRO(data, width, height),
    format
)

@Deprecated("Use new array wrappers")
inline fun <F : ColorFormatInt> MutableIntByteViewBitmap(
    width: Int, height: Int, format: F
): MutableIntByteViewBitmap<F> = BitmapC(
    Int2ByteArray(ByteArray(width * height shl 2).sliceOver(), width, height),
    format
)

@Deprecated("Use new array wrappers")
inline fun <F : ColorFormatInt> MutableIntByteViewBitmap(
    data: Bytes, width: Int, height: Int, format: F
): MutableIntByteViewBitmap<F> = BitmapC(
    Int2ByteArray(data, width, height),
    format
)
