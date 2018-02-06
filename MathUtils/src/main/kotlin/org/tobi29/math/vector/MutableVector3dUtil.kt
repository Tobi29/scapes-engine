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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.math.vector

/**
 * Computes the cross product of the given vectors
 * @receiver The first vector
 * @param other The second vector
 * @param output The vector to store the output in
 * @return The output vector
 */
inline fun MutableVector3d.cross(
    other: MutableVector3d,
    output: MutableVector3d
): MutableVector3d {
    cross(x, y, z, other.x, other.y, other.z) { x, y, z ->
        output.setXYZ(x, y, z)
    }
    return output
}

/**
 * Normalizes the given vector so that its length is `1.0`,
 * or fill with `NaN` if the given vector is `(0, 0, 0)`
 * @receiver The vector
 * @return The vector
 */
// TODO: Kotlin/JS Bug
/*inline*/ fun MutableVector3d.normalize(): MutableVector3d {
    val length = length()
    return this.divide(length)
}

/**
 * Normalizes the given vector so that its length is `1.0`,
 * or fill with `NaN` if the given vector is `(0, 0, 0)`
 * @receiver The vector
 * @return The vector
 */
// TODO: Kotlin/JS Bug
/*inline*/ fun MutableVector3d.normalizeSafe(): MutableVector3d {
    val length = length()
    return if (length == 0.0) setXYZ(0.0, 0.0, 0.0)
    else this.divide(length)
}
