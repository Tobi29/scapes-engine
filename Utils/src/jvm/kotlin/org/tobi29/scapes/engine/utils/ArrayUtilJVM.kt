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
import java.io.UnsupportedEncodingException
import java.util.*
import kotlin.experimental.or

/* impl */fun ByteArray.toHexadecimal(): String {
    val text = StringBuilder(size shl 1)
    for (value in this) {
        val append = (if (value < 0) value + 256 else value.toInt()).toString(
                16)
        if (append.length == 1) {
            text.append('0').append(append)
        } else {
            text.append(append)
        }
    }
    return text.toString()
}

/* impl */fun ByteArray.toHexadecimal(groups: Int): String {
    val text = StringBuilder((size shl 1) + size / groups)
    var group = 0
    val limit = size - 1
    for (i in indices) {
        val value = get(i)
        val append = (if (value < 0) value + 256 else value.toInt()).toString(
                16)
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

/* impl */fun String.fromHexadecimal(): ByteArray {
    try {
        val text = replace(" ", "")
        if (text.length and 1 == 1) {
            throw IllegalArgumentException("String has uneven length")
        }
        val array = ByteArray(text.length shr 1)
        var i = 0
        while (i < text.length) {
            array[i shr 1] = (text.substring(i, i + 1).toByte(
                    16).toInt() shl 4).toByte() or
                    text.substring(i + 1, i + 2).toByte(16)
            i += 2
        }
        return array
    } catch (e: NumberFormatException) {
        throw IllegalArgumentException(e)
    }
}

/* impl */fun ByteArray.toBase64(): String {
    try {
        return Base64.encode(this)
    } catch (e: UnsupportedEncodingException) {
        throw UnsupportedJVMException(e)
    }
}

/* impl */fun String.fromBase64(): ByteArray {
    try {
        return Base64.decode(this)
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException(e)
    } catch (e: UnsupportedEncodingException) {
        throw IllegalArgumentException(e)
    }
}

/* impl */inline fun copyArray(src: BooleanArray,
                               dest: BooleanArray,
                               length: Int,
                               offsetSrc: Int,
                               offsetDest: Int): Unit =
        System.arraycopy(src, offsetSrc, dest, offsetDest, length)

/* impl */inline fun copyArray(src: ByteArray,
                               dest: ByteArray,
                               length: Int,
                               offsetSrc: Int,
                               offsetDest: Int): Unit =
        System.arraycopy(src, offsetSrc, dest, offsetDest, length)

/* impl */inline fun copyArray(src: ShortArray,
                               dest: ShortArray,
                               length: Int,
                               offsetSrc: Int,
                               offsetDest: Int): Unit =
        System.arraycopy(src, offsetSrc, dest, offsetDest, length)

/* impl */inline fun copyArray(src: IntArray,
                               dest: IntArray,
                               length: Int,
                               offsetSrc: Int,
                               offsetDest: Int): Unit =
        System.arraycopy(src, offsetSrc, dest, offsetDest, length)

/* impl */inline fun copyArray(src: LongArray,
                               dest: LongArray,
                               length: Int,
                               offsetSrc: Int,
                               offsetDest: Int): Unit =
        System.arraycopy(src, offsetSrc, dest, offsetDest, length)

/* impl */inline fun copyArray(src: FloatArray,
                               dest: FloatArray,
                               length: Int,
                               offsetSrc: Int,
                               offsetDest: Int): Unit =
        System.arraycopy(src, offsetSrc, dest, offsetDest, length)

/* impl */inline fun copyArray(src: DoubleArray,
                               dest: DoubleArray,
                               length: Int,
                               offsetSrc: Int,
                               offsetDest: Int): Unit =
        System.arraycopy(src, offsetSrc, dest, offsetDest, length)

/* impl */inline fun copyArray(src: CharArray,
                               dest: CharArray,
                               length: Int,
                               offsetSrc: Int,
                               offsetDest: Int): Unit =
        System.arraycopy(src, offsetSrc, dest, offsetDest, length)

/* impl */inline fun <T> copyArray(src: Array<out T>,
                                   dest: Array<in T>,
                                   length: Int,
                                   offsetSrc: Int,
                                   offsetDest: Int): Unit =
        System.arraycopy(src, offsetSrc, dest, offsetDest, length)
