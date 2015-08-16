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

package org.tobi29.scapes.engine.utils.math.matrix;

import org.tobi29.scapes.engine.utils.BufferCreator;
import org.tobi29.scapes.engine.utils.math.FastMath;

import java.nio.FloatBuffer;

public class Matrix3f {
    private final FloatBuffer buffer;
    private float v00, v01, v02, v10, v11, v12, v20, v21, v22;
    private boolean changed = true;

    public Matrix3f() {
        this(BufferCreator.floats(9));
    }

    public Matrix3f(FloatBuffer buffer) {
        this.buffer = buffer;
    }

    public FloatBuffer getBuffer() {
        if (changed) {
            changed = false;
            buffer.rewind();
            buffer.put(v00);
            buffer.put(v01);
            buffer.put(v02);
            buffer.put(v10);
            buffer.put(v11);
            buffer.put(v12);
            buffer.put(v20);
            buffer.put(v21);
            buffer.put(v22);
        }
        buffer.rewind();
        return buffer;
    }

    public void copy(Matrix3f matrix) {
        v00 = matrix.v00;
        v01 = matrix.v01;
        v02 = matrix.v02;
        v10 = matrix.v10;
        v11 = matrix.v11;
        v12 = matrix.v12;
        v20 = matrix.v20;
        v21 = matrix.v21;
        v22 = matrix.v22;
        changed = true;
    }

    public void identity() {
        v00 = 1.0f;
        v01 = 0.0f;
        v02 = 0.0f;
        v10 = 0.0f;
        v11 = 1.0f;
        v12 = 0.0f;
        v20 = 0.0f;
        v21 = 0.0f;
        v22 = 1.0f;
        changed = true;
    }

    public void scale(float x, float y, float z) {
        v00 *= x;
        v10 *= y;
        v20 *= z;
        v01 *= x;
        v11 *= y;
        v21 *= z;
        v02 *= x;
        v12 *= y;
        v22 *= z;
        changed = true;
    }

    public void rotate(float angle, float x, float y, float z) {
        float cos = (float) FastMath.cos(angle * FastMath.DEG_2_RAD);
        float sin = (float) FastMath.sin(angle * FastMath.DEG_2_RAD);
        float oneMinusCos = 1.0f - cos;
        float xy = x * y;
        float yz = y * z;
        float xz = x * z;
        float xSin = x * sin;
        float ySin = y * sin;
        float zSin = z * sin;
        float f00 = x * x * oneMinusCos + cos;
        float f01 = xy * oneMinusCos + zSin;
        float f02 = xz * oneMinusCos - ySin;
        float f10 = xy * oneMinusCos - zSin;
        float f11 = y * y * oneMinusCos + cos;
        float f12 = yz * oneMinusCos + xSin;
        float f20 = xz * oneMinusCos + ySin;
        float f21 = yz * oneMinusCos - xSin;
        float f22 = z * z * oneMinusCos + cos;
        float t00 = v00 * f00 + v10 * f01 + v20 * f02;
        float t01 = v01 * f00 + v11 * f01 + v21 * f02;
        float t02 = v02 * f00 + v12 * f01 + v22 * f02;
        float t10 = v00 * f10 + v10 * f11 + v20 * f12;
        float t11 = v01 * f10 + v11 * f11 + v21 * f12;
        float t12 = v02 * f10 + v12 * f11 + v22 * f12;
        v20 = v00 * f20 + v10 * f21 + v20 * f22;
        v21 = v01 * f20 + v11 * f21 + v21 * f22;
        v22 = v02 * f20 + v12 * f21 + v22 * f22;
        v00 = t00;
        v01 = t01;
        v02 = t02;
        v10 = t10;
        v11 = t11;
        v12 = t12;
        changed = true;
    }

    public void markChanged() {
        changed = true;
    }
}
