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

package org.tobi29.server

import org.tobi29.stdex.atomic.AtomicBoolean
import org.tobi29.stdex.atomic.AtomicLong
import org.tobi29.utils.systemClock
import kotlin.math.max

/**
 * Handle to communicate with the worker from a connection job
 *
 * This class serves 2 purposes:
 * * Notifying the worker that the connection has not timed out
 * * Keeping track of whether or not the connection should gracefully close
 */
class Connection(
    private val requestClose: AtomicBoolean,
    private val timeout: AtomicLong?
) {
    /**
     * Set the timeout to at least [timeout] ms from now
     * @param timeout The time from now in milliseconds
     */
    fun increaseTimeout(timeout: Long) {
        if (this.timeout == null) {
            return
        }
        val nextTime = systemClock.timeMillis() + timeout
        while (true) {
            val prev = this.timeout.get()
            val next = max(prev, nextTime)
            if (this.timeout.compareAndSet(prev, next)) {
                break
            }
        }
    }

    /**
     * Sets [shouldClose] to `true`
     */
    fun requestClose() = requestClose.set(true)

    /**
     * Returns `true` whenever a request was made to close this connection job,
     * possibly called by the worker
     */
    val shouldClose get() = requestClose.get()
}
