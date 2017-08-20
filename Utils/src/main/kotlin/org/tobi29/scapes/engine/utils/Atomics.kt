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

typealias AtomicBoolean = java.util.concurrent.atomic.AtomicBoolean
typealias AtomicInteger = java.util.concurrent.atomic.AtomicInteger
typealias AtomicLong = java.util.concurrent.atomic.AtomicLong
typealias AtomicReference<T> = java.util.concurrent.atomic.AtomicReference<T>

class AtomicDouble {
    private val long = AtomicLong(0L)

    fun get(): Double = long.get().bitsToDouble()

    fun set(newValue: Double) = long.set(newValue.bits())

    fun getAndSet(newValue: Double): Double =
            long.getAndAdd(newValue.bits()).bitsToDouble()

    fun compareAndSet(expect: Double,
                      update: Double): Boolean =
            long.compareAndSet(expect.bits(), update.bits())

    fun getAndAdd(delta: Double): Double =
            getAndUpdate { it + delta }

    fun addAndGet(delta: Double): Double =
            updateAndGet { it + delta }
}

inline fun AtomicInteger.getAndUpdate(update: (Int) -> Int): Int {
    while (true) {
        val current = get()
        val new = update(current)
        if (compareAndSet(current, new)) return current
    }
}

inline fun AtomicLong.getAndUpdate(update: (Long) -> Long): Long {
    while (true) {
        val current = get()
        val new = update(current)
        if (compareAndSet(current, new)) return current
    }
}

inline fun AtomicDouble.getAndUpdate(update: (Double) -> Double): Double {
    while (true) {
        val current = get()
        val new = update(current)
        if (compareAndSet(current, new)) return current
    }
}

inline fun AtomicInteger.updateAndGet(update: (Int) -> Int): Int {
    while (true) {
        val current = get()
        val new = update(current)
        if (compareAndSet(current, new)) return new
    }
}

inline fun AtomicLong.updateAndGet(update: (Long) -> Long): Long {
    while (true) {
        val current = get()
        val new = update(current)
        if (compareAndSet(current, new)) return new
    }
}

inline fun AtomicDouble.updateAndGet(update: (Double) -> Double): Double {
    while (true) {
        val current = get()
        val new = update(current)
        if (compareAndSet(current, new)) return new
    }
}
