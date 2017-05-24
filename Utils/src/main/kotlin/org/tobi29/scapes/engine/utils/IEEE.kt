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

package org.tobi29.scapes.engine.utils

/*
/**
 * Converts the given number to an integer containing the IEEE 754
 * representation
 * @receiver The floating point number to convert
 * @return The data stored in an integer
 */
header fun Float.bits(): Int

/**
 * Converts the given number to an integer containing the IEEE 754
 * representation
 * @receiver The floating point number to convert
 * @return The data stored in an integer
 */
header fun Double.bits(): Long

/**
 * Converts the given integer to a floating point number using the IEEE 754
 * representation
 * @receiver The data bits to convert
 * @return The floating point containing the value encoded in the integer
 */
header fun Int.bitsToFloat(): Float

/**
 * Converts the given integer to a floating point number using the IEEE 754
 * representation
 * @receiver The data bits to convert
 * @return The floating point containing the value encoded in the integer
 */
header fun Double.bitsToDouble(): Double
*/
