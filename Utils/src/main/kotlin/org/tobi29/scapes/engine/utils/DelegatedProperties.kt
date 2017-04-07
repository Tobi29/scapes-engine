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
