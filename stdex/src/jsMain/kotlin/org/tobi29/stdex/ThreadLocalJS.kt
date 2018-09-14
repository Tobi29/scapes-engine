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

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

actual class ThreadLocal<T> actual constructor(
    private val initial: () -> T
) : ReadWriteProperty<Any?, T> {
    private val instanceMut = mutableLazy(initial)
    private val instance by instanceMut

    actual fun get(): T = instance
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = get()

    actual fun set(value: T) = instanceMut.set { value }
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) =
        set(value)

    actual fun remove() = instanceMut.set(initial)
}
