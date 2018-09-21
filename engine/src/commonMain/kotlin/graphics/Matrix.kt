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
package org.tobi29.scapes.engine.graphics

import org.tobi29.math.matrix.MutableMatrix3f
import org.tobi29.math.matrix.MutableMatrix4f
import org.tobi29.stdex.math.toRad

class Matrix {
    val modelViewProjectionMatrix = MutableMatrix4f()
    val modelViewMatrix = MutableMatrix4f()
    val normalMatrix = MutableMatrix3f()

    fun copy(matrix: Matrix) {
        modelViewProjectionMatrix.set(matrix.modelViewProjectionMatrix)
        modelViewMatrix.set(matrix.modelViewMatrix)
        normalMatrix.set(matrix.normalMatrix)
    }

    fun identity() {
        modelViewProjectionMatrix.identity()
        modelViewMatrix.identity()
        normalMatrix.identity()
    }

    fun scale(x: Float,
              y: Float,
              z: Float) {
        modelViewProjectionMatrix.scale(x, y, z)
        modelViewMatrix.scale(x, y, z)
    }

    fun translate(x: Float,
                  y: Float,
                  z: Float) {
        modelViewProjectionMatrix.translate(x, y, z)
        modelViewMatrix.translate(x, y, z)
    }

    fun rotate(angle: Double,
               x: Float,
               y: Float,
               z: Float) = rotateRad(angle.toRad(), x, y, z)

    fun rotateRad(angle: Double,
                  x: Float,
                  y: Float,
                  z: Float) = rotateRad(angle.toFloat(), x, y, z)

    fun rotate(angle: Float,
               x: Float,
               y: Float,
               z: Float) = rotateRad(angle.toRad(), x, y, z)

    fun rotateRad(angle: Float,
                  x: Float,
                  y: Float,
                  z: Float) {
        modelViewProjectionMatrix.rotateRad(angle, x, y, z)
        modelViewMatrix.rotateRad(angle, x, y, z)
        normalMatrix.rotateRad(angle, x, y, z)
    }

    fun rotateAccurate(angle: Double,
                       x: Float,
                       y: Float,
                       z: Float) = rotateAccurateRad(angle.toRad(), x, y, z)

    fun rotateAccurateRad(angle: Double,
                          x: Float,
                          y: Float,
                          z: Float) {
        modelViewProjectionMatrix.rotateAccurateRad(angle, x, y, z)
        modelViewMatrix.rotateAccurateRad(angle, x, y, z)
        normalMatrix.rotateAccurateRad(angle, x, y, z)
    }
}
