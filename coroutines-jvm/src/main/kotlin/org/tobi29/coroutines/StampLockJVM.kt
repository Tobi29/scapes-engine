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

import org.tobi29.stdex.assert
import org.tobi29.utils.Duration64Nanos
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater
import java.util.concurrent.locks.ReentrantLock

actual class StampLock {
    @PublishedApi
    internal val counterCurrent
        get() = counterFU.get(this)

    @Volatile
    @Suppress("UNUSED")
    private var _counter = 0

    private val writeLock = ReentrantLock()

    actual val isHeld get() = writeLock.isHeldByCurrentThread

    actual fun lock() {
        writeLock.lock()
        if (writeLock.holdCount == 1) {
            counterFU.incrementAndGet(this)
        }
        assert { counterCurrent and 1 != 0 }
    }

    actual fun tryLock(): Boolean =
        if (writeLock.tryLock()) {
            if (writeLock.holdCount == 1) {
                counterFU.incrementAndGet(this)
            }
            assert { counterCurrent and 1 != 0 }
            true
        } else false

    actual fun tryLock(timeout: Duration64Nanos): Boolean =
        if (writeLock.tryLock(timeout, TimeUnit.NANOSECONDS)) {
            if (writeLock.holdCount == 1) {
                counterFU.incrementAndGet(this)
            }
            assert { counterCurrent and 1 != 0 }
            true
        } else false

    actual fun unlock() {
        assert { counterCurrent and 1 != 0 }
        if (writeLock.holdCount == 1) {
            counterFU.incrementAndGet(this)
        }
        writeLock.unlock()
    }

    @PublishedApi
    internal fun lockForceRead() {
        writeLock.lock()
    }

    @PublishedApi
    internal fun unlockForceRead() {
        writeLock.unlock()
    }

    companion object {
        @JvmStatic
        private val counterFU = AtomicIntegerFieldUpdater.newUpdater(
            StampLock::class.java, "_counter"
        )
    }
}

actual inline fun <R> StampLock.read(crossinline block: () -> R): R {
    val held = isHeld
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

actual inline fun <R> StampLock.write(block: () -> R): R = try {
    lock()
    block()
} finally {
    unlock()
}
