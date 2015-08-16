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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.OpenGLFunction;
import org.tobi29.scapes.engine.utils.io.ProcessStream;
import org.tobi29.scapes.engine.utils.io.filesystem.Resource;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.Properties;
import java.util.Queue;

public class Shader {
    private static final Logger LOGGER = LoggerFactory.getLogger(Shader.class);
    private final Queue<Uniform> uniforms = new ArrayDeque<>();
    private final int[] uniformLocations;
    private final int vertexShader, fragmentShader, program;

    @OpenGLFunction
    public Shader(Resource vertexResource, Resource fragmentResource,
            Properties properties, ShaderCompileInformation information, GL gl)
            throws IOException {
        vertexShader = createShader(
                information.processVertexSource(readSource(vertexResource)),
                gl.createVertexObject(), gl);
        fragmentShader = createShader(
                information.processFragmentSource(readSource(fragmentResource)),
                gl.createFragmentObject(), gl);
        program = gl.createProgram();
        gl.attach(program, vertexShader);
        gl.attach(program, fragmentShader);
        for (int i = 0; i < 8; i++) {
            String attribute = properties.getProperty("Attribute." + i);
            if (attribute != null) {
                gl.bindAttributeLocation(program, i, attribute);
            }
        }
        for (int i = 0; i < 8; i++) {
            String fragment = properties.getProperty("Fragment." + i);
            if (fragment != null) {
                gl.bindFragmentLocation(program, i, fragment);
            }
        }
        gl.link(program);
        if (!gl.checkLinkStatus(program)) {
            LOGGER.error("Failed to link status bar!");
            gl.printLogProgram(program);
        }
        uniformLocations = new int[32];
        for (int i = 0; i < 32; i++) {
            String uniform = properties.getProperty("Uniform." + i);
            if (uniform == null) {
                uniformLocations[i] = -1;
            } else {
                uniformLocations[i] = gl.getUniformLocation(program, uniform);
            }
        }
        information.postCompile(this);
    }

    private static String readSource(Resource resource) throws IOException {
        return resource.readReturn(stream -> ProcessStream
                .process(stream, ProcessStream.asString()));
    }

    private static int createShader(String source, int shader, GL gl) {
        gl.source(shader, source);
        gl.compile(shader);
        gl.printLogShader(shader);
        return shader;
    }

    public int programID() {
        return program;
    }

    public Queue<Uniform> uniforms() {
        return uniforms;
    }

    public int uniformLocation(int uniform) {
        return uniformLocations[uniform];
    }

    @OpenGLFunction
    public void dispose(GL gl) {
        gl.deleteProgram(program);
        gl.deleteShader(fragmentShader);
        gl.deleteShader(vertexShader);
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
            FloatBuffer matrices) {
        int uniformLocation = uniformLocations[uniform];
        if (uniformLocation != -1) {
            uniforms.add(shaderGL -> shaderGL
                    .setUniformMatrix2(uniformLocation, transpose, matrices));
        }
    }

    public void setUniformMatrix3(int uniform, boolean transpose,
            FloatBuffer matrices) {
        int uniformLocation = uniformLocations[uniform];
        if (uniformLocation != -1) {
            uniforms.add(shaderGL -> shaderGL
                    .setUniformMatrix3(uniformLocation, transpose, matrices));
        }
    }

    public void setUniformMatrix4(int uniform, boolean transpose,
            FloatBuffer matrices) {
        int uniformLocation = uniformLocations[uniform];
        if (uniformLocation != -1) {
            uniforms.add(shaderGL -> shaderGL
                    .setUniformMatrix4(uniformLocation, transpose, matrices));
        }
    }

    @FunctionalInterface
    public interface Uniform {
        void set(GL gl);
    }
}
