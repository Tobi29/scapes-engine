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

import org.tobi29.scapes.engine.utils.math.sqr
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Returns the length of the given vector
 * @param value1 The first value
 * @param value2 The second value
 * @return Length of the given vector
 */
inline fun length(value1: Float,
                  value2: Float): Float {
    return sqrt(lengthSqr(value1, value2))
}

/**
 * Returns the length of the given vector
 * @param value1 The first value
 * @param value2 The second value
 * @return Length of the given vector
 */
inline fun length(value1: Double,
                  value2: Double): Double {
    return sqrt(lengthSqr(value1, value2))
}

/**
 * Returns the length of the given vector
 * @param value1 The first value
 * @param value2 The second value
 * @param value3 The third value
 * @return Length of the given vector
 */
inline fun length(value1: Float,
                  value2: Float,
                  value3: Float): Float {
    return sqrt(lengthSqr(value1, value2, value3))
}

/**
 * Returns the length of the given vector
 * @param value1 The first value
 * @param value2 The second value
 * @param value3 The third value
 * @return Length of the given vector
 */
inline fun length(value1: Double,
                  value2: Double,
                  value3: Double): Double {
    return sqrt(lengthSqr(value1, value2, value3))
}

/**
 * Returns square of the length of the given vector
 * @param value1 The first value
 * @param value2 The second value
 * @return Square of the length of the given vector
 */
inline fun lengthSqr(value1: Int,
                     value2: Int): Int {
    return sqr(value1) + sqr(value2)
}

/**
 * Returns square of the length of the given vector
 * @param value1 The first value
 * @param value2 The second value
 * @return Square of the length of the given vector
 */
inline fun lengthSqr(value1: Float,
                     value2: Float): Float {
    return sqr(value1) + sqr(value2)
}

/**
 * Returns square of the length of the given vector
 * @param value1 The first value
 * @param value2 The second value
 * @return Square of the length of the given vector
 */
inline fun lengthSqr(value1: Long,
                     value2: Long): Long {
    return sqr(value1) + sqr(value2)
}

/**
 * Returns square of the length of the given vector
 * @param value1 The first value
 * @param value2 The second value
 * @return Square of the length of the given vector
 */
inline fun lengthSqr(value1: Double,
                     value2: Double): Double {
    return sqr(value1) + sqr(value2)
}

/**
 * Returns square of the length of the given vector
 * @param value1 The first value
 * @param value2 The second value
 * @param value3 The third value
 * @return Square of the length of the given vector
 */
inline fun lengthSqr(value1: Int,
                     value2: Int,
                     value3: Int): Int {
    return sqr(value1) + sqr(value2) + sqr(value3)
}

/**
 * Returns square of the length of the given vector
 * @param value1 The first value
 * @param value2 The second value
 * @param value3 The third value
 * @return Square of the length of the given vector
 */
inline fun lengthSqr(value1: Long,
                     value2: Long,
                     value3: Long): Long {
    return sqr(value1) + sqr(value2) + sqr(value3)
}

/**
 * Returns square of the length of the given vector
 * @param value1 The first value
 * @param value2 The second value
 * @param value3 The third value
 * @return Square of the length of the given vector
 */
inline fun lengthSqr(value1: Float,
                     value2: Float,
                     value3: Float): Float {
    return sqr(value1) + sqr(value2) + sqr(value3)
}

/**
 * Returns square of the length of the given vector
 * @param value1 The first value
 * @param value2 The second value
 * @param value3 The third value
 * @return Square of the length of the given vector
 */
inline fun lengthSqr(value1: Double,
                     value2: Double,
                     value3: Double): Double {
    return sqr(value1) + sqr(value2) + sqr(value3)
}

/**
 * Returns the distance between the given vectors
 * @param x1 x value of the first vector
 * @param x2 x value of the second vector
 * @return Distance between the given vectors
 */
inline fun distance(x1: Float,
                    x2: Float): Float {
    return abs(x1 - x2)
}

/**
 * Returns the distance between the given vectors
 * @param x1 x value of the first vector
 * @param x2 x value of the second vector
 * @return Distance between the given vectors
 */
inline fun distance(x1: Double,
                    x2: Double): Double {
    return abs(x1 - x2)
}

/**
 * Returns the distance between the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @return Distance between the given vectors
 */
inline fun distance(x1: Float,
                    y1: Float,
                    x2: Float,
                    y2: Float): Float {
    return sqrt(distanceSqr(x1, y1, x2, y2))
}

/**
 * Returns the distance between the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @return Distance between the given vectors
 */
inline fun distance(x1: Double,
                    y1: Double,
                    x2: Double,
                    y2: Double): Double {
    return sqrt(distanceSqr(x1, y1, x2, y2))
}

/**
 * Returns the distance between the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param z1 z value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @param z2 z value of the second vector
 * @return Distance between the given vectors
 */
inline fun distance(x1: Float,
                    y1: Float,
                    z1: Float,
                    x2: Float,
                    y2: Float,
                    z2: Float): Float {
    return sqrt(distanceSqr(x1, y1, z1, x2, y2, z2))
}

/**
 * Returns the distance between the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param z1 z value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @param z2 z value of the second vector
 * @return Distance between the given vectors
 */
inline fun distance(x1: Double,
                    y1: Double,
                    z1: Double,
                    x2: Double,
                    y2: Double,
                    z2: Double): Double {
    return sqrt(distanceSqr(x1, y1, z1, x2, y2, z2))
}

/**
 * Returns square of the distance between the given vectors
 * @param x1 x value of the first vector
 * @param x2 x value of the second vector
 * @return Square of the distance between the given vectors
 */
inline fun distanceSqr(x1: Int,
                       x2: Int): Int {
    return sqr(x1 - x2)
}

/**
 * Returns square of the distance between the given vectors
 * @param x1 x value of the first vector
 * @param x2 x value of the second vector
 * @return Square of the distance between the given vectors
 */
inline fun distanceSqr(x1: Long,
                       x2: Long): Long {
    return sqr(x1 - x2)
}

/**
 * Returns square of the distance between the given vectors
 * @param x1 x value of the first vector
 * @param x2 x value of the second vector
 * @return Square of the distance between the given vectors
 */
inline fun distanceSqr(x1: Float,
                       x2: Float): Float {
    return sqr(x1 - x2)
}

/**
 * Returns square of the distance between the given vectors
 * @param x1 x value of the first vector
 * @param x2 x value of the second vector
 * @return Square of the distance between the given vectors
 */
inline fun distanceSqr(x1: Double,
                       x2: Double): Double {
    return sqr(x1 - x2)
}

/**
 * Returns square of the distance between the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @return Square of the distance between the given vectors
 */
inline fun distanceSqr(x1: Int,
                       y1: Int,
                       x2: Int,
                       y2: Int): Int {
    return sqr(x1 - x2) + sqr(y1 - y2)
}

/**
 * Returns square of the distance between the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @return Square of the distance between the given vectors
 */
inline fun distanceSqr(x1: Long,
                       y1: Long,
                       x2: Long,
                       y2: Long): Long {
    return sqr(x1 - x2) + sqr(y1 - y2)
}

/**
 * Returns square of the distance between the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @return Square of the distance between the given vectors
 */
inline fun distanceSqr(x1: Float,
                       y1: Float,
                       x2: Float,
                       y2: Float): Float {
    return sqr(x1 - x2) + sqr(y1 - y2)
}

/**
 * Returns square of the distance between the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @return Square of the distance between the given vectors
 */
inline fun distanceSqr(x1: Double,
                       y1: Double,
                       x2: Double,
                       y2: Double): Double {
    return sqr(x1 - x2) + sqr(y1 - y2)
}

/**
 * Returns square of the distance between the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param z1 z value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @param z2 z value of the second vector
 * @return Square of the distance between the given vectors
 */
inline fun distanceSqr(x1: Int,
                       y1: Int,
                       z1: Int,
                       x2: Int,
                       y2: Int,
                       z2: Int): Int {
    return sqr(x1 - x2) + sqr(y1 - y2) + sqr(z1 - z2)
}

/**
 * Returns square of the distance between the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param z1 z value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @param z2 z value of the second vector
 * @return Square of the distance between the given vectors
 */
inline fun distanceSqr(x1: Long,
                       y1: Long,
                       z1: Long,
                       x2: Long,
                       y2: Long,
                       z2: Long): Long {
    return sqr(x1 - x2) + sqr(y1 - y2) + sqr(z1 - z2)
}

/**
 * Returns square of the distance between the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param z1 z value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @param z2 z value of the second vector
 * @return Square of the distance between the given vectors
 */
inline fun distanceSqr(x1: Float,
                       y1: Float,
                       z1: Float,
                       x2: Float,
                       y2: Float,
                       z2: Float): Float {
    return sqr(x1 - x2) + sqr(y1 - y2) + sqr(z1 - z2)
}

/**
 * Returns square of the distance between the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param z1 z value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @param z2 z value of the second vector
 * @return Square of the distance between the given vectors
 */
inline fun distanceSqr(x1: Double,
                       y1: Double,
                       z1: Double,
                       x2: Double,
                       y2: Double,
                       z2: Double): Double {
    return sqr(x1 - x2) + sqr(y1 - y2) + sqr(z1 - z2)
}

/**
 * Returns the direction between `(1,0)`, the first vector and the second vector
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @return The direction in radians
 */
inline fun direction(x1: Double,
                     y1: Double,
                     x2: Double,
                     y2: Double): Double {
    return atan2(y2 - y1, x2 - x1)
}

/**
 * Returns the direction between the `(1,0)`, `(0,0)` and the given vector
 * @param x x value of the vector
 * @param y y value of the vector
 * @return The direction in radians
 */
inline fun direction(x: Double,
                     y: Double): Double {
    return atan2(y, x)
}

/**
 * Returns the dot product of the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @return Dot product of the given vectors
 */
inline fun dot(x1: Int,
               y1: Int,
               x2: Int,
               y2: Int): Int {
    return x1 * x2 + y1 * y2
}

/**
 * Returns the dot product of the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @return Dot product of the given vectors
 */
inline fun dot(x1: Long,
               y1: Long,
               x2: Long,
               y2: Long): Long {
    return x1 * x2 + y1 * y2
}

/**
 * Returns the dot product of the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @return Dot product of the given vectors
 */
inline fun dot(x1: Float,
               y1: Float,
               x2: Float,
               y2: Float): Float {
    return x1 * x2 + y1 * y2
}

/**
 * Returns the dot product of the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @return Dot product of the given vectors
 */
inline fun dot(x1: Double,
               y1: Double,
               x2: Double,
               y2: Double): Double {
    return x1 * x2 + y1 * y2
}

/**
 * Returns the dot product of the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param z1 z value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @param z2 z value of the second vector
 * @return Dot product of the given vectors
 */
inline fun dot(x1: Int,
               y1: Int,
               z1: Int,
               x2: Int,
               y2: Int,
               z2: Int): Int {
    return x1 * x2 + y1 * y2 + z1 * z2
}

/**
 * Returns the dot product of the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param z1 z value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @param z2 z value of the second vector
 * @return Dot product of the given vectors
 */
inline fun dot(x1: Long,
               y1: Long,
               z1: Long,
               x2: Long,
               y2: Long,
               z2: Long): Long {
    return x1 * x2 + y1 * y2 + z1 * z2
}

/**
 * Returns the dot product of the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param z1 z value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @param z2 z value of the second vector
 * @return Dot product of the given vectors
 */
inline fun dot(x1: Float,
               y1: Float,
               z1: Float,
               x2: Float,
               y2: Float,
               z2: Float): Float {
    return x1 * x2 + y1 * y2 + z1 * z2
}

/**
 * Returns the dot product of the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param z1 z value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @param z2 z value of the second vector
 * @return Dot product of the given vectors
 */
inline fun dot(x1: Double,
               y1: Double,
               z1: Double,
               x2: Double,
               y2: Double,
               z2: Double): Double {
    return x1 * x2 + y1 * y2 + z1 * z2
}

/**
 * Returns the dot product of the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param z1 z value of the first vector
 * @param w1 w value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @param z2 z value of the second vector
 * @param w2 w value of the second vector
 * @return Dot product of the given vectors
 */
inline fun dot(x1: Int,
               y1: Int,
               z1: Int,
               w1: Int,
               x2: Int,
               y2: Int,
               z2: Int,
               w2: Int): Int {
    return x1 * x2 + y1 * y2 + z1 * z2 + w1 * w2
}

/**
 * Returns the dot product of the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param z1 z value of the first vector
 * @param w1 w value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @param z2 z value of the second vector
 * @param w2 w value of the second vector
 * @return Dot product of the given vectors
 */
inline fun dot(x1: Long,
               y1: Long,
               z1: Long,
               w1: Long,
               x2: Long,
               y2: Long,
               z2: Long,
               w2: Long): Long {
    return x1 * x2 + y1 * y2 + z1 * z2 + w1 * w2
}

/**
 * Returns the dot product of the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param z1 z value of the first vector
 * @param w1 w value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @param z2 z value of the second vector
 * @param w2 w value of the second vector
 * @return Dot product of the given vectors
 */
inline fun dot(x1: Float,
               y1: Float,
               z1: Float,
               w1: Float,
               x2: Float,
               y2: Float,
               z2: Float,
               w2: Float): Float {
    return x1 * x2 + y1 * y2 + z1 * z2 + w1 * w2
}

/**
 * Returns the dot product of the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param z1 z value of the first vector
 * @param w1 w value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @param z2 z value of the second vector
 * @param w2 w value of the second vector
 * @return Dot product of the given vectors
 */
inline fun dot(x1: Double,
               y1: Double,
               z1: Double,
               w1: Double,
               x2: Double,
               y2: Double,
               z2: Double,
               w2: Double): Double {
    return x1 * x2 + y1 * y2 + z1 * z2 + w1 * w2
}

/**
 * Returns the cross product of the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param z1 z value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @param z2 z value of the second vector
 * @return Dot product of the given vectors
 */
inline fun cross(x1: Int,
                 y1: Int,
                 z1: Int,
                 x2: Int,
                 y2: Int,
                 z2: Int): Vector3i =
        cross(x1, y1, z1, x2, y2, z2, ::Vector3i)

/**
 * Returns the cross product of the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param z1 z value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @param z2 z value of the second vector
 * @return Dot product of the given vectors
 */
inline fun cross(x1: Double,
                 y1: Double,
                 z1: Double,
                 x2: Double,
                 y2: Double,
                 z2: Double): Vector3d =
        cross(x1, y1, z1, x2, y2, z2, ::Vector3d)

/**
 * Computes the cross product of the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param z1 z value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @param z2 z value of the second vector
 * @param output Called with the output values
 * @return The return value of [output]
 */
inline fun <R> cross(x1: Int,
                     y1: Int,
                     z1: Int,
                     x2: Int,
                     y2: Int,
                     z2: Int,
                     output: (Int, Int, Int) -> R): R {
    val x = y1 * z2 - z1 * y2
    val y = z1 * x2 - x1 * z2
    val z = x1 * y2 - y1 * x2
    return output(x, y, z)
}

/**
 * Computes the cross product of the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param z1 z value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @param z2 z value of the second vector
 * @param output Called with the output values
 * @return The return value of [output]
 */
inline fun <R> cross(x1: Long,
                     y1: Long,
                     z1: Long,
                     x2: Long,
                     y2: Long,
                     z2: Long,
                     output: (Long, Long, Long) -> R): R {
    val x = y1 * z2 - z1 * y2
    val y = z1 * x2 - x1 * z2
    val z = x1 * y2 - y1 * x2
    return output(x, y, z)
}

/**
 * Computes the cross product of the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param z1 z value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @param z2 z value of the second vector
 * @param output Called with the output values
 * @return The return value of [output]
 */
inline fun <R> cross(x1: Float,
                     y1: Float,
                     z1: Float,
                     x2: Float,
                     y2: Float,
                     z2: Float,
                     output: (Float, Float, Float) -> R): R {
    val x = y1 * z2 - z1 * y2
    val y = z1 * x2 - x1 * z2
    val z = x1 * y2 - y1 * x2
    return output(x, y, z)
}

/**
 * Computes the cross product of the given vectors
 * @param x1 x value of the first vector
 * @param y1 y value of the first vector
 * @param z1 z value of the first vector
 * @param x2 x value of the second vector
 * @param y2 y value of the second vector
 * @param z2 z value of the second vector
 * @param output Called with the output values
 * @return The return value of [output]
 */
inline fun <R> cross(x1: Double,
                     y1: Double,
                     z1: Double,
                     x2: Double,
                     y2: Double,
                     z2: Double,
                     output: (Double, Double, Double) -> R): R {
    val x = y1 * z2 - z1 * y2
    val y = z1 * x2 - x1 * z2
    val z = x1 * y2 - y1 * x2
    return output(x, y, z)
}

/**
 * Checks if the point is inside the region
 * @param x1 x value of the origin of the region
 * @param y1 y value of the origin of the region
 * @param x2 x value of the size of the region, should be positive
 * @param y2 y value of the size of the region, should be positive
 * @param x x value of the point to check
 * @param y y value of the point to check
 * @return True if the point is inside, inclusive on lower end, exclusive on greater
 */
inline fun inside(x1: Float,
                  y1: Float,
                  x2: Float,
                  y2: Float,
                  x: Float,
                  y: Float): Boolean {
    val xx = x - x1
    val yy = y - y1
    return xx >= 0.0f && yy >= 0.0f && xx < x2 && yy < y2
}

/**
 * Checks if the point is inside the region
 * @param x1 x value of the origin of the region
 * @param y1 y value of the origin of the region
 * @param x2 x value of the size of the region, should be positive
 * @param y2 y value of the size of the region, should be positive
 * @param x x value of the point to check
 * @param y y value of the point to check
 * @return True if the point is inside, inclusive on lower end, exclusive on greater
 */
inline fun inside(x1: Double,
                  y1: Double,
                  x2: Double,
                  y2: Double,
                  x: Double,
                  y: Double): Boolean {
    val xx = x - x1
    val yy = y - y1
    return xx >= 0.0 && yy >= 0.0 && xx < x2 && yy < y2
}

/**
 * Checks if the point is inside the region
 * @param x1 x value of the origin of the region
 * @param y1 y value of the origin of the region
 * @param z1 z value of the origin of the region
 * @param x2 x value of the size of the region, should be positive
 * @param y2 y value of the size of the region, should be positive
 * @param z2 z value of the size of the region, should be positive
 * @param x x value of the point to check
 * @param y y value of the point to check
 * @param z z value of the point to check
 * @return True if the point is inside, inclusive on lower end, exclusive on greater
 */
inline fun inside(x1: Float,
                  y1: Float,
                  z1: Float,
                  x2: Float,
                  y2: Float,
                  z2: Float,
                  x: Float,
                  y: Float,
                  z: Float): Boolean {
    val xx = x - x1
    val yy = y - y1
    val zz = z - z1
    return xx >= 0.0 && yy >= 0.0f && zz >= 0.0f && xx < x2 && yy < y2 &&
            zz < z2
}

/**
 * Checks if the point is inside the region
 * @param x1 x value of the origin of the region
 * @param y1 y value of the origin of the region
 * @param z1 z value of the origin of the region
 * @param x2 x value of the size of the region, should be positive
 * @param y2 y value of the size of the region, should be positive
 * @param z2 z value of the size of the region, should be positive
 * @param x x value of the point to check
 * @param y y value of the point to check
 * @param z z value of the point to check
 * @return True if the point is inside, inclusive on lower end, exclusive on greater
 */
inline fun inside(x1: Double,
                  y1: Double,
                  z1: Double,
                  x2: Double,
                  y2: Double,
                  z2: Double,
                  x: Double,
                  y: Double,
                  z: Double): Boolean {
    val xx = x - x1
    val yy = y - y1
    val zz = z - z1
    return xx >= 0.0 && yy >= 0.0 && zz >= 0.0 && xx < x2 && yy < y2 &&
            zz < z2
}
