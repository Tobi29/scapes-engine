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

package org.tobi29.coroutines

/**
 * Simple stamp lock optimized using Kotlin's inline modifier
 * Writes are always synchronized, whilst reads can happen in parallel as long
 * as there is no write going on at the same time
 */
expect class StampLock {
    /**
     * Returns `true` in case a write lock is held
     * @return `true` in case a write lock is held
     */
    val isHeld: Boolean

    /**
     * Acquires a write lock
     */
    fun lock()

    /**
     * Releases a write lock
     */
    fun unlock()
}

/**
 * Acquire read access of the lock, calling [block] one or two times
 * @param block Code block to execute, might be called twice
 * @return The return value of the last call to [block]
 */
expect inline fun <R> StampLock.read(crossinline block: () -> R): R

/**
 * Acquire write access, calling [block] once
 * @param block Code to execute
 * @return The return value of [block]
 */
expect inline fun <R> StampLock.write(block: () -> R): R
