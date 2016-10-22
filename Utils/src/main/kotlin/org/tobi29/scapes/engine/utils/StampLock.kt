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

import java.util.concurrent.atomic.AtomicLong

/**
 * Simple stamp lock optimized using Kotlin's inline modifier
 * Writes are always synchronized, whilst reads can happen in parallel as long
 * as there is no write going on at the same time
 */
class StampLock() {
    /**
     * Counter used internally by the lock and for synchronization
     * Made public for the inline modifier
     */
    val counter = AtomicLong(Long.MIN_VALUE)

    /**
     * Acquire read access of the lock, calling [block] one or two times
     * @param block Code block to execute, might be called twice
     * @return The return value of the last call to [block]
     */
    inline fun <R> read(block: () -> R): R {
        val stamp = counter.get()
        val output = block()
        val validate = counter.get()
        if (stamp == validate && validate and 1L == 0L) {
            return output
        }
        return write(block)
    }

    /**
     * Acquire write access, calling [block] once
     * @param block Code to execute
     * @return The return value of [block]
     */
    inline fun <R> write(block: () -> R): R {
        synchronized(counter) {
            return block()
        }
    }
}
