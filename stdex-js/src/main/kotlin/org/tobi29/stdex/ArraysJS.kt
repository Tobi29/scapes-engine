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

@InlineUtility
@Suppress("NOTHING_TO_INLINE", "UnsafeCastFromDynamic")
inline fun ByteArray.asTypedArray(): Int8Array = asDynamic()

@InlineUtility
@Suppress("NOTHING_TO_INLINE", "UnsafeCastFromDynamic")
inline fun ShortArray.asTypedArray(): Int16Array = asDynamic()

@InlineUtility
@Suppress("NOTHING_TO_INLINE", "UnsafeCastFromDynamic")
inline fun CharArray.asTypedArray(): Uint16Array = asDynamic()

@InlineUtility
@Suppress("NOTHING_TO_INLINE", "UnsafeCastFromDynamic")
inline fun IntArray.asTypedArray(): Int32Array = asDynamic()

@InlineUtility
@Suppress("NOTHING_TO_INLINE", "UnsafeCastFromDynamic")
inline fun FloatArray.asTypedArray(): Float32Array = asDynamic()

@InlineUtility
@Suppress("NOTHING_TO_INLINE", "UnsafeCastFromDynamic")
inline fun DoubleArray.asTypedArray(): Float64Array = asDynamic()

@InlineUtility
@Suppress("NOTHING_TO_INLINE", "UnsafeCastFromDynamic")
inline fun ByteArray.asUnsignedTypedArray(): Uint8Array =
    asTypedArray().asUint8Array()

@InlineUtility
@Suppress("NOTHING_TO_INLINE", "UnsafeCastFromDynamic")
inline fun ShortArray.asUnsignedTypedArray(): Uint16Array =
    asTypedArray().asUint8Array()

@InlineUtility
@Suppress("NOTHING_TO_INLINE", "UnsafeCastFromDynamic")
inline fun IntArray.asUnsignedTypedArray(): Uint32Array =
    asTypedArray().asUint32Array()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int8Array.asUint8Array(): Uint8Array =
    Uint8Array(buffer, byteOffset, length)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Uint8Array.asInt8Array(): Int8Array =
    Int8Array(buffer, byteOffset, length)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int16Array.asUint8Array(): Uint16Array =
    Uint16Array(buffer, byteOffset, length)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Uint16Array.asInt16Array(): Int16Array =
    Int16Array(buffer, byteOffset, length)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int32Array.asUint32Array(): Uint32Array =
    Uint32Array(buffer, byteOffset, length)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Uint32Array.asInt32Array(): Int32Array =
    Int32Array(buffer, byteOffset, length)

@InlineUtility
@Suppress("NOTHING_TO_INLINE", "UnsafeCastFromDynamic")
inline fun Int8Array.asArray(): ByteArray = asDynamic()

@InlineUtility
@Suppress("NOTHING_TO_INLINE", "UnsafeCastFromDynamic")
inline fun Int16Array.asArray(): ShortArray = asDynamic()

@InlineUtility
@Suppress("NOTHING_TO_INLINE", "UnsafeCastFromDynamic")
inline fun Uint16Array.asArray(): CharArray = asDynamic()

@InlineUtility
@Suppress("NOTHING_TO_INLINE", "UnsafeCastFromDynamic")
inline fun Int32Array.asArray(): IntArray = asDynamic()

@InlineUtility
@Suppress("NOTHING_TO_INLINE", "UnsafeCastFromDynamic")
inline fun Float32Array.asArray(): FloatArray = asDynamic()

@InlineUtility
@Suppress("NOTHING_TO_INLINE", "UnsafeCastFromDynamic")
inline fun Float64Array.asArray(): DoubleArray = asDynamic()

@PublishedApi
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
internal actual inline fun copyArray(
    src: BooleanArray,
    dest: BooleanArray,
    length: Int,
    offsetSrc: Int,
    offsetDest: Int
) = copyElements(src, dest, length, offsetSrc, offsetDest)

@PublishedApi
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
internal actual inline fun copyArray(
    src: ByteArray,
    dest: ByteArray,
    length: Int,
    offsetSrc: Int,
    offsetDest: Int
) {
    checkBounds(src.size, dest.size, length, offsetSrc, offsetDest)
    dest.asTypedArray().set(
        src.asTypedArray().subarray(offsetSrc, offsetSrc + length),
        offsetDest
    )
}

@PublishedApi
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
internal actual inline fun copyArray(
    src: ShortArray,
    dest: ShortArray,
    length: Int,
    offsetSrc: Int,
    offsetDest: Int
) {
    checkBounds(src.size, dest.size, length, offsetSrc, offsetDest)
    dest.asTypedArray().set(
        src.asTypedArray().subarray(offsetSrc, offsetSrc + length),
        offsetDest
    )
}

@PublishedApi
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
internal actual inline fun copyArray(
    src: IntArray,
    dest: IntArray,
    length: Int,
    offsetSrc: Int,
    offsetDest: Int
) {
    checkBounds(src.size, dest.size, length, offsetSrc, offsetDest)
    dest.asTypedArray().set(
        src.asTypedArray().subarray(offsetSrc, offsetSrc + length),
        offsetDest
    )
}

@PublishedApi
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
internal actual inline fun copyArray(
    src: LongArray,
    dest: LongArray,
    length: Int,
    offsetSrc: Int,
    offsetDest: Int
) = copyElements(src, dest, length, offsetSrc, offsetDest)

@PublishedApi
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
internal actual inline fun copyArray(
    src: FloatArray,
    dest: FloatArray,
    length: Int,
    offsetSrc: Int,
    offsetDest: Int
) {
    checkBounds(src.size, dest.size, length, offsetSrc, offsetDest)
    dest.asTypedArray().set(
        src.asTypedArray().subarray(offsetSrc, offsetSrc + length),
        offsetDest
    )
}

@PublishedApi
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
internal actual inline fun copyArray(
    src: DoubleArray,
    dest: DoubleArray,
    length: Int,
    offsetSrc: Int,
    offsetDest: Int
) {
    checkBounds(src.size, dest.size, length, offsetSrc, offsetDest)
    dest.asTypedArray().set(
        src.asTypedArray().subarray(offsetSrc, offsetSrc + length),
        offsetDest
    )
}

@PublishedApi
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
internal actual inline fun copyArray(
    src: CharArray,
    dest: CharArray,
    length: Int,
    offsetSrc: Int,
    offsetDest: Int
) {
    checkBounds(src.size, dest.size, length, offsetSrc, offsetDest)
    dest.asTypedArray().set(
        src.asTypedArray().subarray(offsetSrc, offsetSrc + length),
        offsetDest
    )
}

@PublishedApi
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
internal actual inline fun <T> copyArray(
    src: Array<out T>,
    dest: Array<in T>,
    length: Int,
    offsetSrc: Int,
    offsetDest: Int
) = copyElements(src, dest, length, offsetSrc, offsetDest)

@PublishedApi
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
internal inline fun checkBounds(
    sizeSrc: Int,
    sizeDest: Int,
    length: Int,
    offsetSrc: Int,
    offsetDest: Int
) {
    if (length < 0 || offsetSrc < 0 || offsetDest < 0
        || (offsetSrc + length > sizeSrc)
        || (offsetDest + length > sizeDest))
        throw IndexOutOfBoundsException("Invalid copy bounds")
}

@PublishedApi
@Suppress("UnsafeCastFromDynamic")
internal fun copyElements(
    src: dynamic,
    dest: dynamic,
    length: Int,
    offsetSrc: Int,
    offsetDest: Int
) {
    checkBounds(src.length, dest.length, length, offsetSrc, offsetDest)
    if (src === dest) { // Is it ever possible to share data of non-typed arrays?
        src.copyWithin(offsetDest, offsetSrc, offsetSrc + length)
    } else {
        for (i in 0 until length) {
            dest[i + offsetDest] = src[i + offsetSrc]
        }
    }
}
