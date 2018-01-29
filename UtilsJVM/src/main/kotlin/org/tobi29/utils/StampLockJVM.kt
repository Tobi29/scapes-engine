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

package org.tobi29.utils

import org.tobi29.stdex.assert
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater
import java.util.concurrent.locks.ReentrantLock

/**
 * Simple stamp lock optimized using Kotlin's inline modifier
 * Writes are always synchronized, whilst reads can happen in parallel as long
 * as there is no write going on at the same time
 */
class StampLock {
    /**
     * Returns the current counter value
     *
     * **Note:** Exposed to allow inlining
     */
    val counterCurrent get() = counterFU.get(this)

    @Volatile
    @Suppress("UNUSED")
    private var _counter = 0

    private val writeLock = ReentrantLock()

    /**
     * Acquire read access of the lock, calling [block] one or two times
     * @param block Code block to execute, might be called twice
     * @return The return value of the last call to [block]
     */
    inline fun <R> read(crossinline block: () -> R): R {
        val held = isHeld()
        var force = false
        while (true) {
            if (force) {
                lockForceRead()
            }
            try {
                val stamp = if (held || force) 0 else counterCurrent
                val output = block()
                val validate = if (held || force) 0 else counterCurrent
                if (stamp == validate && validate and 1 == 0) {
                    return output
                }
            } finally {
                if (force) {
                    unlockForceRead()
                }
            }
            force = true
        }
    }

    /**
     * Acquire write access, calling [block] once
     * @param block Code to execute
     * @return The return value of [block]
     */
    inline fun <R> write(block: () -> R): R {
        return try {
            lock()
            block()
        } finally {
            unlock()
        }
    }

    /**
     * Returns `true` in case a write lock is held
     * @return `true` in case a write lock is held
     */
    fun isHeld() = writeLock.isHeldByCurrentThread

    /**
     * Acquires a write lock
     */
    fun lock() {
        writeLock.lock()
        if (writeLock.holdCount == 1) {
            counterFU.incrementAndGet(this)
        }
        assert { counterCurrent and 1 != 0 }
    }

    /**
     * Releases a write lock
     */
    fun unlock() {
        assert { counterCurrent and 1 != 0 }
        if (writeLock.holdCount == 1) {
            counterFU.incrementAndGet(this)
        }
        writeLock.unlock()
    }

    /**
     * Returns the current counter value
     *
     * **Note:** Exposed to allow inlining
     */
    fun lockForceRead() {
        writeLock.lock()
    }

    /**
     * Returns the current counter value
     *
     * **Note:** Exposed to allow inlining
     */
    fun unlockForceRead() {
        writeLock.unlock()
    }

    companion object {
        @JvmStatic
        private val counterFU = AtomicIntegerFieldUpdater.newUpdater(
            StampLock::class.java, "_counter"
        )
    }
}
