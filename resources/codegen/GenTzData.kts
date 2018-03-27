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

import java.io.File
import java.io.IOException

fun combineToInt(b3: Byte, b2: Byte, b1: Byte, b0: Byte): Int =
    (b3.toInt() and 0xFF shl 24) or
            (b2.toInt() and 0xFF shl 16) or
            (b1.toInt() and 0xFF shl 8) or
            (b0.toInt() and 0xFF shl 0)

fun combineToLong(
    b7: Byte,
    b6: Byte,
    b5: Byte,
    b4: Byte,
    b3: Byte,
    b2: Byte,
    b1: Byte,
    b0: Byte
): Long =
    (b7.toLong() and 0xFF shl 56) or
            (b6.toLong() and 0xFF shl 48) or
            (b5.toLong() and 0xFF shl 40) or
            (b4.toLong() and 0xFF shl 32) or
            (b3.toLong() and 0xFF shl 24) or
            (b2.toLong() and 0xFF shl 16) or
            (b1.toLong() and 0xFF shl 8) or
            (b0.toLong() and 0xFF shl 0)

data class ttinfo(
    val tt_gmtoff: Int,
    val tt_isdst: Boolean,
    val tt_abbrind: Int
)

fun ByteArray.cstring(index: Int): String =
    String(this, index, run {
        var i = index
        while (this[i] != 0.toByte() && i < size) i++
        i
    } - index)

data class OffsetZone(
    val offset: Int,
    val isDst: Boolean,
    val timezoneAbbr: String
)

data class TzFile(
    val start: OffsetZone,
    val transitions: List<Pair<Long, OffsetZone>>
)

fun TzFile(
    transitionTimes: LongArray,
    transitions: IntArray,
    types: Array<ttinfo>,
    chars: ByteArray,
    leaps: Array<Pair<Long, Int>>,
    standardIndicators: BooleanArray,
    gmtIndicators: BooleanArray
): TzFile {
    val ti = types.map { (tt_gmtoff, tt_isdst, tt_abbrind) ->
        val abbr = chars.cstring(tt_abbrind)
        OffsetZone(tt_gmtoff, tt_isdst, abbr)
    }
    var t = (0 until transitionTimes.size)
        .map { transitionTimes[it] to ti[transitions[it]] }
    val tf = t.first()
    val s = tf.second
    t = t.drop(1)
    return TzFile(s, t)
}

fun ByteArray.readTzFile(): TzFile {
    val long = false

    var i = 0

    if (this[i++] != 'T'.toByte()
        || this[i++] != 'Z'.toByte()
        || this[i++] != 'i'.toByte()
        || this[i++] != 'f'.toByte())
        throw IOException("Invalid magic string")

    val version = this[i++]

    i += 15

    val tzh_ttisgmtcnt =
        combineToInt(this[i++], this[i++], this[i++], this[i++])
    val tzh_ttisstdcnt =
        combineToInt(this[i++], this[i++], this[i++], this[i++])
    val tzh_leapcnt =
        combineToInt(this[i++], this[i++], this[i++], this[i++])
    val tzh_timecnt =
        combineToInt(this[i++], this[i++], this[i++], this[i++])
    val tzh_typecnt =
        combineToInt(this[i++], this[i++], this[i++], this[i++])
    val tzh_charcnt =
        combineToInt(this[i++], this[i++], this[i++], this[i++])
    val length = 5 * 4 +
            6 * 4 +
            tzh_ttisgmtcnt * 1 +
            tzh_ttisstdcnt * 1 +
            (tzh_leapcnt * if (long) 12 else 8) +
            (tzh_timecnt * if (long) 9 else 5) +
            tzh_typecnt * 6 +
            tzh_charcnt * 1

    return when (version) {
        0.toByte() -> readTzFile(0, false)
        else -> readTzFile(length, true)
    }
}

fun ByteArray.readTzFile(start: Int, long: Boolean): TzFile {
    var i = start

    if (this[i++] != 'T'.toByte()
        || this[i++] != 'Z'.toByte()
        || this[i++] != 'i'.toByte()
        || this[i++] != 'f'.toByte())
        throw IOException("Invalid magic string")

    val version = this[i++]

    i += 15

    val tzh_ttisgmtcnt =
        combineToInt(this[i++], this[i++], this[i++], this[i++])
    val tzh_ttisstdcnt =
        combineToInt(this[i++], this[i++], this[i++], this[i++])
    val tzh_leapcnt =
        combineToInt(this[i++], this[i++], this[i++], this[i++])
    val tzh_timecnt =
        combineToInt(this[i++], this[i++], this[i++], this[i++])
    val tzh_typecnt =
        combineToInt(this[i++], this[i++], this[i++], this[i++])
    val tzh_charcnt =
        combineToInt(this[i++], this[i++], this[i++], this[i++])
    val length = 5 * 4 +
            6 * 4 +
            tzh_ttisgmtcnt * 1 +
            tzh_ttisstdcnt * 1 +
            (tzh_leapcnt * if (long) 12 else 8) +
            (tzh_timecnt * if (long) 9 else 5) +
            tzh_typecnt * 6 +
            tzh_charcnt * 1

    val transitionTimes = LongArray(tzh_timecnt) {
        if (long) combineToLong(
            this[i++], this[i++], this[i++], this[i++],
            this[i++], this[i++], this[i++], this[i++]
        )
        else combineToInt(
            this[i++], this[i++], this[i++], this[i++]
        ).toLong()
    }

    val transitions = IntArray(tzh_timecnt) { this[i++].toInt() and 0xFF }

    val types = Array(tzh_typecnt) {
        ttinfo(
            combineToInt(this[i++], this[i++], this[i++], this[i++]),
            this[i++] != 0.toByte(),
            this[i++].toInt() and 0xFF
        )
    }

    val chars = ByteArray(tzh_charcnt) { this[i++] }

    val leaps = Array(tzh_leapcnt) {
        (if (long) combineToLong(
            this[i++], this[i++], this[i++], this[i++],
            this[i++], this[i++], this[i++], this[i++]
        )
        else combineToInt(
            this[i++], this[i++], this[i++], this[i++]
        ).toLong()) to
                combineToInt(this[i++], this[i++], this[i++], this[i++])
    }

    val standardIndicators = BooleanArray(tzh_ttisstdcnt) {
        this[i++] != 0.toByte()
    }

    val gmtIndicators = BooleanArray(tzh_ttisstdcnt) {
        this[i++] != 0.toByte()
    }

    assert(i - start == length)

    return TzFile(
        transitionTimes,
        transitions,
        types,
        chars,
        leaps,
        standardIndicators,
        gmtIndicators
    )
}

fun File.readTzFile() = readBytes().readTzFile()

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

fun collectOffsets(transitions: Map<String, TzFile>): Set<OffsetZone> =
    transitions.flatMap {
        it.value.transitions.map { it.second } + it.value.start
    }.toSet()

fun TzFile.compactTransitions(
    compactOffset: (OffsetZone) -> String
): String = (listOf(
    0L to OffsetZone(0, false, "UT")
) + transitions).zipWithNext()
    .joinToString(",") { (a, b) ->
        "${(b.first - a.first).toStringCaseSensitive(
            offsetBase
        )}=${compactOffset(b.second)}"
    }

val zoneinfo = File("/usr/share/zoneinfo")
val data = args.map { zone ->
    val tzFile: TzFile = File(zoneinfo, zone).readTzFile()
    zone to tzFile
}.toMap()

val offsetZones = collectOffsets(data)
    .sortedByDescending { offset ->
        data.values.sumBy {
            (if (it.start == offset) 1 else 0) +
                    it.transitions.count { it.second == offset }
        }
    }

val offsetBase = 62
val idBase = 62

print(
    """/*
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
        "${offsetZones.joinToString(",") {
        "${it.timezoneAbbr}=${it.offset.toStringCaseSensitive(
            idBase
        )}"
    }}"
    )

    private val _tzdata = ConcurrentHashMap<String, TimeZone>()
    internal val tzdata = _tzdata.readOnly()

    init {"""
)

val inserted = HashMap<String, String>()

data.forEach { (zone, tzFile) ->
    val entry =
        "${offsetZones.indexOf(tzFile.start).toStringCaseSensitive(
            idBase
        )},${tzFile.compactTransitions {
            offsetZones.indexOf(it).toStringCaseSensitive(idBase)
        }}"
    val share = inserted[entry]
    if (share != null) {
        println(
            """
        link("$zone", "$share")"""
        )
    } else {
        println(
            """
        insert(
            "$zone",
            "$entry"
        )"""
        )
        inserted[entry] = zone
    }
}

println(
    """
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
}"""
)
