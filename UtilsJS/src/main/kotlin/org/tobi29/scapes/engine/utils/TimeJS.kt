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

package org.tobi29.scapes.engine.utils

impl val systemClock = object : Clock {
    override fun timeMillis(): InstantMillis =
            Date.now().toLong()

    override fun timeNanos(): InstantNanos =
            (Date.now() * 1000000.0).toInt128()
}

impl val steadyClock = object : SteadyClock {
    override fun timeSteadyNanos(): InstantSteadyNanos =
            (performance.now() * 1000000.0).toLong()
}

private external val performance: Performance

private external abstract class Performance {
    fun now(): Double
}

private external class Date {
    companion object {
        fun now(): Double
    }
}
