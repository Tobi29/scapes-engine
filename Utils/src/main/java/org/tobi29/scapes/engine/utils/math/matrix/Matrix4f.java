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

public class Matrix4f {
    private static final int V00, V01, V02, V03, V10, V11, V12, V13, V20, V21,
            V22, V23, V30, V31, V32, V33;

    static {
        int i = 0;
        V00 = i++;
        V01 = i++;
        V02 = i++;
        V03 = i++;
        V10 = i++;
        V11 = i++;
        V12 = i++;
        V13 = i++;
        V20 = i++;
        V21 = i++;
        V22 = i++;
        V23 = i++;
        V30 = i++;
        V31 = i++;
        V32 = i++;
        V33 = i++;
    }

    private final float[] values = new float[16];

    public float[] values() {
        return values;
    }

    public void putInto(ByteBuffer buffer) {
        for (float value : values) {
            buffer.putFloat(value);
        }
    }

    public void copy(Matrix4f matrix) {
        System.arraycopy(matrix.values, 0, values, 0, values.length);
    }

    public void identity() {
        values[V00] = 1.0f;
        values[V01] = 0.0f;
        values[V02] = 0.0f;
        values[V03] = 0.0f;
        values[V10] = 0.0f;
        values[V11] = 1.0f;
        values[V12] = 0.0f;
        values[V13] = 0.0f;
        values[V20] = 0.0f;
        values[V21] = 0.0f;
        values[V22] = 1.0f;
        values[V23] = 0.0f;
        values[V30] = 0.0f;
        values[V31] = 0.0f;
        values[V32] = 0.0f;
        values[V33] = 1.0f;
    }

    public void scale(float x, float y, float z) {
        for (int i = 0; i < 4; i++) {
            values[i] = values[i] * x;
        }
        for (int i = 4; i < 8; i++) {
            values[i] = values[i] * y;
        }
        for (int i = 8; i < 12; i++) {
            values[i] = values[i] * z;
        }
    }

    public void translate(float x, float y, float z) {
        values[V30] = values[V30] + values[V00] * x + values[V10] * y +
                values[V20] * z;
        values[V31] = values[V31] + values[V01] * x + values[V11] * y +
                values[V21] * z;
        values[V32] = values[V32] + values[V02] * x + values[V12] * y +
                values[V22] * z;
        values[V33] = values[V33] + values[V03] * x + values[V13] * y +
                values[V23] * z;
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
        float v03 = values[V03];
        float v10 = values[V10];
        float v11 = values[V11];
        float v12 = values[V12];
        float v13 = values[V13];
        float v20 = values[V20];
        float v21 = values[V21];
        float v22 = values[V22];
        float v23 = values[V23];
        float t00 = v00 * f00 + v10 * f01 + v20 * f02;
        float t01 = v01 * f00 + v11 * f01 + v21 * f02;
        float t02 = v02 * f00 + v12 * f01 + v22 * f02;
        float t03 = v03 * f00 + v13 * f01 + v23 * f02;
        float t10 = v00 * f10 + v10 * f11 + v20 * f12;
        float t11 = v01 * f10 + v11 * f11 + v21 * f12;
        float t12 = v02 * f10 + v12 * f11 + v22 * f12;
        float t13 = v03 * f10 + v13 * f11 + v23 * f12;
        values[V20] = v00 * f20 + v10 * f21 + v20 * f22;
        values[V21] = v01 * f20 + v11 * f21 + v21 * f22;
        values[V22] = v02 * f20 + v12 * f21 + v22 * f22;
        values[V23] = v03 * f20 + v13 * f21 + v23 * f22;
        values[V00] = t00;
        values[V01] = t01;
        values[V02] = t02;
        values[V03] = t03;
        values[V10] = t10;
        values[V11] = t11;
        values[V12] = t12;
        values[V13] = t13;
    }

    public void multiply(Matrix4f o, Matrix4f d) {
        float v00 = values[V00];
        float v01 = values[V01];
        float v02 = values[V02];
        float v03 = values[V03];
        float v10 = values[V10];
        float v11 = values[V11];
        float v12 = values[V12];
        float v13 = values[V13];
        float v20 = values[V20];
        float v21 = values[V21];
        float v22 = values[V22];
        float v23 = values[V23];
        float v30 = values[V30];
        float v31 = values[V31];
        float v32 = values[V32];
        float v33 = values[V33];
        float o00 = o.values[V00];
        float o01 = o.values[V01];
        float o02 = o.values[V02];
        float o03 = o.values[V03];
        float o10 = o.values[V10];
        float o11 = o.values[V11];
        float o12 = o.values[V12];
        float o13 = o.values[V13];
        float o20 = o.values[V20];
        float o21 = o.values[V21];
        float o22 = o.values[V22];
        float o23 = o.values[V23];
        float o30 = o.values[V30];
        float o31 = o.values[V31];
        float o32 = o.values[V32];
        float o33 = o.values[V33];
        d.values[V00] = v00 * o00 + v10 * o01 + v20 * o02 + v30 * o03;
        d.values[V01] = v01 * o00 + v11 * o01 + v21 * o02 + v31 * o03;
        d.values[V02] = v02 * o00 + v12 * o01 + v22 * o02 + v32 * o03;
        d.values[V03] = v03 * o00 + v13 * o01 + v23 * o02 + v33 * o03;
        d.values[V10] = v00 * o10 + v10 * o11 + v20 * o12 + v30 * o13;
        d.values[V11] = v01 * o10 + v11 * o11 + v21 * o12 + v31 * o13;
        d.values[V12] = v02 * o10 + v12 * o11 + v22 * o12 + v32 * o13;
        d.values[V13] = v03 * o10 + v13 * o11 + v23 * o12 + v33 * o13;
        d.values[V20] = v00 * o20 + v10 * o21 + v20 * o22 + v30 * o23;
        d.values[V21] = v01 * o20 + v11 * o21 + v21 * o22 + v31 * o23;
        d.values[V22] = v02 * o20 + v12 * o21 + v22 * o22 + v32 * o23;
        d.values[V23] = v03 * o20 + v13 * o21 + v23 * o22 + v33 * o23;
        d.values[V30] = v00 * o30 + v10 * o31 + v20 * o32 + v30 * o33;
        d.values[V31] = v01 * o30 + v11 * o31 + v21 * o32 + v31 * o33;
        d.values[V32] = v02 * o30 + v12 * o31 + v22 * o32 + v32 * o33;
        d.values[V33] = v03 * o30 + v13 * o31 + v23 * o32 + v33 * o33;
    }

    public Vector3 multiply(Vector3 v) {
        double x = v.doubleX();
        double y = v.doubleY();
        double z = v.doubleZ();
        double w = 1.0;
        double v1 = values[V00] * x + values[V10] * y + values[V20] * z +
                values[V30] * w;
        double v2 = values[V01] * x + values[V11] * y + values[V21] * z +
                values[V31] * w;
        double v3 = values[V02] * x + values[V12] * y + values[V22] * z +
                values[V32] * w;
        return new Vector3d(v1, v2, v3);
    }

    public void perspective(float fov, float aspectRatio, float near,
            float far) {
        float delta = far - near;
        float cotangent =
                1.0f / (float) FastMath.tan(fov / 2.0f * FastMath.DEG_2_RAD);
        values[V00] = cotangent / aspectRatio;
        values[V11] = cotangent;
        float value2 = -(far + near) / delta;
        values[V22] = value2;
        float value1 = -1.0f;
        values[V23] = value1;
        float value = -2.0f * near * far / delta;
        values[V32] = value;
        values[V33] = 0.0f;
    }

    public void orthogonal(float left, float right, float bottom, float top,
            float zNear, float zFar) {
        values[V00] = 2.0f / (right - left);
        values[V01] = 0.0f;
        values[V02] = 0.0f;
        values[V03] = 0.0f;
        values[V10] = 0.0f;
        values[V11] = 2.0f / (top - bottom);
        values[V12] = 0.0f;
        values[V13] = 0.0f;
        values[V20] = 0.0f;
        values[V21] = 0.0f;
        values[V22] = 2.0f / (zFar - zNear);
        values[V23] = 0.0f;
        float value2 = -(right + left) / (right - left);
        values[V30] = value2;
        float value1 = -(top + bottom) / (top - bottom);
        values[V31] = value1;
        float value = -(zFar + zNear) / (zFar - zNear);
        values[V32] = value;
        values[V33] = 1.0f;
    }

    public boolean invert(Matrix4f temp, Matrix4f out) {
        if (temp != this) {
            temp.copy(this);
        }
        out.identity();
        for (int i = 0; i < 4; i++) {
            int i4 = i << 2;
            int swap = i;
            for (int j = i + 1; j < 4; j++) {
                if (Math.abs(temp.values[(j << 2) + i]) >
                        Math.abs(temp.values[i4 + i])) {
                    swap = j;
                }
            }
            if (swap != i) {
                int swap4 = swap << 2;
                for (int k = 0; k < 4; k++) {
                    float t = temp.values[i4 + k];
                    temp.values[i4 + k] = temp.values[swap4 + k];
                    temp.values[swap4 + k] = t;
                    t = out.values[i4 + k];
                    out.values[i4 + k] = out.values[swap4 + k];
                    out.values[swap4 + k] = t;
                }
            }
            if (temp.values[i4 + i] == 0) {
                return false;
            }
            float t = temp.values[i4 + i];
            for (int k = 0; k < 4; k++) {
                temp.values[i4 + k] = temp.values[i4 + k] / t;
                out.values[i4 + k] = out.values[i4 + k] / t;
            }
            for (int j = 0; j < 4; j++) {
                if (j != i) {
                    int j4 = j << 2;
                    t = temp.values[j4 + i];
                    for (int k = 0; k < 4; k++) {
                        temp.values[j4 + k] =
                                temp.values[j4 + k] - temp.values[i4 + k] * t;
                        out.values[j4 + k] =
                                out.values[j4 + k] - out.values[i4 + k] * t;
                    }
                }
            }
        }
        return true;
    }
}
