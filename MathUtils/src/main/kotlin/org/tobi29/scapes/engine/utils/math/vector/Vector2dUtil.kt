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

package org.tobi29.scapes.engine.utils.math.vector

import org.tobi29.scapes.engine.utils.math.sqrt

/**
 * Returns the sum of the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new [Vector2d] containing the sum
 */
inline operator fun Vector2d.plus(a: Double): Vector2d {
    return Vector2d(x + a, y + a)
}

/**
 * Returns the difference between the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new [Vector2d] containing the difference
 */
inline operator fun Vector2d.minus(a: Double): Vector2d {
    return Vector2d(x - a, y - a)
}

/**
 * Returns the product of the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new [Vector2d] containing the product
 */
inline operator fun Vector2d.times(a: Double): Vector2d {
    return Vector2d(x * a, y * a)
}

/**
 * Returns the quotient of the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new [Vector2d] containing the quotient
 */
inline operator fun Vector2d.div(a: Double): Vector2d {
    return Vector2d(x / a, y / a)
}

/**
 * Returns the sum of the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new [Vector2d] containing the sum
 */
inline operator fun Vector2d.plus(other: Vector2d): Vector2d {
    return Vector2d(x + other.x, y + other.y)
}

/**
 * Returns the difference between the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new [Vector2d] containing the sum
 */
inline operator fun Vector2d.minus(other: Vector2d): Vector2d {
    return Vector2d(x - other.x, y - other.y)
}

/**
 * Returns the product of the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new [Vector2d] containing the sum
 */
inline operator fun Vector2d.times(other: Vector2d): Vector2d {
    return Vector2d(x * other.x, y * other.y)
}

/**
 * Returns the quotient of the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new [Vector2d] containing the sum
 */
inline operator fun Vector2d.div(other: Vector2d): Vector2d {
    return Vector2d(x / other.x, y / other.y)
}

/**
 * Returns the length of the given [Vector2d]
 * @receiver The vector to use
 * @return Length of the given [Vector2d]
 */
inline fun Vector2d.length(): Double {
    return sqrt(lengthSqr())
}

/**
 * Returns square of the length of the given [Vector2d]
 * @receiver The vector to use
 * @return Square of the length of the given [Vector2d]
 */
inline fun Vector2d.lengthSqr(): Double {
    return lengthSqr(x, y)
}

/**
 * Returns the distance between the given [Vector2d]s
 * @receiver The first vector
 * @param other The second vector
 * @return Distance between the given [Vector2d]s
 */
inline infix fun Vector2d.distance(other: Vector2d): Double {
    return sqrt(distanceSqr(other))
}

/**
 * Returns square of the distance between the given [Vector2d]s
 * @receiver The first vector
 * @param other The second vector
 * @return Square of the distance between the given [Vector2d]s
 */
inline infix fun Vector2d.distanceSqr(other: Vector2d): Double {
    return distanceSqr(x, y, other.x, other.y)
}

/**
 * Returns the direction between `(1,0)`, the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return The direction in radians
 */
inline fun Vector2d.direction(other: Vector2d): Double {
    return direction(x, y, other.x, other.y)
}

/**
 * Returns the direction between `(1,0)`, `(0,0)` and the given vector
 * @receiver The vector
 * @return The direction in radians
 */
inline fun Vector2d.direction(): Double {
    return direction(x, y)
}

/**
 * Returns the dot product of the given vectors
 * @receiver The first vector
 * @param other The second vector
 * @return Dot product of the given vectors
 */
inline infix fun Vector2d.dot(other: Vector2d): Double {
    return dot(x, y, other.x, other.y)
}

/**
 * Checks if [point] is inside the region [origin] and [size]
 * @param origin Origin of the region
 * @param size Size of the region, all values should be positive
 * @param point The point to check
 * @return True if the point is inside, inclusive on lower end, exclusive on greater
 */
inline fun inside(origin: Vector2d,
                  size: Vector2d,
                  point: Vector2d): Boolean {
    return inside(origin.x, origin.y, size.x,
            size.y, point.x, point.y)
}

/**
 * Returns the normalized version of the vector
 * @receiver The vector
 * @return Normalized vector with a length of `1.0`, or filled with `NaN` if the given vector is `(0,0)`
 */
inline fun Vector2d.normalize(): Vector2d {
    val length = length()
    return this / length
}

/**
 * Returns the normalized version of the vector
 * @receiver The vector
 * @return Normalized vector with a length of `1.0`, or filled with `0` if the given vector is `(0,0)`
 */
inline fun Vector2d.normalizeSafe(): Vector2d {
    val length = length()
    if (length == 0.0) {
        return Vector2d.ZERO
    }
    return this / length
}
