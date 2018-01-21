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
inline fun Vector2i.map(transform: (Int) -> Int): Vector2i =
        Vector2i(transform(x), transform(y))

/**
 * Maps all values of two given vectors into a new one
 * @receiver The first vector to take the initial values from
 * @param other The second vector to take the initial values from
 * @param transform Called on each value to form the new vector
 * @return A new vector containing the transformed values
 */
inline fun Vector2i.map(other: Vector2i,
                        transform: (Int, Int) -> Int): Vector2i =
        Vector2i(transform(x, other.x), transform(y, other.y))

/**
 * Maps all values of the given vector into a new one
 * @receiver The vector to take the initial values from
 * @param transform Called on each value to form the new vector
 * @return A new vector containing the transformed values
 */
inline fun Vector2i.mapToDouble(transform: (Int) -> Double): Vector2d =
        Vector2d(transform(x), transform(y))

/**
 * Maps all values of two given vectors into a new one
 * @receiver The first vector to take the initial values from
 * @param other The second vector to take the initial values from
 * @param transform Called on each value to form the new vector
 * @return A new vector containing the transformed values
 */
inline fun Vector2i.mapToDouble(other: Vector2i,
                                transform: (Int, Int) -> Double): Vector2d =
        Vector2d(transform(x, other.x), transform(y, other.y))

/**
 * Returns the sum of the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new vector containing the sum
 */
inline operator fun Vector2i.plus(a: Int): Vector2i =
        map { it + a }

/**
 * Returns the difference between the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new vector containing the difference
 */
inline operator fun Vector2i.minus(a: Int): Vector2i =
        map { it - a }

/**
 * Returns the product of the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new vector containing the product
 */
inline operator fun Vector2i.times(a: Int): Vector2i =
        map { it * a }

/**
 * Returns the quotient of the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new vector containing the quotient
 */
inline operator fun Vector2i.div(a: Int): Vector2i =
        map { it / a }

/**
 * Returns the sum of the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new vector containing the sum
 */
inline operator fun Vector2i.plus(other: Vector2i): Vector2i =
        map(other) { a, b -> a + b }

/**
 * Returns the difference between the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new vector containing the difference
 */
inline operator fun Vector2i.minus(other: Vector2i): Vector2i =
        map(other) { a, b -> a - b }

/**
 * Returns the product of the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new vector containing the product
 */
inline operator fun Vector2i.times(other: Vector2i): Vector2i =
        map(other) { a, b -> a * b }

/**
 * Returns the quotient of the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new vector containing the quotient
 */
inline operator fun Vector2i.div(other: Vector2i): Vector2i =
        map(other) { a, b -> a / b }

/**
 * Returns the length of the given vector
 * @receiver The vector to use
 * @return Length of the given vector
 */
// TODO: Kotlin/JS Bug
/*inline*/ fun Vector2i.length(): Double =
        length(x.toDouble(), y.toDouble())

/**
 * Returns square of the length of the given vector
 * @receiver The vector to use
 * @return Square of the length of the given vector
 */
inline fun Vector2i.lengthSqr(): Int =
        lengthSqr(x, y)

/**
 * Returns the distance between the given vectors
 * @receiver The first vector
 * @param other The second vector
 * @return Distance between the given vectors
 */
// TODO: Kotlin/JS Bug
/*inline*/ infix fun Vector2i.distance(other: Vector2i): Double =
        distance(x.toDouble(), y.toDouble(),
                other.x.toDouble(), other.y.toDouble())

/**
 * Returns square of the distance between the given vectors
 * @receiver The first vector
 * @param other The second vector
 * @return Square of the distance between the given vectors
 */
inline infix fun Vector2i.distanceSqr(other: Vector2i): Int =
        distanceSqr(x, y, other.x, other.y)

/**
 * Returns the direction between `(1,0)`, the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return The direction in radians
 */
// TODO: Kotlin/JS Bug
/*inline*/ fun Vector2i.direction(other: Vector2i): Double =
        direction(x.toDouble(), y.toDouble(),
                other.x.toDouble(), other.y.toDouble())

/**
 * Returns the direction between `(1,0)`, `(0,0)` and the given vector
 * @receiver The vector
 * @return The direction in radians
 */
// TODO: Kotlin/JS Bug
/*inline*/ fun Vector2i.direction(): Double =
        direction(x.toDouble(), y.toDouble())

/**
 * Returns the dot product of the given vectors
 * @receiver The first vector
 * @param other The second vector
 * @return Dot product of the given vectors
 */
inline infix fun Vector2i.dot(other: Vector2i): Int =
        dot(x, y, other.x, other.y)

/**
 * Checks if [point] is inside the region [origin] and [size]
 * @param origin Origin of the region
 * @param size Size of the region, all values should be positive
 * @param point The point to check
 * @return True if the point is inside, inclusive on lower end, exclusive on greater
 */
inline fun inside(origin: Vector2i,
                  size: Vector2i,
                  point: Vector2i): Boolean =
        inside(origin.x.toDouble(), origin.y.toDouble(), size.x.toDouble(),
                size.y.toDouble(), point.x.toDouble(), point.y.toDouble())
