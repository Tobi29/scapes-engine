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

package org.tobi29.scapes.engine.backends.stb.font

expect class STBRPRect
expect fun STBRPRect.close()

expect class STBRPRectBuffer
expect val STBRPRectBuffer.size: Int
expect operator fun STBRPRectBuffer.get(index: Int): STBRPRect

expect class STBTTAlignedQuad
expect fun STBTTAlignedQuad.close()

expect class STBTTBakedChar
expect fun STBTTBakedChar.close()

expect class STBTTBakedCharBuffer
expect val STBTTBakedCharBuffer.size: Int
expect operator fun STBTTBakedCharBuffer.get(index: Int): STBTTBakedChar

expect class STBTTBitmap
expect fun STBTTBitmap.close()

expect class STBTTFontinfo
expect fun STBTTFontinfo(): STBTTFontinfo
expect fun STBTTFontinfo.close()

expect class STBTTPackContext
expect fun STBTTPackContext.close()

expect class STBTTPackedchar
expect fun STBTTPackedchar.close()

expect class STBTTPackedcharBuffer
expect val STBTTPackedcharBuffer.size: Int
expect operator fun STBTTPackedcharBuffer.get(index: Int): STBTTPackedchar

expect class STBTTPackRange
expect fun STBTTPackRange.close()

expect class STBTTPackRangeBuffer
expect val STBTTPackRangeBuffer.size: Int
expect operator fun STBTTPackRangeBuffer.get(index: Int): STBTTPackRange

expect class STBTTVertex
expect fun STBTTVertex.close()

expect class STBTTVertexBuffer
expect val STBTTVertexBuffer.size: Int
expect operator fun STBTTVertexBuffer.get(index: Int): STBTTVertex
