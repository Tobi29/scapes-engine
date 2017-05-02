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

package org.tobi29.scapes.engine.utils.math

class Plane(p1x: Double = 0.0,
            p1y: Double = 0.0,
            p1z: Double = 0.0,
            p2x: Double = 0.0,
            p2y: Double = 0.0,
            p2z: Double = 0.0,
            p3x: Double = 0.0,
            p3y: Double = 0.0,
            p3z: Double = 0.0) {
    var normalx = 0.0
    var normaly = 0.0
    var normalz = 0.0
    var p2x = 0.0
    var p2y = 0.0
    var p2z = 0.0

    init {
        set3Points(p1x, p1y, p1z, p2x, p2y, p2z, p3x, p3y, p3z)
    }

    fun set3Points(p1x: Double,
                   p1y: Double,
                   p1z: Double,
                   p2x: Double,
                   p2y: Double,
                   p2z: Double,
                   p3x: Double,
                   p3y: Double,
                   p3z: Double) {
        val e1x = p1x - p2x
        val e1y = p1y - p2y
        val e1z = p1z - p2z
        val e2x = p3x - p2x
        val e2y = p3y - p2y
        val e2z = p3z - p2z
        normalx = e2y * e1z - e2z * e1y
        normaly = e2z * e1x - e2x * e1z
        normalz = e2x * e1y - e2y * e1x
        val l = sqrt(normalx * normalx + normaly * normaly +
                normalz * normalz)
        if (abs(l) > 0.0f) {
            normalx /= l
            normaly /= l
            normalz /= l
        }
        this.p2x = p2x
        this.p2y = p2y
        this.p2z = p2z
    }

    fun distance(x: Double,
                 y: Double,
                 z: Double): Double {
        return -normalx * p2x - normaly * p2y - normalz * p2z +
                normalx * x + normaly * y + normalz * z
    }
}
