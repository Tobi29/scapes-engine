/*
 * Copyright 2012-2016 Tobi29
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

import com.owtelse.codec.Base64
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.ByteBuffer

/**
 * Joins all values of a given array into a string
 * @param array     Array for values
 * @param delimiter Separator to put between values
 * @return A String containing the joined values
 */
fun join(vararg array: Byte,
         delimiter: String = ", "): String {
    val text = StringBuilder(array.size shl 1)
    val length = array.size - 1
    for (i in 0..length - 1) {
        text.append(array[i].toInt()).append(delimiter)
    }
    if (length >= 0) {
        text.append(array[length].toInt())
    }
    return text.toString()
}

/**
 * Joins all values of a given array into a string
 * @param array     Array for values
 * @param delimiter Separator to put between values
 * @return A String containing the joined values
 */
fun join(vararg array: Short,
         delimiter: String = ", "): String {
    val text = StringBuilder(array.size shl 1)
    val length = array.size - 1
    for (i in 0..length - 1) {
        text.append(array[i].toInt()).append(delimiter)
    }
    if (length >= 0) {
        text.append(array[length].toInt())
    }
    return text.toString()
}

/**
 * Joins all values of a given array into a string
 * @param array     Array for values
 * @param delimiter Separator to put between values
 * @return A String containing the joined values
 */
fun join(vararg array: Int,
         delimiter: String = ", "): String {
    val text = StringBuilder(array.size shl 1)
    val length = array.size - 1
    for (i in 0..length - 1) {
        text.append(array[i]).append(delimiter)
    }
    if (length >= 0) {
        text.append(array[length])
    }
    return text.toString()
}

/**
 * Joins all values of a given array into a string
 * @param array     Array for values
 * @param delimiter Separator to put between values
 * @return A String containing the joined values
 */
fun join(vararg array: Long,
         delimiter: String = ", "): String {
    val text = StringBuilder(array.size shl 1)
    val length = array.size - 1
    for (i in 0..length - 1) {
        text.append(array[i]).append(delimiter)
    }
    if (length >= 0) {
        text.append(array[length])
    }
    return text.toString()
}

/**
 * Joins all values of a given array into a string
 * @param array     Array for values
 * @param delimiter Separator to put between values
 * @return A String containing the joined values
 */
fun join(vararg array: Float,
         delimiter: String = ", "): String {
    val text = StringBuilder(array.size shl 1)
    val length = array.size - 1
    for (i in 0..length - 1) {
        text.append(array[i]).append(delimiter)
    }
    if (length >= 0) {
        text.append(array[length])
    }
    return text.toString()
}

/**
 * Joins all values of a given array into a string
 * @param array     Array for values
 * @param delimiter Separator to put between values
 * @return A String containing the joined values
 */
fun join(vararg array: Double,
         delimiter: String = ", "): String {
    val text = StringBuilder(array.size shl 1)
    val length = array.size - 1
    for (i in 0..length - 1) {
        text.append(array[i]).append(delimiter)
    }
    if (length >= 0) {
        text.append(array[length])
    }
    return text.toString()
}

/**
 * Joins all values of a given array into a string
 * @param array     Array for values
 * @param delimiter Separator to put between values
 * @return A String containing the joined values
 */
fun join(vararg array: Any,
         delimiter: String = ", "): String {
    val text = StringBuilder(array.size shl 1)
    val length = array.size - 1
    for (i in 0..length - 1) {
        text.append(array[i]).append(delimiter)
    }
    if (length >= 0) {
        text.append(array[length])
    }
    return text.toString()
}

/**
 * Converts a byte array into a hexadecimal string
 * @receiver Array to convert
 * @return String containing the hexadecimal data
 */
fun ByteArray.toHexadecimal(): String {
    val text = StringBuilder(size shl 1)
    for (value in this) {
        val append = Integer.toHexString(
                if (value < 0) value + 256 else value.toInt())
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
        val append = Integer.toHexString(
                if (value < 0) value + 256 else value.toInt())
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
 * @throws IOException Thrown in case of an invalid string
 */
@Throws(IOException::class)
fun String.fromHexadecimal(): ByteArray {
    try {
        val text = replace(" ", "")
        if (text.length and 1 == 1) {
            throw IOException("String has uneven length")
        }
        val array = ByteArray(text.length shr 1)
        var i = 0
        while (i < text.length) {
            array[i shr 1] = Integer.parseInt(text.substring(i, i + 2),
                    16).toByte()
            i += 2
        }
        return array
    } catch (e: NumberFormatException) {
        throw IOException(e)
    }

}

/**
 * Converts a byte array to a Base64 string
 * @receiver Array to convert
 * @return String containing the data
 */
fun ByteArray.toBase64(): String {
    try {
        return Base64.encode(this)
    } catch (e: UnsupportedEncodingException) {
        throw UnsupportedJVMException(e)
    }

}

/**
 * Converts a Base64 string to a byte array
 * @receiver String to convert
 * @return Byte array containing the data
 * @throws IOException When an invalid base64 was given
 */
fun String.fromBase64(): ByteArray {
    try {
        return Base64.decode(this)
    } catch (e: IllegalArgumentException) {
        throw IOException(e)
    } catch (e: UnsupportedEncodingException) {
        throw IOException(e)
    }

}

/**
 * Fills the given array with values
 * @receiver Array to fill
 * @param supplier Supplier called for each value written to the array
 */
inline fun ByteArray.fill(supplier: (Int) -> Byte) {
    for (i in indices) {
        set(i, supplier(i))
    }
}

/**
 * Fills the given array with values
 * @receiver Array to fill
 * @param supplier Supplier called for each value written to the array
 */
inline fun ShortArray.fill(supplier: (Int) -> Short) {
    for (i in indices) {
        set(i, supplier(i))
    }
}

/**
 * Fills the given array with values
 * @receiver Array to fill
 * @param supplier Supplier called for each value written to the array
 */
inline fun IntArray.fill(supplier: (Int) -> Int) {
    for (i in indices) {
        set(i, supplier(i))
    }
}

/**
 * Fills the given array with values
 * @receiver Array to fill
 * @param supplier Supplier called for each value written to the array
 */
inline fun LongArray.fill(supplier: (Int) -> Long) {
    for (i in indices) {
        set(i, supplier(i))
    }
}

/**
 * Fills the given array with values
 * @receiver Array to fill
 * @param supplier Supplier called for each value written to the array
 */
inline fun FloatArray.fill(supplier: (Int) -> Float) {
    for (i in indices) {
        set(i, supplier(i))
    }
}

/**
 * Fills the given array with values
 * @receiver Array to fill
 * @param supplier Supplier called for each value written to the array
 */
inline fun DoubleArray.fill(supplier: (Int) -> Double) {
    for (i in indices) {
        set(i, supplier(i))
    }
}

/**
 * Fills the given array with values
 * @receiver Array to fill
 * @param supplier Supplier called for each value written to the array
 * @param E type
 */
inline fun <E> Array<in E>.fill(supplier: (Int) -> E) {
    for (i in indices) {
        set(i, supplier(i))
    }
}

/**
 * Fills a buffer with the given value
 * @receiver Buffer to fill
 * @param supplier Supplier called for each value written to the buffer
 * @return The given buffer
 */
inline fun ByteBuffer.fill(supplier: () -> Byte): ByteBuffer {
    while (hasRemaining()) {
        put(supplier())
    }
    return this
}
