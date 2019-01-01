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

inline fun MutableVector3i.negate() {
    x = -x
    y = -y
    z = -z
}

inline fun MutableVector3i.add(a: Int) {
    x += a
    y += a
    z += a
}

inline fun MutableVector3i.addX(x: Int) {
    this.x += x
}

inline fun MutableVector3i.addY(y: Int) {
    this.y += y
}

inline fun MutableVector3i.addZ(z: Int) {
    this.z += z
}

inline fun MutableVector3i.add(vector: ReadVector3i) {
    x += vector.x
    y += vector.y
    z += vector.z
}

inline fun MutableVector3i.subtract(a: Int) {
    x -= a
    y -= a
    z -= a
}

inline fun MutableVector3i.subtract(vector: ReadVector3i) {
    x -= vector.x
    y -= vector.y
    z -= vector.z
}

inline fun MutableVector3i.multiply(a: Int) {
    x *= a
    y *= a
    z *= a
}

inline fun MutableVector3i.multiply(vector: ReadVector3i) {
    x *= vector.x
    y *= vector.y
    z *= vector.z
}

inline fun MutableVector3i.divide(a: Int) {
    x /= a
    y /= a
    z /= a
}

inline fun MutableVector3i.divide(vector: ReadVector3i) {
    x /= vector.x
    y /= vector.y
    z /= vector.z
}
