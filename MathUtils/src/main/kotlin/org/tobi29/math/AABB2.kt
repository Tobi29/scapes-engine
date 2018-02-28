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

package org.tobi29.math

import org.tobi29.math.vector.MutableVector2d
import org.tobi29.math.vector.ReadVector2d
import org.tobi29.math.vector.Vector2d
import org.tobi29.math.vector.map

class AABB2(
    val min: MutableVector2d = MutableVector2d(),
    val max: MutableVector2d = MutableVector2d()
) {
    constructor(
        min: ReadVector2d,
        max: ReadVector2d
    ) : this(min.x, min.y, max.x, max.y)

    constructor(aabb: AABB2) : this(
        aabb.min.x, aabb.min.y,
        aabb.max.x, aabb.max.y
    )

    constructor(
        minX: Double, minY: Double,
        maxX: Double, maxY: Double
    ) : this(
        MutableVector2d(minX, minY),
        MutableVector2d(maxX, maxY)
    )

    fun set(aabb: AABB2) {
        min.x = aabb.min.x
        min.y = aabb.min.y
        max.x = aabb.max.x
        max.y = aabb.max.y
    }
}

inline val AABB2.center: Vector2d
    get() = min.map(max) { a, b -> (a + b) * 0.5 }

inline val AABB2.extends: Vector2d
    get() = min.map(max) { a, b -> (b - a) * 0.5 }

fun AABB2.add(
    x: Double,
    y: Double
) {
    min.x += x
    max.x += x
    min.y += y
    max.y += y
}

inline fun AABB2.subtract(
    x: Double,
    y: Double
) = add(-x, -y)

fun AABB2.getVertexNX(normalx: Double): Double {
    var p = min.x
    if (normalx < 0) {
        p = max.x
    }
    return p
}

fun AABB2.getVertexNY(normaly: Double): Double {
    var p = min.y
    if (normaly < 0) {
        p = max.y
    }
    return p
}

fun AABB2.getVertexPX(normalx: Double): Double {
    var p = min.x
    if (normalx > 0) {
        p = max.x
    }
    return p
}

fun AABB2.getVertexPY(normaly: Double): Double {
    var p = min.y
    if (normaly > 0) {
        p = max.y
    }
    return p
}

inline fun AABB2.grow(
    x: Double,
    y: Double
) = grow(x, y, x, y)

fun AABB2.grow(
    x1: Double,
    y1: Double,
    x2: Double,
    y2: Double
) {
    min.x -= x1
    min.y -= y1
    max.x += x2
    max.y += y2
}

fun AABB2.scale(value: Double) = scale(value, value)

fun AABB2.scale(x: Double, y: Double) {
    min.x *= x
    min.y *= y
    max.x *= x
    max.y *= y
}

fun AABB2.inside(check: ReadVector2d): Boolean =
    inside(check.x, check.y)

fun AABB2.inside(x: Double, y: Double): Boolean =
    !(max.x < x || min.x > x)
            && !(max.y < y || min.y > y)

infix fun AABB2.overlaps(check: AABB2): Boolean =
    !(max.x <= check.min.x || min.x >= check.max.x)
            && !(max.y <= check.min.y || min.y >= check.max.y)

fun AABB2.moveOutX(
    base: Double,
    aabbs: Iterable<AABB2>
): Double = aabbs.fold(base) { v, e -> moveOutX(v, e) }

fun AABB2.moveOutY(
    base: Double,
    aabbs: Iterable<AABB2>
): Double = aabbs.fold(base) { v, e -> moveOutY(v, e) }

fun AABB2.moveOutX(
    base: Double,
    check: AABB2,
    error: Double = 0.00001
): Double = offset(
    min.x, check.min.x, max.x, check.max.x,
    min.y, check.min.y, max.y, check.max.y,
    base, error
)

fun AABB2.moveOutY(
    base: Double,
    check: AABB2,
    error: Double = 0.00001
): Double = offset(
    min.y, check.min.y, max.y, check.max.y,
    min.x, check.min.x, max.x, check.max.x,
    base, error
)

private fun offset(
    min1: Double, min2: Double,
    max1: Double, max2: Double,
    minH1: Double, minH2: Double,
    maxH1: Double, maxH2: Double,
    base: Double, error: Double
): Double {
    if (maxH1 <= minH2 + error || minH1 >= maxH2 - error) return base
    var currentBase = base
    if (currentBase > 0.0 && max1 <= min2 + error) {
        val diff = min2 - max1
        if (diff < currentBase) {
            currentBase = diff
        }
    }
    if (currentBase < 0.0 && min1 >= max2 - error) {
        val diff = max2 - min1
        if (diff > currentBase) {
            currentBase = diff
        }
    }
    return currentBase
}
