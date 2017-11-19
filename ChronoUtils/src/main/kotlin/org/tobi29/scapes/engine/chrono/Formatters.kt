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

import org.tobi29.scapes.engine.utils.*

/**
 * Prints a date time in ISO 8601 format excluding milliseconds:
 * [+-]?Y*YYYY-MM-DDThh:mm:ss
 */
fun isoDateTime(dateTime: DateTime) = dateTime.run {
    "${isoDate(date)}T${isoTime(time)}"
}

/**
 * Prints a date time in ISO 8601 format including milliseconds:
 * [+-]?Y*YYYY-MM-DDThh:mm:ss.sss
 */
fun isoDateTimeWithMillis(dateTime: DateTime) = dateTime.run {
    "${isoDate(date)}T${isoTimeWithMillis(time)}"
}

/**
 * Prints a date time and offset in ISO 8601 format excluding milliseconds:
 * [+-]?Y*YYYY-MM-DDThh:mm:ss(Z|[+-]hh:mm)
 */
fun isoOffsetDateTime(offsetDateTime: OffsetDateTime) = offsetDateTime.run {
    "${isoDateTime(dateTime)}${isoOffset(offset)}"
}

/**
 * Prints a date time and offset in ISO 8601 format including milliseconds:
 * [+-]?Y*YYYY-MM-DDThh:mm:ss.sss(Z|[+-]hh:mm)
 */
fun isoOffsetDateTimeWithMillis(offsetDateTime: OffsetDateTime) = offsetDateTime.run {
    "${isoDateTimeWithMillis(dateTime)}${isoOffset(offset)}"
}

/**
 * Prints a date in ISO 8601 format: [+-]?Y*YYYY-MM-DD
 */
fun isoDate(date: Date): String = date.run {
    "${isoYear(year)}-${isoMonth(month)}-${isoDay(day)}"
}

/**
 * Prints a year in ISO 8601 format: [+-]?Y*YYYY
 */
fun isoYear(year: Year,
            maxLength: Int = Int.MAX_VALUE,
            allowNegative: Boolean = true): String {
    if (maxLength < 4)
        throw IllegalArgumentException("Invalid max length: $maxLength")

    return if (year < 0) {
        if (!allowNegative) {
            throw IllegalArgumentException("Negative year: $year")
        }
        "-${(if (year == Int.MIN_VALUE) Int.MAX_VALUE
        else 1 - year).toString().prefixToLength('0', 4, maxLength)}"

    } else {
        year.toString().prefixToLength('0', 4, maxLength)
    }
}

/**
 * Prints a month in ISO 8601 format: MM
 */
fun isoMonth(month: Month): String {
    return month.value.toString(length = 2)
}

/**
 * Prints a day in ISO 8601 format: DD
 */
fun isoDay(day: Day): String {
    if (day !in 1..31)
        throw IllegalArgumentException("Invalid day: $day")

    return day.toString(length = 2)
}

/**
 * Prints a time in ISO 8601 format excluding milliseconds: hh:mm:ss
 */
fun isoTime(time: Time): String = time.run {
    "${isoHour(hour)}:${isoMinute(minute)}:${isoSecond(second)}"
}

/**
 * Prints a time in ISO 8601 format including milliseconds: hh:mm:ss.sss
 */
fun isoTimeWithMillis(time: Time): String = time.run {
    "${isoTime(this)}.${isoMillisecondFromNanos(nanosecond)}"
}

/**
 * Prints an hour in ISO 8601 format: hh
 */
fun isoHour(hour: Hour): String {
    if (hour !in 0..24)
        throw IllegalArgumentException("Invalid hour: $hour")

    return hour.toString(length = 2)
}

/**
 * Prints a minute in ISO 8601 format: mm
 */
fun isoMinute(minute: Minute): String {
    if (minute !in 0..59)
        throw IllegalArgumentException("Invalid minute: $minute")

    return minute.toString(length = 2)
}

/**
 * Prints a second in ISO 8601 format: ss
 */
fun isoSecond(second: Second): String {
    if (second !in 0..60)
        throw IllegalArgumentException("Invalid second: $second")

    return second.toString(length = 2)
}

/**
 * Prints a millisecond in ISO 8601 format: sss
 */
fun isoMillisecondFromNanos(nanosecond: Nanosecond): String =
        isoMillisecond(nanosecond / 1000000)

/**
 * Prints a millisecond in ISO 8601 format: sss
 */
fun isoMillisecond(millisecond: Int): String {
    if (millisecond !in 0..999)
        throw IllegalArgumentException("Invalid millisecond: $millisecond")

    return millisecond.toString(length = 3)
}

/**
 * Prints an offset in ISO 8601 format: (Z|[+-]hh:mm)
 */
fun isoOffset(offset: DurationNanos): String {
    if (offset.nanos == 0.toInt128())
        return "Z"

    if (offset.nanos % NANOS_PER_MINUTE != 0.toInt128()) {
        throw IllegalArgumentException("Offset not aligned to minutes: $offset")
    }

    val negative = offset.nanos < 0.toInt128()

    val abs = if (negative) -offset else offset

    val hours = abs.hours.toInt()
    val minutes = (abs.minutes % 60.toInt128()).toInt()

    return "${if (negative) '-' else '+'}${isoHour(hours)}:${isoMinute(
            minutes)}"
}
