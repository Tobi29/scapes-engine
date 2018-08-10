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

package org.tobi29.scapes.engine.backends.stb.font

actual typealias STBRPRect = org.lwjgl.stb.STBRPRect

actual inline fun STBRPRect.close() = close()

actual typealias STBRPRectBuffer = org.lwjgl.stb.STBRPRect.Buffer

actual inline val STBRPRectBuffer.size get() = remaining()
actual inline operator fun STBRPRectBuffer.get(index: Int) =
    get(position() + index)

actual typealias STBTTAlignedQuad = org.lwjgl.stb.STBTTAlignedQuad

actual inline fun STBTTAlignedQuad.close() = close()

actual typealias STBTTBakedChar = org.lwjgl.stb.STBTTBakedChar

actual inline fun STBTTBakedChar.close() = close()

actual typealias STBTTBakedCharBuffer = org.lwjgl.stb.STBTTBakedChar.Buffer

actual inline val STBTTBakedCharBuffer.size get() = remaining()
actual inline operator fun STBTTBakedCharBuffer.get(index: Int) =
    get(position() + index)

actual typealias STBTTBitmap = org.lwjgl.stb.STBTTBitmap

actual inline fun STBTTBitmap.close() = close()

actual typealias STBTTFontinfo = org.lwjgl.stb.STBTTFontinfo

actual inline fun STBTTFontinfo() = STBTTFontinfo.malloc()

actual inline fun STBTTFontinfo.close() = close()

actual typealias STBTTPackContext = org.lwjgl.stb.STBTTPackContext

actual inline fun STBTTPackContext.close() = close()

actual typealias STBTTPackedchar = org.lwjgl.stb.STBTTPackedchar

actual inline fun STBTTPackedchar.close() = close()

actual typealias STBTTPackedcharBuffer = org.lwjgl.stb.STBTTPackedchar.Buffer

actual inline val STBTTPackedcharBuffer.size get() = remaining()
actual inline operator fun STBTTPackedcharBuffer.get(index: Int) =
    get(position() + index)

actual typealias STBTTPackRange = org.lwjgl.stb.STBTTPackRange

actual inline fun STBTTPackRange.close() = close()

actual typealias STBTTPackRangeBuffer = org.lwjgl.stb.STBTTPackRange.Buffer

actual inline val STBTTPackRangeBuffer.size get() = remaining()
actual inline operator fun STBTTPackRangeBuffer.get(index: Int) =
    get(position() + index)

actual typealias STBTTVertex = org.lwjgl.stb.STBTTVertex

actual inline fun STBTTVertex.close() = close()

actual typealias STBTTVertexBuffer = org.lwjgl.stb.STBTTVertex.Buffer

actual inline val STBTTVertexBuffer.size get() = remaining()
actual inline operator fun STBTTVertexBuffer.get(index: Int) =
    get(position() + index)
