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

package org.tobi29.scapes.engine.utils.math

/**
 * Returns the cube-root value of [value]
 * @param value The value
 * @return Cube-root value of [value]
 */
expect fun cbrt(value: Float): Float

/**
 * Returns the cube-root value of [value]
 * @param value The value
 * @return Cube-root value of [value]
 */
expect fun cbrt(value: Double): Double

/**
 * Returns the next integer below the given value
 * @receiver value The value
 * @return Next integer below the given value
 */
expect fun Float.floorToInt(): Int

/**
 * Returns the next integer below the given value
 * @receiver The value
 * @return Next integer below the given value
 */
expect fun Float.floorToLong(): Long

/**
 * Returns the next integer below the given value
 * @receiver value The value
 * @return Next integer below the given value
 */
expect fun Double.floorToInt(): Int

/**
 * Returns the next integer below the given value
 * @receiver The value
 * @return Next integer below the given value
 */
expect fun Double.floorToLong(): Long

/**
 * Returns the next integer above the given value
 * @receiver value The value
 * @return Next integer above the given value
 */
expect fun Float.ceilToInt(): Int

/**
 * Returns the next integer above the given value
 * @receiver The value
 * @return Next integer above the given value
 */
expect fun Float.ceilToLong(): Long

/**
 * Returns the next integer above the given value
 * @receiver value The value
 * @return Next integer above the given value
 */
expect fun Double.ceilToInt(): Int

/**
 * Returns the next integer above the given value
 * @receiver The value
 * @return Next integer above the given value
 */
expect fun Double.ceilToLong(): Long

/**
 * Counts the amount of leading zeros
 * @param value The value
 * @return The amount of leading zeros in range 0..32
 */
expect fun clz(value: Int): Int
