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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.graphics

import org.tobi29.math.vector.MutableVector3d
import org.tobi29.math.vector.Vector3d
import org.tobi29.stdex.math.remP

inline fun hsvToRgb(color: Vector3d): Vector3d =
    hsvToRgb(color.x, color.y, color.z)

fun hsvToRgb(h: Double, s: Double, v: Double): Vector3d =
    hsvToRgb(h, s, v) { r, g, b -> Vector3d(r, g, b) }

fun hsvToRgb(h: Double, s: Double, v: Double, o: MutableVector3d) {
    hsvToRgb(h, s, v) { r, g, b -> o.setXYZ(r, g, b) }
}

inline fun <R> hsvToRgb(
    h: Double, s: Double, v: Double,
    output: (Double, Double, Double) -> R
): R {
    val c = (h * 6.0).toInt() remP 6
    val f = h * 6.0 - c
    val p = v * (1.0 - s)
    val q = v * (1.0 - f * s)
    val t = v * (1.0 - (1.0 - f) * s)
    val r: Double
    val g: Double
    val b: Double
    when (c) {
        0 -> {
            r = v
            g = t
            b = p
        }
        1 -> {
            r = q
            g = v
            b = p
        }
        2 -> {
            r = p
            g = v
            b = t
        }
        3 -> {
            r = p
            g = q
            b = v
        }
        4 -> {
            r = t
            g = p
            b = v
        }
        5 -> {
            r = v
            g = p
            b = q
        }
        else -> throw IllegalArgumentException("Invalid hue: $h")
    }
    return output(r, g, b)
}

// TODO: Remove after 0.0.14

@Deprecated(
    "Use hsvToRgb",
    ReplaceWith("hsvToRgb", "org.tobi29.graphics.hsvToRgb")
)
inline fun hsvToRGB(color: Vector3d): Vector3d =
    hsvToRgb(color)

@Deprecated(
    "Use hsvToRgb",
    ReplaceWith("hsvToRgb", "org.tobi29.graphics.hsvToRgb")
)
inline fun hsvToRGB(h: Double, s: Double, v: Double): Vector3d =
    hsvToRgb(h, s, v)

@Deprecated(
    "Use hsvToRgb",
    ReplaceWith("hsvToRgb", "org.tobi29.graphics.hsvToRgb")
)
inline fun hsvToRGB(h: Double, s: Double, v: Double, o: MutableVector3d) =
    hsvToRgb(h, s, v, o)
