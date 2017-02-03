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

import org.tobi29.scapes.engine.utils.io.tag.TagStructure
import org.tobi29.scapes.engine.utils.io.tag.getDouble
import org.tobi29.scapes.engine.utils.math.sqrt

/**
 * Create vector from the tag structure entry, as created by [Vector3d.write]
 */
inline fun TagStructure.getVector3d(key: String) = getStructure(
        key)?.let(::Vector3d)

/**
 * Creates vector from the given tag structure, as created by [Vector3d.write]
 */
inline fun Vector3d(tagStructure: TagStructure) = Vector3d(
        tagStructure.getDouble("X") ?: 0.0, tagStructure.getDouble("Y") ?: 0.0,
        tagStructure.getDouble("Z") ?: 0.0)

/**
 * Returns the sum of the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new [Vector3d] containing the sum
 */
inline operator fun Vector3d.plus(a: Double): Vector3d {
    return Vector3d(x + a, y + a, z + a)
}

/**
 * Returns the difference between the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new [Vector3d] containing the difference
 */
inline operator fun Vector3d.minus(a: Double): Vector3d {
    return Vector3d(x - a, y - a, z - a)
}

/**
 * Returns the product of the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new [Vector3d] containing the product
 */
inline operator fun Vector3d.times(a: Double): Vector3d {
    return Vector3d(x * a, y * a, z * a)
}

/**
 * Returns the quotient of the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new [Vector3d] containing the quotient
 */
inline operator fun Vector3d.div(a: Double): Vector3d {
    return Vector3d(x / a, y / a, z / a)
}

/**
 * Returns the sum of the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new [Vector3d] containing the sum
 */
inline operator fun Vector3d.plus(other: Vector3d): Vector3d {
    return Vector3d(x + other.x, y + other.y, z + other.z)
}

/**
 * Returns the difference between the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new [Vector3d] containing the difference
 */
inline operator fun Vector3d.minus(other: Vector3d): Vector3d {
    return Vector3d(x - other.x, y - other.y, z - other.z)
}

/**
 * Returns the product of the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new [Vector3d] containing the product
 */
inline operator fun Vector3d.times(other: Vector3d): Vector3d {
    return Vector3d(x * other.x, y * other.y, z * other.z)
}

/**
 * Returns the quotient of the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new [Vector3d] containing the quotient
 */
inline operator fun Vector3d.div(other: Vector3d): Vector3d {
    return Vector3d(x / other.x, y / other.y, z / other.z)
}

/**
 * Returns the length of the given [Vector3d]
 * @receiver The vector to use
 * @return Length of the given [Vector3d]
 */
inline fun Vector3d.length(): Double {
    return sqrt(lengthSqr())
}

/**
 * Returns square of the length of the given [Vector3d]
 * @receiver The vector to use
 * @return Square of the length of the given [Vector3d]
 */
inline fun Vector3d.lengthSqr(): Double {
    return lengthSqr(x, y, z)
}

/**
 * Returns the distance between the given [Vector3d]s
 * @receiver The first vector
 * @param other The second vector
 * @return Distance between the given [Vector3d]s
 */
inline infix fun Vector3d.distance(other: Vector3d): Double {
    return sqrt(distanceSqr(other))
}

/**
 * Returns square of the distance between the given [Vector3d]s
 * @receiver The first vector
 * @param other The second vector
 * @return Square of the distance between the given [Vector3d]s
 */
inline infix fun Vector3d.distanceSqr(other: Vector3d): Double {
    return distanceSqr(x, y, z, other.x,
            other.y, other.z)
}

/**
 * Returns the dot product of the given vectors
 * @receiver The first vector
 * @param other The second vector
 * @return Dot product of the given vectors
 */
inline infix fun Vector3d.dot(other: Vector3d): Double {
    return dot(x, y, z, other.x,
            other.y, other.z)
}

/**
 * Returns the cross product of the given vectors
 * @receiver The first vector
 * @param other The second vector
 * @return Dot product of the given vectors
 */
inline infix fun Vector3d.cross(other: Vector3d): Vector3d {
    return cross(x, y, z, other.x,
            other.y, other.z)
}

/**
 * Checks if [point] is inside the rectangle [origin] and [size]
 * @param origin Origin of the region
 * @param size Size of the region, all values should be positive
 * @param point The point to check
 * @return True if the point is inside, inclusive on lower end, exclusive on greater
 */
inline fun inside(origin: Vector3d,
                  size: Vector3d,
                  point: Vector3d): Boolean {
    return inside(origin.x, origin.y, origin.z,
            size.x, size.y, size.z, point.x,
            point.y, point.z)
}

/**
 * Returns the normalized version of the vector
 * @receiver The vector
 * @return Normalized vector with a length of `1.0`, or filled with `NaN` if the given vector is `(0,0,0)`
 */
inline fun Vector3d.normalize(): Vector3d {
    val length = length()
    return this / length
}

/**
 * Returns the normalized version of the vector
 * @receiver The vector
 * @return Normalized vector with a length of `1.0`, or filled with `0` if the given vector is `(0,0,0)`
 */
inline fun Vector3d.normalizeSafe(): Vector3d {
    val length = length()
    if (length == 0.0) {
        return Vector3d.ZERO
    }
    return this / length
}
