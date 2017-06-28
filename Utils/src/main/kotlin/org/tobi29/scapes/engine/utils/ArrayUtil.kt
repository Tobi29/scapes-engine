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

package org.tobi29.scapes.engine.utils

/*
/**
 * Converts a byte array into a hexadecimal string
 * @receiver Array to convert
 * @return String containing the hexadecimal data
 */
header fun ByteArray.toHexadecimal(): String

/**
 * Converts a byte array into a hexadecimal string
 * @receiver Array to convert
 * @param groups How many bytes to group until separated by a space
 * @return String containing the hexadecimal data
 */
header fun ByteArray.toHexadecimal(groups: Int): String

/**
 * Converts a hexadecimal string to a byte array Silently discards spaces
 * @receiver String to convert
 * @return A byte array containing the data
 * @throws IllegalArgumentException Thrown in case of an invalid string
 */
header fun String.fromHexadecimal(): ByteArray

/**
 * Converts a byte array to a Base64 string
 * @receiver Array to convert
 * @return String containing the data
 */
header fun ByteArray.toBase64(): String

/**
 * Converts a Base64 string to a byte array
 * @receiver String to convert
 * @return Byte array containing the data
 * @throws IllegalArgumentException When an invalid base64 was given
 */
header fun String.fromBase64(): ByteArray
*/
