package org.tobi29.scapes.engine.utils

/**
 * Time instant using milliseconds as unit
 *
 * **Note:** The value shall be consistent with unix timestamps whilst taking
 * timezones into account
 */
typealias InstantMillis = Long

/**
 * Time duration using milliseconds as unit
 */
typealias DurationMillis = Long

/**
 * Time instant using nanoseconds as unit
 *
 * **Note:** The origin and exact resolution may differ based on implementation,
 * however it should return usable values for calculating deltas in the range
 * of about a millisecond to a few years, however absolute values have no
 * meaning across multiple processes
 */
typealias InstantNanos = Long

/**
 * Time duration using nanoseconds as unit
 */
typealias DurationNanos = Long

/**
 * Source of time instants
 *
 * Invoking it multiple times
 */
interface Clock {
    /**
     * Returns system time instant in milliseconds
     * @returns Current time in milliseconds
     */
    fun timeMillis(): InstantMillis

    /**
     * Returns system time instant in nanoseconds
     * @returns Current time in nanoseconds
     */
    fun timeNanos(): InstantNanos = timeMillis() * 1000000L

    /**
     * Returns system time instant in milliseconds
     * @returns Current time in milliseconds
     * @see [timeMillis]
     */
    operator fun invoke(): InstantMillis = timeMillis()
}
