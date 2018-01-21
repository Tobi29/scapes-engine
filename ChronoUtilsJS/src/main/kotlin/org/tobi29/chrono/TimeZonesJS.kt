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

import org.tobi29.utils.InstantNanos
import org.tobi29.utils.millis
import org.tobi29.utils.toInt128

actual val timeZoneUTC: TimeZone = timeZoneOf("UTC")

actual val timeZoneLocal: TimeZone = timeZoneOf(Moment.tz.guess())

actual fun timeZoneOf(name: String): TimeZone =
        MomentTimeZone(Moment.tz.zone(name))

class MomentTimeZone(val mtZone: Moment.MomentZone) : TimeZone {
    override fun encodeWithOffset(instant: InstantNanos): Set<OffsetDateTime> = try {
        val millis = instant.millis.toDouble()
        val offset = mtZone.utcOffset(millis).toDouble() * 60000.0
        val js = Moment.utc(millis - offset)
        val date = Date(js.year(), Month.ofValue(js.month() + 1), js.date())
        val time = Time(js.hours(), js.minutes(), js.seconds(),
                js.milliseconds() * 1000000)
        setOf(OffsetDateTime(DateTime(date, time),
                offset.toInt128() * 1000000.toInt128()))
    } catch (e: Exception) {
        emptySet()
    }

    override fun decode(dateTime: DateTime): Set<InstantNanos> = try {
        val moment = Moment.tz(arrayOf<Number>(dateTime.date.year,
                dateTime.date.month.value - 1,
                dateTime.date.day, dateTime.time.hour, dateTime.time.minute,
                dateTime.time.second, dateTime.time.nanosecond / 1000000),
                mtZone.name)
        val millis = moment.valueOf()
        setOf(millis.toInt128() * 1000000.toInt128())
    } catch (e: Exception) {
        emptySet()
    }
}
