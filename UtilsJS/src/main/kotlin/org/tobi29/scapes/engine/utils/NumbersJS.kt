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

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.DataView

private val convertArray = ThreadLocal { DataView(ArrayBuffer(8)) }

impl fun Float.bits() = convertArray.get().let { array ->
    array.setFloat32(0, this)
    array.getInt32(0)
}

impl fun Double.bits() = convertArray.get().let { array ->
    // Using little-endian as most machines use it
    array.setFloat64(0, this, true)
    val a = array.getInt32(0, true).toLong()
    val b = array.getInt32(4, true).toLong()
    (b shl 32) + (a and 0xFFFFFFFF)
}

impl fun Int.bitsToFloat() = convertArray.get().let { array ->
    array.setInt32(0, this)
    array.getFloat32(0)
}

impl fun Long.bitsToDouble() = convertArray.get().let { array ->
    // Using little-endian as most machines use it
    array.setInt32(0, this.toInt(), true)
    array.setInt32(4, (this ushr 32).toInt(), true)
    array.getFloat64(0, true)
}

@Suppress("UnsafeCastFromDynamic")
impl inline fun Int.toString(radix: Int): String =
        asDynamic().toString(radix)

@Suppress("UnsafeCastFromDynamic")
impl inline fun Long.toString(radix: Int): String =
        asDynamic().toString(radix)
