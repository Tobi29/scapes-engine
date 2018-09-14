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

package org.tobi29.math.vector

import org.tobi29.stdex.math.ceilToInt
import org.tobi29.stdex.math.floorToInt
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * Maps all values of the given vector into a new one
 * @receiver The vector to take the initial values from
 * @param transform Called on each value to form the new vector
 * @return A new vector containing the transformed values
 */
inline fun ReadVector3d.map(
    transform: (Double) -> Double
): Vector3d = Vector3d(transform(x), transform(y), transform(z))

/**
 * Maps all values of two given vectors into a new one
 * @receiver The first vector to take the initial values from
 * @param other The second vector to take the initial values from
 * @param transform Called on each value to form the new vector
 * @return A new vector containing the transformed values
 */
inline fun ReadVector3d.map(
    other: ReadVector3d,
    transform: (Double, Double) -> Double
): Vector3d = Vector3d(
    transform(x, other.x), transform(y, other.y), transform(z, other.z)
)

/**
 * Maps all values of the given vector into a new one
 * @receiver The vector to take the initial values from
 * @param transform Called on each value to form the new vector
 * @return A new vector containing the transformed values
 */
inline fun ReadVector3d.mapToInt(
    transform: (Double) -> Int
): Vector3i = Vector3i(transform(x), transform(y), transform(z))

/**
 * Maps all values of two given vectors into a new one
 * @receiver The first vector to take the initial values from
 * @param other The second vector to take the initial values from
 * @param transform Called on each value to form the new vector
 * @return A new vector containing the transformed values
 */
inline fun ReadVector3d.mapToInt(
    other: ReadVector3d,
    transform: (Double, Double) -> Int
): Vector3i = Vector3i(
    transform(x, other.x), transform(y, other.y), transform(z, other.z)
)

/**
 * Returns the sum of the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new vector containing the sum
 */
inline operator fun ReadVector3d.plus(a: Double): Vector3d =
    map { it + a }

/**
 * Returns the difference between the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new vector containing the difference
 */
inline operator fun ReadVector3d.minus(a: Double): Vector3d =
    map { it - a }

/**
 * Returns the product of the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new vector containing the product
 */
inline operator fun ReadVector3d.times(a: Double): Vector3d =
    map { it * a }

/**
 * Returns the quotient of the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new vector containing the quotient
 */
inline operator fun ReadVector3d.div(a: Double): Vector3d =
    map { it / a }

/**
 * Returns the sum of the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new vector containing the sum
 */
inline operator fun ReadVector3d.plus(other: ReadVector3d): Vector3d =
    map(other) { a, b -> a + b }

/**
 * Returns the difference between the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new vector containing the difference
 */
inline operator fun ReadVector3d.minus(other: ReadVector3d): Vector3d =
    map(other) { a, b -> a - b }

/**
 * Returns the product of the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new vector containing the product
 */
inline operator fun ReadVector3d.times(other: ReadVector3d): Vector3d =
    map(other) { a, b -> a * b }

/**
 * Returns the quotient of the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new vector containing the quotient
 */
inline operator fun ReadVector3d.div(other: ReadVector3d): Vector3d =
    map(other) { a, b -> a / b }

/**
 * Returns the length of the given vector
 * @receiver The vector to use
 * @return Length of the given vector
 */
// TODO: Kotlin/JS Bug
/*inline*/ fun ReadVector3d.length(): Double =
    length(x, y, z)

/**
 * Returns square of the length of the given vector
 * @receiver The vector to use
 * @return Square of the length of the given vector
 */
inline fun ReadVector3d.lengthSqr(): Double =
    lengthSqr(x, y, z)

/**
 * Returns the distance between the given vectors
 * @receiver The first vector
 * @param other The second vector
 * @return Distance between the given vectors
 */
// TODO: Kotlin/JS Bug
/*inline*/ infix fun ReadVector3d.distance(other: ReadVector3d): Double =
    distance(x, y, z, other.x, other.y, other.z)


/**
 * Returns square of the distance between the given vectors
 * @receiver The first vector
 * @param other The second vector
 * @return Square of the distance between the given vectors
 */
inline infix fun ReadVector3d.distanceSqr(other: ReadVector3d): Double =
    distanceSqr(x, y, z, other.x, other.y, other.z)

/**
 * Returns the dot product of the given vectors
 * @receiver The first vector
 * @param other The second vector
 * @return Dot product of the given vectors
 */
inline infix fun ReadVector3d.dot(other: ReadVector3d): Double =
    dot(x, y, z, other.x, other.y, other.z)

/**
 * Returns the cross product of the given vectors
 * @receiver The first vector
 * @param other The second vector
 * @return Dot product of the given vectors
 */
inline infix fun ReadVector3d.cross(other: ReadVector3d): Vector3d =
    cross(x, y, z, other.x, other.y, other.z)

/**
 * Checks if [point] is inside the rectangle [origin] and [size]
 * @param origin Origin of the region
 * @param size Size of the region, all values should be positive
 * @param point The point to check
 * @return True if the point is inside, inclusive on lower end, exclusive on greater
 */
inline fun inside(
    origin: ReadVector3d,
    size: ReadVector3d,
    point: ReadVector3d
): Boolean = inside(
    origin.x, origin.y, origin.z,
    size.x, size.y, size.z,
    point.x, point.y, point.z
)

/**
 * Returns the normalized version of the vector
 * @receiver The vector
 * @return Normalized vector with a length of `1.0`, or filled with `NaN` if the given vector is `(0, 0, 0)`
 */
// TODO: Kotlin/JS Bug
/*inline*/ fun ReadVector3d.normalized(): Vector3d {
    val length = length()
    return this / length
}

/**
 * Returns the normalized version of the vector
 * @receiver The vector
 * @return Normalized vector with a length of `1.0`, or filled with `0` if the given vector is `(0, 0, 0)`
 */
// TODO: Kotlin/JS Bug
/*inline*/ fun ReadVector3d.normalizedSafe(): Vector3d {
    val length = length()
    if (length == 0.0) {
        return Vector3d.ZERO
    }
    return this / length
}

inline fun ReadVector3d.floor(): Vector3d = map { floor(it) }

inline fun ReadVector3d.round(): Vector3d = map { round(it) }

inline fun ReadVector3d.ceil(): Vector3d = map { ceil(it) }

inline fun ReadVector3d.floorToInt(): Vector3i = mapToInt { it.floorToInt() }

inline fun ReadVector3d.roundToInt(): Vector3i = mapToInt { it.roundToInt() }

inline fun ReadVector3d.ceilToInt(): Vector3i = mapToInt { it.ceilToInt() }
