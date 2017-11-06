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

import kotlin.math.sqrt

/**
 * Returns the length of the given [MutableVector3d]
 * @receiver The vector to use
 * @return Length of the given [MutableVector3d]
 */
inline fun MutableVector3d.length(): Double {
    return sqrt(lengthSqr())
}

/**
 * Returns square of the length of the given [MutableVector3d]
 * @receiver The vector to use
 * @return Square of the length of the given [MutableVector3d]
 */
inline fun MutableVector3d.lengthSqr(): Double {
    return lengthSqr(x, y, z)
}

/**
 * Returns the distance between the given [MutableVector3d]s
 * @receiver The first vector
 * @param other The second vector
 * @return Distance between the given [MutableVector3d]s
 */
inline infix fun MutableVector3d.distance(other: MutableVector3d): Double {
    return sqrt(distanceSqr(other))
}

/**
 * Returns square of the distance between the given [MutableVector3d]s
 * @receiver The first vector
 * @param other The second vector
 * @return Square of the distance between the given [MutableVector3d]s
 */
inline infix fun MutableVector3d.distanceSqr(other: MutableVector3d): Double {
    return distanceSqr(x, y, z, other.x,
            other.y, other.z)
}

/**
 * Returns the dot product of the given vectors
 * @receiver The first vector
 * @param other The second vector
 * @return Dot product of the given vectors
 */
inline infix fun MutableVector3d.dot(other: MutableVector3d): Double {
    return dot(x, y, z, other.x, other.y, other.z)
}

/**
 * Computes the cross product of the given vectors
 * @receiver The first vector
 * @param other The second vector
 * @param output The vector to store the output in
 * @return Dot product of the given vectors
 */
inline fun MutableVector3d.cross(other: MutableVector3d,
                                 output: MutableVector3d): MutableVector3d {
    cross(x, y, z, other.x, other.y, other.z) { x, y, z -> output.set(x, y, z) }
    return output
}

/**
 * Checks if [point] is inside the rectangle [origin] and [size]
 * @param origin Origin of the region
 * @param size Size of the region, all values should be positive
 * @param point The point to check
 * @return True if the point is inside, inclusive on lower end, exclusive on greater
 */
inline fun inside(origin: MutableVector3d,
                  size: MutableVector3d,
                  point: MutableVector3d): Boolean {
    return inside(origin.x, origin.y, origin.z,
            size.x, size.y, size.z, point.x,
            point.y, point.z)
}

/**
 * Normalizes the vector
 * @see Vector3d.normalize
 * @receiver The vector
 */
inline fun MutableVector3d.normalize(): MutableVector3d {
    val length = length()
    return this / length
}

/**
 * Normalizes the vector
 * @see Vector3d.normalizeSafe
 * @receiver The vector
 */
inline fun MutableVector3d.normalizeSafe(): MutableVector3d {
    val length = length()
    return if (length == 0.0) set(0.0, 0.0, 0.0)
    else this / length
}
