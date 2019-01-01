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

package org.tobi29.stdex.concurrent

/**
 * Basic interface for a lock
 */
interface Lock {
    /**
     * Returns `true` in case a write lock is held
     */
    val isHeld: Boolean

    /**
     * Acquires a write lock
     */
    fun lock()

    /**
     * Tries to acquire a write lock
     */
    fun tryLock(): Boolean

    /**
     * Tries to acquire a write lock with given timeout
     * @param timeout Timeout in nanoseconds
     */
    fun tryLock(timeout: Long): Boolean

    /**
     * Releases a write lock
     */
    fun unlock()
}

/**
 * A reentrant lock, behaving like `java.util.concurrent.locks.ReentrantLock`
 * on Kotlin/JVM and mimics [isHeld] on Kotlin/JS and Kotlin/Native with no
 * actual locking
 */
expect class ReentrantLock() : Lock

/**
 * A stamp read-write lock, behaving like [ReentrantLock] but also exposing
 * [read]
 */
expect class StampLock() : Lock

/**
 * Executes [block] whilst holding the given lock
 */
inline fun <R> Lock.withLock(block: () -> R): R = try {
    lock()
    block()
} finally {
    unlock()
}

/**
 * Acquire read access of the lock, calling [block] one or two times
 * @param block Code block to execute, might be called twice
 * @return The return value of the last call to [block]
 */
expect inline fun <R> StampLock.read(crossinline block: () -> R): R
