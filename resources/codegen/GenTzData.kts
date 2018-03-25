#!/usr/bin/kotlinc -script
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

val digits = "0123456789abcdefghijklmnopqrstuvwxzyABCDEFGHIJKLMNOPQRSTUVWXYZ"

fun checkRadix(radix: Int) {
    if (radix !in 1..62)
        throw IllegalArgumentException("Invalid radix: $radix")
}

fun Int.toDigit(): Char = when (this) {
    in 0..9 -> '0' + this
    in 10..35 -> 'a' + (this - 10)
    in 36..61 -> 'A' + (this - 36)
    else -> throw IllegalArgumentException("Invalid digit: $this")
}

fun Int.toStringCaseSensitive(radix: Int): String {
    checkRadix(radix)

    if (this == 0) return "0"
    val str = CharArray(33)
    val sign = this > 0
    var value = if (sign) -this else this
    var i = str.size
    while (value < 0) {
        str[--i] = (-(value % radix)).toDigit()
        value /= radix
    }
    if (!sign) str[--i] = '-'
    return String(str, i, str.size - i)
}

fun Long.toStringCaseSensitive(radix: Int): String {
    checkRadix(radix)

    if (this == 0L) return "0"
    val str = CharArray(65)
    val sign = this > 0L
    var value = if (sign) -this else this
    var i = str.size
    while (value < 0L) {
        str[--i] = (-(value % radix)).toInt().toDigit()
        value /= radix
    }
    if (!sign) str[--i] = '-'
    return String(str, i, str.size - i)
}

val timezonePattern = """[A-Za-z0-9/_+-]*"""
val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
val weekdaysPattern = weekdays.joinToString("|")
val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
val monthsPattern = months.joinToString("|")
val dayPattern = """[0-3]?[0-9]"""
val timePattern = """([0-2][0-9]):([0-5][0-9]):([0-5][0-9])"""
val yearPattern = """-?[0-9]+"""
val timezoneAbbrPattern = """\+?-?[A-Za-z0-9]+"""
val offsetPattern = """-?[0-9]+"""
val dateTimePattern = listOf(weekdaysPattern, monthsPattern, dayPattern, timePattern, yearPattern, timezoneAbbrPattern)
    .joinToString(""")\s+(""", """(""", """)""")

val normalPattern = """($timezonePattern)\s+$dateTimePattern\s+=\s+$dateTimePattern\s+isdst=(0|1)\s+gmtoff=($offsetPattern)"""
// TODO: Get non-bugged data for this
val nullPattern = """($timezonePattern)\s+(-?[0-9]+)\s+=\s+NULL"""

val normalRegex = normalPattern.toRegex()
val nullRegex = nullPattern.toRegex()

data class Time(
    val hour: Int,
    val minute: Int,
    val second: Int
)

data class DateTime(
    val weekday: String,
    val month: String,
    val day: Int,
    val time: Time,
    val year: Int,
    val timezoneAbbr: String
)

data class ZDumpLine(
    val timezone: String,
    val utTime: DateTime,
    val localTime: DateTime,
    val isDst: Boolean,
    val gmtOffset: Int
)

data class OffsetZone(
    val offset: Int,
    val timezoneAbbr: String
)

data class SinceDateTime(
    val month: String,
    val day: Int,
    val time: Time,
    val year: Int
)

fun Time.toSeconds(): Int =
    (hour * 60 + minute) * 60 + second

val Int.isLeap: Boolean
    get() = this % 4 == 0 && (this % 100 != 0 || this % 400 == 0)

val epochYear = 1970

fun SinceDateTime.toPosixOffset(): Long {
    var days = day - 1

    days += (epochYear until year)
        .sumBy { if (it.isLeap) 366 else 365 }
    days -= (year until epochYear)
        .sumBy { if (it.isLeap) 366 else 365 }

    val feb = if (year.isLeap) 29 else 28
    days += when(month) {
        "Jan" -> 0
        "Feb" -> 31
        "Mar" -> 31 + feb
        "Apr" -> 31 + feb + 31
        "May" -> 31 + feb + 31 + 30
        "Jun" -> 31 + feb + 31 + 30 + 31
        "Jul" -> 31 + feb + 31 + 30 + 31 + 30
        "Aug" -> 31 + feb + 31 + 30 + 31 + 30 + 31
        "Sep" -> 31 + feb + 31 + 30 + 31 + 30 + 31 + 31
        "Oct" -> 31 + feb + 31 + 30 + 31 + 30 + 31 + 31 + 30
        "Nov" -> 31 + feb + 31 + 30 + 31 + 30 + 31 + 31 + 30 + 31
        "Dec" -> 31 + feb + 31 + 30 + 31 + 30 + 31 + 31 + 30 + 31 + 30
        else -> throw IllegalArgumentException("Invalid month: $month")
    }
    return ((days * 24L + time.hour) * 60L + time.minute) * 60L + time.second
}

data class SinceState(
    val since: SinceDateTime,
    val offset: OffsetZone
)

fun readTransitions(): Map<String, List<Pair<ZDumpLine, ZDumpLine>>> {
    val lines = ArrayList<ZDumpLine>()
    val zones = HashSet<String>()

    while (true) {
        val line = readLine() ?: break
        val match = normalRegex.matchEntire(line)
        if (match != null) {
            val g = match.groupValues
            zones.add(g[1])
            try {
                lines.add(ZDumpLine(
                    g[1],
                    DateTime(
                        g[2], g[3], g[4].toInt(),
                        Time(g[6].toInt(), g[7].toInt(), g[8].toInt()),
                        g[9].toInt(),
                        g[10]
                    ),
                    DateTime(
                        g[11], g[12], g[13].toInt(),
                        Time(g[15].toInt(), g[16].toInt(), g[17].toInt()),
                        g[18].toInt(),
                        g[19]
                    ),
                    g[20] == "1",
                    g[21].toInt()
                ))
                continue
            } catch (e: NumberFormatException) {
            }
        }
        val nullMatch = nullRegex.matchEntire(line)
        if (nullMatch != null) {
            val g = nullMatch.groupValues
            zones.add(g[1])
            continue
        }
        System.err.println("Invalid line: $line")
    }

    val transitions = lines.asSequence().windowed(2, 2).map { it[0] to it[1] }.toList()

    return zones.map { zone ->
        zone to transitions.filter { it.first.timezone == zone && it.second.timezone == zone }
    }.toMap()
}

fun collectOffsets(transitions: Map<String, List<Pair<ZDumpLine, ZDumpLine>>>): Set<OffsetZone> =
    transitions
        .flatMap { it.value.flatMap { listOf(it.first, it.second) } }
        .map { OffsetZone(it.gmtOffset, it.localTime.timezoneAbbr) }
        .toSet()

fun assembleSinces(transitions: List<Pair<ZDumpLine, ZDumpLine>>): Pair<OffsetZone, List<SinceState>> =
    (transitions.firstOrNull()?.first?.let { OffsetZone(it.gmtOffset, it.localTime.timezoneAbbr) } ?: OffsetZone(0, "UT")) to
        transitions.asSequence()
            .map { it.second }
            .map { SinceState(SinceDateTime(it.utTime.month, it.utTime.day, it.utTime.time, it.utTime.year), OffsetZone(it.gmtOffset, it.localTime.timezoneAbbr)) }
            .toList()

fun compactSinces(
    sinces: List<SinceState>,
    compactOffset: (OffsetZone) -> String
): String = (listOf(SinceState(SinceDateTime("Jan", 1, Time(0, 0, 0), 1970), OffsetZone(0, "UT"))) + sinces).zipWithNext()
    .joinToString(",") { (a, b) -> "${(b.since.toPosixOffset() - a.since.toPosixOffset()).toStringCaseSensitive(offsetBase)}=${compactOffset(b.offset)}" }

val transitions = readTransitions()

val data = transitions.mapValues { assembleSinces(it.value) }

val offsetZones = collectOffsets(transitions).sortedByDescending { offset -> data.values.sumBy { (if (it.first == offset) 1 else 0) + it.second.count { it.offset == offset } } }

val offsetBase = 62
val idBase = 62

print("""/*
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

// GENERATED FILE, DO NOT EDIT DIRECTLY!!!
// Generation script can be found in `resources/codegen/GenTzData.kts`.
// Run `resources/codegen/codegen.sh` to update sources.

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.chrono

import org.tobi29.stdex.ConcurrentHashMap
import org.tobi29.stdex.readOnly
import org.tobi29.stdex.toIntCaseSensitive
import org.tobi29.stdex.toLongCaseSensitive

object TzData {
    private val unknownZone = OffsetZone("?", 0)

    private val offsets: List<OffsetZone> = parseOffsets(
        "${offsetZones.joinToString(",") { "${it.timezoneAbbr}=${it.offset.toStringCaseSensitive(idBase)}" }}"
    )

    private val _tzdata = ConcurrentHashMap<String, TimeZone>()
    internal val tzdata = _tzdata.readOnly()

    init {""")

val inserted = HashMap<String, String>()

data.forEach {
    val entry = "${offsetZones.indexOf(it.value.first).toStringCaseSensitive(idBase)},${compactSinces(it.value.second) { "${offsetZones.indexOf(it).toStringCaseSensitive(idBase)}" }}"
    val share = inserted[entry]
    if (share != null) {
        println("""
        link("${it.key}", "$share")""")
    } else {
        println("""
        insert(
            "${it.key}",
            "$entry"
        )""")
        inserted[entry] = it.key
    }
}

println("""
    }

    private fun parseOffsets(str: String): List<OffsetZone> =
        str.split(',').map { pattern ->
            val equals = pattern.lastIndexOf('=')
            val name = pattern.substring(0, equals)
            val offset = pattern.substring(equals + 1).toIntCaseSensitive(62)
            OffsetZone(name, offset)
        }

    private fun parseTzEntry(str: String): TzEntry =
        str.split(',', limit = 2).let { initialSplit ->
            val initial =
                offsets.getOrElse(initialSplit[0].toIntCaseSensitive(62)) { unknownZone }
            val sinces = if (initialSplit[1].isNotEmpty())
                initialSplit[1].splitToSequence(',').map {
                    val split = it.split('=', limit = 2)
                    val since = split[0].toLongCaseSensitive(62)
                    val offsetZone =
                        offsets.getOrElse(split[1].toIntCaseSensitive(62)) { unknownZone }
                    SinceData(since, offsetZone)
                }.toList() else emptyList()
            initial to sinces
        }

    private fun insert(
        name: String,
        str: String
    ) = _tzdata.put(name, TimeZone(name, lazy { parseTzEntry(str) }))

    private fun link(
        name: String,
        other: String
    ) = _tzdata.put(name, _tzdata[other]!!)
}""")
