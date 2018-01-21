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

package org.tobi29.chrono

import org.tobi29.utils.*
import org.threeten.bp.Instant as TTInstant
import org.threeten.bp.LocalDate as TTLocalDate
import org.threeten.bp.LocalDateTime as TTLocalDateTime
import org.threeten.bp.LocalTime as TTLocalTime
import org.threeten.bp.OffsetDateTime as TTOffsetDateTime
import org.threeten.bp.ZoneId as TTZoneId
import org.threeten.bp.ZoneOffset as TTZoneOffset

actual val timeZoneUTC: TimeZone = ThreeTenTimeZone(TTZoneOffset.UTC)

actual val timeZoneLocal: TimeZone = ThreeTenTimeZone(TTZoneId.systemDefault())

actual fun timeZoneOf(name: String): TimeZone =
        ThreeTenTimeZone(TTZoneId.of(name))

class ThreeTenTimeZone(val ttZone: TTZoneId) : TimeZone {
    override fun encodeWithOffset(instant: InstantNanos): Set<OffsetDateTime> = try {
        setOf(instant.ttInstant.atZone(
                ttZone).toOffsetDateTime().chOffsetDateTime)
    } catch (e: Exception) {
        emptySet()
    }

    override fun decode(dateTime: DateTime): Set<InstantNanos> = try {
        setOf(dateTime.ttLocalDateTime.atZone(ttZone).toInstant().chInstant)
    } catch (e: Exception) {
        emptySet()
    }
}

val TTInstant.chInstant
    get() = epochSecond.toInt128() * 1000000000.toInt128() + nano.toInt128()

val InstantNanos.ttInstant: TTInstant
    get() = TTInstant.ofEpochSecond(seconds.also {
        if (it > Long.MAX_VALUE.toInt128()
                || it < Long.MIN_VALUE.toInt128())
            throw IllegalArgumentException("Instant ouf of range")
    }.toLongClamped(), (nanos % 1000000000.toInt128()).toLong())

val TTOffsetDateTime.chOffsetDateTime
    get() = OffsetDateTime(toLocalDateTime().chDateTime,
            Duration.fromSeconds(offset.totalSeconds.toLong()))

val TTLocalDateTime.chDateTime
    get() = DateTime(date = toLocalDate().chDate, time = toLocalTime().chTime)

val DateTime.ttLocalDateTime: TTLocalDateTime
    get() = TTLocalDateTime.of(date.ttLocalDate, time.ttLocalTime)

val TTLocalDate.chDate
    get() = Date(year = year, month = Month.ofValue(monthValue),
            day = dayOfMonth)

val Date.ttLocalDate: TTLocalDate
    get() = TTLocalDate.of(year, month.value, day)

val TTLocalTime.chTime
    get() = Time(hour = hour, minute = minute, second = second,
            nanosecond = nano)

val Time.ttLocalTime: TTLocalTime
    get() = TTLocalTime.of(hour, minute, second, nanosecond)

