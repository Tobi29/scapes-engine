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

import java8.util.function.IntFunction;
import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;
import org.tobi29.scapes.engine.utils.math.vector.Vector3d;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

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

    private final ByteBuffer buffer;
    private final FloatBuffer b;

    public Matrix4f(IntFunction<ByteBuffer> buffer) {
        this.buffer = buffer.apply(16 << 2);
        b = this.buffer.asFloatBuffer();
    }

    public FloatBuffer getBuffer() {
        b.rewind();
        return b;
    }

    public ByteBuffer getByteBuffer() {
        buffer.rewind();
        return buffer;
    }

    public void copy(Matrix4f matrix) {
        b.rewind();
        matrix.b.rewind();
        b.put(matrix.b);
    }

    private float g() {
        return b.get();
    }

    private float gc() {
        return g(b.position());
    }

    private float g(int i) {
        return b.get(i);
    }

    private void s(float value) {
        b.put(value);
    }

    private void s(int i, float value) {
        b.put(i, value);
    }

    public void identity() {
        s(V00, 1.0f);
        s(V01, 0.0f);
        s(V02, 0.0f);
        s(V03, 0.0f);
        s(V10, 0.0f);
        s(V11, 1.0f);
        s(V12, 0.0f);
        s(V13, 0.0f);
        s(V20, 0.0f);
        s(V21, 0.0f);
        s(V22, 1.0f);
        s(V23, 0.0f);
        s(V30, 0.0f);
        s(V31, 0.0f);
        s(V32, 0.0f);
        s(V33, 1.0f);
    }

    public void scale(float x, float y, float z) {
        b.rewind();
        for (int i = 0; i < 4; i++) {
            s(gc() * x);
        }
        for (int i = 0; i < 4; i++) {
            s(gc() * y);
        }
        for (int i = 0; i < 4; i++) {
            s(gc() * z);
        }
    }

    public void translate(float x, float y, float z) {
        s(V30, g(V30) + g(V00) * x + g(V10) * y + g(V20) * z);
        s(V31, g(V31) + g(V01) * x + g(V11) * y + g(V21) * z);
        s(V32, g(V32) + g(V02) * x + g(V12) * y + g(V22) * z);
        s(V33, g(V33) + g(V03) * x + g(V13) * y + g(V23) * z);
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
        float v00 = g(V00);
        float v01 = g(V01);
        float v02 = g(V02);
        float v03 = g(V03);
        float v10 = g(V10);
        float v11 = g(V11);
        float v12 = g(V12);
        float v13 = g(V13);
        float v20 = g(V20);
        float v21 = g(V21);
        float v22 = g(V22);
        float v23 = g(V23);
        float t00 = v00 * f00 + v10 * f01 + v20 * f02;
        float t01 = v01 * f00 + v11 * f01 + v21 * f02;
        float t02 = v02 * f00 + v12 * f01 + v22 * f02;
        float t03 = v03 * f00 + v13 * f01 + v23 * f02;
        float t10 = v00 * f10 + v10 * f11 + v20 * f12;
        float t11 = v01 * f10 + v11 * f11 + v21 * f12;
        float t12 = v02 * f10 + v12 * f11 + v22 * f12;
        float t13 = v03 * f10 + v13 * f11 + v23 * f12;
        s(V20, v00 * f20 + v10 * f21 + v20 * f22);
        s(V21, v01 * f20 + v11 * f21 + v21 * f22);
        s(V22, v02 * f20 + v12 * f21 + v22 * f22);
        s(V23, v03 * f20 + v13 * f21 + v23 * f22);
        s(V00, t00);
        s(V01, t01);
        s(V02, t02);
        s(V03, t03);
        s(V10, t10);
        s(V11, t11);
        s(V12, t12);
        s(V13, t13);
    }

    public void multiply(Matrix4f o, Matrix4f d) {
        float v00 = g(V00);
        float v01 = g(V01);
        float v02 = g(V02);
        float v03 = g(V03);
        float v10 = g(V10);
        float v11 = g(V11);
        float v12 = g(V12);
        float v13 = g(V13);
        float v20 = g(V20);
        float v21 = g(V21);
        float v22 = g(V22);
        float v23 = g(V23);
        float v30 = g(V30);
        float v31 = g(V31);
        float v32 = g(V32);
        float v33 = g(V33);
        float o00 = o.g(V00);
        float o01 = o.g(V01);
        float o02 = o.g(V02);
        float o03 = o.g(V03);
        float o10 = o.g(V10);
        float o11 = o.g(V11);
        float o12 = o.g(V12);
        float o13 = o.g(V13);
        float o20 = o.g(V20);
        float o21 = o.g(V21);
        float o22 = o.g(V22);
        float o23 = o.g(V23);
        float o30 = o.g(V30);
        float o31 = o.g(V31);
        float o32 = o.g(V32);
        float o33 = o.g(V33);
        d.s(V00, v00 * o00 + v10 * o01 + v20 * o02 + v30 * o03);
        d.s(V01, v01 * o00 + v11 * o01 + v21 * o02 + v31 * o03);
        d.s(V02, v02 * o00 + v12 * o01 + v22 * o02 + v32 * o03);
        d.s(V03, v03 * o00 + v13 * o01 + v23 * o02 + v33 * o03);
        d.s(V10, v00 * o10 + v10 * o11 + v20 * o12 + v30 * o13);
        d.s(V11, v01 * o10 + v11 * o11 + v21 * o12 + v31 * o13);
        d.s(V12, v02 * o10 + v12 * o11 + v22 * o12 + v32 * o13);
        d.s(V13, v03 * o10 + v13 * o11 + v23 * o12 + v33 * o13);
        d.s(V20, v00 * o20 + v10 * o21 + v20 * o22 + v30 * o23);
        d.s(V21, v01 * o20 + v11 * o21 + v21 * o22 + v31 * o23);
        d.s(V22, v02 * o20 + v12 * o21 + v22 * o22 + v32 * o23);
        d.s(V23, v03 * o20 + v13 * o21 + v23 * o22 + v33 * o23);
        d.s(V30, v00 * o30 + v10 * o31 + v20 * o32 + v30 * o33);
        d.s(V31, v01 * o30 + v11 * o31 + v21 * o32 + v31 * o33);
        d.s(V32, v02 * o30 + v12 * o31 + v22 * o32 + v32 * o33);
        d.s(V33, v03 * o30 + v13 * o31 + v23 * o32 + v33 * o33);
    }

    public Vector3 multiply(Vector3 v) {
        double x = v.doubleX();
        double y = v.doubleY();
        double z = v.doubleZ();
        double w = 1.0;
        double v1 = g(V00) * x + g(V10) * y + g(V20) * z + g(V30) * w;
        double v2 = g(V01) * x + g(V11) * y + g(V21) * z + g(V31) * w;
        double v3 = g(V02) * x + g(V12) * y + g(V22) * z + g(V32) * w;
        return new Vector3d(v1, v2, v3);
    }

    public void perspective(float fov, float aspectRatio, float near,
            float far) {
        float delta = far - near;
        float cotangent =
                1.0f / (float) FastMath.tan(fov / 2.0f * FastMath.DEG_2_RAD);
        s(V00, cotangent / aspectRatio);
        s(V11, cotangent);
        s(V22, -(far + near) / delta);
        s(V23, -1.0f);
        s(V32, -2.0f * near * far / delta);
        s(V33, 0.0f);
    }

    public void orthogonal(float left, float right, float bottom, float top,
            float zNear, float zFar) {
        s(V00, 2.0f / (right - left));
        s(V01, 0.0f);
        s(V02, 0.0f);
        s(V03, 0.0f);
        s(V10, 0.0f);
        s(V11, 2.0f / (top - bottom));
        s(V12, 0.0f);
        s(V13, 0.0f);
        s(V20, 0.0f);
        s(V21, 0.0f);
        s(V22, 2.0f / (zFar - zNear));
        s(V23, 0.0f);
        s(V30, -(right + left) / (right - left));
        s(V31, -(top + bottom) / (top - bottom));
        s(V32, -(zFar + zNear) / (zFar - zNear));
        s(V33, 1.0f);
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
                if (Math.abs(temp.g((j << 2) + i)) > Math.abs(temp.g(i4 + i))) {
                    swap = j;
                }
            }
            if (swap != i) {
                int swap4 = swap << 2;
                for (int k = 0; k < 4; k++) {
                    float t = temp.g(i4 + k);
                    temp.s(i4 + k, temp.g(swap4 + k));
                    temp.s(swap4 + k, t);
                    t = out.g(i4 + k);
                    out.s(i4 + k, out.g(swap4 + k));
                    out.s(swap4 + k, t);
                }
            }
            if (temp.g(i4 + i) == 0) {
                return false;
            }
            float t = temp.g(i4 + i);
            for (int k = 0; k < 4; k++) {
                temp.s(i4 + k, temp.g(i4 + k) / t);
                out.s(i4 + k, out.g(i4 + k) / t);
            }
            for (int j = 0; j < 4; j++) {
                if (j != i) {
                    int j4 = j << 2;
                    t = temp.g(j4 + i);
                    for (int k = 0; k < 4; k++) {
                        temp.s(j4 + k, temp.g(j4 + k) - temp.g(i4 + k) * t);
                        out.s(j4 + k, out.g(j4 + k) - out.g(i4 + k) * t);
                    }
                }
            }
        }
        return true;
    }
}
