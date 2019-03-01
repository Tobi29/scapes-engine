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

data class Plane(
    val origin: Vector3d,
    val normal: Vector3d
) {
    fun distance(point: ReadVector3d): Double =
        (normal dot point) - (normal dot origin)

    companion object {
        fun from3Points(
            a: Vector3d,
            b: Vector3d,
            c: Vector3d
        ): Plane {
            val ab = a - b
            val cb = c - b
            return Plane(
                origin = a,
                normal = (cb cross ab).normalizedSafe()
            )
        }
    }
}
