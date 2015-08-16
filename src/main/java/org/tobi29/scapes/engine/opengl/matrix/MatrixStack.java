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

import org.tobi29.scapes.engine.utils.BufferCreatorNative;

import java.nio.FloatBuffer;

public class MatrixStack {
    private final Matrix[] stack;
    private int i;

    public MatrixStack(int matrices) {
        stack = new Matrix[matrices];
        FloatBuffer modelViewBuffer = BufferCreatorNative.floatsD(16);
        FloatBuffer normalBuffer = BufferCreatorNative.floatsD(9);
        for (int i = 0; i < stack.length; i++) {
            stack[i] = new Matrix(modelViewBuffer, normalBuffer);
        }
        stack[0].identity();
    }

    public Matrix push() {
        Matrix bottom = stack[i++];
        if (i >= stack.length) {
            throw new IllegalStateException("Stack overflow.");
        }
        Matrix top = stack[i];
        top.copy(bottom);
        return top;
    }

    public Matrix pop() {
        i--;
        if (i < 0) {
            throw new IllegalStateException("Stack underflow.");
        }
        Matrix bottom = stack[i];
        bottom.markChanged();
        return bottom;
    }

    public Matrix current() {
        return stack[i];
    }
}
