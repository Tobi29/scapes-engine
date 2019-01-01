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

@file:JvmName("TimeZonesJVMKt")

package org.tobi29.chrono

import org.tobi29.utils.*

actual val timeZoneLocal: TimeZone
    get() = java.util.TimeZone.getDefault().let { defaultTimeZone ->
        try {
            timeZoneOf(defaultTimeZone.id)
        } catch (e: IllegalArgumentException) {
            val time = systemClock.timeNanos()
            timeZoneForOffset(
                time,
                Duration.fromMillis(
                    defaultTimeZone.getOffset(
                        time.millis.toLongClamped()
                    ).toInt128()
                )
            ) ?: timeZoneOf("UTC")
        }
    }
