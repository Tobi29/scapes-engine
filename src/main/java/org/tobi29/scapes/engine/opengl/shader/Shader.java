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
package org.tobi29.scapes.engine.opengl.shader;

import java8.util.function.Consumer;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.OpenGLFunction;
import org.tobi29.scapes.engine.utils.Pair;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

public class Shader {
    private final Queue<Consumer<GL>> uniforms = new ArrayDeque<>();
    private final int[] uniformLocations;
    private final int program;

    @OpenGLFunction
    public Shader(String source, ShaderPreprocessor processor, GL gl)
            throws IOException {
        Pair<Integer, int[]> program = gl.createProgram(source, processor);
        this.program = program.a;
        uniformLocations = program.b;
    }

    public int programID() {
        return program;
    }

    public void updateUniforms(GL gl) {
        while (!uniforms.isEmpty()) {
            uniforms.poll().accept(gl);
        }
    }

    public int uniformLocation(int uniform) {
        return uniformLocations[uniform];
    }

    @OpenGLFunction
    public void dispose(GL gl) {
        gl.deleteProgram(program);
    }

    public void setUniform1f(int uniform, float v0) {
        int uniformLocation = uniformLocations[uniform];
        if (uniformLocation != -1) {
            uniforms.add(
                    shaderGL -> shaderGL.setUniform1f(uniformLocation, v0));
        }
    }

    public void setUniform2f(int uniform, float v0, float v1) {
        int uniformLocation = uniformLocations[uniform];
        if (uniformLocation != -1) {
            uniforms.add(
                    shaderGL -> shaderGL.setUniform2f(uniformLocation, v0, v1));
        }
    }

    public void setUniform3f(int uniform, float v0, float v1, float v2) {
        int uniformLocation = uniformLocations[uniform];
        if (uniformLocation != -1) {
            uniforms.add(shaderGL -> shaderGL
                    .setUniform3f(uniformLocation, v0, v1, v2));
        }
    }

    public void setUniform4f(int uniform, float v0, float v1, float v2,
            float v3) {
        int uniformLocation = uniformLocations[uniform];
        if (uniformLocation != -1) {
            uniforms.add(shaderGL -> shaderGL
                    .setUniform4f(uniformLocation, v0, v1, v2, v3));
        }
    }

    public void setUniform1i(int uniform, int v0) {
        int uniformLocation = uniformLocations[uniform];
        if (uniformLocation != -1) {
            uniforms.add(
                    shaderGL -> shaderGL.setUniform1i(uniformLocation, v0));
        }
    }

    public void setUniform2i(int uniform, int v0, int v1) {
        int uniformLocation = uniformLocations[uniform];
        if (uniformLocation != -1) {
            uniforms.add(
                    shaderGL -> shaderGL.setUniform2i(uniformLocation, v0, v1));
        }
    }

    public void setUniform3i(int uniform, int v0, int v1, int v2) {
        int uniformLocation = uniformLocations[uniform];
        if (uniformLocation != -1) {
            uniforms.add(shaderGL -> shaderGL
                    .setUniform3i(uniformLocation, v0, v1, v2));
        }
    }

    public void setUniform4i(int uniform, int v0, int v1, int v2, int v3) {
        int uniformLocation = uniformLocations[uniform];
        if (uniformLocation != -1) {
            uniforms.add(shaderGL -> shaderGL
                    .setUniform4i(uniformLocation, v0, v1, v2, v3));
        }
    }

    public void setUniform1(int uniform, FloatBuffer values) {
        int uniformLocation = uniformLocations[uniform];
        if (uniformLocation != -1) {
            uniforms.add(
                    shaderGL -> shaderGL.setUniform1(uniformLocation, values));
        }
    }

    public void setUniform2(int uniform, FloatBuffer values) {
        int uniformLocation = uniformLocations[uniform];
        if (uniformLocation != -1) {
            uniforms.add(
                    shaderGL -> shaderGL.setUniform2(uniformLocation, values));
        }
    }

    public void setUniform3(int uniform, FloatBuffer values) {
        int uniformLocation = uniformLocations[uniform];
        if (uniformLocation != -1) {
            uniforms.add(
                    shaderGL -> shaderGL.setUniform3(uniformLocation, values));
        }
    }

    public void setUniform4(int uniform, FloatBuffer values) {
        int uniformLocation = uniformLocations[uniform];
        if (uniformLocation != -1) {
            uniforms.add(
                    shaderGL -> shaderGL.setUniform4(uniformLocation, values));
        }
    }

    public void setUniform1(int uniform, IntBuffer values) {
        int uniformLocation = uniformLocations[uniform];
        if (uniformLocation != -1) {
            uniforms.add(
                    shaderGL -> shaderGL.setUniform1(uniformLocation, values));
        }
    }

    public void setUniform2(int uniform, IntBuffer values) {
        int uniformLocation = uniformLocations[uniform];
        if (uniformLocation != -1) {
            uniforms.add(
                    shaderGL -> shaderGL.setUniform2(uniformLocation, values));
        }
    }

    public void setUniform3(int uniform, IntBuffer values) {
        int uniformLocation = uniformLocations[uniform];
        if (uniformLocation != -1) {
            uniforms.add(
                    shaderGL -> shaderGL.setUniform3(uniformLocation, values));
        }
    }

    public void setUniform4(int uniform, IntBuffer values) {
        int uniformLocation = uniformLocations[uniform];
        if (uniformLocation != -1) {
            uniforms.add(
                    shaderGL -> shaderGL.setUniform4(uniformLocation, values));
        }
    }

    public void setUniformMatrix2(int uniform, boolean transpose,
            float[] matrices) {
        int uniformLocation = uniformLocations[uniform];
        if (uniformLocation != -1) {
            uniforms.add(shaderGL -> shaderGL
                    .setUniformMatrix2(uniformLocation, transpose, matrices));
        }
    }

    public void setUniformMatrix3(int uniform, boolean transpose,
            float[] matrices) {
        int uniformLocation = uniformLocations[uniform];
        if (uniformLocation != -1) {
            uniforms.add(shaderGL -> shaderGL
                    .setUniformMatrix3(uniformLocation, transpose, matrices));
        }
    }

    public void setUniformMatrix4(int uniform, boolean transpose,
            float[] matrices) {
        int uniformLocation = uniformLocations[uniform];
        if (uniformLocation != -1) {
            uniforms.add(shaderGL -> shaderGL
                    .setUniformMatrix4(uniformLocation, transpose, matrices));
        }
    }
}
