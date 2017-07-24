package org.tobi29.scapes.engine.utils

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
header fun Long.bitsToDouble(): Double

header fun Int.toString(radix: Int): String

header fun Long.toString(radix: Int): String
