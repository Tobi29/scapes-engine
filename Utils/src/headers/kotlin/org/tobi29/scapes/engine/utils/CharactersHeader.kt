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

package org.tobi29.scapes.engine.utils

/**
 * Checks if the given character is an ISO control character
 * @receiver The character to check
 * @return `true` if the given character is an ISO control character
 */
expect fun Char.isISOControl(): Boolean

/**
 * Checks if the given codepoint is an ISO control character
 * @receiver The codepoint to check
 * @return `true` if the given codepoint is an ISO control character
 */
expect fun Codepoint.isISOControl(): Boolean

/**
 * Checks if the given codepoint can be represented in a [Char]
 *
 * **Note:** Whenever casting an [Int] (especially if declared using
 * [Codepoint]) consider checking with this method first
 * @receiver The codepoint to check
 * @return `tru` if the given codepoint can be represented in a [Char]
 */
expect fun Codepoint.isBmpCodepoint(): Boolean

/**
 * Retrieve the high surrogate character from the given codepoint
 *
 * **Note:** This may return any [Char] if the given codepoint is not surrogate
 * @receiver The codepoint to read
 * @return The high surrogate character from the given codepoint
 */
expect fun Codepoint.highSurrogate(): Char

/**
 * Retrieve the low surrogate character from the given codepoint
 *
 * **Note:** This may return any [Char] if the given codepoint is not surrogate
 * @receiver The codepoint to read
 * @return The low surrogate character from the given codepoint
 */
expect fun Codepoint.lowSurrogate(): Char

/**
 * Combine the given surrogate characters into a codepoint
 *
 * **Note:** This may return any [Char] if the given characters are not a
 * surrogate pair
 * @param high High surrogate
 * @param low Low surrogate
 * @return Codepoint for the given surrogate characters
 */
expect fun surrogateCodepoint(high: Char,
                              low: Char): Codepoint
