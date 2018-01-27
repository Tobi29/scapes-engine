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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.stdex

actual inline fun Char.isISOControl(): Boolean =
    java.lang.Character.isISOControl(this)

actual inline fun Codepoint.isISOControl(): Boolean =
    java.lang.Character.isISOControl(this)

actual inline fun Codepoint.isBmpCodepoint(): Boolean =
    java.lang.Character.isBmpCodePoint(this)

actual inline fun Codepoint.highSurrogate(): Char =
    java.lang.Character.highSurrogate(this)

actual inline fun Codepoint.lowSurrogate(): Char =
    java.lang.Character.lowSurrogate(this)

actual inline fun surrogateCodepoint(high: Char, low: Char): Codepoint =
    java.lang.Character.toCodePoint(high, low)
