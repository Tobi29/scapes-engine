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

import org.tobi29.math.vector.MutableVector3d
import org.tobi29.math.vector.ReadVector3d
import org.tobi29.math.vector.Vector3d
import org.tobi29.math.vector.map

class AABB3(
    val min: MutableVector3d = MutableVector3d(),
    val max: MutableVector3d = MutableVector3d()
) {
    constructor(
        min: ReadVector3d,
        max: ReadVector3d
    ) : this(min.x, min.y, min.z, max.x, max.y, max.z)

    constructor(aabb: AABB3) : this(
        aabb.min.x, aabb.min.y, aabb.min.z,
        aabb.max.x, aabb.max.y, aabb.max.z
    )

    constructor(
        minX: Double, minY: Double, minZ: Double,
        maxX: Double, maxY: Double, maxZ: Double
    ) : this(
        MutableVector3d(minX, minY, minZ),
        MutableVector3d(maxX, maxY, maxZ)
    )

    fun set(aabb: AABB3) {
        min.x = aabb.min.x
        min.y = aabb.min.y
        min.z = aabb.min.z
        max.x = aabb.max.x
        max.y = aabb.max.y
        max.z = aabb.max.z
    }
}

inline val AABB3.center: Vector3d
    get() = min.map(max) { a, b -> (a + b) * 0.5 }

inline val AABB3.extends: Vector3d
    get() = min.map(max) { a, b -> (b - a) * 0.5 }

fun AABB3.add(
    x: Double,
    y: Double,
    z: Double
) {
    min.x += x
    max.x += x
    min.y += y
    max.y += y
    min.z += z
    max.z += z
}

inline fun AABB3.subtract(
    x: Double,
    y: Double,
    z: Double
) = add(-x, -y, -z)

fun AABB3.getVertexNX(normalx: Double): Double {
    var p = min.x
    if (normalx < 0) {
        p = max.x
    }
    return p
}

fun AABB3.getVertexNY(normaly: Double): Double {
    var p = min.y
    if (normaly < 0) {
        p = max.y
    }
    return p
}

fun AABB3.getVertexNZ(normalz: Double): Double {
    var p = min.z
    if (normalz < 0) {
        p = max.z
    }
    return p
}

fun AABB3.getVertexPX(normalx: Double): Double {
    var p = min.x
    if (normalx > 0) {
        p = max.x
    }
    return p
}

fun AABB3.getVertexPY(normaly: Double): Double {
    var p = min.y
    if (normaly > 0) {
        p = max.y
    }
    return p
}

fun AABB3.getVertexPZ(normalz: Double): Double {
    var p = min.z
    if (normalz > 0) {
        p = max.z
    }
    return p
}

inline fun AABB3.grow(
    x: Double,
    y: Double,
    z: Double
) = grow(x, y, z, x, y, z)

fun AABB3.grow(
    x1: Double,
    y1: Double,
    z1: Double,
    x2: Double,
    y2: Double,
    z2: Double
) {
    min.x -= x1
    min.y -= y1
    min.z -= z1
    max.x += x2
    max.y += y2
    max.z += z2
}

fun AABB3.scale(value: Double) = scale(value, value, value)

fun AABB3.scale(x: Double, y: Double, z: Double) {
    min.x *= x
    min.y *= y
    min.z *= z
    max.x *= x
    max.y *= y
    max.z *= z
}

fun AABB3.inside(check: ReadVector3d): Boolean =
    inside(check.x, check.y, check.z)

fun AABB3.inside(x: Double, y: Double, z: Double): Boolean =
    !(max.x < x || min.x > x)
            && !(max.y < y || min.y > y)
            && !(max.z < z || min.z > z)

infix fun AABB3.overlaps(check: AABB3): Boolean =
    !(max.x <= check.min.x || min.x >= check.max.x)
            && !(max.y <= check.min.y || min.y >= check.max.y)
            && !(max.z <= check.min.z || min.z >= check.max.z)

fun AABB3.moveOutX(
    base: Double,
    aabbs: Iterable<AABB3>
): Double = aabbs.fold(base) { v, e -> moveOutX(v, e) }

fun AABB3.moveOutY(
    base: Double,
    aabbs: Iterable<AABB3>
): Double = aabbs.fold(base) { v, e -> moveOutY(v, e) }

fun AABB3.moveOutZ(
    base: Double,
    aabbs: Iterable<AABB3>
): Double = aabbs.fold(base) { v, e -> moveOutZ(v, e) }

fun AABB3.moveOutX(
    base: Double,
    check: AABB3,
    error: Double = 0.00001
): Double = offset(
    min.x, check.min.x, max.x, check.max.x,
    min.y, check.min.y, max.y, check.max.y,
    min.z, check.min.z, max.z, check.max.z,
    base, error
)

fun AABB3.moveOutY(
    base: Double,
    check: AABB3,
    error: Double = 0.00001
): Double = offset(
    min.y, check.min.y, max.y, check.max.y,
    min.x, check.min.x, max.x, check.max.x,
    min.z, check.min.z, max.z, check.max.z,
    base, error
)

fun AABB3.moveOutZ(
    base: Double,
    check: AABB3,
    error: Double = 0.00001
): Double = offset(
    min.z, check.min.z, max.z, check.max.z,
    min.x, check.min.x, max.x, check.max.x,
    min.y, check.min.y, max.y, check.max.y,
    base, error
)

private fun offset(
    min1: Double, min2: Double,
    max1: Double, max2: Double,
    minH1: Double, minH2: Double,
    maxH1: Double, maxH2: Double,
    minV1: Double, minV2: Double,
    maxV1: Double, maxV2: Double,
    base: Double, error: Double
): Double {
    if (maxH1 <= minH2 + error || minH1 >= maxH2 - error) return base
    if (maxV1 <= minV2 + error || minV1 >= maxV2 - error) return base
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
