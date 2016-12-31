/*
 * Copyright 2012-2016 Tobi29
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

package org.tobi29.scapes.engine.utils.math

import org.tobi29.scapes.engine.utils.math.vector.Vector3d

class AABB(var minX: Double, var minY: Double, var minZ: Double, var maxX: Double, var maxY: Double,
           var maxZ: Double) {

    constructor(aabb: AABB) : this(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX,
            aabb.maxY, aabb.maxZ)

    private fun offset(min1: Double,
                       min2: Double,
                       max1: Double,
                       max2: Double,
                       minH1: Double,
                       minH2: Double,
                       maxH1: Double,
                       maxH2: Double,
                       minV1: Double,
                       minV2: Double,
                       maxV1: Double,
                       maxV2: Double,
                       base: Double,
                       error: Double): Double {
        var base = base
        if (maxH1 <= minH2 + error || minH1 >= maxH2 - error) {
            return base
        }
        if (maxV1 <= minV2 + error || minV1 >= maxV2 - error) {
            return base
        }
        if (base > 0 && max1 <= min2 + error) {
            val diff = min2 - max1
            if (diff < base) {
                base = diff
            }
        }
        if (base < 0 && min1 >= max2 - error) {
            val diff = max2 - min1
            if (diff > base) {
                base = diff
            }
        }
        return base
    }

    fun moveOutX(aabbs: Sequence<AABB>,
                 base: Double): Double {
        var output = base
        aabbs.forEach { output = moveOutX(it, output) }
        return output
    }

    fun moveOutY(aabbs: Sequence<AABB>,
                 base: Double): Double {
        var output = base
        aabbs.forEach { output = moveOutY(it, output) }
        return output
    }

    fun moveOutZ(aabbs: Sequence<AABB>,
                 base: Double): Double {
        var output = base
        aabbs.forEach { output = moveOutZ(it, output) }
        return output
    }


    fun copy(aabb: AABB): AABB {
        minX = aabb.minX
        minY = aabb.minY
        minZ = aabb.minZ
        maxX = aabb.maxX
        maxY = aabb.maxY
        maxZ = aabb.maxZ
        return this
    }

    fun add(x: Double,
            y: Double,
            z: Double): AABB {
        minX += x
        maxX += x
        minY += y
        maxY += y
        minZ += z
        maxZ += z
        return this
    }

    fun subtract(x: Double,
                 y: Double,
                 z: Double): AABB {
        minX -= x
        maxX -= x
        minY -= y
        maxY -= y
        minZ -= z
        maxZ -= z
        return this
    }

    fun getVertexNX(normalx: Double): Double {
        var p = minX
        if (normalx < 0) {
            p = maxX
        }
        return p
    }

    fun getVertexNY(normaly: Double): Double {
        var p = minY
        if (normaly < 0) {
            p = maxY
        }
        return p
    }

    fun getVertexNZ(normalz: Double): Double {
        var p = minZ
        if (normalz < 0) {
            p = maxZ
        }
        return p
    }

    fun getVertexPX(normalx: Double): Double {
        var p = minX
        if (normalx > 0) {
            p = maxX
        }
        return p
    }

    fun getVertexPY(normaly: Double): Double {
        var p = minY
        if (normaly > 0) {
            p = maxY
        }
        return p
    }

    fun getVertexPZ(normalz: Double): Double {
        var p = minZ
        if (normalz > 0) {
            p = maxZ
        }
        return p
    }

    fun grow(x: Double,
             y: Double,
             z: Double): AABB {
        minX -= x
        minY -= y
        minZ -= z
        maxX += x
        maxY += y
        maxZ += z
        return this
    }

    fun grow(x1: Double,
             y1: Double,
             z1: Double,
             x2: Double,
             y2: Double,
             z2: Double): AABB {
        minX -= x1
        minY -= y1
        minZ -= z1
        maxX += x2
        maxY += y2
        maxZ += z2
        return this
    }

    fun scale(value: Double): AABB {
        return scale(value, value, value)
    }

    fun scale(x: Double,
              y: Double,
              z: Double): AABB {
        minX *= x
        minY *= y
        minZ *= z
        maxX *= x
        maxY *= y
        maxZ *= z
        return this
    }

    fun inside(check: Vector3d): Boolean {
        return inside(check.x, check.y, check.z)
    }

    fun inside(x: Double,
               y: Double,
               z: Double): Boolean {
        return !(maxX < x || minX > x) && !(maxY < y || minY > y) &&
                !(maxZ < z || minZ > z)
    }

    fun moveOutX(check: AABB,
                 base: Double,
                 error: Double = 0.00001): Double {
        return offset(minX, check.minX, maxX, check.maxX, minY, check.minY,
                maxY, check.maxY, minZ, check.minZ, maxZ, check.maxZ, base,
                error)
    }

    fun moveOutY(check: AABB,
                 base: Double,
                 error: Double = 0.00001): Double {
        return offset(minY, check.minY, maxY, check.maxY, minX, check.minX,
                maxX, check.maxX, minZ, check.minZ, maxZ, check.maxZ, base,
                error)
    }

    fun moveOutZ(check: AABB,
                 base: Double,
                 error: Double = 0.00001): Double {
        return offset(minZ, check.minZ, maxZ, check.maxZ, minX, check.minX,
                maxX, check.maxX, minY, check.minY, maxY, check.maxY, base,
                error)
    }

    fun overlay(check: AABB): Boolean {
        return !(maxX <= check.minX || minX >= check.maxX) &&
                !(maxY <= check.minY || minY >= check.maxY) &&
                !(maxZ <= check.minZ || minZ >= check.maxZ)
    }
}
