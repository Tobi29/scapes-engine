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

import org.tobi29.utils.DurationNanos
import org.tobi29.utils.InstantNanos
import org.tobi29.utils.toInt128
import org.tobi29.utils.toLongClamped

/**
 * UTC time zone, behaves the same across all systems
 */
val timeZoneUtc: TimeZone get() = timeZoneOf("UTC")

/**
 * Local system time zone
 */
expect val timeZoneLocal: TimeZone

/**
 * Time zone for given name
 * @param name Name of timezone, currently implementation dependant
 * @return Time zone handle
 */
fun timeZoneOf(name: String): TimeZone = TzData.tzdata[name]
        ?: throw IllegalArgumentException("Invalid zone: $name")

val timeZones: Sequence<TimeZone>
    get() = TzData.tzdata.asSequence().map { timeZoneOf(it.key) }

class TimeZone internal constructor(
    val id: String,
    private val data: Lazy<TzEntry>
) {
    private val initial: OffsetZone get() = data.value.first
    private val sinceData: List<SinceData> get() = data.value.second

    fun offsetAt(instant: InstantNanos): DurationNanos {
        val instantFast = (instant / 1000000000L.toInt128()).toLongClamped()
        windows { start, end, offset ->
            if (instantFast < end)
                return offset.offset.toInt128() * 1000000000L.toInt128()
        }
        throw IllegalStateException("Internal error")
    }

    fun offsetsInto(epoch: EpochNanos): Set<DurationNanos> {
        val epochFast = (epoch / 1000000000L.toInt128()).toLongClamped()
        val offsets = HashSet<DurationNanos>()
        windows { start, end, offset ->
            if (epochFast - offset.offset in start until end)
                offsets.add(offset.offset.toInt128() * 1000000000L.toInt128())
        }
        return offsets
    }

    fun dumpRanges(): List<Triple<DateTime, DateTime, DurationNanos>> {
        val ranges =
            ArrayList<Triple<DateTime, DateTime, DurationNanos>>(sinceData.size + 1)
        windows { start, end, offset ->
            ranges.add(
                Triple(
                    (start.toInt128() * 1000000000L.toInt128()).toDateTime(),
                    (end.toInt128() * 1000000000L.toInt128()).toDateTime(),
                    offset.offset.toInt128() * 1000000000L.toInt128()
                )
            )
        }
        return ranges
    }

    private inline fun windows(block: (Long, Long, OffsetZone) -> Unit) {
        var begin = Long.MIN_VALUE
        var end = 0L
        var offset = initial
        repeat(sinceData.size + 1) { i ->
            val since = sinceData.getOrNull(i)
            end = if (since != null) end + since.since else Long.MAX_VALUE
            block(begin, end, offset)
            begin = end
            if (since != null) {
                offset = sinceData[i].offset
            }
        }
    }
}

fun TimeZone.encode(instant: InstantNanos): DateTime =
    offsetAt(instant).let { (instant + it).toDateTime() }

fun TimeZone.encodeWithOffset(instant: InstantNanos): OffsetDateTime =
    offsetAt(instant).let { OffsetDateTime((instant + it).toDateTime(), it) }

fun TimeZone.decode(dateTime: DateTime): Set<InstantNanos> {
    val epoch = dateTime.toEpochNanos()
    return offsetsInto(epoch).mapTo(HashSet()) {
        epoch - it
    }
}

val TimeZone.isEtc: Boolean
    get() = id.startsWith("Etc/") || id.startsWith("GMT")
            || id == "Universal" || id == "UTC"

inline fun timeZoneForOffset(
    instant: InstantNanos,
    offset: DurationNanos,
    filter: (TimeZone) -> Boolean = { true }
): TimeZone? =
    timeZones.firstOrNull { it.offsetAt(instant) == offset && filter(it) }

internal data class OffsetZone(
    val name: String,
    val offset: Int
)

internal data class SinceData(
    val since: Long,
    val offset: OffsetZone
)

internal typealias TzEntry = Pair<OffsetZone, List<SinceData>>
