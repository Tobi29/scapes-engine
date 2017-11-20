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

@file:Suppress("NOTHING_TO_INLINE", "UnsafeCastFromDynamic")

package org.tobi29.scapes.engine.chrono

@JsModule("moment-timezone")
@JsNonModule
external object Moment {
    interface MomentZone {
        var name: String
        var abbrs: Array<String>
        var untils: Array<Number>
        var offsets: Array<Number>
        var population: Number
        fun abbr(timestamp: Number): String
        fun utcOffset(timestamp: Number): Number
        fun parse(timestamp: Number): Number
    }

    interface LoadData {
        var version: String
        var links: Array<String>
        var zones: Array<String>
    }

    interface MomentTimezone {
        fun zone(timezone: String): MomentZone
        fun add(packedZoneString: String)
        fun add(packedZoneString: Array<String>)
        fun link(packedLinkString: String)
        fun link(packedLinkString: Array<String>)
        fun load(data: LoadData)
        fun names(): Array<String>
        fun guess(): String
        fun setDefault(timezone: String)
    }

    interface Moment {
        fun tz(): String
        fun tz(timezone: String): Moment
        fun zoneAbbr(): String
        fun zoneName(): String

        fun valueOf(): Double

        fun year(): Int
        fun month(): Int
        fun date(): Int
        fun hours(): Int
        fun minutes(): Int
        fun seconds(): Int
        fun milliseconds(): Int
    }

    fun utc(): Moment

    fun utc(date: Number): Moment

    fun utc(date: Array<Number>): Moment

    fun utc(date: String): Moment

    fun utc(date: String,
            format: dynamic /* MomentFormatSpecification */): Moment

    fun utc(date: String,
            format: dynamic /* MomentFormatSpecification */,
            strict: Boolean): Moment

    fun utc(date: String,
            format: dynamic /* MomentFormatSpecification */,
            language: String): Moment

    fun utc(date: String,
            format: dynamic /* MomentFormatSpecification */,
            language: String,
            strict: Boolean): Moment

    fun utc(date: Date): Moment

    fun utc(date: Moment): Moment

    fun utc(date: Any): Moment

    var tz: MomentTimezone = definedExternally

    fun tz(): Moment

    fun tz(timezone: String): Moment

    fun tz(date: Number,
           timezone: String): Moment

    fun tz(date: Array<Number>,
           timezone: String): Moment

    fun tz(date: String,
           timezone: String): Moment

    fun tz(date: String,
           format: dynamic /* MomentFormatSpecification */,
           timezone: String): Moment

    fun tz(date: String,
           format: dynamic /* MomentFormatSpecification */,
           strict: Boolean,
           timezone: String): Moment

    fun tz(date: String,
           format: dynamic /* MomentFormatSpecification */,
           language: String,
           timezone: String): Moment

    fun tz(date: String,
           format: dynamic /* MomentFormatSpecification */,
           language: String,
           strict: Boolean,
           timezone: String): Moment

    fun tz(date: Date,
           timezone: String): Moment

    fun tz(date: Moment,
           timezone: String): Moment

    fun tz(date: Any,
           timezone: String): Moment
}

inline operator fun Moment.invoke(): Moment.Moment =
        asDynamic()()

inline operator fun Moment.invoke(date: Number): Moment.Moment =
        asDynamic()(date)

inline operator fun Moment.invoke(date: Array<Number>): Moment.Moment =
        asDynamic()(date)

inline operator fun Moment.invoke(date: String): Moment.Moment =
        asDynamic()(date)

inline operator fun Moment.invoke(date: String,
                                  format: dynamic /* MomentFormatSpecification */): Moment.Moment =
        asDynamic()(date, format)

inline operator fun Moment.invoke(date: String,
                                  format: dynamic /* MomentFormatSpecification */,
                                  strict: Boolean): Moment.Moment =
        asDynamic()(date, format, strict)

inline operator fun Moment.invoke(date: String,
                                  format: dynamic /* MomentFormatSpecification */,
                                  language: String): Moment.Moment =
        asDynamic()(date, format, language)

inline operator fun Moment.invoke(date: String,
                                  format: dynamic /* MomentFormatSpecification */,
                                  language: String,
                                  strict: Boolean): Moment.Moment =
        asDynamic()(date, format, language, strict)

inline operator fun Moment.invoke(date: Date): Moment.Moment =
        asDynamic()(date)

inline operator fun Moment.invoke(date: Moment.Moment): Moment.Moment =
        asDynamic()(date)

inline operator fun Moment.invoke(date: Any): Moment.Moment =
        asDynamic()(date)
