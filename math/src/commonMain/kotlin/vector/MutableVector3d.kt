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

import org.tobi29.arrays.Doubles
import org.tobi29.io.tag.ReadTagMutableMap
import org.tobi29.io.tag.toDouble
import org.tobi29.stdex.InlineUtility
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

data class MutableVector3d(
    override var x: Double = 0.0,
    override var y: Double = 0.0,
    override var z: Double = 0.0
) : ReadVector3d, Doubles, ReadWriteProperty<Any?, Vector3d> {
    constructor(vector: ReadVector3d) : this(vector.x, vector.y, vector.z)

    constructor(vector: ReadVector3i) : this(
        vector.x.toDouble(),
        vector.y.toDouble(),
        vector.z.toDouble()
    )

    fun now(): Vector3d = Vector3d(x, y, z)

    override fun set(
        index: Int,
        value: Double
    ): Unit = when (index) {
        0 -> x = value
        1 -> y = value
        2 -> z = value
        else -> throw IndexOutOfBoundsException("$index")
    }

    fun setX(x: Double) = apply {
        this.x = x
    }

    fun setY(y: Double) = apply {
        this.y = y
    }

    fun setZ(z: Double) = apply {
        this.z = z
    }

    fun setXYZ(
        x: Double,
        y: Double,
        z: Double
    ) = apply {
        this.x = x
        this.y = y
        this.z = z
    }

    fun set(a: ReadVector3d) = apply {
        x = a.x
        y = a.y
        z = a.z
    }

    fun set(map: ReadTagMutableMap) {
        map["X"]?.toDouble()?.let { x = it }
        map["Y"]?.toDouble()?.let { y = it }
        map["Z"]?.toDouble()?.let { z = it }
    }

    @InlineUtility
    @Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")
    override inline fun getValue(
        thisRef: Any?, property: KProperty<*>
    ): Vector3d = now()

    @InlineUtility
    @Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")
    override inline fun setValue(
        thisRef: Any?, property: KProperty<*>, value: Vector3d
    ) {
        set(value)
    }

    @InlineUtility
    @Suppress("NOTHING_TO_INLINE")
    inline fun setValue(
        thisRef: Any?, property: KProperty<*>, value: ReadVector3d
    ) {
        set(value)
    }
}
