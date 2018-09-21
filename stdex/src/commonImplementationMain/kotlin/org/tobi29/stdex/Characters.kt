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

@PublishedApi
@Constant
internal inline val MIN_HIGH_SURROGATE
    get() = '\uD800'

@PublishedApi
@Constant
internal inline val MIN_LOW_SURROGATE
    get() = '\uDC00'

@PublishedApi
@Constant
internal inline val MIN_SUPPLEMENTARY_CODE_POINT
    get() = 0x010000

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun Char.isISOControl(): Boolean =
    toInt().isISOControl()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun Codepoint.isISOControl(): Boolean =
    this <= 0x9F && (this >= 0x7F || (this.ushr(5) == 0))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun Codepoint.isBmpCodepoint(): Boolean =
    this in 0 until MIN_SUPPLEMENTARY_CODE_POINT

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun Codepoint.highSurrogate(): Char =
    ((this ushr 10) + MIN_HIGH_SURROGATE.toInt()
            - (MIN_SUPPLEMENTARY_CODE_POINT ushr 10)).toChar()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun Codepoint.lowSurrogate(): Char =
    ((this and 0x3ff) + MIN_LOW_SURROGATE.toInt()).toChar()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun surrogateCodepoint(high: Char, low: Char): Codepoint =
    (((high - MIN_HIGH_SURROGATE) shl 10)
            + (low - MIN_LOW_SURROGATE)
            + MIN_SUPPLEMENTARY_CODE_POINT)
