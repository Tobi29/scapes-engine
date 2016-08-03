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

package org.tobi29.scapes.engine.backends.lwjgl3.opengl;

import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.graphics.GL;
import org.tobi29.scapes.engine.graphics.Shader;
import org.tobi29.scapes.engine.graphics.ShaderCompileInformation;
import org.tobi29.scapes.engine.graphics.ShaderPreprocessor;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.shader.CompiledShader;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

final class ShaderGL implements Shader {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ShaderGL.class);
    private final CompiledShader shader;
    private final ShaderCompileInformation information;
    private final Queue<Runnable> uniforms = new ArrayDeque<>();
    private boolean stored, valid, markAsDisposed;
    private int[] uniformLocations;
    private int program;
    private long used;
    private Runnable detach;

    public ShaderGL(CompiledShader shader,
            ShaderCompileInformation information) {
        this.shader = shader;
        this.information = information;
    }

    @Override
    public boolean ensureStored(GL gl) {
        if (!stored) {
            store(gl);
        }
        used = System.currentTimeMillis();
        return valid;
    }

    @Override
    public void ensureDisposed(GL gl) {
        if (stored) {
            dispose(gl);
            reset();
        }
    }

    @Override
    public boolean isStored() {
        return stored;
    }

    @Override
    public boolean isUsed(long time) {
        return time - used < 1000 && !markAsDisposed;
    }

    @Override
    public void dispose(GL gl) {
        gl.check();
        GL20.glDeleteProgram(program);
    }

    @Override
    public void reset() {
        assert stored;
        stored = false;
        detach.run();
        detach = null;
        valid = false;
        markAsDisposed = false;
    }

    @Override
    public void activate(GL gl) {
        if (!ensureStored(gl)) {
            return;
        }
        gl.check();
        GL20.glUseProgram(program);
    }

    @Override
    public void updateUniforms(GL gl) {
        gl.check();
        while (!uniforms.isEmpty()) {
            uniforms.poll().run();
        }
    }

    @Override
    public int uniformLocation(int uniform) {
        return uniformLocations[uniform];
    }

    @Override
    public void setUniform1f(int uniform, float v0) {
        uniforms.add(() -> GL20.glUniform1f(uniformLocation(uniform), v0));
    }

    @Override
    public void setUniform2f(int uniform, float v0, float v1) {
        uniforms.add(() -> GL20.glUniform2f(uniformLocation(uniform), v0, v1));
    }

    @Override
    public void setUniform3f(int uniform, float v0, float v1, float v2) {
        uniforms.add(
                () -> GL20.glUniform3f(uniformLocation(uniform), v0, v1, v2));
    }

    @Override
    public void setUniform4f(int uniform, float v0, float v1, float v2,
            float v3) {
        uniforms.add(() -> GL20
                .glUniform4f(uniformLocation(uniform), v0, v1, v2, v3));
    }

    @Override
    public void setUniform1i(int uniform, int v0) {
        uniforms.add(() -> GL20.glUniform1i(uniformLocation(uniform), v0));
    }

    @Override
    public void setUniform2i(int uniform, int v0, int v1) {
        uniforms.add(() -> GL20.glUniform2i(uniformLocation(uniform), v0, v1));
    }

    @Override
    public void setUniform3i(int uniform, int v0, int v1, int v2) {
        uniforms.add(
                () -> GL20.glUniform3i(uniformLocation(uniform), v0, v1, v2));
    }

    @Override
    public void setUniform4i(int uniform, int v0, int v1, int v2, int v3) {
        uniforms.add(() -> GL20
                .glUniform4i(uniformLocation(uniform), v0, v1, v2, v3));
    }

    @Override
    public void setUniform1(int uniform, FloatBuffer values) {
        uniforms.add(() -> GL20.glUniform1fv(uniformLocation(uniform), values));
    }

    @Override
    public void setUniform2(int uniform, FloatBuffer values) {
        uniforms.add(() -> GL20.glUniform2fv(uniformLocation(uniform), values));
    }

    @Override
    public void setUniform3(int uniform, FloatBuffer values) {
        uniforms.add(() -> GL20.glUniform3fv(uniformLocation(uniform), values));
    }

    @Override
    public void setUniform4(int uniform, FloatBuffer values) {
        uniforms.add(() -> GL20.glUniform4fv(uniformLocation(uniform), values));
    }

    @Override
    public void setUniform1(int uniform, IntBuffer values) {
        uniforms.add(() -> GL20.glUniform1iv(uniformLocation(uniform), values));
    }

    @Override
    public void setUniform2(int uniform, IntBuffer values) {
        uniforms.add(() -> GL20.glUniform2iv(uniformLocation(uniform), values));
    }

    @Override
    public void setUniform3(int uniform, IntBuffer values) {
        uniforms.add(() -> GL20.glUniform3iv(uniformLocation(uniform), values));
    }

    @Override
    public void setUniform4(int uniform, IntBuffer values) {
        uniforms.add(() -> GL20.glUniform4iv(uniformLocation(uniform), values));
    }

    @Override
    public void setUniformMatrix2(int uniform, boolean transpose,
            float[] matrices) {
        uniforms.add(() -> GL20
                .glUniformMatrix2fv(uniformLocation(uniform), transpose,
                        matrices));
    }

    @Override
    public void setUniformMatrix3(int uniform, boolean transpose,
            float[] matrices) {
        uniforms.add(() -> GL20
                .glUniformMatrix3fv(uniformLocation(uniform), transpose,
                        matrices));
    }

    @Override
    public void setUniformMatrix4(int uniform, boolean transpose,
            float[] matrices) {
        uniforms.add(() -> GL20
                .glUniformMatrix4fv(uniformLocation(uniform), transpose,
                        matrices));
    }

    private void store(GL gl) {
        assert !stored;
        stored = true;
        gl.check();
        ShaderPreprocessor processor = information.preCompile(gl);
        try {
            Pair<Integer, int[]> program =
                    GLUtils.createProgram(shader, processor.properties());
            this.program = program.a;
            uniformLocations = program.b;
        } catch (IOException e) {
            LOGGER.warn("Failed to generate shader: {}", e.getMessage());
        }
        information.postCompile(gl, this);
        valid = true;
        detach = gl.shaderTracker().attach(this);
    }
}
