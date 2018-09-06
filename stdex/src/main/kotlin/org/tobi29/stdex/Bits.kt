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

package org.tobi29.stdex

import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.experimental.xor

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline infix fun Int.lrot(other: Int): Int =
    (this shl other) or (this ushr -other)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline infix fun Int.rrot(other: Int): Int =
    (this ushr other) or (this shl -other)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline infix fun Long.lrot(other: Int): Long =
    (this shl other) or (this ushr -other)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline infix fun Long.rrot(other: Int): Long =
    (this ushr other) or (this shl -other)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun bitMaskB(index: Int): Byte = (1 shl index).toByte()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun bitMaskI(index: Int): Int = 1 shl index

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun bitMaskL(index: Int): Long = 1L shl index

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Byte.maskAll(mask: Byte): Boolean = this and mask == mask

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Short.maskAll(mask: Short): Boolean = this and mask == mask

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.maskAll(mask: Int): Boolean = this and mask == mask

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Long.maskAll(mask: Long): Boolean = this and mask == mask

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Byte.maskAny(mask: Byte): Boolean = this and mask != 0.toByte()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Short.maskAny(mask: Short): Boolean = this and mask != 0.toShort()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.maskAny(mask: Int): Boolean = this and mask != 0

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Long.maskAny(mask: Long): Boolean = this and mask != 0L

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Byte.maskAt(index: Int): Boolean = maskAll(bitMaskB(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.maskAt(index: Int): Boolean = maskAll(bitMaskI(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Long.maskAt(index: Int): Boolean = maskAll(bitMaskL(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Byte.set(mask: Byte): Byte = this or mask

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.set(mask: Int): Int = this or mask

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Long.set(mask: Long): Long = this or mask

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Byte.setAt(index: Int): Byte = set(bitMaskB(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.setAt(index: Int): Int = set(bitMaskI(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Long.setAt(index: Int): Long = set(bitMaskL(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Byte.unset(mask: Byte): Byte = this and mask.inv()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.unset(mask: Int): Int = this and mask.inv()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Long.unset(mask: Long): Long = this and mask.inv()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Byte.unsetAt(index: Int): Byte = unset(bitMaskB(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.unsetAt(index: Int): Int = unset(bitMaskI(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Long.unsetAt(index: Int): Long = unset(bitMaskL(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Byte.toggle(mask: Byte): Byte = this xor mask

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.toggle(mask: Int): Int = this xor mask

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Long.toggle(mask: Long): Long = this xor mask

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Byte.toggleAt(index: Int): Byte = toggle(bitMaskB(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.toggleAt(index: Int): Int = toggle(bitMaskI(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Long.toggleAt(index: Int): Long = toggle(bitMaskL(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Byte.set(mask: Byte, value: Boolean): Byte =
    if (value) set(mask) else unset(mask)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.set(mask: Int, value: Boolean): Int =
    if (value) set(mask) else unset(mask)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Long.set(mask: Long, value: Boolean): Long =
    if (value) set(mask) else unset(mask)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Byte.setAt(index: Int, value: Boolean): Byte =
    if (value) setAt(index) else unsetAt(index)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.setAt(index: Int, value: Boolean): Int =
    if (value) setAt(index) else unsetAt(index)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Long.setAt(index: Int, value: Boolean): Long =
    if (value) setAt(index) else unsetAt(index)
