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

impl class AtomicBoolean impl constructor(private var value: Boolean) {
    constructor() : this(false)

    impl fun get() = value

    impl fun compareAndSet(expect: Boolean,
                           update: Boolean) =
            if (value == expect) {
                value = update
                true
            } else {
                false
            }

    impl fun set(newValue: Boolean) {
        value = newValue
    }

    impl fun getAndSet(newValue: Boolean): Boolean {
        var prev: Boolean
        do {
            prev = get()
        } while (!compareAndSet(prev, newValue))
        return prev
    }

    override fun toString() = get().toString()
}

impl class AtomicInteger impl constructor(private var value: Int)/* : Number() */ {
    constructor() : this(0)

    impl fun get() = value

    impl fun set(newValue: Int) {
        value = newValue
    }

    impl fun getAndSet(newValue: Int) = value.also { value = newValue }

    impl fun compareAndSet(expect: Int,
                           update: Int) =
            if (value == expect) {
                value = update
                true
            } else {
                false
            }

    impl fun getAndIncrement() = addAndGet(1)

    impl fun getAndDecrement() = addAndGet(-1)

    impl fun getAndAdd(delta: Int) = value.also { value += delta }

    impl fun incrementAndGet() = addAndGet(1)

    impl fun decrementAndGet() = addAndGet(-1)

    impl fun addAndGet(delta: Int) = value.let { value += delta; value }

    override fun toString() = get().toString()
    /* override */ fun toByte() = get().toByte()
    /* override */ fun toShort() = get().toShort()
    /* override */ fun toInt() = get()
    /* override */ fun toLong() = get().toLong()
    /* override */ fun toFloat() = get().toFloat()
    /* override */ fun toDouble() = get().toDouble()
    /* override */ fun toChar() = get().toChar()
}

impl class AtomicLong impl constructor(private var value: Long)/* : Number() */ {
    constructor() : this(0L)

    impl fun get() = value

    impl fun set(newValue: Long) {
        value = newValue
    }

    impl fun getAndSet(newValue: Long) = value.also { value = newValue }

    impl fun compareAndSet(expect: Long,
                           update: Long) =
            if (value == expect) {
                value = update
                true
            } else {
                false
            }

    impl fun getAndIncrement() = addAndGet(1)

    impl fun getAndDecrement() = addAndGet(-1)

    impl fun getAndAdd(delta: Long) = value.also { value += delta }

    impl fun incrementAndGet() = addAndGet(1)

    impl fun decrementAndGet() = addAndGet(-1)

    impl fun addAndGet(delta: Long) = value.let { value += delta; value }

    override fun toString() = get().toString()
    /* override */ fun toByte() = get().toByte()
    /* override */ fun toShort() = get().toShort()
    /* override */ fun toInt() = get().toInt()
    /* override */ fun toLong() = get()
    /* override */ fun toFloat() = get().toFloat()
    /* override */ fun toDouble() = get().toDouble()
    /* override */ fun toChar() = get().toChar()
}

impl class AtomicReference<V> impl constructor(private var value: V) {
    impl fun get() = value

    impl fun set(newValue: V) {
        value = newValue
    }

    impl fun compareAndSet(expect: V,
                           update: V) =
            if (value === expect) {
                value = update
                true
            } else {
                false
            }

    impl fun getAndSet(newValue: V) = value.also { value = newValue }

    override fun toString() = get().toString()
}
