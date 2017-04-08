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

import com.owtelse.codec.Base64
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.ByteBuffer
import java.util.*

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
inline fun BooleanArray.fill(supplier: (Int) -> Boolean) {
    for (i in indices) {
        set(i, supplier(i))
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
 */
inline fun CharArray.fill(supplier: (Int) -> Char) {
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

/**
 * Check if the given array equals [other]
 * @receiver The first array
 * @param other The second array
 * @return `true` the size is equal and all entries are
 */
inline infix fun BooleanArray.equals(other: BooleanArray) =
        Arrays.equals(this, other)

/**
 * Check if the given array equals [other]
 * @receiver The first array
 * @param other The second array
 * @return `true` the size is equal and all entries are
 */
inline infix fun ByteArray.equals(other: ByteArray) =
        Arrays.equals(this, other)

/**
 * Check if the given array equals [other]
 * @receiver The first array
 * @param other The second array
 * @return `true` the size is equal and all entries are
 */
inline infix fun ShortArray.equals(other: ShortArray) =
        Arrays.equals(this, other)

/**
 * Check if the given array equals [other]
 * @receiver The first array
 * @param other The second array
 * @return `true` the size is equal and all entries are
 */
inline infix fun IntArray.equals(other: IntArray) =
        Arrays.equals(this, other)

/**
 * Check if the given array equals [other]
 * @receiver The first array
 * @param other The second array
 * @return `true` the size is equal and all entries are
 */
inline infix fun LongArray.equals(other: LongArray) =
        Arrays.equals(this, other)

/**
 * Check if the given array equals [other]
 * @receiver The first array
 * @param other The second array
 * @return `true` the size is equal and all entries are
 */
inline infix fun FloatArray.equals(other: FloatArray) =
        Arrays.equals(this, other)

/**
 * Check if the given array equals [other]
 * @receiver The first array
 * @param other The second array
 * @return `true` the size is equal and all entries are
 */
inline infix fun DoubleArray.equals(other: DoubleArray) =
        Arrays.equals(this, other)

/**
 * Check if the given array equals [other]
 * @receiver The first array
 * @param other The second array
 * @return `true` the size is equal and all entries are
 */
inline infix fun CharArray.equals(other: CharArray) =
        Arrays.equals(this, other)

/**
 * Check if the given array equals [other]
 * @receiver The first array
 * @param other The second array
 * @return `true` the size is equal and all entries are
 */
inline infix fun <E> Array<in E>.equals(other: Array<in E>) =
        Arrays.equals(this, other)

/**
 * Check if the given array equals [other]
 * @receiver The first array
 * @param other The second array
 * @return `true` the size is equal and all entries are
 */
inline infix fun <E> Array<in E>.deepEquals(other: Array<in E>) =
        Arrays.deepEquals(this, other)

/**
 * Calculate a hash code for the given array
 * @receiver The array
 * @return A hash code computes alike a list
 */
inline fun BooleanArray.arrayHashCode() = Arrays.hashCode(this)

/**
 * Calculate a hash code for the given array
 * @receiver The array
 * @return A hash code computes alike a list
 */
inline fun ByteArray.arrayHashCode() = Arrays.hashCode(this)

/**
 * Calculate a hash code for the given array
 * @receiver The array
 * @return A hash code computes alike a list
 */
inline fun ShortArray.arrayHashCode() = Arrays.hashCode(this)

/**
 * Calculate a hash code for the given array
 * @receiver The array
 * @return A hash code computes alike a list
 */
inline fun IntArray.arrayHashCode() = Arrays.hashCode(this)

/**
 * Calculate a hash code for the given array
 * @receiver The array
 * @return A hash code computes alike a list
 */
inline fun LongArray.arrayHashCode() = Arrays.hashCode(this)

/**
 * Calculate a hash code for the given array
 * @receiver The array
 * @return A hash code computes alike a list
 */
inline fun FloatArray.arrayHashCode() = Arrays.hashCode(this)

/**
 * Calculate a hash code for the given array
 * @receiver The array
 * @return A hash code computes alike a list
 */
inline fun DoubleArray.arrayHashCode() = Arrays.hashCode(this)

/**
 * Calculate a hash code for the given array
 * @receiver The array
 * @return A hash code computes alike a list
 */
inline fun CharArray.arrayHashCode() = Arrays.hashCode(this)

/**
 * Calculate a hash code for the given array
 * @receiver The array
 * @return A hash code computes alike a list
 */
inline fun <E> Array<in E>.arrayHashCode() = Arrays.hashCode(this)

/**
 * Calculate a hash code for the given array
 * @receiver The array
 * @return A hash code computes alike a list
 */
inline fun <E> Array<in E>.deepArrayHashCode() = Arrays.deepHashCode(this)
