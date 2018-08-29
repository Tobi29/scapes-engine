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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.stdex

import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.experimental.xor

inline infix fun Int.lrot(other: Int): Int =
    (this shl other) or (this ushr -other)

inline infix fun Int.rrot(other: Int): Int =
    (this ushr other) or (this shl -other)

inline infix fun Long.lrot(other: Int): Long =
    (this shl other) or (this ushr -other)

inline infix fun Long.rrot(other: Int): Long =
    (this ushr other) or (this shl -other)

inline fun bitMaskB(index: Int): Byte = (1 shl index).toByte()

inline fun bitMaskI(index: Int): Int = 1 shl index

inline fun bitMaskL(index: Int): Long = 1L shl index

inline fun Byte.maskAll(mask: Byte): Boolean = this and mask == mask

inline fun Short.maskAll(mask: Short): Boolean = this and mask == mask

inline fun Int.maskAll(mask: Int): Boolean = this and mask == mask

inline fun Long.maskAll(mask: Long): Boolean = this and mask == mask

inline fun Byte.maskAny(mask: Byte): Boolean = this and mask != 0.toByte()

inline fun Short.maskAny(mask: Short): Boolean = this and mask != 0.toShort()

inline fun Int.maskAny(mask: Int): Boolean = this and mask != 0

inline fun Long.maskAny(mask: Long): Boolean = this and mask != 0L

inline fun Byte.maskAt(index: Int): Boolean = maskAll(bitMaskB(index))

inline fun Int.maskAt(index: Int): Boolean = maskAll(bitMaskI(index))

inline fun Long.maskAt(index: Int): Boolean = maskAll(bitMaskL(index))

inline fun Byte.set(mask: Byte): Byte = this or mask

inline fun Int.set(mask: Int): Int = this or mask

inline fun Long.set(mask: Long): Long = this or mask

inline fun Byte.setAt(index: Int): Byte = set(bitMaskB(index))

inline fun Int.setAt(index: Int): Int = set(bitMaskI(index))

inline fun Long.setAt(index: Int): Long = set(bitMaskL(index))

inline fun Byte.unset(mask: Byte): Byte = this and mask.inv()

inline fun Int.unset(mask: Int): Int = this and mask.inv()

inline fun Long.unset(mask: Long): Long = this and mask.inv()

inline fun Byte.unsetAt(index: Int): Byte = unset(bitMaskB(index))

inline fun Int.unsetAt(index: Int): Int = unset(bitMaskI(index))

inline fun Long.unsetAt(index: Int): Long = unset(bitMaskL(index))

inline fun Byte.toggle(mask: Byte): Byte = this xor mask

inline fun Int.toggle(mask: Int): Int = this xor mask

inline fun Long.toggle(mask: Long): Long = this xor mask

inline fun Byte.toggleAt(index: Int): Byte = toggle(bitMaskB(index))

inline fun Int.toggleAt(index: Int): Int = toggle(bitMaskI(index))

inline fun Long.toggleAt(index: Int): Long = toggle(bitMaskL(index))

inline fun Byte.set(mask: Byte, value: Boolean): Byte =
    if (value) set(mask) else unset(mask)

inline fun Int.set(mask: Int, value: Boolean): Int =
    if (value) set(mask) else unset(mask)

inline fun Long.set(mask: Long, value: Boolean): Long =
    if (value) set(mask) else unset(mask)

inline fun Byte.setAt(index: Int, value: Boolean): Byte =
    if (value) setAt(index) else unsetAt(index)

inline fun Int.setAt(index: Int, value: Boolean): Int =
    if (value) setAt(index) else unsetAt(index)

inline fun Long.setAt(index: Int, value: Boolean): Long =
    if (value) setAt(index) else unsetAt(index)
