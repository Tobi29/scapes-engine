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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.utils.math

// FIXME: Workaround for https://youtrack.jetbrains.com/issue/KT-13554

infix fun Byte.or(other: Byte): Byte {
    return (this.toInt() or other.toInt()).toByte()
}

infix fun Byte.and(other: Byte): Byte {
    return (this.toInt() and other.toInt()).toByte()
}

infix fun Byte.shl(other: Byte): Byte {
    return (this.toInt() shl other.toInt()).toByte()
}

infix fun Byte.shr(other: Byte): Byte {
    return (this.toInt() shr other.toInt()).toByte()
}

infix fun Byte.ushr(other: Byte): Byte {
    return (this.toInt() ushr other.toInt()).toByte()
}

fun Byte.inv(): Byte {
    return (this.toInt().inv() and 0xFF).toByte()
}
