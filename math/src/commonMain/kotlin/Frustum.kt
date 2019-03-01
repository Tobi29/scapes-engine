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

import org.tobi29.math.vector.*
import org.tobi29.stdex.math.toRad
import kotlin.math.tan

data class FrustumConfiguration(
    val near: Double,
    val far: Double,
    val angle: Double,
    val ratio: Double
) {
    val nearSize: Vector2d
    val farSize: Vector2d
    val range: Double

    init {
        val tang = tan(angle.toRad() * 0.5)
        val nh = near * tang
        nearSize = Vector2d(nh * ratio, nh)
        val fh = far * tang
        farSize = Vector2d(fh * ratio, fh)
        range = length(far, max(farSize))
    }

}

class Frustum(
    position: Vector3d,
    lookAt: Vector3d,
    up: Vector3d,
    configuration: FrustumConfiguration
) {
    private val planes: Array<Plane>

    init {
        val z = (position - lookAt).normalizedSafe()
        val x = (up cross z).normalizedSafe()
        val y = z cross x
        val nc = position - z * configuration.near
        val fc = position - z * configuration.far
        val nw = x * configuration.nearSize.x
        val nh = y * configuration.nearSize.y
        val fw = x * configuration.farSize.x
        val fh = y * configuration.farSize.y
        val ntl = nc + nh - nw
        val ntr = nc + nh + nw
        val nbl = nc - nh - nw
        val nbr = nc - nh + nw
        val ftl = fc + fh - fw
        val ftr = fc + fh + fw
        val fbl = fc - fh - fw
        val fbr = fc - fh + fw
        planes = arrayOf(
            Plane.from3Points(ntr, ntl, ftl),
            Plane.from3Points(nbl, nbr, fbr),
            Plane.from3Points(ntl, nbl, fbl),
            Plane.from3Points(nbr, ntr, fbr),
            Plane.from3Points(ntl, ntr, nbr),
            Plane.from3Points(ftr, ftl, fbl)
        )
    }

    fun inView(aabb: AABB3): Int {
        var result = 2
        for (i in 0..5) {
            val plane = planes[i]
            if (plane.distance(aabb.getVertexP(plane.normal)) < 0.0) {
                return 0
            } else if (plane.distance(aabb.getVertexN(plane.normal)) < 0.0) {
                result = 1
            }
        }
        return result
    }
}
