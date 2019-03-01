/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.math.vector

import org.tobi29.arrays.Ints
import org.tobi29.io.tag.ReadTagMutableMap
import org.tobi29.io.tag.toInt
import org.tobi29.stdex.InlineUtility
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

data class MutableVector2i(
    override var x: Int = 0,
    override var y: Int = 0
) : ReadVector2i, Ints, ReadWriteProperty<Any?, Vector2i> {
    constructor(vector: ReadVector2i) : this(vector.x, vector.y)

    fun now(): Vector2i = Vector2i(x, y)

    override fun set(
        index: Int,
        value: Int
    ): Unit = when (index) {
        0 -> x = value
        1 -> y = value
        else -> throw IndexOutOfBoundsException("$index")
    }

    fun setX(x: Int) = apply {
        this.x = x
    }

    fun setY(y: Int) = apply {
        this.y = y
    }

    fun setXY(
        x: Int,
        y: Int
    ) = apply {
        this.x = x
        this.y = y
    }

    fun set(a: ReadVector2i) = apply {
        x = a.x
        y = a.y
    }

    fun set(map: ReadTagMutableMap) {
        map["X"]?.toInt()?.let { x = it }
        map["Y"]?.toInt()?.let { y = it }
    }

    @InlineUtility
    @Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")
    override inline fun getValue(
        thisRef: Any?, property: KProperty<*>
    ): Vector2i = now()

    @InlineUtility
    @Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")
    override inline fun setValue(
        thisRef: Any?, property: KProperty<*>, value: Vector2i
    ) {
        set(value)
    }

    @InlineUtility
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun setValue(
        thisRef: Any?, property: KProperty<*>, value: ReadVector2i
    ) {
        set(value)
    }
}
