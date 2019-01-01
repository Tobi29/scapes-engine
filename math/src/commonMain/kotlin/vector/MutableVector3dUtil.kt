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

inline fun MutableVector3d.negate() {
    x = -x
    y = -y
    z = -z
}

inline fun MutableVector3d.add(a: Double) {
    x += a
    y += a
    z += a
}

inline fun MutableVector3d.addX(x: Double) {
    this.x += x
}

inline fun MutableVector3d.addY(y: Double) {
    this.y += y
}

inline fun MutableVector3d.addZ(z: Double) {
    this.z += z
}

inline fun MutableVector3d.add(vector: ReadVector3d) {
    x += vector.x
    y += vector.y
    z += vector.z
}

inline fun MutableVector3d.subtract(a: Double) {
    x -= a
    y -= a
    z -= a
}

inline fun MutableVector3d.subtract(vector: ReadVector3d) {
    x -= vector.x
    y -= vector.y
    z -= vector.z
}

inline fun MutableVector3d.multiply(a: Double) {
    x *= a
    y *= a
    z *= a
}

inline fun MutableVector3d.multiply(vector: ReadVector3d) {
    x *= vector.x
    y *= vector.y
    z *= vector.z
}

inline fun MutableVector3d.divide(a: Double) {
    x /= a
    y /= a
    z /= a
}

inline fun MutableVector3d.divide(vector: ReadVector3d) {
    x /= vector.x
    y /= vector.y
    z /= vector.z
}

/**
 * Computes the cross product of the given vectors
 * @receiver The first vector
 * @param other The second vector
 * @param output The vector to store the output in
 */
inline fun MutableVector3d.cross(
    other: MutableVector3d,
    output: MutableVector3d
) {
    cross(x, y, z, other.x, other.y, other.z) { x, y, z ->
        output.setXYZ(x, y, z)
    }
}

/**
 * Normalizes the given vector so that its length is `1.0`,
 * or fill with `NaN` if the given vector is `(0, 0, 0)`
 * @receiver The vector
 */
// TODO: Kotlin/JS Bug
/*inline*/ fun MutableVector3d.normalize() {
    val length = length()
    this.divide(length)
}

/**
 * Normalizes the given vector so that its length is `1.0`,
 * or fill with `NaN` if the given vector is `(0, 0, 0)`
 * @receiver The vector
 */
// TODO: Kotlin/JS Bug
/*inline*/ fun MutableVector3d.normalizeSafe() {
    val length = length()
    if (length == 0.0) setXYZ(0.0, 0.0, 0.0)
    this.divide(length)
}
