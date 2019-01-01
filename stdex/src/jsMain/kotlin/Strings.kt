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

package org.tobi29.stdex

import org.khronos.webgl.BufferDataSource
import org.khronos.webgl.Uint8Array

@PublishedApi
internal actual fun ByteArray.utf8ToStringImpl(
    offset: Int,
    size: Int
): String = TextDecoder().decode(asTypedArray().let {
    Uint8Array(it.buffer, it.byteOffset + offset, it.byteLength)
})

@PublishedApi
internal actual fun String.utf8ToArrayImpl(
    destination: ByteArray?,
    offset: Int,
    size: Int
): ByteArray = TextEncoder().encode(this).asInt8Array().asArray().let {
    if (destination == null && offset == 0
        && (size < 0 || size == it.size)) it
    else {
        val array = destination ?: ByteArray(
            offset + if (size < 0) it.size else size
        )
        copy(it, array, if (size < 0) it.size else size.coerceAtMost(it.size))
        array
    }
}

@PublishedApi
internal actual fun String.copyToArrayImpl(
    destination: CharArray,
    offset: Int,
    startIndex: Int,
    endIndex: Int
): CharArray {
    if (startIndex < 0 || endIndex >= length || endIndex < startIndex
        || offset < 0 || offset + (endIndex - startIndex) >= destination.size)
        throw IndexOutOfBoundsException("Invalid offset or indices")
    val destOffset = offset - startIndex
    for (i in startIndex until endIndex) {
        destination[i + destOffset] = this[i]
    }
    return destination
}

private external class TextDecodeOptions {
    var stream: Boolean
}

private external class TextDecoder {
    val encoding: String
    fun decode(
        input: BufferDataSource? = definedExternally,
        options: TextDecodeOptions? = definedExternally
    ): String
}

private external class TextEncoder(
    label: String? = definedExternally,
    options: TextDecodeOptions? = definedExternally
) {
    val encoding: String
    fun encode(input: String? = definedExternally): Uint8Array
}

@Suppress("NOTHING_TO_INLINE")
private inline fun TextDecodeOptions(stream: Boolean): dynamic {
    val options = object {}.asDynamic()
    options.stream = stream
    return options
}
