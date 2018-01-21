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

import kotlin.math.sqrt

/**
 * Returns the length of the given [MutableVector2d]
 * @receiver The vector to use
 * @return Length of the given [MutableVector2d]
 */
inline fun MutableVector2d.length(): Double {
    return sqrt(lengthSqr())
}

/**
 * Returns square of the length of the given [MutableVector2d]
 * @receiver The vector to use
 * @return Square of the length of the given [MutableVector2d]
 */
inline fun MutableVector2d.lengthSqr(): Double {
    return lengthSqr(x, y)
}

/**
 * Returns the distance between the given [MutableVector2d]s
 * @receiver The first vector
 * @param other The second vector
 * @return Distance between the given [MutableVector2d]s
 */
inline infix fun MutableVector2d.distance(other: MutableVector2d): Double {
    return sqrt(distanceSqr(other))
}

/**
 * Returns square of the distance between the given [MutableVector2d]s
 * @receiver The first vector
 * @param other The second vector
 * @return Square of the distance between the given [MutableVector2d]s
 */
inline infix fun MutableVector2d.distanceSqr(other: MutableVector2d): Double {
    return distanceSqr(x, y, other.x, other.y)
}

/**
 * Returns the direction between `(1,0)`, the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return The direction in radians
 */
// TODO: Kotlin/JS Bug
/*inline*/ fun MutableVector2d.direction(other: MutableVector2d): Double {
    return direction(x, y, other.x, other.y)
}

/**
 * Returns the direction between `(1,0)`, `(0,0)` and the given vector
 * @receiver The vector
 * @return The direction in radians
 */
// TODO: Kotlin/JS Bug
/*inline*/ fun MutableVector2d.direction(): Double {
    return direction(x, y)
}

/**
 * Returns the dot product of the given vectors
 * @receiver The first vector
 * @param other The second vector
 * @return Dot product of the given vectors
 */
inline infix fun MutableVector2d.dot(other: MutableVector2d): Double {
    return dot(x, y, other.x, other.y)
}

/**
 * Checks if [point] is inside the region [origin] and [size]
 * @param origin Origin of the region
 * @param size Size of the region, all values should be positive
 * @param point The point to check
 * @return True if the point is inside, inclusive on lower end, exclusive on greater
 */
inline fun inside(origin: MutableVector2d,
                  size: MutableVector2d,
                  point: MutableVector2d): Boolean {
    return inside(origin.x, origin.y,
            size.x, size.y,
            point.x, point.y)
}

/**
 * Returns the normalized version of the vector
 * @receiver The vector
 * @return Normalized vector with a length of `1.0`, or filled with `NaN` if the given vector is `(0,0)`
 */
// TODO: Kotlin/JS Bug
/*inline*/ fun MutableVector2d.normalize(): MutableVector2d {
    val length = length()
    return this / length
}

/**
 * Returns the normalized version of the vector
 * @receiver The vector
 * @return Normalized vector with a length of `1.0`, or filled with `0` if the given vector is `(0,0)`
 */
// TODO: Kotlin/JS Bug
/*inline*/ fun MutableVector2d.normalizeSafe(): MutableVector2d {
    val length = length()
    return if (length == 0.0) setXY(0.0, 0.0)
    else this / length
}
