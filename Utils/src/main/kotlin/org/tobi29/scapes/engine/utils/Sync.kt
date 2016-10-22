/*
 * Copyright 2012-2016 Tobi29
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

import mu.KLogging
import org.tobi29.scapes.engine.utils.math.clamp
import org.tobi29.scapes.engine.utils.task.Joiner
import java.util.concurrent.locks.LockSupport

class Sync(private var currentTPS: Double, minSkipDelay: Long, private val logSkip: Boolean, private val name: String) {
    private val minSkipDelay: Long
    private val maxDiff: Long
    private var delta = 0.0
    private var lastSync: Long = 0
    private var sync: Long = 0
    private var diff: Long = 0
    private var tickDiff: Long = 0

    init {
        if (minSkipDelay < 0) {
            throw IllegalArgumentException(
                    "Minimum skip delay is negative")
        }
        maxDiff = (1000000000.0 / currentTPS).toLong()
        this.minSkipDelay = -minSkipDelay
        delta = 1.0 / currentTPS
    }

    /**
     * Initializes the sync to start capping.
     * This method should be called right before the first iteration starts
     */
    fun init() {
        sync = System.nanoTime()
        lastSync = sync
    }

    /**
     * Get the TPS
     * @return Calculated TPS (1 / delta)
     */
    fun tps(): Double {
        return currentTPS
    }

    /**
     * Get the delta time in seconds
     * @param min Minimum value
     * @param max Maximum value
     * @return Current delta (1 / TPS), clamped between min and max
     */
    fun delta(min: Double = 0.0001,
              max: Double = 1.0): Double {
        return clamp(delta, min, max)
    }

    /**
     * Get the delta
     * @return Nanoseconds between syncs (excluding sleep)
     */
    fun diff(): Int {
        return diff.toInt()
    }

    /**
     * Get the delta
     * @return Nanoseconds between syncs (including sleep)
     */
    fun tickDiff(): Int {
        return tickDiff.toInt()
    }

    /**
     * Get the nano time of last sync
     * @return Nanoseconds between syncs
     */
    fun lastSync(): Long {
        return lastSync
    }

    /**
     * Get the anticipated delta
     * @return Nanoseconds between syncs including sleeping time
     */
    fun maxDiff(): Int {
        return maxDiff.toInt()
    }

    /**
     * Execute cap and calculate TPS
     * @param joiner Joiner that the [Joiner.Joinable.sleep] method is called on
     * @see .tick
     */
    fun cap(joiner: Joiner.Joinable) {
        cap() { sleep ->
            // Using nanos is useless as the implementation on wait is bad
            if (sleep >= 1000000) {
                joiner.sleep(sleep / 1000000)
            }
        }
    }

    /**
     * Execute cap and calculate TPS
     * @param park Callback for executing the sleep
     * @see .tick
     */
    fun cap(park: (Long) -> Unit = {
        LockSupport.parkNanos(it)
    }) {
        val current = System.nanoTime()
        diff = current - lastSync
        sync += maxDiff
        val sleep = sync - current
        if (sleep < minSkipDelay) {
            if (logSkip) {
                val oldSync = sync
                sync = lastSync + maxDiff
                logger.warn { "$name-Sync is skipping ${sync - oldSync} nanoseconds!" }
            } else {
                sync = lastSync + maxDiff
            }
        } else if (sleep > maxDiff) {
            park(maxDiff)
        } else {
            park(sleep)
        }
        val newSync = System.nanoTime()
        tickDiff = newSync - lastSync
        currentTPS = 1000000000.0 / tickDiff
        val delta = tickDiff / 1000000000.0
        if (delta.isNaN()) {
            this.delta = 0.0
        } else {
            this.delta = delta
        }
        lastSync = newSync
    }

    /**
     * Calculate TPS without capping
     * Capping counterpart: [.cap]
     * @see .cap
     */
    fun tick() {
        val newSync = System.nanoTime()
        diff = newSync - lastSync
        tickDiff = diff
        currentTPS = 1000000000.0 / tickDiff
        val delta = tickDiff / 1000000000.0
        if (delta.isNaN()) {
            this.delta = 0.0
        } else {
            this.delta = delta
        }
        lastSync = newSync
    }

    companion object : KLogging()
}
