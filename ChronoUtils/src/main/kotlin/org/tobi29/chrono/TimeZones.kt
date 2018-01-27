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

interface TimeZone {
    fun encode(instant: InstantNanos): Set<DateTime> = encodeWithOffset(instant)
        .asSequence().map { it.dateTime }.toSet()

    fun encodeWithOffset(instant: InstantNanos): Set<OffsetDateTime>

    fun decode(dateTime: DateTime): Set<InstantNanos>
}

/**
 * UTC time zone, behaves the same across all systems
 */
expect val timeZoneUTC: TimeZone

/**
 * Local system time zone
 */
expect val timeZoneLocal: TimeZone

/**
 * Time zone for given name
 * @param name Name of timezone, currently implementation dependant
 * @return Time zone handle
 */
expect fun timeZoneOf(name: String): TimeZone
