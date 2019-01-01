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

package org.tobi29.math.matrix

import org.tobi29.math.vector.dot

inline fun <R> matrix4dMultiply(
    xx: Double, xy: Double, xz: Double, xw: Double,
    yx: Double, yy: Double, yz: Double, yw: Double,
    zx: Double, zy: Double, zz: Double, zw: Double,
    wx: Double, wy: Double, wz: Double, ww: Double,
    x: Double, y: Double, z: Double, w: Double,
    output: (Double, Double, Double, Double) -> R
): R = output(
    dot(xx, yx, zx, wx, x, y, z, w),
    dot(xy, yy, zy, wy, x, y, z, w),
    dot(xz, yz, zz, wz, x, y, z, w),
    dot(xw, yw, zw, ww, x, y, z, w)
)
