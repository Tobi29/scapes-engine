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
import org.tobi29.scapes.engine.utils.io.tag.getInt
import org.tobi29.scapes.engine.utils.math.sqrt

/**
 * Create vector from the tag structure entry, as created by [Vector2i.write]
 */
inline fun TagStructure.getVector2i(key: String) = getStructure(
        key)?.let(::Vector2i)

/**
 * Creates vector from the given tag structure, as created by [Vector2i.write]
 */
inline fun Vector2i(tagStructure: TagStructure) = Vector2i(
        tagStructure.getInt("X") ?: 0, tagStructure.getInt("Y") ?: 0)

/**
 * Returns the sum of the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new [Vector2i] containing the sum
 */
inline operator fun Vector2i.plus(a: Int): Vector2i {
    return Vector2i(x + a, y + a)
}

/**
 * Returns the difference between the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new [Vector2i] containing the difference
 */
inline operator fun Vector2i.minus(a: Int): Vector2i {
    return Vector2i(x - a, y - a)
}

/**
 * Returns the product of the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new [Vector2i] containing the product
 */
inline operator fun Vector2i.times(a: Int): Vector2i {
    return Vector2i(x * a, y * a)
}

/**
 * Returns the quotient of the given vector and [a]
 * @receiver The first vector
 * @param a The second value
 * @return A new [Vector2i] containing the quotient
 */
inline operator fun Vector2i.div(a: Int): Vector2i {
    return Vector2i(x / a, y / a)
}

/**
 * Returns the sum of the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new [Vector2i] containing the sum
 */
inline operator fun Vector2i.plus(other: Vector2i): Vector2i {
    return Vector2i(x + other.x, y + other.y)
}

/**
 * Returns the difference between the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new [Vector2i] containing the difference
 */
inline operator fun Vector2i.minus(other: Vector2i): Vector2i {
    return Vector2i(x - other.x, y - other.y)
}

/**
 * Returns the product of the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new [Vector2i] containing the product
 */
inline operator fun Vector2i.times(other: Vector2i): Vector2i {
    return Vector2i(x * other.x, y * other.y)
}

/**
 * Returns the quotient of the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return A new [Vector2i] containing the quotient
 */
inline operator fun Vector2i.div(other: Vector2i): Vector2i {
    return Vector2i(x / other.x, y / other.y)
}

/**
 * Returns the length of the given [Vector2i]
 * @receiver The vector to use
 * @return Length of the given [Vector2i]
 */
inline fun Vector2i.length(): Double {
    return sqrt(lengthSqr())
}

/**
 * Returns square of the length of the given [Vector2i]
 * @receiver The vector to use
 * @return Square of the length of the given [Vector2i]
 */
inline fun Vector2i.lengthSqr(): Double {
    return lengthSqr(x.toDouble(), y.toDouble())
}

/**
 * Returns the distance between the given [Vector2i]s
 * @receiver The first vector
 * @param other The second vector
 * @return Distance between the given [Vector2i]s
 */
inline infix fun Vector2i.distance(other: Vector2i): Double {
    return sqrt(distanceSqr(other))
}

/**
 * Returns square of the distance between the given [Vector2i]s
 * @receiver The first vector
 * @param other The second vector
 * @return Square of the distance between the given [Vector2i]s
 */
inline infix fun Vector2i.distanceSqr(other: Vector2i): Double {
    return distanceSqr(x.toDouble(), y.toDouble(), other.x.toDouble(),
            other.y.toDouble())
}

/**
 * Returns the direction between `(1,0)`, the given vector and [other]
 * @receiver The first vector
 * @param other The second vector
 * @return The direction in radians
 */
inline fun Vector2i.direction(other: Vector2i): Double {
    return direction(x.toDouble(), y.toDouble(), other.x.toDouble(),
            other.y.toDouble())
}

/**
 * Returns the direction between `(1,0)`, `(0,0)` and the given vector
 * @receiver The vector
 * @return The direction in radians
 */
inline fun Vector2i.direction(): Double {
    return direction(x.toDouble(), y.toDouble())
}

/**
 * Returns the dot product of the given vectors
 * @receiver The first vector
 * @param other The second vector
 * @return Dot product of the given vectors
 */
inline infix fun Vector2i.dot(other: Vector2i): Double {
    return dot(x.toDouble(), y.toDouble(), other.x.toDouble(),
            other.y.toDouble())
}

/**
 * Checks if [point] is inside the region [origin] and [size]
 * @param origin Origin of the region
 * @param size Size of the region, all values should be positive
 * @param point The point to check
 * @return True if the point is inside, inclusive on lower end, exclusive on greater
 */
inline fun inside(origin: Vector2i,
                  size: Vector2i,
                  point: Vector2i): Boolean {
    return inside(origin.x.toDouble(), origin.y.toDouble(), size.x.toDouble(),
            size.y.toDouble(), point.x.toDouble(), point.y.toDouble())
}
