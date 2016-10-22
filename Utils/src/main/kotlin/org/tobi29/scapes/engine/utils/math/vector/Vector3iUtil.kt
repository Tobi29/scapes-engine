/*
 * Copyright 2012-2016 Tobi29
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
 * @return A new [Vector3i] containing the sum
 */
inline operator fun Vector3i.plus(a: Int): Vector3i {
    return Vector3i(x + a, y + a, z + a)
}

/**
 * Returns the difference between the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new [Vector3i] containing the difference
 */
inline operator fun Vector3i.minus(a: Int): Vector3i {
    return Vector3i(x - a, y - a, z - a)
}

/**
 * Returns the product of the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new [Vector3i] containing the product
 */
inline operator fun Vector3i.times(a: Int): Vector3i {
    return Vector3i(x * a, y * a, z * a)
}

/**
 * Returns the quotient of the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new [Vector3i] containing the quotient
 */
inline operator fun Vector3i.div(a: Int): Vector3i {
    return Vector3i(x / a, y / a, z / a)
}

/**
 * Returns the sum of the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new [Vector3i] containing the sum
 */
inline operator fun Vector3i.plus(other: Vector3i): Vector3i {
    return Vector3i(x + other.x, y + other.y, z + other.z)
}

/**
 * Returns the difference between the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new [Vector3i] containing the difference
 */
inline operator fun Vector3i.minus(other: Vector3i): Vector3i {
    return Vector3i(x - other.x, y - other.y, z - other.z)
}

/**
 * Returns the product of the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new [Vector3i] containing the product
 */
inline operator fun Vector3i.times(other: Vector3i): Vector3i {
    return Vector3i(x * other.x, y * other.y, z * other.z)
}

/**
 * Returns the quotient of the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new [Vector3i] containing the quotient
 */
inline operator fun Vector3i.div(other: Vector3i): Vector3i {
    return Vector3i(x / other.x, y / other.y, z / other.z)
}

/**
 * Returns the length of the given [Vector3i]
 * @receiver The vector to use
 * @return Length of the given [Vector3i]
 */
inline fun Vector3i.length(): Double {
    return sqrt(lengthSqr())
}

/**
 * Returns square of the length of the given [Vector3i]
 * @receiver The vector to use
 * @return Square of the length of the given [Vector3i]
 */
inline fun Vector3i.lengthSqr(): Double {
    return lengthSqr(x.toDouble(), y.toDouble(), z.toDouble())
}

/**
 * Returns the distance between the given [Vector3i]s
 * @receiver The first vector
 * @param other The second vector
 * @return Distance between the given [Vector3i]s
 */
inline infix fun Vector3i.distance(other: Vector3i): Double {
    return sqrt(distanceSqr(other))
}

/**
 * Returns square of the distance between the given [Vector3i]s
 * @receiver The first vector
 * @param other The second vector
 * @return Square of the distance between the given [Vector3i]s
 */
inline infix fun Vector3i.distanceSqr(other: Vector3i): Double {
    return distanceSqr(x.toDouble(), y.toDouble(), z.toDouble(),
            other.x.toDouble(),
            other.y.toDouble(), other.z.toDouble())
}

/**
 * Returns the dot product of the given vectors
 * @receiver The first vector
 * @param other The second vector
 * @return Dot product of the given vectors
 */
inline infix fun Vector3i.dot(other: Vector3i): Double {
    return dot(x.toDouble(), y.toDouble(), z.toDouble(), other.x.toDouble(),
            other.y.toDouble(), other.z.toDouble())
}

/**
 * Checks if [point] is inside the rectangle [origin] and [size]
 * @param origin Origin of the region
 * @param size Size of the region, all values should be positive
 * @param point The point to check
 * @return True if the point is inside, inclusive on lower end, exclusive on greater
 */
inline fun inside(origin: Vector3i,
                  size: Vector3i,
                  point: Vector3i): Boolean {
    return inside(origin.x.toDouble(), origin.y.toDouble(), origin.z.toDouble(),
            size.x.toDouble(), size.y.toDouble(), size.z.toDouble(),
            point.x.toDouble(), point.y.toDouble(), point.z.toDouble())
}
