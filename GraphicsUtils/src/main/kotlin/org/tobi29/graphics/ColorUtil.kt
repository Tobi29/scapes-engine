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
package org.tobi29.graphics

import org.tobi29.stdex.math.remP
import org.tobi29.math.vector.MutableVector3d
import org.tobi29.math.vector.Vector3d

fun hsvToRGB(color: Vector3d): Vector3d {
    return hsvToRGB(color.x, color.y, color.z)
}

fun hsvToRGB(h: Double,
             s: Double,
             v: Double): Vector3d {
    val c = (h * 6.0).toInt() remP 6
    val f = h * 6.0 - c
    val p = v * (1.0 - s)
    val q = v * (1.0 - f * s)
    val t = v * (1.0 - (1.0 - f) * s)
    return when (c) {
        0 -> Vector3d(v, t, p)
        1 -> Vector3d(q, v, p)
        2 -> Vector3d(p, v, t)
        3 -> Vector3d(p, q, v)
        4 -> Vector3d(t, p, v)
        5 -> Vector3d(v, p, q)
        else -> throw IllegalArgumentException("Invalid hue: $h")
    }
}

fun hsvToRGB(h: Double,
             s: Double,
             v: Double,
             o: MutableVector3d) {
    val c = (h * 6.0).toInt() remP 6
    val f = h * 6.0 - c
    val p = v * (1.0 - s)
    val q = v * (1.0 - f * s)
    val t = v * (1.0 - (1.0 - f) * s)
    when (c) {
        0 -> o.setXYZ(v, t, p)
        1 -> o.setXYZ(q, v, p)
        2 -> o.setXYZ(p, v, t)
        3 -> o.setXYZ(p, q, v)
        4 -> o.setXYZ(t, p, v)
        5 -> o.setXYZ(v, p, q)
        else -> throw IllegalArgumentException("Invalid hue: $h")
    }
}
