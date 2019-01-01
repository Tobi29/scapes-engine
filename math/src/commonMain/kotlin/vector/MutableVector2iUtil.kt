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


inline fun MutableVector2i.negate() {
    x = -x
    y = -y
}

inline fun MutableVector2i.add(a: Int) {
    x += a
    y += a
}

inline fun MutableVector2i.addX(x: Int) {
    this.x += x
}

inline fun MutableVector2i.addY(y: Int) {
    this.y += y
}

inline fun MutableVector2i.add(vector: ReadVector2i) {
    x += vector.x
    y += vector.y
}

inline fun MutableVector2i.subtract(a: Int) {
    x -= a
    y -= a
}

inline fun MutableVector2i.subtract(vector: ReadVector2i) {
    x -= vector.x
    y -= vector.y
}

inline fun MutableVector2i.multiply(a: Int) {
    x *= a
    y *= a
}

inline fun MutableVector2i.multiply(vector: ReadVector2i) {
    x *= vector.x
    y *= vector.y
}

inline fun MutableVector2i.divide(a: Int) {
    x /= a
    y /= a
}

inline fun MutableVector2i.divide(vector: ReadVector2i) {
    x /= vector.x
    y /= vector.y
}
