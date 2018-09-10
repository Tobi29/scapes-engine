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

package org.tobi29.stdex.math

import org.tobi29.stdex.InlineUtility
import kotlin.math.pow

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun cbrt(value: Float) = value.pow(1.0f / 3.0f)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun cbrt(value: Double) = value.pow(1.0 / 3.0)

actual fun Float.floorToInt(): Int = toInt().let { int ->
    if (this >= 0.0f || int.toFloat() == this
        || int == Int.MIN_VALUE) int else int - 1
}

actual fun Float.floorToLong(): Long = toLong().let { int ->
    if (this >= 0.0f || int.toFloat() == this
        || int == Long.MIN_VALUE) int else int - 1L
}

actual fun Double.floorToInt(): Int = toInt().let { int ->
    if (this >= 0.0 || int.toDouble() == this
        || int == Int.MIN_VALUE) int else int - 1
}

actual fun Double.floorToLong(): Long = toLong().let { int ->
    if (this >= 0.0 || int.toDouble() == this
        || int == Long.MIN_VALUE) int else int - 1L
}

actual fun Float.ceilToInt(): Int = toInt().let { int ->
    if (this <= 0.0 || int.toFloat() == this
        || int == Int.MAX_VALUE) int else int + 1
}

actual fun Float.ceilToLong(): Long = toLong().let { int ->
    if (this <= 0.0 || int.toFloat() == this
        || int == Long.MAX_VALUE) int else int + 1L
}

actual fun Double.ceilToInt(): Int = toInt().let { int ->
    if (this <= 0.0 || int.toDouble() == this
        || int == Int.MAX_VALUE) int else int + 1
}

actual fun Double.ceilToLong(): Long = toLong().let { int ->
    if (this <= 0.0 || int.toDouble() == this
        || int == Long.MAX_VALUE) int else int + 1L
}
