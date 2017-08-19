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

package org.tobi29.scapes.engine.utils.task

import org.tobi29.scapes.engine.utils.SteadyClock
import org.tobi29.scapes.engine.utils.math.roundL
import org.tobi29.scapes.engine.utils.steadyClock

@Suppress("NOTHING_TO_INLINE")
class Timer(val clock: SteadyClock = steadyClock) {
    var lastTimestamp = 0L
    var lastSync = 0L

    fun init() {
        lastSync = clock.timeSteadyNanos()
        lastTimestamp = lastSync
    }

    inline fun cap(maxDiff: Long,
                   park: (Long) -> Unit,
                   minSkipDelay: Long = 0L): Long =
            cap(maxDiff, park, minSkipDelay, {})

    inline fun cap(maxDiff: Long,
                   park: (Long) -> Unit,
                   minSkipDelay: Long = 0L,
                   logSkip: (Long) -> Unit): Long {
        var tickDiff = 0L
        cap(maxDiff, park, minSkipDelay, logSkip, {}, { tickDiff = it })
        return tickDiff
    }

    inline fun cap(maxDiff: Long,
                   park: (Long) -> Unit,
                   minSkipDelay: Long = 0L,
                   logSkip: (Long) -> Unit,
                   diff: (Long) -> Unit,
                   tickDiff: (Long) -> Unit) {
        tick(maxDiff, { nextSync ->
            val sleep = nextSync - clock.timeSteadyNanos()
            when {
                sleep < -minSkipDelay -> {
                    logSkip(lastTimestamp + maxDiff - lastSync)
                    return@tick lastTimestamp + maxDiff
                }
                else -> park(sleep.coerceAtMost(maxDiff))
            }
            lastSync + maxDiff
        }, diff, tickDiff)
    }

    inline fun tick(maxDiff: Long = 0L): Long =
            tick(maxDiff, { clock.timeSteadyNanos() })

    inline fun tick(maxDiff: Long = 0L,
                    park: (Long) -> Long): Long {
        var tickDiff = 0L
        tick(maxDiff, park, {}, { tickDiff = it })
        return tickDiff
    }

    inline fun tick(maxDiff: Long = 0L,
                    park: (Long) -> Long,
                    diff: (Long) -> Unit,
                    tickDiff: (Long) -> Unit) {
        val current = clock.timeSteadyNanos()
        lastSync = park(lastSync + maxDiff)
        val newSync = clock.timeSteadyNanos()
        val last = lastTimestamp
        diff(current - last)
        tickDiff(newSync - last)
        lastTimestamp = newSync
    }

    companion object {
        fun toTps(diff: Long) = 1000000000.0 / diff
        fun toDiff(tps: Double) = roundL(1000000000.0 / tps)
        fun toDelta(diff: Long) = diff / 1000000000.0
    }
}

inline fun Timer.loop(maxDiff: Long,
                      park: (Long) -> Unit,
                      step: (Double) -> Boolean) =
        loop(maxDiff, park, 0L, {}, step)

inline fun Timer.loop(maxDiff: Long,
                      park: (Long) -> Unit,
                      minSkipDelay: Long,
                      step: (Double) -> Boolean) =
        loop(maxDiff, park, minSkipDelay, {}, step)

inline fun Timer.loop(maxDiff: Long,
                      park: (Long) -> Unit,
                      minSkipDelay: Long = 0L,
                      logSkip: (Long) -> Unit,
                      step: (Double) -> Boolean) {
    while (true) {
        val tickDiff = cap(maxDiff, park, minSkipDelay, logSkip)
        if (!step(Timer.toDelta(tickDiff))) break
    }
}
