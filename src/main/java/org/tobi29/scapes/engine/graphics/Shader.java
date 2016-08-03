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

package org.tobi29.scapes.engine.graphics;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public interface Shader extends GraphicsObject {
    void activate(GL gl);

    void updateUniforms(GL gl);

    int uniformLocation(int uniform);

    void setUniform1f(int uniform, float v0);

    void setUniform2f(int uniform, float v0, float v1);

    void setUniform3f(int uniform, float v0, float v1, float v2);

    void setUniform4f(int uniform, float v0, float v1, float v2, float v3);

    void setUniform1i(int uniform, int v0);

    void setUniform2i(int uniform, int v0, int v1);

    void setUniform3i(int uniform, int v0, int v1, int v2);

    void setUniform4i(int uniform, int v0, int v1, int v2, int v3);

    void setUniform1(int uniform, FloatBuffer values);

    void setUniform2(int uniform, FloatBuffer values);

    void setUniform3(int uniform, FloatBuffer values);

    void setUniform4(int uniform, FloatBuffer values);

    void setUniform1(int uniform, IntBuffer values);

    void setUniform2(int uniform, IntBuffer values);

    void setUniform3(int uniform, IntBuffer values);

    void setUniform4(int uniform, IntBuffer values);

    void setUniformMatrix2(int uniform, boolean transpose, float[] matrices);

    void setUniformMatrix3(int uniform, boolean transpose, float[] matrices);

    void setUniformMatrix4(int uniform, boolean transpose, float[] matrices);
}
