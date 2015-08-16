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

public class Matrix4f {
    private final FloatBuffer buffer;
    private float v00, v01, v02, v03, v10, v11, v12, v13, v20, v21, v22, v23,
            v30, v31, v32, v33;
    private boolean changed = true;

    public Matrix4f() {
        this(BufferCreator.floats(16));
    }

    public Matrix4f(FloatBuffer buffer) {
        this.buffer = buffer;
    }

    public FloatBuffer getBuffer() {
        if (changed) {
            changed = false;
            buffer.rewind();
            buffer.put(v00);
            buffer.put(v01);
            buffer.put(v02);
            buffer.put(v03);
            buffer.put(v10);
            buffer.put(v11);
            buffer.put(v12);
            buffer.put(v13);
            buffer.put(v20);
            buffer.put(v21);
            buffer.put(v22);
            buffer.put(v23);
            buffer.put(v30);
            buffer.put(v31);
            buffer.put(v32);
            buffer.put(v33);
        }
        buffer.rewind();
        return buffer;
    }

    public void copy(Matrix4f matrix) {
        v00 = matrix.v00;
        v01 = matrix.v01;
        v02 = matrix.v02;
        v03 = matrix.v03;
        v10 = matrix.v10;
        v11 = matrix.v11;
        v12 = matrix.v12;
        v13 = matrix.v13;
        v20 = matrix.v20;
        v21 = matrix.v21;
        v22 = matrix.v22;
        v23 = matrix.v23;
        v30 = matrix.v30;
        v31 = matrix.v31;
        v32 = matrix.v32;
        v33 = matrix.v33;
        changed = true;
    }

    public void identity() {
        v00 = 1.0f;
        v01 = 0.0f;
        v02 = 0.0f;
        v03 = 0.0f;
        v10 = 0.0f;
        v11 = 1.0f;
        v12 = 0.0f;
        v13 = 0.0f;
        v20 = 0.0f;
        v21 = 0.0f;
        v22 = 1.0f;
        v23 = 0.0f;
        v30 = 0.0f;
        v31 = 0.0f;
        v32 = 0.0f;
        v33 = 1.0f;
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
        v03 *= x;
        v13 *= y;
        v23 *= z;
        changed = true;
    }

    public void translate(float x, float y, float z) {
        v30 += v00 * x + v10 * y + v20 * z;
        v31 += v01 * x + v11 * y + v21 * z;
        v32 += v02 * x + v12 * y + v22 * z;
        v33 += v03 * x + v13 * y + v23 * z;
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
        float t03 = v03 * f00 + v13 * f01 + v23 * f02;
        float t10 = v00 * f10 + v10 * f11 + v20 * f12;
        float t11 = v01 * f10 + v11 * f11 + v21 * f12;
        float t12 = v02 * f10 + v12 * f11 + v22 * f12;
        float t13 = v03 * f10 + v13 * f11 + v23 * f12;
        v20 = v00 * f20 + v10 * f21 + v20 * f22;
        v21 = v01 * f20 + v11 * f21 + v21 * f22;
        v22 = v02 * f20 + v12 * f21 + v22 * f22;
        v23 = v03 * f20 + v13 * f21 + v23 * f22;
        v00 = t00;
        v01 = t01;
        v02 = t02;
        v03 = t03;
        v10 = t10;
        v11 = t11;
        v12 = t12;
        v13 = t13;
        changed = true;
    }

    public void multiply(Matrix4f o, Matrix4f d) {
        d.v00 = v00 * o.v00 + v10 * o.v01 + v20 * o.v02 + v30 * o.v03;
        d.v01 = v01 * o.v00 + v11 * o.v01 + v21 * o.v02 + v31 * o.v03;
        d.v02 = v02 * o.v00 + v12 * o.v01 + v22 * o.v02 + v32 * o.v03;
        d.v03 = v03 * o.v00 + v13 * o.v01 + v23 * o.v02 + v33 * o.v03;
        d.v10 = v00 * o.v10 + v10 * o.v11 + v20 * o.v12 + v30 * o.v13;
        d.v11 = v01 * o.v10 + v11 * o.v11 + v21 * o.v12 + v31 * o.v13;
        d.v12 = v02 * o.v10 + v12 * o.v11 + v22 * o.v12 + v32 * o.v13;
        d.v13 = v03 * o.v10 + v13 * o.v11 + v23 * o.v12 + v33 * o.v13;
        d.v20 = v00 * o.v20 + v10 * o.v21 + v20 * o.v22 + v30 * o.v23;
        d.v21 = v01 * o.v20 + v11 * o.v21 + v21 * o.v22 + v31 * o.v23;
        d.v22 = v02 * o.v20 + v12 * o.v21 + v22 * o.v22 + v32 * o.v23;
        d.v23 = v03 * o.v20 + v13 * o.v21 + v23 * o.v22 + v33 * o.v23;
        d.v30 = v00 * o.v30 + v10 * o.v31 + v20 * o.v32 + v30 * o.v33;
        d.v31 = v01 * o.v30 + v11 * o.v31 + v21 * o.v32 + v31 * o.v33;
        d.v32 = v02 * o.v30 + v12 * o.v31 + v22 * o.v32 + v32 * o.v33;
        d.v33 = v03 * o.v30 + v13 * o.v31 + v23 * o.v32 + v33 * o.v33;
        d.changed = true;
    }

    public void perspective(float fov, float aspectRatio, float near,
            float far) {
        float delta = far - near;
        float cotangent =
                1.0f / (float) FastMath.tan(fov / 2.0f * FastMath.DEG_2_RAD);
        v00 = cotangent / aspectRatio;
        v11 = cotangent;
        v22 = -(far + near) / delta;
        v23 = -1.0f;
        v32 = -2.0f * near * far / delta;
        v33 = 0.0f;
        changed = true;
    }

    public void orthogonal(float left, float right, float bottom, float top,
            float zNear, float zFar) {
        v00 = 2.0f / (right - left);
        v01 = 0.0f;
        v02 = 0.0f;
        v03 = 0.0f;
        v10 = 0.0f;
        v11 = 2.0f / (top - bottom);
        v12 = 0.0f;
        v13 = 0.0f;
        v20 = 0.0f;
        v21 = 0.0f;
        v22 = 2.0f / (zFar - zNear);
        v23 = 0.0f;
        v30 = -(right + left) / (right - left);
        v31 = -(top + bottom) / (top - bottom);
        v32 = -(zFar + zNear) / (zFar - zNear);
        v33 = 1.0f;
        changed = true;
    }

    public void markChanged() {
        changed = true;
    }
}
