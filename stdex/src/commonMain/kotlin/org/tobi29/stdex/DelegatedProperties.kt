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

package org.tobi29.stdex

import org.tobi29.stdex.atomic.AtomicReference
import org.tobi29.stdex.concurrent.ReentrantLock
import org.tobi29.stdex.concurrent.withLock
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Simple delegated property that calls the given get
 * @param T Property type
 * @param get The getter implementation
 * @return An object for property delegation
 */
inline fun <T> property(crossinline get: () -> T) =
    object : ReadOnlyProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>) =
            get()
    }

/**
 * Simple delegated property that calls the given get and set
 * @param T Property type
 * @param get The getter implementation
 * @param set The setter implementation
 * @return An object for property delegation
 */
inline fun <T> property(
    crossinline get: () -> T,
    crossinline set: (T) -> Unit
) =
    object : ReadWriteProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>) =
            get()

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) =
            set(value)
    }

/**
 * Alternative implementation of [Lazy] allowing reinitializing the value over
 * and over
 */
class MutableLazy<T> : Lazy<T> {
    private val initializer = AtomicReference<(() -> T)?>(null)
    private val lock = ReentrantLock()
    @Volatile
    private var _value: Any?

    /**
     * Constructs a lazy value with the given initializer
     */
    constructor(initializer: () -> T) {
        this.initializer.set(initializer)
        _value = Uninitialized
    }

    /**
     * Initializes the value to the given one
     */
    constructor(value: T) {
        _value = value
    }

    override var value: T
        get() {
            var value = _value
            if (value === Uninitialized) {
                lock.withLock {
                    value = _value
                    if (value === Uninitialized) {
                        value = (initializer.getAndSet(
                            null
                        ) ?: throw IllegalStateException(
                            "No initializer and no value"
                        ))()
                        _value = value
                    }
                }
            }
            @Suppress("UNCHECKED_CAST")
            return value as T
        }
        set(value) {
            lock.withLock {
                this.initializer.set(null)
                _value = value
            }
        }

    /**
     * Drop the value and set a new initializer
     */
    fun set(initializer: () -> T) {
        lock.withLock {
            this.initializer.set(initializer)
            _value = Uninitialized
        }
    }

    override fun isInitialized() = _value !== Uninitialized

    override fun toString() = _value.let {
        if (it === Uninitialized) {
            "Lazy value not initialized yet."
        } else {
            it.toString()
        }
    }

    private object Uninitialized
}

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun <T> mutableLazy(noinline initializer: () -> T) =
    MutableLazy(initializer)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun <T> mutableLazy(value: T) =
    MutableLazy(value)

inline fun <R1, R2, V> access(
    property: ReadWriteProperty<R2, V>,
    crossinline map: R1.() -> R2
) = property.let { p ->
    object : ReadWriteProperty<R1, V> {
        private fun m(thisRef: R1) = map(thisRef)

        override fun getValue(thisRef: R1, property: KProperty<*>) =
            p.getValue(m(thisRef), property)

        override fun setValue(thisRef: R1, property: KProperty<*>, value: V) {
            p.setValue(m(thisRef), property, value)
        }
    }
}

fun <R, V> access(accessor: R.() -> ReadWriteProperty<Any?, V>) =
    object : ReadWriteProperty<R, V> {
        override fun getValue(thisRef: R, property: KProperty<*>) =
            accessor(thisRef).getValue(thisRef, property)

        override fun setValue(thisRef: R, property: KProperty<*>, value: V) {
            accessor(thisRef).setValue(thisRef, property, value)
        }
    }
