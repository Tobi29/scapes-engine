/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.math.vector

inline fun MutableVector2d.negate() {
    x = -x
    y = -y
}

inline fun MutableVector2d.add(a: Double) {
    x += a
    y += a
}

inline fun MutableVector2d.addX(x: Double) {
    this.x += x
}

inline fun MutableVector2d.addY(y: Double) {
    this.y += y
}

inline fun MutableVector2d.add(vector: ReadVector2d) {
    x += vector.x
    y += vector.y
}

inline fun MutableVector2d.subtract(a: Double) {
    x -= a
    y -= a
}

inline fun MutableVector2d.subtract(vector: ReadVector2d) {
    x -= vector.x
    y -= vector.y
}

inline fun MutableVector2d.multiply(a: Double) {
    x *= a
    y *= a
}

inline fun MutableVector2d.multiply(vector: ReadVector2d) {
    x *= vector.x
    y *= vector.y
}

inline fun MutableVector2d.divide(a: Double) {
    x /= a
    y /= a
}

inline fun MutableVector2d.divide(vector: ReadVector2d) {
    x /= vector.x
    y /= vector.y
}

/**
 * Normalizes the given vector so that its length is `1.0`,
 * or fill with `NaN` if the given vector is `(0, 0)`
 * @receiver The vector
 */
// TODO: Kotlin/JS Bug
/*inline*/ fun MutableVector2d.normalize() {
    val length = length()
    this.divide(length)
}

/**
 * Normalizes the given vector so that its length is `1.0`,
 * or fill with `NaN` if the given vector is `(0, 0)`
 * @receiver The vector
 */
// TODO: Kotlin/JS Bug
/*inline*/ fun MutableVector2d.normalizeSafe() {
    val length = length()
    if (length == 0.0) setXY(0.0, 0.0)
    this.divide(length)
}
