/*
 * Copyright 2012-2018 Tobi29
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

package org.tobi29.chrono

import org.tobi29.stdex.math.remP
import org.tobi29.utils.DurationNanos
import org.tobi29.utils.Int128
import org.tobi29.utils.toInt128
import org.tobi29.utils.toLongClamped

typealias EpochNanos = Int128

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
    val offset: DurationNanos
)

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
    val time: Time
)

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
    val day: Day
)

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

    fun firstDayInYear(leapYear: Boolean = false): Int = when (this) {
        JANUARY -> 0
        FEBRUARY -> 31
        MARCH -> 31 + FEBRUARY.length(leapYear)
        APRIL -> 31 + FEBRUARY.length(leapYear) + 31
        MAY -> 31 + FEBRUARY.length(leapYear) + 31 + 30
        JUNE -> 31 + FEBRUARY.length(leapYear) + 31 + 30 + 31
        JULY -> 31 + FEBRUARY.length(leapYear) + 31 + 30 + 31 + 30
        AUGUST -> 31 + FEBRUARY.length(leapYear) + 31 + 30 + 31 + 30 + 31
        SEPTEMBER -> 31 + FEBRUARY.length(leapYear) + 31 + 30 + 31 + 30 + 31 + 31
        OCTOBER -> 31 + FEBRUARY.length(leapYear) + 31 + 30 + 31 + 30 + 31 + 31 + 30
        NOVEMBER -> 31 + FEBRUARY.length(leapYear) + 31 + 30 + 31 + 30 + 31 + 31 + 30 + 31
        DECEMBER -> 31 + FEBRUARY.length(leapYear) + 31 + 30 + 31 + 30 + 31 + 31 + 30 + 31 + 30
    }

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
                "Invalid month value: $value"
            )
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
    val nanosecond: Nanosecond
)

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

fun DateTime.toEpochNanos(): EpochNanos {
    var days = date.day - 1L + date.month.firstDayInYear(date.year.isLeap)

    var year = date.year
    if (year >= 400) {
        days += (year / 400) * 146097L
    } else if (year <= -400) {
        days += ((year + 399) / 400 - 1) * 146097L
    }
    year %= 400
    days += (epochYear until year)
        .sumBy { if (it.isLeap) 366 else 365 }
    days -= (year until epochYear)
        .sumBy { if (it.isLeap) 366 else 365 }

    return (((days * 24L + time.hour) * 60L + time.minute) * 60L +
            time.second).toInt128() * 1000000000L.toInt128() +
            time.nanosecond.toInt128()
}

fun EpochNanos.toDateTime(): DateTime {
    // We return the maximum/minimum values possible in a DateTime
    // to avoid surprising overflows or expections
    if (this >= "67767976233532799999999999".toInt128())
        return DateTime(
            Date(Int.MAX_VALUE, Month.DECEMBER, 31),
            Time(23, 59, 59, 999999999)
        )
    else if (this <= "-67768100567971200000000000".toInt128())
        return DateTime(
            Date(Int.MIN_VALUE, Month.JANUARY, 1),
            Time(0, 0, 0, 0)
        )

    val nanosecond128 = this remP 1000000000L.toInt128()
    val nanosecond = nanosecond128.toInt()
    var remaining =
        ((this - nanosecond128) / 1000000000L.toInt128()).toLongClamped()
    val second = (remaining remP 60L).toInt()
    remaining -= second
    remaining /= 60L
    val minute = (remaining remP 60L).toInt()
    remaining -= minute
    remaining /= 60L
    val hour = (remaining remP 24L).toInt()
    remaining -= hour
    remaining /= 24L

    var year = epochYear
    if (remaining >= 146097L) {
        year += (remaining / 146097L).toInt() * 400
    } else if (remaining <= -146097L) {
        year += ((remaining + 146096L) / 146097L - 1L).toInt() * 400
    }
    remaining %= 146097L
    if (remaining > 0) {
        while (true) {
            val length = if (year.isLeap) 366 else 365
            if (remaining < length) break
            remaining -= length
            year++
        }
    } else {
        while (remaining < 0) {
            year--
            remaining += if (year.isLeap) 366 else 365
        }
    }

    val month = Month.values()
        .last { it.firstDayInYear(year.isLeap) <= remaining }
    remaining -= month.firstDayInYear(year.isLeap)
    val day = remaining.toInt() + 1
    val date = Date(year, month, day)
    val time = Time(hour, minute, second, nanosecond)
    return DateTime(date, time)
}

private const val epochYear = 1970
