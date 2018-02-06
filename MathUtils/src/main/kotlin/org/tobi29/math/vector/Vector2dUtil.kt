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

package org.tobi29.math.vector

/**
 * Maps all values of the given vector into a new one
 * @receiver The vector to take the initial values from
 * @param transform Called on each value to form the new vector
 * @return A new vector containing the transformed values
 */
inline fun ReadVector2d.map(
    transform: (Double) -> Double
): Vector2d = Vector2d(transform(x), transform(y))

/**
 * Maps all values of two given vectors into a new one
 * @receiver The first vector to take the initial values from
 * @param other The second vector to take the initial values from
 * @param transform Called on each value to form the new vector
 * @return A new vector containing the transformed values
 */
inline fun ReadVector2d.map(
    other: ReadVector2d,
    transform: (Double, Double) -> Double
): Vector2d = Vector2d(transform(x, other.x), transform(y, other.y))

/**
 * Maps all values of the given vector into a new one
 * @receiver The vector to take the initial values from
 * @param transform Called on each value to form the new vector
 * @return A new vector containing the transformed values
 */
inline fun ReadVector2d.mapToInt(
    transform: (Double) -> Int
): Vector2i = Vector2i(transform(x), transform(y))

/**
 * Maps all values of two given vectors into a new one
 * @receiver The first vector to take the initial values from
 * @param other The second vector to take the initial values from
 * @param transform Called on each value to form the new vector
 * @return A new vector containing the transformed values
 */
inline fun ReadVector2d.mapToInt(
    other: ReadVector2d,
    transform: (Double, Double) -> Int
): Vector2i = Vector2i(transform(x, other.x), transform(y, other.y))

/**
 * Returns the sum of the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new vector containing the sum
 */
inline operator fun ReadVector2d.plus(a: Double): Vector2d =
    map { it + a }

/**
 * Returns the difference between the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new vector containing the difference
 */
inline operator fun ReadVector2d.minus(a: Double): Vector2d =
    map { it - a }

/**
 * Returns the product of the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new vector containing the product
 */
inline operator fun ReadVector2d.times(a: Double): Vector2d =
    map { it * a }

/**
 * Returns the quotient of the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new vector containing the quotient
 */
inline operator fun ReadVector2d.div(a: Double): Vector2d =
    map { it / a }

/**
 * Returns the sum of the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new vector containing the sum
 */
inline operator fun ReadVector2d.plus(other: ReadVector2d): Vector2d =
    map(other) { a, b -> a + b }

/**
 * Returns the difference between the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new vector containing the difference
 */
inline operator fun ReadVector2d.minus(other: ReadVector2d): Vector2d =
    map(other) { a, b -> a - b }

/**
 * Returns the product of the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new vector containing the product
 */
inline operator fun ReadVector2d.times(other: ReadVector2d): Vector2d =
    map(other) { a, b -> a * b }

/**
 * Returns the quotient of the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new vector containing the quotient
 */
inline operator fun ReadVector2d.div(other: ReadVector2d): Vector2d =
    map(other) { a, b -> a / b }

/**
 * Returns the length of the given vector
 * @receiver The vector to use
 * @return Length of the given vector
 */
// TODO: Kotlin/JS Bug
/*inline*/ fun ReadVector2d.length(): Double =
    length(x, y)

/**
 * Returns square of the length of the given vector
 * @receiver The vector to use
 * @return Square of the length of the given vector
 */
inline fun ReadVector2d.lengthSqr(): Double =
    lengthSqr(x, y)

/**
 * Returns the distance between the given vectors
 * @receiver The first vector
 * @param other The second vector
 * @return Distance between the given vectors
 */
// TODO: Kotlin/JS Bug
/*inline*/ infix fun ReadVector2d.distance(other: ReadVector2d): Double =
    distance(
        x, y,
        other.x, other.y
    )

/**
 * Returns square of the distance between the given vectors
 * @receiver The first vector
 * @param other The second vector
 * @return Square of the distance between the given vectors
 */
inline infix fun ReadVector2d.distanceSqr(other: ReadVector2d): Double =
    distanceSqr(x, y, other.x, other.y)

/**
 * Returns the direction between `(1,0)`, the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return The direction in radians
 */
// TODO: Kotlin/JS Bug
/*inline*/ fun ReadVector2d.direction(other: ReadVector2d): Double =
    direction(
        x, y,
        other.x, other.y
    )

/**
 * Returns the direction between `(1,0)`, `(0,0)` and the given vector
 * @receiver The vector
 * @return The direction in radians
 */
// TODO: Kotlin/JS Bug
/*inline*/ fun ReadVector2d.direction(): Double =
    direction(x, y)

/**
 * Returns the dot product of the given vectors
 * @receiver The first vector
 * @param other The second vector
 * @return Dot product of the given vectors
 */
inline infix fun ReadVector2d.dot(other: ReadVector2d): Double =
    dot(x, y, other.x, other.y)

/**
 * Checks if [point] is inside the region [origin] and [size]
 * @param origin Origin of the region
 * @param size Size of the region, all values should be positive
 * @param point The point to check
 * @return True if the point is inside, inclusive on lower end, exclusive on greater
 */
inline fun inside(
    origin: ReadVector2d,
    size: ReadVector2d,
    point: ReadVector2d
): Boolean = inside(
    origin.x, origin.y, size.x,
    size.y, point.x, point.y
)

/**
 * Returns the normalized version of the vector
 * @receiver The vector
 * @return Normalized vector with a length of `1.0`, or filled with `NaN` if the given vector is `(0, 0)`
 */
// TODO: Kotlin/JS Bug
/*inline*/ fun ReadVector2d.normalized(): Vector2d {
    val length = length()
    return this / length
}

/**
 * Returns the normalized version of the vector
 * @receiver The vector
 * @return Normalized vector with a length of `1.0`, or filled with `0` if the given vector is `(0, 0)`
 */
// TODO: Kotlin/JS Bug
/*inline*/ fun ReadVector2d.normalizedSafe(): Vector2d {
    val length = length()
    if (length == 0.0) {
        return Vector2d.ZERO
    }
    return this / length
}
