package org.tobi29.scapes.engine.utils

/**
 * Time instant using nanoseconds as unit
 *
 * **Note:** The value shall be consistent with unix timestamps with UTC
 * as the timezone
 */
typealias InstantNanos = Int128

/**
 * Time duration using nanoseconds as unit
 */
typealias DurationNanos = Int128

/**
 * Time duration using nanoseconds as unit
 */
typealias Duration64Nanos = Long

/**
 * Time instant using milliseconds as unit
 *
 * **Note:** The value shall be consistent with unix timestamps with UTC
 * as the timezone
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
typealias InstantSteadyNanos = Long

/**
 * Nanoseconds in a millisecond
 */
val NANOS_PER_MILLISECOND: Int128 = (1000L * 1000L).toInt128()

/**
 * Nanoseconds in a second
 */
val NANOS_PER_SECOND: Int128 = NANOS_PER_MILLISECOND * (1000L).toInt128()

/**
 * Nanoseconds in a minute
 */
val NANOS_PER_MINUTE: Int128 = NANOS_PER_SECOND * (60L).toInt128()

/**
 * Nanoseconds in an hour
 */
val NANOS_PER_HOUR: Int128 = NANOS_PER_MINUTE * (60L).toInt128()

/**
 * Nanoseconds in a day
 */
val NANOS_PER_DAY: Int128 = NANOS_PER_HOUR * (24L).toInt128()

/**
 * Convert the given time to nanoseconds
 * @receiver Nanosecond time to convert
 */
val InstantNanos.nanos: Int128 get() = this

/**
 * Convert the given time to milliseconds
 * @receiver Nanosecond time to convert
 */
val InstantNanos.millis: Int128 get() = nanos / NANOS_PER_MILLISECOND

/**
 * Convert the given time to seconds
 * @receiver Nanosecond time to convert
 */
val InstantNanos.seconds: Int128 get() = nanos / NANOS_PER_SECOND

/**
 * Object for converting timestamps of various units to nanoseconds
 */
object Instant {
    /**
     * Convert a nanosecond timestamp to nanoseconds
     * @param nanos Timestamp to convert
     * @return Timestamp in nanoseconds
     */
    fun fromNanos(nanos: Int128): InstantNanos = nanos

    /**
     * Convert a millisecond timestamp to nanoseconds
     * @param millis Timestamp to convert
     * @return Timestamp in nanoseconds
     */
    fun fromMillis(millis: Int128): InstantNanos =
            millis * NANOS_PER_MILLISECOND

    /**
     * Convert a millisecond timestamp to nanoseconds
     * @param millis Timestamp to convert
     * @return Timestamp in nanoseconds
     */
    fun fromMillis(millis: InstantMillis): InstantNanos =
            fromMillis(millis.toInt128())

    /**
     * Convert a second timestamp to nanoseconds
     * @param seconds Timestamp to convert
     * @return Timestamp in nanoseconds
     */
    fun fromSeconds(seconds: Int128): InstantNanos =
            seconds * NANOS_PER_SECOND

    /**
     * Convert a second timestamp to nanoseconds
     * @param seconds Timestamp to convert
     * @return Timestamp in nanoseconds
     */
    fun fromSeconds(seconds: Long): InstantNanos =
            fromSeconds(seconds.toInt128())
}

/*
  Kotlin has no support for creating a new type from existing classes yet

/**
 * Convert the given time to nanoseconds
 * @receiver Nanosecond time to convert
 */
val DurationNanos.nanos: Int128 get() = this

/**
 * Convert the given time to milliseconds
 * @receiver Nanosecond time to convert
 */
val DurationNanos.millis: Int128 get() = nanos / NANOS_PER_MILLISECOND

/**
 * Convert the given time to seconds
 * @receiver Nanosecond time to convert
 */
val DurationNanos.seconds: Int128 get() = nanos / NANOS_PER_SECOND
*/

/**
 * Convert the given time to minutes
 * @receiver Nanosecond time to convert
 */
val DurationNanos.minutes: Int128 get() = nanos / NANOS_PER_MINUTE

/**
 * Convert the given time to hours
 * @receiver Nanosecond time to convert
 */
val DurationNanos.hours: Int128 get() = nanos / NANOS_PER_HOUR

/**
 * Convert the given time to days
 * @receiver Nanosecond time to convert
 */
val DurationNanos.days: Int128 get() = nanos / NANOS_PER_DAY

/**
 * Object for converting durations of various units to nanoseconds
 */
object Duration {
    /**
     * Convert a nanosecond duration to nanoseconds
     * @param nanos Duration to convert
     * @return Duration in nanoseconds
     */
    fun fromNanos(nanos: Int128): DurationNanos = nanos

    /**
     * Convert a nanosecond duration to nanoseconds
     * @param nanos Duration to convert
     * @return Duration in nanoseconds
     */
    fun fromNanos(nanos: Duration64Nanos): DurationNanos =
            nanos.toInt128()

    /**
     * Convert a millisecond duration to nanoseconds
     * @param millis Duration to convert
     * @return Duration in nanoseconds
     */
    fun fromMillis(millis: Int128): DurationNanos =
            millis * NANOS_PER_MILLISECOND

    /**
     * Convert a millisecond duration to nanoseconds
     * @param millis Duration to convert
     * @return Duration in nanoseconds
     */
    fun fromMillis(millis: DurationMillis): DurationNanos =
            fromMillis(millis.toInt128())

    /**
     * Convert a second duration to nanoseconds
     * @param seconds Duration to convert
     * @return Duration in nanoseconds
     */
    fun fromSeconds(seconds: Int128): DurationNanos =
            seconds * NANOS_PER_SECOND

    /**
     * Convert a second duration to nanoseconds
     * @param seconds Duration to convert
     * @return Duration in nanoseconds
     */
    fun fromSeconds(seconds: Long): DurationNanos =
            fromSeconds(seconds.toInt128())

    /**
     * Convert a minute duration to nanoseconds
     * @param minutes Duration to convert
     * @return Duration in nanoseconds
     */
    fun fromMinutes(minutes: Int128): DurationNanos =
            minutes * NANOS_PER_MINUTE

    /**
     * Convert a minute duration to nanoseconds
     * @param minutes Duration to convert
     * @return Duration in nanoseconds
     */
    fun fromMinutes(minutes: Long): DurationNanos =
            fromMinutes(minutes.toInt128())

    /**
     * Convert an hour duration to nanoseconds
     * @param hours Duration to convert
     * @return Duration in nanoseconds
     */
    fun fromHours(hours: Int128): DurationNanos =
            hours * NANOS_PER_HOUR

    /**
     * Convert an hour duration to nanoseconds
     * @param hours Duration to convert
     * @return Duration in nanoseconds
     */
    fun fromHours(hours: Long): DurationNanos =
            fromHours(hours.toInt128())

    /**
     * Convert a day duration to nanoseconds
     * @param days Duration to convert
     * @return Duration in nanoseconds
     */
    fun fromDays(days: Int128): DurationNanos =
            days * NANOS_PER_DAY

    /**
     * Convert a day duration to nanoseconds
     * @param days Duration to convert
     * @return Duration in nanoseconds
     */
    fun fromDays(days: Long): DurationNanos =
            fromDays(days.toInt128())
}


/**
 * Source of time instants
 */
interface Clock {
    /**
     * Returns time instant in milliseconds
     * @return Current time in milliseconds
     */
    fun timeMillis(): InstantMillis =
            (timeNanos() / 1000000L.toInt128()).toLongClamped()

    /**
     * Returns time instant in nanoseconds
     * @return Current time in nanoseconds
     */
    fun timeNanos(): InstantNanos

    /**
     * Returns time instant in nanoseconds
     *
     * Convenience operator for [timeNanos]
     * @return Current time in nanoseconds
     * @see [timeNanos]
     */
    operator fun invoke(): InstantNanos = timeNanos()
}

/**
 * Source of timestamps
 *
 * Unlike [Clock] this has no specific origin but instead guarantees that value
 * monotonically increase.
 */
interface SteadyClock {
    /**
     * Returns a timestamp in nanoseconds
     *
     * Invoking it again guarantees a value greater or equal to this one
     * @return Current time in nanoseconds
     */
    fun timeSteadyNanos(): InstantSteadyNanos
}
