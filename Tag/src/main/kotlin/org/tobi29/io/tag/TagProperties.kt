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

package org.tobi29.io.tag

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class MutableTagProperty(
    private val key: String,
    private val access: () -> MutableTagMap?
) : ReadWriteProperty<Any?, MutableTag?> {
    override fun getValue(thisRef: Any?, property: KProperty<*>) =
        getTag(key, access)

    override fun setValue(
        thisRef: Any?,
        property: KProperty<*>,
        value: MutableTag?
    ) = setTag(key, access, value)
}

class MutableTagPropertyStatic(
    private val key: String,
    private val tag: MutableTagMap
) : ReadWriteProperty<Any?, MutableTag?> {
    override fun getValue(thisRef: Any?, property: KProperty<*>) =
        getTag(key, { tag })

    override fun setValue(
        thisRef: Any?,
        property: KProperty<*>,
        value: MutableTag?
    ) = setTag(key, { tag }, value)
}

fun MutableTagMap.setTag(key: String, value: MutableTag?) {
    if (value == null) {
        remove(key)
    } else {
        put(key, value)
    }
}

inline fun getTag(key: String, access: () -> MutableTagMap?) =
    access()?.get(key)

inline fun setTag(
    key: String,
    access: () -> MutableTagMap?,
    value: MutableTag?
) {
    val parent = access() ?: return
    if (value == null) {
        parent.remove(key)
    } else {
        parent[key] = value
    }
}

fun MutableTagMap.tag(key: String) = MutableTagPropertyStatic(key, this)

inline fun <T> MutableTagMap.tag(
    key: String,
    crossinline map: (MutableTag?) -> T,
    crossinline unmap: (T) -> MutableTag?
) = object : ReadWriteProperty<Any?, T> {
    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>
    ) = map(getTag(key, { this@tag }))

    override fun setValue(
        thisRef: Any?,
        property: KProperty<*>,
        value: T
    ) = setTag(key, { this@tag }, unmap(value))
}

fun tag(key: String, access: () -> MutableTagMap) =
    MutableTagProperty(key, access)

inline fun <T> tag(
    key: String,
    crossinline map: (MutableTag?) -> T,
    crossinline unmap: (T) -> MutableTag?,
    crossinline access: () -> MutableTagMap
) = object : ReadWriteProperty<Any?, T> {
    private fun doAccess() = access()

    override fun getValue(thisRef: Any?, property: KProperty<*>) =
        map(getTag(key, { doAccess() }))

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) =
        setTag(key, { doAccess() }, unmap(value))
}

fun MutableTagMap.tagMap(key: String) =
    object : ReadWriteProperty<Any?, MutableTagMap> {
        override fun getValue(thisRef: Any?, property: KProperty<*>) =
            mapMut(key)

        override fun setValue(
            thisRef: Any?,
            property: KProperty<*>,
            value: MutableTagMap
        ) {
            this@tagMap[key] = value
        }
    }
