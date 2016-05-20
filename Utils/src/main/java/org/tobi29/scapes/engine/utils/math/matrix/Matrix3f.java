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

import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;
import org.tobi29.scapes.engine.utils.math.vector.Vector3d;

import java.nio.ByteBuffer;

public class Matrix3f {
    private static final int V00, V01, V02, V10, V11, V12, V20, V21, V22;

    static {
        int i = 0;
        V00 = i++;
        V01 = i++;
        V02 = i++;
        V10 = i++;
        V11 = i++;
        V12 = i++;
        V20 = i++;
        V21 = i++;
        V22 = i++;
    }

    private final float[] values = new float[9];

    public float[] values() {
        return values;
    }

    public void putInto(ByteBuffer buffer) {
        for (float value : values) {
            buffer.putFloat(value);
        }
    }

    public void copy(Matrix3f matrix) {
        System.arraycopy(matrix.values, 0, values, 0, values.length);
    }

    public void identity() {
        values[V00] = 1.0f;
        values[V01] = 0.0f;
        values[V02] = 0.0f;
        values[V10] = 0.0f;
        values[V11] = 1.0f;
        values[V12] = 0.0f;
        values[V20] = 0.0f;
        values[V21] = 0.0f;
        values[V22] = 1.0f;
    }

    public void scale(float x, float y, float z) {
        for (int i = 0; i < 3; i++) {
            values[i] = values[i] * x;
        }
        for (int i = 3; i < 6; i++) {
            values[i] = values[i] * y;
        }
        for (int i = 6; i < 9; i++) {
            values[i] = values[i] * z;
        }
    }

    public void rotate(float angle, float x, float y, float z) {
        rotateRad(angle * (float) FastMath.DEG_2_RAD, x, y, z);
    }

    public void rotateRad(float angle, float x, float y, float z) {
        float cos = (float) FastMath.cosTable(angle);
        float sin = (float) FastMath.sinTable(angle);
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
        float v00 = values[V00];
        float v01 = values[V01];
        float v02 = values[V02];
        float v10 = values[V10];
        float v11 = values[V11];
        float v12 = values[V12];
        float v20 = values[V20];
        float v21 = values[V21];
        float v22 = values[V22];
        float t00 = v00 * f00 + v10 * f01 + v20 * f02;
        float t01 = v01 * f00 + v11 * f01 + v21 * f02;
        float t02 = v02 * f00 + v12 * f01 + v22 * f02;
        float t10 = v00 * f10 + v10 * f11 + v20 * f12;
        float t11 = v01 * f10 + v11 * f11 + v21 * f12;
        float t12 = v02 * f10 + v12 * f11 + v22 * f12;
        values[V20] = v00 * f20 + v10 * f21 + v20 * f22;
        values[V21] = v01 * f20 + v11 * f21 + v21 * f22;
        values[V22] = v02 * f20 + v12 * f21 + v22 * f22;
        values[V00] = t00;
        values[V01] = t01;
        values[V02] = t02;
        values[V10] = t10;
        values[V11] = t11;
        values[V12] = t12;
    }

    public void multiply(Matrix3f o, Matrix3f d) {
        float v00 = values[V00];
        float v01 = values[V01];
        float v02 = values[V02];
        float v10 = values[V10];
        float v11 = values[V11];
        float v12 = values[V12];
        float v20 = values[V20];
        float v21 = values[V21];
        float v22 = values[V22];
        float o00 = o.values[V00];
        float o01 = o.values[V01];
        float o02 = o.values[V02];
        float o10 = o.values[V10];
        float o11 = o.values[V11];
        float o12 = o.values[V12];
        float o20 = o.values[V20];
        float o21 = o.values[V21];
        float o22 = o.values[V22];
        d.values[V00] = v00 * o00 + v10 * o01 + v20 * o02;
        d.values[V01] = v01 * o00 + v11 * o01 + v21 * o02;
        d.values[V02] = v02 * o00 + v12 * o01 + v22 * o02;
        d.values[V10] = v00 * o10 + v10 * o11 + v20 * o12;
        d.values[V11] = v01 * o10 + v11 * o11 + v21 * o12;
        d.values[V12] = v02 * o10 + v12 * o11 + v22 * o12;
        d.values[V20] = v00 * o20 + v10 * o21 + v20 * o22;
        d.values[V21] = v01 * o20 + v11 * o21 + v21 * o22;
        d.values[V22] = v02 * o20 + v12 * o21 + v22 * o22;
    }

    public Vector3 multiply(Vector3 v) {
        double x = v.doubleX();
        double y = v.doubleY();
        double z = v.doubleZ();
        double w = 1.0;
        double v1 = values[V00] * x + values[V10] * y + values[V20] * z;
        double v2 = values[V01] * x + values[V11] * y + values[V21] * z;
        double v3 = values[V02] * x + values[V12] * y + values[V22] * z;
        return new Vector3d(v1, v2, v3);
    }
}
