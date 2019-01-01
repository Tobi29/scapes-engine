/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.utils

import org.w3c.dom.Window
import kotlin.browser.window

@PublishedApi
internal object JSClock : Clock {
    override fun timeMillis(): InstantMillis =
        Date.now().toLong()

    override fun timeNanos(): InstantNanos =
        (Date.now() * 1000000.0).toInt128()
}

@PublishedApi
internal object JSSteadyClock : SteadyClock {
    override fun timeSteadyNanos(): InstantSteadyNanos =
        timeSteadyNanosImpl()
}

private val timeSteadyNanosImpl: () -> InstantSteadyNanos = run {
    if (window.performanceNowSupported) {
        val performance = window.performance
        return@run {
            (performance.now() * 1000000.0).toLong()
        }
    }
    if (window.processHrtimeSupported) {
        val process = window.process
        return@run {
            val time = process.hrtime()
            time[0] * 1000000000L + time[1]
        }
    }
    console.warn(
        "Neither `performance.now()` nor `process.hrtime()` are available, ${""
        }`steadyClock` may not be monotonic or might be inaccurate due to ${""
        }using `Date.now()` as fallback."
    )
    return@run {
        (Date.now() * 1000000.0).toLong()
    }
}

actual inline val systemClock: Clock get() = JSClock

actual inline val steadyClock: SteadyClock get() = JSSteadyClock

private inline val Window.performanceSupported: Boolean
    get() = performance.asDynamic() != undefined

private inline val Window.performanceNowSupported: Boolean
    get() = performanceSupported && performance.asDynamic().now != undefined

private inline val Window.processSupported: Boolean
    get() = process.asDynamic() != undefined

private inline val Window.processHrtimeSupported: Boolean
    get() = processSupported && process.asDynamic().hrtime != undefined

@Suppress("UnsafeCastFromDynamic")
private inline val Window.process: Process
    get() = asDynamic().process

private external interface Process {
    fun hrtime(time: Array<Int> = definedExternally): Array<Int>
}

private external class Date {
    companion object {
        fun now(): Double
    }
}
