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

package org.tobi29.scapes.engine.math.vector

/**
 * Maps all values of the given vector into a new one
 * @receiver The vector to take the initial values from
 * @param transform Called on each value to form the new vector
 * @return A new vector containing the transformed values
 */
inline fun Vector3i.map(transform: (Int) -> Int): Vector3i =
        Vector3i(transform(x), transform(y), transform(z))

/**
 * Maps all values of two given vectors into a new one
 * @receiver The first vector to take the initial values from
 * @param other The second vector to take the initial values from
 * @param transform Called on each value to form the new vector
 * @return A new vector containing the transformed values
 */
inline fun Vector3i.map(other: Vector3i,
                        transform: (Int, Int) -> Int): Vector3i =
        Vector3i(transform(x, other.x), transform(y, other.y),
                transform(z, other.z))

/**
 * Maps all values of the given vector into a new one
 * @receiver The vector to take the initial values from
 * @param transform Called on each value to form the new vector
 * @return A new vector containing the transformed values
 */
inline fun Vector3i.mapToDouble(transform: (Int) -> Double): Vector3d =
        Vector3d(transform(x), transform(y), transform(z))

/**
 * Maps all values of two given vectors into a new one
 * @receiver The first vector to take the initial values from
 * @param other The second vector to take the initial values from
 * @param transform Called on each value to form the new vector
 * @return A new vector containing the transformed values
 */
inline fun Vector3i.mapToDouble(other: Vector3i,
                                transform: (Int, Int) -> Double): Vector3d =
        Vector3d(transform(x, other.x), transform(y, other.y),
                transform(z, other.z))

/**
 * Returns the sum of the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new vector containing the sum
 */
inline operator fun Vector3i.plus(a: Int): Vector3i =
        map { it + a }

/**
 * Returns the difference between the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new vector containing the difference
 */
inline operator fun Vector3i.minus(a: Int): Vector3i =
        map { it - a }

/**
 * Returns the product of the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new vector containing the product
 */
inline operator fun Vector3i.times(a: Int): Vector3i =
        map { it * a }

/**
 * Returns the quotient of the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new vector containing the quotient
 */
inline operator fun Vector3i.div(a: Int): Vector3i =
        map { it / a }

/**
 * Returns the sum of the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new vector containing the sum
 */
inline operator fun Vector3i.plus(other: Vector3i): Vector3i =
        map(other) { a, b -> a + b }

/**
 * Returns the difference between the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new vector containing the difference
 */
inline operator fun Vector3i.minus(other: Vector3i): Vector3i =
        map(other) { a, b -> a - b }

/**
 * Returns the product of the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new vector containing the product
 */
inline operator fun Vector3i.times(other: Vector3i): Vector3i =
        map(other) { a, b -> a * b }

/**
 * Returns the quotient of the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new vector containing the quotient
 */
inline operator fun Vector3i.div(other: Vector3i): Vector3i =
        map(other) { a, b -> a / b }

/**
 * Returns the length of the given vector
 * @receiver The vector to use
 * @return Length of the given vector
 */
// TODO: Kotlin/JS Bug
/*inline*/ fun Vector3i.length(): Double =
        length(x.toDouble(), y.toDouble(), z.toDouble())

/**
 * Returns square of the length of the given vector
 * @receiver The vector to use
 * @return Square of the length of the given vector
 */
inline fun Vector3i.lengthSqr(): Int =
        lengthSqr(x, y, z)

/**
 * Returns the distance between the given vectors
 * @receiver The first vector
 * @param other The second vector
 * @return Distance between the given vectors
 */
// TODO: Kotlin/JS Bug
/*inline*/ infix fun Vector3i.distance(other: Vector3i): Double =
        distance(x.toDouble(), y.toDouble(), z.toDouble(),
                other.x.toDouble(), other.y.toDouble(), other.z.toDouble())


/**
 * Returns square of the distance between the given vectors
 * @receiver The first vector
 * @param other The second vector
 * @return Square of the distance between the given vectors
 */
inline infix fun Vector3i.distanceSqr(other: Vector3i): Int =
        distanceSqr(x, y, z, other.x, other.y, other.z)

/**
 * Returns the dot product of the given vectors
 * @receiver The first vector
 * @param other The second vector
 * @return Dot product of the given vectors
 */
inline infix fun Vector3i.dot(other: Vector3i): Int =
        dot(x, y, z, other.x, other.y, other.z)

/**
 * Returns the cross product of the given vectors
 * @receiver The first vector
 * @param other The second vector
 * @return Dot product of the given vectors
 */
inline infix fun Vector3i.cross(other: Vector3i): Vector3i =
        cross(x, y, z, other.x, other.y, other.z)

/**
 * Checks if [point] is inside the rectangle [origin] and [size]
 * @param origin Origin of the region
 * @param size Size of the region, all values should be positive
 * @param point The point to check
 * @return True if the point is inside, inclusive on lower end, exclusive on greater
 */
inline fun inside(origin: Vector3i,
                  size: Vector3i,
                  point: Vector3i): Boolean =
        inside(origin.x.toDouble(), origin.y.toDouble(), origin.z.toDouble(),
                size.x.toDouble(), size.y.toDouble(), size.z.toDouble(),
                point.x.toDouble(), point.y.toDouble(), point.z.toDouble())
