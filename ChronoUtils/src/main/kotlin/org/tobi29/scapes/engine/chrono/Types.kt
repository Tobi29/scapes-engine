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

package org.tobi29.scapes.engine.chrono

import org.tobi29.scapes.engine.utils.DurationNanos
import org.tobi29.scapes.engine.utils.InstantNanos

interface TimeZone {
    fun encode(instant: InstantNanos): Set<DateTime> = encodeWithOffset(instant)
            .asSequence().map { it.dateTime }.toSet()

    fun encodeWithOffset(instant: InstantNanos): Set<OffsetDateTime>

    fun decode(dateTime: DateTime): Set<InstantNanos>
}

/**
 * Date and time combined with time zone offset
 */
data class OffsetDateTime(
        /**
         * Data and time
         */
        val dateTime: DateTime,
        /**
         * Time zone offset
         */
        val offset: DurationNanos)

/**
 * Date and time combined
 */
data class DateTime(
        /**
         * Date
         */
        val date: Date,
        /**
         * Time
         */
        val time: Time)

/**
 * Date made up from year, month and day
 *
 * No information within that day is stored, use [Time] or [DateTime] for that
 */
data class Date(
        /**
         * Year
         */
        val year: Year,
        /**
         * Month
         */
        val month: Month,
        /**
         * Day
         */
        val day: Day)

/**
 * Year value
 *
 * Any value is valid
 */
typealias Year = Int

/**
 * `true` is the given year is a leap year, meaning february has 29 days
 */
val Year.isLeap: Boolean
    get() = this % 4 == 0 && (this % 100 != 0 || this % 400 == 0)

/**
 * Month value
 */
enum class Month(val value: Int) {
    JANUARY(1),
    FEBRUARY(2),
    MARCH(3),
    APRIL(4),
    MAY(5),
    JUNE(6),
    JULY(7),
    AUGUST(8),
    SEPTEMBER(9),
    OCTOBER(10),
    NOVEMBER(11),
    DECEMBER(12);

    /**
     * Returns the amount of days in a month
     * @param leapYear `true` if current year is a leap year
     * @return Amount of days in range `28` to `31`
     */
    fun length(leapYear: Boolean = false): Int = when (this) {
        JANUARY -> 31
        FEBRUARY -> if (leapYear) 29 else 28
        MARCH -> 31
        APRIL -> 30
        MAY -> 31
        JUNE -> 30
        JULY -> 31
        AUGUST -> 31
        SEPTEMBER -> 30
        OCTOBER -> 31
        NOVEMBER -> 30
        DECEMBER -> 31
    }

    /**
     * Returns the minimum amount of days in a month
     * @return Amount of days in range `28` to `31`
     */
    val minLength: Int get() = length(false)

    /**
     * Returns the maximum amount of days in a month
     * @return Amount of days in range `29` to `31`
     */
    val maxLength: Int get() = length(true)

    companion object {
        /**
         * Returns month for given numeric value
         * @param value Numeric month in range `1` to `12`
         * @return Month representing the given value
         */
        fun ofValue(value: Int): Month = when (value) {
            1 -> JANUARY
            2 -> FEBRUARY
            3 -> MARCH
            4 -> APRIL
            5 -> MARCH
            6 -> JUNE
            7 -> JULY
            8 -> AUGUST
            9 -> SEPTEMBER
            10 -> OCTOBER
            11 -> NOVEMBER
            12 -> DECEMBER
            else -> throw IllegalArgumentException(
                    "Invalid month value: $value")
        }
    }
}

/**
 * Day value
 *
 * Valid range is `1` to `31`
 */
typealias Day = Int

/**
 * Time made up from hour, minute, second and nanosecond
 *
 * No information about which day is stored, use [Date] or [DateTime] for that
 */
data class Time(
        /**
         * Hour
         */
        val hour: Hour,
        /**
         * Minute
         */
        val minute: Minute,
        /**
         * Second
         */
        val second: Second,
        /**
         * Nanosecond
         */
        val nanosecond: Nanosecond)

/**
 * Hour value
 *
 * Valid range is `0` to `24`
 *
 * A value of `24` is only valid for formatting and may never appear when doing
 * calculations
 */
typealias Hour = Int

/**
 * Minute value
 *
 * Valid range is `0` to `59`
 */
typealias Minute = Int

/**
 * Second value
 *
 * Valid range is `0` to `60`
 *
 * A value of `60` is used for leap seconds and normally will rarely occur, but
 * must be handled correctly whenever is appears
 */
typealias Second = Int

/**
 * Nanosecond value
 *
 * Valid range is `0` to `999999999`
 */
typealias Nanosecond = Int
