package org.tobi29.scapes.engine.backends.opengl;

import java8.util.function.Consumer;
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
    private final OpenGLBind openGL;
    private final CompiledShader shader;
    private final ShaderCompileInformation information;
    private final Queue<Consumer<OpenGL>> uniforms = new ArrayDeque<>();
    private boolean stored, valid, markAsDisposed;
    private int[] uniformLocations;
    private int program;
    private long used;
    private Runnable detach;

    public ShaderGL(CompiledShader shader, ShaderCompileInformation information,
            OpenGLBind openGL) {
        this.shader = shader;
        this.information = information;
        this.openGL = openGL;
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
        OpenGL openGL = this.openGL.get(gl);
        openGL.deleteProgram(program);
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
        OpenGL openGL = this.openGL.get(gl);
        openGL.activateShader(program);
    }

    @Override
    public void updateUniforms(GL gl) {
        OpenGL openGL = this.openGL.get(gl);
        while (!uniforms.isEmpty()) {
            uniforms.poll().accept(openGL);
        }
    }

    @Override
    public int uniformLocation(int uniform) {
        return uniformLocations[uniform];
    }

    @Override
    public void setUniform1f(int uniform, float v0) {
        uniforms.add(shaderGL -> shaderGL
                .setUniform1f(uniformLocation(uniform), v0));
    }

    @Override
    public void setUniform2f(int uniform, float v0, float v1) {
        uniforms.add(shaderGL -> shaderGL
                .setUniform2f(uniformLocation(uniform), v0, v1));
    }

    @Override
    public void setUniform3f(int uniform, float v0, float v1, float v2) {
        uniforms.add(shaderGL -> shaderGL
                .setUniform3f(uniformLocation(uniform), v0, v1, v2));
    }

    @Override
    public void setUniform4f(int uniform, float v0, float v1, float v2,
            float v3) {
        uniforms.add(shaderGL -> shaderGL
                .setUniform4f(uniformLocation(uniform), v0, v1, v2, v3));
    }

    @Override
    public void setUniform1i(int uniform, int v0) {
        uniforms.add(shaderGL -> shaderGL
                .setUniform1i(uniformLocation(uniform), v0));
    }

    @Override
    public void setUniform2i(int uniform, int v0, int v1) {
        uniforms.add(shaderGL -> shaderGL
                .setUniform2i(uniformLocation(uniform), v0, v1));
    }

    @Override
    public void setUniform3i(int uniform, int v0, int v1, int v2) {
        uniforms.add(shaderGL -> shaderGL
                .setUniform3i(uniformLocation(uniform), v0, v1, v2));
    }

    @Override
    public void setUniform4i(int uniform, int v0, int v1, int v2, int v3) {
        uniforms.add(shaderGL -> shaderGL
                .setUniform4i(uniformLocation(uniform), v0, v1, v2, v3));
    }

    @Override
    public void setUniform1(int uniform, FloatBuffer values) {
        uniforms.add(shaderGL -> shaderGL
                .setUniform1(uniformLocation(uniform), values));
    }

    @Override
    public void setUniform2(int uniform, FloatBuffer values) {
        uniforms.add(shaderGL -> shaderGL
                .setUniform2(uniformLocation(uniform), values));
    }

    @Override
    public void setUniform3(int uniform, FloatBuffer values) {
        uniforms.add(shaderGL -> shaderGL
                .setUniform3(uniformLocation(uniform), values));
    }

    @Override
    public void setUniform4(int uniform, FloatBuffer values) {
        uniforms.add(shaderGL -> shaderGL
                .setUniform4(uniformLocation(uniform), values));
    }

    @Override
    public void setUniform1(int uniform, IntBuffer values) {
        uniforms.add(shaderGL -> shaderGL
                .setUniform1(uniformLocation(uniform), values));
    }

    @Override
    public void setUniform2(int uniform, IntBuffer values) {
        uniforms.add(shaderGL -> shaderGL
                .setUniform2(uniformLocation(uniform), values));
    }

    @Override
    public void setUniform3(int uniform, IntBuffer values) {
        uniforms.add(shaderGL -> shaderGL
                .setUniform3(uniformLocation(uniform), values));
    }

    @Override
    public void setUniform4(int uniform, IntBuffer values) {
        uniforms.add(shaderGL -> shaderGL
                .setUniform4(uniformLocation(uniform), values));
    }

    @Override
    public void setUniformMatrix2(int uniform, boolean transpose,
            float[] matrices) {
        uniforms.add(shaderGL -> shaderGL
                .setUniformMatrix2(uniformLocation(uniform), transpose,
                        matrices));
    }

    @Override
    public void setUniformMatrix3(int uniform, boolean transpose,
            float[] matrices) {
        uniforms.add(shaderGL -> shaderGL
                .setUniformMatrix3(uniformLocation(uniform), transpose,
                        matrices));
    }

    @Override
    public void setUniformMatrix4(int uniform, boolean transpose,
            float[] matrices) {
        uniforms.add(shaderGL -> shaderGL
                .setUniformMatrix4(uniformLocation(uniform), transpose,
                        matrices));
    }

    private void store(GL gl) {
        assert !stored;
        stored = true;
        OpenGL openGL = this.openGL.get(gl);
        ShaderPreprocessor processor = information.preCompile(gl);
        try {
            Pair<Integer, int[]> program =
                    openGL.createProgram(shader, processor.properties());
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
