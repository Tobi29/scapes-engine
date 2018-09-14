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

package org.tobi29.stdex.atomic

expect class AtomicReference<V>(value: V) {
    fun get(): V

    fun set(newValue: V)

    fun compareAndSet(expect: V, update: V): Boolean

    fun getAndSet(newValue: V): V
}

expect class AtomicArray<E>(values: Array<E>) {
    fun length(): Int

    operator fun get(i: Int): E

    operator fun set(i: Int, newValue: E)

    fun getAndSet(i: Int, newValue: E): E

    fun compareAndSet(i: Int, expect: E, update: E): Boolean
}

expect inline fun <reified E> AtomicArray(
    length: Int,
    crossinline init: (Int) -> E
): AtomicArray<E>

expect inline fun <reified E> atomicArrayOf(size: Int): AtomicArray<E?>

expect class AtomicBoolean(value: Boolean) {
    fun get(): Boolean

    fun compareAndSet(expect: Boolean, update: Boolean): Boolean

    fun set(newValue: Boolean)

    fun getAndSet(newValue: Boolean): Boolean
}

expect class AtomicInt(value: Int) {
    fun get(): Int

    fun set(newValue: Int)

    fun getAndSet(newValue: Int): Int

    fun compareAndSet(expect: Int, update: Int): Boolean

    fun getAndIncrement(): Int

    fun getAndDecrement(): Int

    fun getAndAdd(delta: Int): Int

    fun incrementAndGet(): Int

    fun decrementAndGet(): Int

    fun addAndGet(delta: Int): Int
}

expect class AtomicLong(value: Long) {
    fun get(): Long

    fun set(newValue: Long)

    fun getAndSet(newValue: Long): Long

    fun compareAndSet(expect: Long, update: Long): Boolean

    fun getAndIncrement(): Long

    fun getAndDecrement(): Long

    fun getAndAdd(delta: Long): Long

    fun incrementAndGet(): Long

    fun decrementAndGet(): Long

    fun addAndGet(delta: Long): Long
}

class AtomicDouble(initial: Double) {
    private val long = AtomicLong(initial.toRawBits())

    fun get(): Double = Double.fromBits(long.get())

    fun set(newValue: Double) = long.set(newValue.toRawBits())

    fun getAndSet(newValue: Double): Double =
        Double.fromBits(long.getAndSet(newValue.toRawBits()))

    fun compareAndSet(expect: Double, update: Double): Boolean =
        long.compareAndSet(expect.toRawBits(), update.toRawBits())

    fun getAndAdd(delta: Double): Double = getAndUpdate { it + delta }

    fun addAndGet(delta: Double): Double = updateAndGet { it + delta }
}

inline fun <V> AtomicReference<V>.getAndUpdate(update: (V) -> V): V {
    while (true) {
        val current = get()
        val new = update(current)
        if (compareAndSet(current, new)) return current
    }
}

inline fun AtomicInt.getAndUpdate(update: (Int) -> Int): Int {
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

inline fun <V> AtomicReference<V>.updateAndGet(update: (V) -> V): V {
    while (true) {
        val current = get()
        val new = update(current)
        if (compareAndSet(current, new)) return new
    }
}

inline fun AtomicInt.updateAndGet(update: (Int) -> Int): Int {
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
