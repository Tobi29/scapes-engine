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

/**
 * Constructor for creating a [ThreadLocal] from a lambda expression
 * @param supplier: Supplier that will be called once per thread
 * @param T: The type of the elements in the [ThreadLocal]
 * @return A new instance of [ThreadLocal]
 */
inline fun <T> ThreadLocal(crossinline supplier: (Thread) -> T): ThreadLocal<T> {
    return object : ThreadLocal<T>() {
        override fun initialValue(): T {
            return supplier(Thread.currentThread())
        }
    }
}
