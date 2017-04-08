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

import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KProperty

/**
 * Generic interface for delegated properties
 * @param R Receiver type
 * @param T Property type
 */
interface DelegatedProperty<in R, out T> {
    /**
     * To be used by the compiler
     */
    operator fun getValue(thisRef: R,
                          property: KProperty<*>): T
}

/**
 * Generic interface for mutable delegated properties
 * @param R Receiver type
 * @param T Property type
 */
interface DelegatedMutableProperty<in R, T> : DelegatedProperty<R, T> {
    /**
     * To be used by the compiler
     */
    operator fun setValue(thisRef: R,
                          property: KProperty<*>,
                          value: T)
}

/**
 * Simple delegated property that calls the given get
 * @param T Property type
 * @param get The getter implementation
 * @return An object for property delegation
 */
inline fun <T> property(crossinline get: () -> T) =
        object : DelegatedProperty<Any?, T> {
            override fun getValue(thisRef: Any?,
                                  property: KProperty<*>) = get()
        }

/**
 * Simple delegated property that calls the given get and set
 * @param T Property type
 * @param get The getter implementation
 * @param set The setter implementation
 * @return An object for property delegation
 */
inline fun <T> property(crossinline get: () -> T,
                        crossinline set: (T) -> Unit) =
        object : DelegatedMutableProperty<Any?, T> {
            override fun getValue(thisRef: Any?,
                                  property: KProperty<*>) = get()

            override fun setValue(thisRef: Any?,
                                  property: KProperty<*>,
                                  value: T) = set(value)
        }

/**
 * Alternative implementation of [Lazy] allowing reinitializing the value over
 * and over
 */
class MutableLazy<T> : Lazy<T> {
    private val initializer = AtomicReference<(() -> T)?>(null)
    @Volatile private var _value: Any?

    /**
     * Constructs a lazy value with the given initializer
     */
    constructor(initializer: () -> T) {
        this.initializer.set(initializer)
        _value = UNINITIALIZED_VALUE
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
            if (value == UNINITIALIZED_VALUE) {
                synchronized(initializer) {
                    value = _value
                    if (value == UNINITIALIZED_VALUE) {
                        value = (initializer.getAndSet(
                                null) ?: throw IllegalStateException(
                                "No initializer and no value"))()
                        _value = value
                    }
                }
            }
            @Suppress("UNCHECKED_CAST")
            return value as T
        }
        set(value) {
            synchronized(initializer) {
                this.initializer.set(null)
                _value = value
            }
        }

    /**
     * Drop the value and set a new initializer
     */
    fun set(initializer: () -> T) {
        synchronized(initializer) {
            this.initializer.set(initializer)
            _value = UNINITIALIZED_VALUE
        }
    }

    override fun isInitialized() = _value !== UNINITIALIZED_VALUE

    override fun toString() = _value.let {
        if (it === UNINITIALIZED_VALUE) {
            "Lazy value not initialized yet."
        } else {
            it.toString()
        }
    }

    private object UNINITIALIZED_VALUE
}

fun <T> mutableLazy(initializer: () -> T) = MutableLazy(initializer)

fun <T> mutableLazy(value: T) = MutableLazy(value)
