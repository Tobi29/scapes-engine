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

package org.tobi29.arrays

import org.tobi29.stdex.toString
import kotlin.experimental.or

/**
 * Converts a byte array into a hexadecimal string
 * @receiver Array to convert
 * @return String containing the hexadecimal data
 */
fun ByteArray.toHexadecimal(): String {
    val text = StringBuilder(size shl 1)
    for (value in this) {
        val append = (if (value < 0) value + 256 else value.toInt())
            .toString(16)
        if (append.length == 1) {
            text.append('0').append(append)
        } else {
            text.append(append)
        }
    }
    return text.toString()
}

/**
 * Converts a byte array into a hexadecimal string
 * @receiver Array to convert
 * @param groups How many bytes to group until separated by a space
 * @return String containing the hexadecimal data
 */
fun ByteArray.toHexadecimal(groups: Int): String {
    val text = StringBuilder((size shl 1) + size / groups)
    var group = 0
    val limit = size - 1
    for (i in indices) {
        val value = get(i)
        val append = (if (value < 0) value + 256 else value.toInt())
            .toString(16)
        if (append.length == 1) {
            text.append('0').append(append)
        } else {
            text.append(append)
        }
        group++
        if (group >= groups && i < limit) {
            text.append(' ')
            group = 0
        }
    }
    return text.toString()
}

/**
 * Converts a hexadecimal string to a byte array Silently discards spaces
 * @receiver String to convert
 * @return A byte array containing the data
 * @throws IllegalArgumentException Thrown in case of an invalid string
 */
fun String.fromHexadecimal(): ByteArray {
    val text = replace(" ", "")
    if (text.length and 1 == 1) {
        throw IllegalArgumentException("String has uneven length")
    }
    val array = ByteArray(text.length shr 1)
    var i = 0
    while (i < text.length) {
        val c1 = text.substring(i, i + 1).let {
            it.toByteOrNull(16)
                    ?: throw IllegalArgumentException("Invalid hex: $it")
        }
        val c2 = text.substring(i + 1, i + 2).let {
            it.toByteOrNull(16)
                    ?: throw IllegalArgumentException("Invalid hex: $it")
        }
        array[i shr 1] = (c1.toInt() shl 4).toByte() or c2
        i += 2
    }
    return array
}
