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
package org.tobi29.math

import org.tobi29.math.vector.*
import kotlin.math.abs

object Intersection {
    fun intersectPointerPane(lp1: Vector3d,
                             lp2: Vector3d,
                             pane: PointerPane): Vector3d? {
        val minX: Double
        val minY: Double
        val minZ: Double
        val maxX: Double
        val maxY: Double
        val maxZ: Double
        when (pane.face) {
            Face.UP -> {
                minX = pane.x + pane.aabb.minX
                minY = pane.y + pane.aabb.minY
                minZ = pane.z + pane.aabb.maxZ
                maxX = pane.x + pane.aabb.maxX
                maxY = pane.y + pane.aabb.maxY
                maxZ = pane.z + pane.aabb.maxZ
            }
            Face.DOWN -> {
                minX = pane.x + pane.aabb.minX
                minY = pane.y + pane.aabb.minY
                minZ = pane.z + pane.aabb.minZ
                maxX = pane.x + pane.aabb.maxX
                maxY = pane.y + pane.aabb.maxY
                maxZ = pane.z + pane.aabb.minZ
            }
            Face.NORTH -> {
                minX = pane.x + pane.aabb.minX
                minY = pane.y + pane.aabb.minY
                minZ = pane.z + pane.aabb.minZ
                maxX = pane.x + pane.aabb.maxX
                maxY = pane.y + pane.aabb.minY
                maxZ = pane.z + pane.aabb.maxZ
            }
            Face.EAST -> {
                minX = pane.x + pane.aabb.maxX
                minY = pane.y + pane.aabb.minY
                minZ = pane.z + pane.aabb.minZ
                maxX = pane.x + pane.aabb.maxX
                maxY = pane.y + pane.aabb.maxY
                maxZ = pane.z + pane.aabb.maxZ
            }
            Face.SOUTH -> {
                minX = pane.x + pane.aabb.minX
                minY = pane.y + pane.aabb.maxY
                minZ = pane.z + pane.aabb.minZ
                maxX = pane.x + pane.aabb.maxX
                maxY = pane.y + pane.aabb.maxY
                maxZ = pane.z + pane.aabb.maxZ
            }
            Face.WEST -> {
                minX = pane.x + pane.aabb.minX
                minY = pane.y + pane.aabb.minY
                minZ = pane.z + pane.aabb.minZ
                maxX = pane.x + pane.aabb.minX
                maxY = pane.y + pane.aabb.maxY
                maxZ = pane.z + pane.aabb.maxZ
            }
            else -> {
                minX = 0.0
                minY = 0.0
                minZ = 0.0
                maxX = 0.0
                maxY = 0.0
                maxZ = 0.0
            }
        }
        val intersection = intersectPlane(
                lp1, lp2, Vector3d(maxX, minY, minZ),
                Vector3d(minX, maxY, minZ),
                Vector3d(minX, minY, maxZ)) ?: return null
        if (maxX < intersection.x || minX > intersection.x) {
            return null
        }
        if (maxY < intersection.y || minY > intersection.y) {
            return null
        }
        if (maxZ < intersection.z || minZ > intersection.z) {
            return null
        }
        return intersection
    }

    fun intersectPlane(lp1: Vector3d,
                       lp2: Vector3d,
                       p1: Vector3d,
                       p2: Vector3d,
                       p3: Vector3d): Vector3d? {
        val e1 = p2.minus(p1)
        val e2 = p3.minus(p1)
        val normal = e1.cross(e2)
        return intersectPlane(lp1,
                lp2, p1, normal)
    }

    fun intersectPlane(lp1: Vector3d,
                       lp2: Vector3d,
                       p1: Vector3d,
                       normal: Vector3d): Vector3d? {
        val ldir = lp2.minus(lp1)
        val numerator = normal.dot(ldir)
        if (abs(numerator) > 0.0001) {
            val p1tolp1 = p1.minus(lp1)
            val t = normal.dot(p1tolp1) / numerator
            if (t <= 0 || t >= 1) {
                return null
            }
            val pos = lp1.plus(ldir.times(t))
            return pos
        }
        return null
    }
}
