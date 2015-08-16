/*
 * Copyright 2012-2015 Tobi29
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

package org.tobi29.scapes.engine.opengl.matrix;

import org.tobi29.scapes.engine.utils.math.matrix.Matrix3f;
import org.tobi29.scapes.engine.utils.math.matrix.Matrix4f;

import java.nio.FloatBuffer;

public class Matrix {
    private final Matrix4f modelViewMatrix;
    private final Matrix3f normalMatrix;

    public Matrix(FloatBuffer modelViewBuffer, FloatBuffer normalBuffer) {
        modelViewMatrix = new Matrix4f(modelViewBuffer);
        normalMatrix = new Matrix3f(normalBuffer);
    }

    public void copy(Matrix matrix) {
        modelViewMatrix.copy(matrix.modelViewMatrix);
        normalMatrix.copy(matrix.normalMatrix);
    }

    public void identity() {
        modelViewMatrix.identity();
        normalMatrix.identity();
    }

    public void scale(float x, float y, float z) {
        modelViewMatrix.scale(x, y, z);
    }

    public void translate(float x, float y, float z) {
        modelViewMatrix.translate(x, y, z);
    }

    public void rotate(float angle, float x, float y, float z) {
        modelViewMatrix.rotate(angle, x, y, z);
        normalMatrix.rotate(angle, x, y, z);
    }

    public void markChanged() {
        modelViewMatrix.markChanged();
        normalMatrix.markChanged();
    }

    public Matrix4f modelView() {
        return modelViewMatrix;
    }

    public Matrix3f normal() {
        return normalMatrix;
    }
}
