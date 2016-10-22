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
package org.tobi29.scapes.engine.graphics

import org.tobi29.scapes.engine.utils.math.matrix.Matrix3f
import org.tobi29.scapes.engine.utils.math.matrix.Matrix4f

class Matrix {
    private val modelViewMatrix = Matrix4f()
    private val normalMatrix = Matrix3f()

    fun copy(matrix: Matrix) {
        modelViewMatrix.copy(matrix.modelViewMatrix)
        normalMatrix.copy(matrix.normalMatrix)
    }

    fun identity() {
        modelViewMatrix.identity()
        normalMatrix.identity()
    }

    fun scale(x: Float,
              y: Float,
              z: Float) {
        modelViewMatrix.scale(x, y, z)
    }

    fun translate(x: Float,
                  y: Float,
                  z: Float) {
        modelViewMatrix.translate(x, y, z)
    }

    fun rotate(angle: Double,
               x: Float,
               y: Float,
               z: Float) {
        modelViewMatrix.rotate(angle.toFloat(), x, y, z)
        normalMatrix.rotate(angle.toFloat(), x, y, z)
    }

    fun rotate(angle: Float,
               x: Float,
               y: Float,
               z: Float) {
        modelViewMatrix.rotate(angle, x, y, z)
        normalMatrix.rotate(angle, x, y, z)
    }

    fun rotateAccurate(angle: Double,
                       x: Float,
                       y: Float,
                       z: Float) {
        modelViewMatrix.rotateAccurate(angle, x, y, z)
        normalMatrix.rotateAccurate(angle, x, y, z)
    }

    fun modelView(): Matrix4f {
        return modelViewMatrix
    }

    fun normal(): Matrix3f {
        return normalMatrix
    }
}
