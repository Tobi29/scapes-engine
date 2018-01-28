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

package org.tobi29.utils

import org.tobi29.stdex.prefixToLength
import org.tobi29.stdex.toString

/**
 * Converts the given number to a string, forcing a certain number of digits,
 * throwing when too long
 * @param radix Radix for output format
 * @param length Number of digits to force (excluding sign)
 * @receiver Number to format
 * @throws IllegalArgumentException When number is too great
 * @return String with exact length of [length] or with leading dash when negative
 */
fun Int.toString(radix: Int = 10, length: Int): String =
    toString(radix).forceDigits(length)

/**
 * Converts the given number to a string, forcing a certain number of digits,
 * throwing when too long
 * @param radix Radix for output format
 * @param length Number of digits to force (excluding sign)
 * @receiver Number to format
 * @throws IllegalArgumentException When number is too great
 * @return String with exact length of [length] or with leading dash when negative
 */
fun Long.toString(radix: Int = 10, length: Int): String =
    toString(radix).forceDigits(length)

private fun String.forceDigits(length: Int, zero: Char = '0'): String {
    val negative = getOrNull(0) == '-'
    val str = if (negative) substring(1) else this
    val output = str.prefixToLength(zero, length, length)
    return if (negative) "-$output" else output
}
