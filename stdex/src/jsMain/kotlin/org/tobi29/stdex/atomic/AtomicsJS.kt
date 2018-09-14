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

actual class AtomicReference<V> actual constructor(private var value: V) {
    actual fun get() = value

    actual fun set(newValue: V) {
        value = newValue
    }

    actual fun compareAndSet(expect: V, update: V) =
        if (value === expect) {
            value = update
            true
        } else {
            false
        }

    actual fun getAndSet(newValue: V) = value.also { value = newValue }

    override fun toString() = get().toString()
}

@Suppress("UNUSED_PARAMETER")
actual class AtomicArray<E> @PublishedApi internal constructor(
    private val values: Array<E>,
    unused: Nothing?
) {
    actual constructor(values: Array<E>) : this(values.copyOf(), null)

    actual fun length() = values.size

    actual operator fun get(i: Int) = values[i]

    actual operator fun set(i: Int, newValue: E) {
        values[i] = newValue
    }

    actual fun compareAndSet(i: Int, expect: E, update: E) =
        if (values[i] === expect) {
            values[i] = update
            true
        } else {
            false
        }

    actual fun getAndSet(i: Int, newValue: E) =
        values[i].also { values[i] = newValue }

    override fun toString() = values.joinToString(prefix = "[", postfix = "]")
}

actual inline fun <reified E> AtomicArray(
    length: Int,
    crossinline init: (Int) -> E
) = AtomicArray(Array(length) { init(it) }, null)

actual inline fun <reified E> atomicArrayOf(size: Int) =
    AtomicArray<E?>(arrayOfNulls(size))

actual class AtomicBoolean actual constructor(private var value: Boolean) {
    actual fun get() = value

    actual fun compareAndSet(expect: Boolean, update: Boolean) =
        if (value == expect) {
            value = update
            true
        } else {
            false
        }

    actual fun set(newValue: Boolean) {
        value = newValue
    }

    actual fun getAndSet(newValue: Boolean): Boolean {
        var prev: Boolean
        do {
            prev = get()
        } while (!compareAndSet(prev, newValue))
        return prev
    }

    override fun toString() = get().toString()
}

actual class AtomicInt actual constructor(private var value: Int)/* : Number() */ {
    actual fun get() = value

    actual fun set(newValue: Int) {
        value = newValue
    }

    actual fun getAndSet(newValue: Int) = value.also { value = newValue }

    actual fun compareAndSet(expect: Int, update: Int) =
        if (value == expect) {
            value = update
            true
        } else {
            false
        }

    actual fun getAndIncrement() = addAndGet(1)

    actual fun getAndDecrement() = addAndGet(-1)

    actual fun getAndAdd(delta: Int) = value.also { value += delta }

    actual fun incrementAndGet() = addAndGet(1)

    actual fun decrementAndGet() = addAndGet(-1)

    actual fun addAndGet(delta: Int) = value.let { value += delta; value }

    override fun toString() = get().toString()
    /* override */ fun toByte() = get().toByte()
    /* override */ fun toShort() = get().toShort()
    /* override */ fun toInt() = get()
    /* override */ fun toLong() = get().toLong()
    /* override */ fun toFloat() = get().toFloat()
    /* override */ fun toDouble() = get().toDouble()
    /* override */ fun toChar() = get().toChar()
}

actual class AtomicLong actual constructor(private var value: Long)/* : Number() */ {
    actual fun get() = value

    actual fun set(newValue: Long) {
        value = newValue
    }

    actual fun getAndSet(newValue: Long) = value.also { value = newValue }

    actual fun compareAndSet(expect: Long, update: Long) =
        if (value == expect) {
            value = update
            true
        } else {
            false
        }

    actual fun getAndIncrement() = addAndGet(1)

    actual fun getAndDecrement() = addAndGet(-1)

    actual fun getAndAdd(delta: Long) = value.also { value += delta }

    actual fun incrementAndGet() = addAndGet(1)

    actual fun decrementAndGet() = addAndGet(-1)

    actual fun addAndGet(delta: Long) = value.let { value += delta; value }

    override fun toString() = get().toString()
    /* override */ fun toByte() = get().toByte()
    /* override */ fun toShort() = get().toShort()
    /* override */ fun toInt() = get().toInt()
    /* override */ fun toLong() = get()
    /* override */ fun toFloat() = get().toFloat()
    /* override */ fun toDouble() = get().toDouble()
    /* override */ fun toChar() = get().toChar()
}
