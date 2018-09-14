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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.coroutines

import org.tobi29.utils.Duration64Nanos

actual class StampLock {
    private var held = 0

    actual val isHeld get() = held > 0

    actual fun lock() {
        if (held == Int.MAX_VALUE) error("Lock overflowed")
        held++
    }

    actual inline fun tryLock(): Boolean {
        lock()
        return true
    }

    actual inline fun tryLock(timeout: Duration64Nanos): Boolean {
        lock()
        return true
    }

    actual fun unlock() {
        if (held == 0) error("Lock underflowed")
        held--
    }
}

actual inline fun <R> StampLock.read(crossinline block: () -> R): R = block()

actual inline fun <R> StampLock.write(block: () -> R): R = try {
    lock()
    block()
} finally {
    unlock()
}
