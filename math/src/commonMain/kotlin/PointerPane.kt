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

package org.tobi29.math

class PointerPane(
    val aabb: AABB3 = AABB3(),
    var face: Face = Face.NONE,
    var x: Int = 0,
    var y: Int = 0,
    var z: Int = 0
) {
    fun set(
        aabb: AABB3,
        face: Face,
        x: Int, y: Int, z: Int
    ) {
        set(
            aabb.min.x, aabb.min.y, aabb.min.z,
            aabb.max.x, aabb.max.y, aabb.max.z,
            face,
            x, y, z
        )
    }

    fun set(
        minX: Double, minY: Double, minZ: Double,
        maxX: Double, maxY: Double, maxZ: Double,
        face: Face,
        x: Int, y: Int, z: Int
    ) {
        aabb.min.x = minX
        aabb.min.y = minY
        aabb.min.z = minZ
        aabb.max.x = maxX
        aabb.max.y = maxY
        aabb.max.z = maxZ
        this.face = face
        this.x = x
        this.y = y
        this.z = z
    }
}
